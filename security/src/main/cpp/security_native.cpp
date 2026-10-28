#include <jni.h>

#include <chrono>
#include <dirent.h>
#include <fstream>
#include <iomanip>
#include <sstream>
#include <stdexcept>
#include <string>
#include <vector>

namespace {
    constexpr const char* API_KEY = "my_secret_key";

    static unsigned char key[] = {
            0x1A, 0x2B, 0x3C, 0x4D
    };

    std::string decode() {
        std::string s;
        for (auto b : key) {
            s.push_back(b ^ 0x55);
        }
        return s;
    }

    jlong currentTimeMillis() {
        const auto now = std::chrono::system_clock::now();
        const auto millis = std::chrono::duration_cast<std::chrono::milliseconds>(
                now.time_since_epoch()
        );
        return static_cast<jlong>(millis.count());
    }

    bool lineStartsWith(const std::string& line, const char* prefix) {
        return line.rfind(prefix, 0) == 0;
    }

    int readTracerPid(const std::string& statusPath) {
        std::ifstream status(statusPath);
        std::string line;
        while (std::getline(status, line)) {
            if (!lineStartsWith(line, "TracerPid:")) {
                continue;
            }

            std::istringstream stream(line.substr(std::string("TracerPid:").size()));
            int tracerPid = 0;
            stream >> tracerPid;
            return tracerPid;
        }

        return 0;
    }

    bool hasProcessTracer() {
        return readTracerPid("/proc/self/status") > 0;
    }

    bool hasThreadTracer() {
        DIR* taskDir = opendir("/proc/self/task");
        if (taskDir == nullptr) {
            return false;
        }

        bool detected = false;
        dirent* entry;
        while ((entry = readdir(taskDir)) != nullptr) {
            if (entry->d_name[0] == '.') {
                continue;
            }

            std::string statusPath = "/proc/self/task/";
            statusPath += entry->d_name;
            statusPath += "/status";
            if (readTracerPid(statusPath) > 0) {
                detected = true;
                break;
            }
        }

        closedir(taskDir);
        return detected;
    }

    bool containsAny(const std::string& value, const std::vector<std::string>& needles) {
        for (const std::string& needle : needles) {
            if (value.find(needle) != std::string::npos) {
                return true;
            }
        }
        return false;
    }

    bool hasDebuggerLibraryLoaded() {
        std::ifstream maps("/proc/self/maps");
        std::string line;
        const std::vector<std::string> debuggerMarkers = {
                "liblldb",
                "lldb-server",
                "gdbserver",
                "frida",
                "gum-js-loop",
                "re.frida.server"
        };

        while (std::getline(maps, line)) {
            if (containsAny(line, debuggerMarkers)) {
                return true;
            }
        }

        return false;
    }

    std::string nativeDebugStatus() {
        std::vector<std::string> findings;

        if (hasProcessTracer()) {
            findings.emplace_back("process-tracer");
        }
        if (hasThreadTracer()) {
            findings.emplace_back("thread-tracer");
        }
        if (hasDebuggerLibraryLoaded()) {
            findings.emplace_back("debugger-library");
        }

        if (findings.empty()) {
            return "clean";
        }

        std::ostringstream result;
        for (size_t i = 0; i < findings.size(); ++i) {
            if (i > 0) {
                result << ",";
            }
            result << findings[i];
        }
        return result.str();
    }

    void throwIllegalArgument(JNIEnv* env, const char* message) {
        jclass errorClass = env->FindClass("java/lang/IllegalArgumentException");
        if (errorClass != nullptr) {
            env->ThrowNew(errorClass, message);
        }
    }

    std::string toString(JNIEnv* env, jstring value) {
        if (value == nullptr) {
            return "";
        }

        const char* chars = env->GetStringUTFChars(value, nullptr);
        if (chars == nullptr) {
            return "";
        }

        std::string result(chars);
        env->ReleaseStringUTFChars(value, chars);
        return result;
    }

    uint8_t rotateLeft8(uint8_t value, uint8_t shift) {
        shift &= 7;
        return static_cast<uint8_t>((value << shift) | (value >> (8 - shift)));
    }

    uint8_t rotateRight8(uint8_t value, uint8_t shift) {
        shift &= 7;
        return static_cast<uint8_t>((value >> shift) | (value << (8 - shift)));
    }

    uint64_t mix64(uint64_t value) {
        value ^= value >> 27;
        value *= 0x3C79AC492BA7B653ULL;
        value ^= value >> 33;
        value *= 0x1C69B3F74AC4AE35ULL;
        value ^= value >> 27;
        return value;
    }

    uint64_t hashText(const std::string& value) {
        uint64_t hash = 0xA0761D6478BD642FULL;
        for (unsigned char item : value) {
            hash ^= static_cast<uint64_t>(item) + 0x9E3779B185EBCA87ULL + (hash << 6) + (hash >> 2);
            hash = mix64(hash);
        }
        return hash;
    }

    uint64_t timeSeed(
            const std::string& packageName,
            const std::string& deviceId,
            const std::string& requestId,
            jlong timestampMillis
    ) {
        uint64_t seed = static_cast<uint64_t>(timestampMillis);
        seed ^= hashText(packageName);
        seed ^= mix64(hashText(deviceId) + 0xA24BAED4963EE407ULL);
        seed ^= mix64(hashText(requestId) + 0x9FB21C651E98DF25ULL);
        seed ^= 0xD6E8FEB86659FD93ULL;
        return mix64(seed);
    }

    uint8_t streamByte(uint64_t seed, size_t index) {
        uint64_t state = seed + (static_cast<uint64_t>(index) + 1ULL) * 0x9E3779B97F4A7C15ULL;
        state ^= static_cast<uint64_t>(index) << 32;
        state = mix64(state);
        return static_cast<uint8_t>((state >> ((index & 7U) * 8U)) & 0xFFU);
    }

    std::string transform(const std::string& input, uint64_t seed, bool encrypt) {
        std::string output(input.size(), '\0');
        for (size_t i = 0; i < input.size(); ++i) {
            uint8_t stream = streamByte(seed, i);
            uint8_t rotate = static_cast<uint8_t>((stream ^ i) & 7U);
            uint8_t value = static_cast<uint8_t>(input[i]);
            if (encrypt) {
                value = static_cast<uint8_t>(value + stream + (i * 17U));
                value = rotateLeft8(value, rotate);
                value ^= static_cast<uint8_t>(stream + 0xA5U);
            } else {
                value ^= static_cast<uint8_t>(stream + 0xA5U);
                value = rotateRight8(value, rotate);
                value = static_cast<uint8_t>(value - stream - (i * 17U));
            }
            output[i] = static_cast<char>(value);
        }
        return output;
    }

    uint32_t checksum(const std::string& value, uint64_t seed) {
        uint64_t state = seed ^ 0xC2B2AE3D27D4EB4FULL;
        for (unsigned char item : value) {
            state ^= static_cast<uint64_t>(item) + 0x165667B19E3779F9ULL;
            state = mix64(state);
        }
        state ^= static_cast<uint64_t>(value.size()) * 0x85EBCA77C2B2AE63ULL;
        state = mix64(state);
        return static_cast<uint32_t>((state >> 32) ^ state);
    }

    std::string hexEncode(const std::string& value) {
        static constexpr char hex[] = "0123456789abcdef";
        std::string encoded;
        encoded.reserve(value.size() * 2);
        for (unsigned char item : value) {
            encoded.push_back(hex[item >> 4]);
            encoded.push_back(hex[item & 0x0F]);
        }
        return encoded;
    }

    int hexValue(char value) {
        if (value >= '0' && value <= '9') {
            return value - '0';
        }
        if (value >= 'a' && value <= 'f') {
            return value - 'a' + 10;
        }
        if (value >= 'A' && value <= 'F') {
            return value - 'A' + 10;
        }
        return -1;
    }

    std::string hexDecode(const std::string& value) {
        if ((value.size() % 2) != 0) {
            throw std::invalid_argument("Token payload is malformed");
        }

        std::string decoded;
        decoded.reserve(value.size() / 2);
        for (size_t i = 0; i < value.size(); i += 2) {
            int high = hexValue(value[i]);
            int low = hexValue(value[i + 1]);
            if (high < 0 || low < 0) {
                throw std::invalid_argument("Token payload is malformed");
            }
            decoded.push_back(static_cast<char>((high << 4) | low));
        }
        return decoded;
    }

    std::string checksumHex(uint32_t value) {
        std::ostringstream stream;
        stream << std::hex << std::setfill('0') << std::setw(8) << value;
        return stream.str();
    }

    uint32_t checksumFromHex(const std::string& value) {
        if (value.size() != 8) {
            throw std::invalid_argument("Token checksum is malformed");
        }

        uint32_t parsed = 0;
        for (char item : value) {
            int nibble = hexValue(item);
            if (nibble < 0) {
                throw std::invalid_argument("Token checksum is malformed");
            }
            parsed = static_cast<uint32_t>((parsed << 4) | nibble);
        }
        return parsed;
    }

    std::vector<std::string> split(const std::string& value, char delimiter) {
        std::vector<std::string> parts;
        std::stringstream stream(value);
        std::string part;
        while (std::getline(stream, part, delimiter)) {
            parts.push_back(part);
        }
        if (!value.empty() && value.back() == delimiter) {
            parts.emplace_back();
        }
        return parts;
    }

    std::string createTimeBoundToken(
            const std::string& apiKey,
            const std::string& packageName,
            const std::string& deviceId,
            const std::string& requestId,
            jlong timestampMillis
    ) {
        if (apiKey.empty()) {
            throw std::invalid_argument("API key must not be empty");
        }
        if (packageName.empty()) {
            throw std::invalid_argument("Package name must not be empty");
        }
        if (deviceId.empty()) {
            throw std::invalid_argument("Device id must not be empty");
        }
        if (requestId.empty()) {
            throw std::invalid_argument("Request id must not be empty");
        }
        if (timestampMillis <= 0) {
            throw std::invalid_argument("Timestamp is invalid");
        }

        uint64_t seed = timeSeed(packageName, deviceId, requestId, timestampMillis);
        std::string plain = packageName + "\n" + requestId + "\n" + deviceId + "\n" + apiKey;
        std::string encrypted = transform(plain, seed, true);
        return "tb1." + hexEncode(encrypted) + "." + checksumHex(checksum(plain, seed));
    }

    std::string openTimeBoundToken(
            const std::string& token,
            const std::string& packageName,
            const std::string& deviceId,
            const std::string& requestId,
            jlong timestampMillis
    ) {
        std::vector<std::string> parts = split(token, '.');
        if (parts.size() != 3 || parts[0] != "tb1") {
            throw std::invalid_argument("Token format is invalid");
        }
        if (packageName.empty()) {
            throw std::invalid_argument("Package name must not be empty");
        }
        if (deviceId.empty()) {
            throw std::invalid_argument("Device id must not be empty");
        }
        if (requestId.empty()) {
            throw std::invalid_argument("Request id must not be empty");
        }
        if (timestampMillis <= 0) {
            throw std::invalid_argument("Timestamp is invalid");
        }

        uint64_t seed = timeSeed(packageName, deviceId, requestId, timestampMillis);
        std::string encrypted = hexDecode(parts[1]);
        std::string plain = transform(encrypted, seed, false);
        uint32_t expectedChecksum = checksumFromHex(parts[2]);
        if (checksum(plain, seed) != expectedChecksum) {
            throw std::invalid_argument("Token checksum is invalid");
        }

        const std::string prefix = packageName + "\n" + requestId + "\n" + deviceId + "\n";
        if (plain.rfind(prefix, 0) != 0) {
            throw std::invalid_argument("Token binding is invalid");
        }

        return plain.substr(prefix.size());
    }

    jlong JNICALL n0(
        JNIEnv*,
        jclass
) {
        return currentTimeMillis();
    }

    jboolean JNICALL n1(
        JNIEnv*,
        jclass
) {
        return nativeDebugStatus() != "clean" ? JNI_TRUE : JNI_FALSE;
    }

    jstring JNICALL n2(
        JNIEnv* env,
        jclass
) {
        const std::string status = nativeDebugStatus();
        return env->NewStringUTF(status.c_str());
    }

    jstring JNICALL n3(
        JNIEnv* env,
        jclass,
        jstring apiKey,
        jstring packageName,
        jstring deviceId,
        jstring requestId,
        jlong timestampMillis
) {
        try {
            const std::string token = createTimeBoundToken(
                    toString(env, apiKey),
                    toString(env, packageName),
                    toString(env, deviceId),
                    toString(env, requestId),
                    timestampMillis
            );
            return env->NewStringUTF(token.c_str());
        } catch (const std::exception& error) {
            throwIllegalArgument(env, error.what());
            return nullptr;
        }
    }

    jstring JNICALL n4(
        JNIEnv* env,
        jclass,
        jstring token,
        jstring packageName,
        jstring deviceId,
        jstring requestId,
        jlong timestampMillis
) {
        try {
            const std::string apiKey = openTimeBoundToken(
                    toString(env, token),
                    toString(env, packageName),
                    toString(env, deviceId),
                    toString(env, requestId),
                    timestampMillis
            );
            return env->NewStringUTF(apiKey.c_str());
        } catch (const std::exception& error) {
            throwIllegalArgument(env, error.what());
            return nullptr;
        }
    }

    std::string decodeJniString(const unsigned char* value, size_t size) {
        std::string decoded;
        decoded.reserve(size);
        for (size_t i = 0; i < size; ++i) {
            decoded.push_back(static_cast<char>(value[i] ^ 0x5A));
        }
        return decoded;
    }

    bool registerNativeMethods(JNIEnv* env) {
        static constexpr unsigned char nativeSecurityClass[] = {
                0x39, 0x35, 0x37, 0x75, 0x29, 0x2F, 0x36, 0x35, 0x2E, 0x33,
                0x35, 0x34, 0x75, 0x29, 0x3F, 0x39, 0x2F, 0x28, 0x33, 0x2E,
                0x23, 0x75, 0x14, 0x3B, 0x2E, 0x33, 0x2C, 0x3F, 0x09, 0x3F,
                0x39, 0x2F, 0x28, 0x33, 0x2E, 0x23
        };
        static constexpr unsigned char getTimeMethod[] = {
                0x34, 0x3B, 0x2E, 0x33, 0x2C, 0x3F, 0x1D, 0x3F, 0x2E, 0x19,
                0x2F, 0x28, 0x28, 0x3F, 0x34, 0x2E, 0x0E, 0x33, 0x37, 0x3F,
                0x17, 0x33, 0x36, 0x36, 0x33, 0x29
        };
        static constexpr unsigned char isDebuggerAttachedMethod[] = {
                0x34, 0x3B, 0x2E, 0x33, 0x2C, 0x3F, 0x13, 0x29, 0x14, 0x3B,
                0x2E, 0x33, 0x2C, 0x3F, 0x1E, 0x3F, 0x38, 0x2F, 0x3D, 0x3D,
                0x3F, 0x28, 0x1B, 0x2E, 0x2E, 0x3B, 0x39, 0x32, 0x3F, 0x3E
        };
        static constexpr unsigned char getDebugStatusMethod[] = {
                0x34, 0x3B, 0x2E, 0x33, 0x2C, 0x3F, 0x1D, 0x3F, 0x2E, 0x14,
                0x3B, 0x2E, 0x33, 0x2C, 0x3F, 0x1E, 0x3F, 0x38, 0x2F, 0x3D,
                0x09, 0x2E, 0x3B, 0x2E, 0x2F, 0x29
        };
        static constexpr unsigned char createTokenMethod[] = {
                0x34, 0x3B, 0x2E, 0x33, 0x2C, 0x3F, 0x19, 0x28, 0x3F, 0x3B,
                0x2E, 0x3F, 0x0E, 0x33, 0x37, 0x3F, 0x18, 0x35, 0x2F, 0x34,
                0x3E, 0x1B, 0x2A, 0x33, 0x11, 0x3F, 0x23, 0x0E, 0x35, 0x31,
                0x3F, 0x34
        };
        static constexpr unsigned char openTokenMethod[] = {
                0x34, 0x3B, 0x2E, 0x33, 0x2C, 0x3F, 0x15, 0x2A, 0x3F, 0x34,
                0x0E, 0x33, 0x37, 0x3F, 0x18, 0x35, 0x2F, 0x34, 0x3E, 0x1B,
                0x2A, 0x33, 0x11, 0x3F, 0x23, 0x0E, 0x35, 0x31, 0x3F, 0x34
        };
        static constexpr unsigned char longSignature[] = {0x72, 0x73, 0x10};
        static constexpr unsigned char booleanSignature[] = {0x72, 0x73, 0x00};
        static constexpr unsigned char stringSignature[] = {
                0x72, 0x73, 0x16, 0x30, 0x3B, 0x2C, 0x3B, 0x75, 0x36, 0x3B,
                0x34, 0x3D, 0x75, 0x09, 0x2E, 0x28, 0x33, 0x34, 0x3D, 0x61
        };
        static constexpr unsigned char tokenSignature[] = {
                0x72, 0x16, 0x30, 0x3B, 0x2C, 0x3B, 0x75, 0x36, 0x3B, 0x34,
                0x3D, 0x75, 0x09, 0x2E, 0x28, 0x33, 0x34, 0x3D, 0x61, 0x16,
                0x30, 0x3B, 0x2C, 0x3B, 0x75, 0x36, 0x3B, 0x34, 0x3D, 0x75,
                0x09, 0x2E, 0x28, 0x33, 0x34, 0x3D, 0x61, 0x16, 0x30, 0x3B,
                0x2C, 0x3B, 0x75, 0x36, 0x3B, 0x34, 0x3D, 0x75, 0x09, 0x2E,
                0x28, 0x33, 0x34, 0x3D, 0x61, 0x16, 0x30, 0x3B, 0x2C, 0x3B,
                0x75, 0x36, 0x3B, 0x34, 0x3D, 0x75, 0x09, 0x2E, 0x28, 0x33,
                0x34, 0x3D, 0x61, 0x10, 0x73, 0x16, 0x30, 0x3B, 0x2C, 0x3B,
                0x75, 0x36, 0x3B, 0x34, 0x3D, 0x75, 0x09, 0x2E, 0x28, 0x33,
                0x34, 0x3D, 0x61
        };

        const std::string className = decodeJniString(nativeSecurityClass, sizeof(nativeSecurityClass));
        const std::string getTimeName = decodeJniString(getTimeMethod, sizeof(getTimeMethod));
        const std::string isDebuggerAttachedName = decodeJniString(
                isDebuggerAttachedMethod,
                sizeof(isDebuggerAttachedMethod)
        );
        const std::string getDebugStatusName = decodeJniString(
                getDebugStatusMethod,
                sizeof(getDebugStatusMethod)
        );
        const std::string createTokenName = decodeJniString(createTokenMethod, sizeof(createTokenMethod));
        const std::string openTokenName = decodeJniString(openTokenMethod, sizeof(openTokenMethod));
        const std::string longSig = decodeJniString(longSignature, sizeof(longSignature));
        const std::string booleanSig = decodeJniString(booleanSignature, sizeof(booleanSignature));
        const std::string stringSig = decodeJniString(stringSignature, sizeof(stringSignature));
        const std::string tokenSig = decodeJniString(tokenSignature, sizeof(tokenSignature));

        jclass nativeSecurity = env->FindClass(className.c_str());
        if (nativeSecurity == nullptr) {
            return false;
        }

        std::vector<JNINativeMethod> methods = {
                {const_cast<char*>(getTimeName.c_str()), const_cast<char*>(longSig.c_str()),
                 reinterpret_cast<void*>(n0)},
                {const_cast<char*>(isDebuggerAttachedName.c_str()), const_cast<char*>(booleanSig.c_str()),
                 reinterpret_cast<void*>(n1)},
                {const_cast<char*>(getDebugStatusName.c_str()), const_cast<char*>(stringSig.c_str()),
                 reinterpret_cast<void*>(n2)},
                {const_cast<char*>(createTokenName.c_str()), const_cast<char*>(tokenSig.c_str()),
                 reinterpret_cast<void*>(n3)},
                {const_cast<char*>(openTokenName.c_str()), const_cast<char*>(tokenSig.c_str()),
                 reinterpret_cast<void*>(n4)}
        };

        const jint registerResult = env->RegisterNatives(
                nativeSecurity,
                methods.data(),
                static_cast<jint>(methods.size())
        );
        env->DeleteLocalRef(nativeSecurity);
        return registerResult == JNI_OK;
    }
}

extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*) {
    JNIEnv* env = nullptr;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    if (!registerNativeMethods(env)) {
        return JNI_ERR;
    }

    return JNI_VERSION_1_6;
}
