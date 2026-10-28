// original: NativeSecurity
package com.sulotion.security;

public final class NativeSecurity {
    private static final String LIBRARY_NAME = "security-native";

    static {
        System.loadLibrary(LIBRARY_NAME);
    }

    private NativeSecurity() {
    }

    public static long getCurrentTimeMillis() {
        return nativeGetCurrentTimeMillis();
    }

    public static boolean isNativeDebuggerAttached() {
        return nativeIsNativeDebuggerAttached();
    }

    public static String getNativeDebugStatus() {
        return nativeGetNativeDebugStatus();
    }

    public static String createTimeBoundApiKeyToken(
            String apiKey,
            String packageName,
            String deviceId,
            String requestId,
            long timestampMillis
    ) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("API key must not be null or empty");
        }
        if (packageName == null || packageName.isEmpty()) {
            throw new IllegalArgumentException("Package name must not be null or empty");
        }
        if (deviceId == null || deviceId.isEmpty()) {
            throw new IllegalArgumentException("Device id must not be null or empty");
        }
        if (requestId == null || requestId.isEmpty()) {
            throw new IllegalArgumentException("Request id must not be null or empty");
        }
        return nativeCreateTimeBoundApiKeyToken(
                apiKey,
                packageName,
                deviceId,
                requestId,
                timestampMillis
        );
    }

    public static String openTimeBoundApiKeyToken(
            String token,
            String packageName,
            String deviceId,
            String requestId,
            long timestampMillis
    ) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token must not be null or empty");
        }
        if (packageName == null || packageName.isEmpty()) {
            throw new IllegalArgumentException("Package name must not be null or empty");
        }
        if (deviceId == null || deviceId.isEmpty()) {
            throw new IllegalArgumentException("Device id must not be null or empty");
        }
        if (requestId == null || requestId.isEmpty()) {
            throw new IllegalArgumentException("Request id must not be null or empty");
        }
        return nativeOpenTimeBoundApiKeyToken(
                token,
                packageName,
                deviceId,
                requestId,
                timestampMillis
        );
    }

    private static native long nativeGetCurrentTimeMillis();

    private static native boolean nativeIsNativeDebuggerAttached();

    private static native String nativeGetNativeDebugStatus();

    private static native String nativeCreateTimeBoundApiKeyToken(
            String apiKey,
            String packageName,
            String deviceId,
            String requestId,
            long timestampMillis
    );

    private static native String nativeOpenTimeBoundApiKeyToken(
            String token,
            String packageName,
            String deviceId,
            String requestId,
            long timestampMillis
    );
}
