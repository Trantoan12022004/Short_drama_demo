// original: DeviceIntegrityGuard
package com.sulotion.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import java.io.File
import java.util.Locale

object DeviceIntegrityGuard {

    enum class FindingType {
        ROOT_BINARY,
        ROOT_PACKAGE,
        ROOT_BUILD_TAG,
        UNSAFE_SYSTEM_PROPERTY,
        DEBUGGER,
        INSTRUMENTATION_ARTIFACT,
    }

    data class Finding(
        val type: FindingType,
        val evidence: String,
    )

    data class Report(
        val findings: List<Finding>,
    ) {
        val isCompromised: Boolean
            get() = findings.isNotEmpty()

        fun summary(): String {
            if (findings.isEmpty()) {
                return "trusted"
            }
            return findings.joinToString(separator = "; ") { "${it.type}:${it.evidence}" }
        }
    }

    fun inspect(
        context: Context,
        includeDebuggerCheck: Boolean = true,
    ): Report {
        val findings = buildList {
            addAll(checkBuildTags())
            addAll(checkSystemProperties())
            addAll(checkRootFiles())
            addAll(checkRootPackages(context))
            addAll(checkInstrumentationArtifacts())

            if (includeDebuggerCheck && isDebuggerAttached()) {
                add(Finding(FindingType.DEBUGGER, "debugger"))
            }
        }

        return Report(findings.distinct())
    }

    fun assertTrusted(
        context: Context,
        includeDebuggerCheck: Boolean = true,
    ): Report {
        val report = inspect(context, includeDebuggerCheck)
        check(!report.isCompromised) { report.summary() }
        return report
    }

    private fun checkBuildTags(): List<Finding> {
        val tags = Build.TAGS ?: return emptyList()
        return if (tags.contains(decoded(47, 62, 40, 47, 118, 48, 62, 34, 40))) {
            listOf(Finding(FindingType.ROOT_BUILD_TAG, "build-tags"))
        } else {
            emptyList()
        }
    }

    private fun checkSystemProperties(): List<Finding> {
        val unsafeProps = listOf(
            decoded(41, 52, 117, 63, 62, 57, 46, 60, 60, 58, 57, 55, 62) to decoded(106),
            decoded(41, 52, 117, 40, 62, 56, 46, 41, 62) to decoded(107),
        )

        return unsafeProps.mapNotNull { (key, unsafeValue) ->
            val actualValue = systemProperty(key)
            if (actualValue == unsafeValue) {
                Finding(FindingType.UNSAFE_SYSTEM_PROPERTY, key)
            } else {
                null
            }
        }
    }

    private fun checkRootFiles(): List<Finding> {
        return rootFilePaths.mapNotNull { path ->
            val file = File(path)
            if (file.exists()) {
                Finding(FindingType.ROOT_BINARY, path.substringAfterLast('/'))
            } else {
                null
            }
        }
    }

    private fun checkRootPackages(context: Context): List<Finding> {
        return rootPackages.mapNotNull { packageName ->
            if (isPackageInstalled(context, packageName)) {
                Finding(FindingType.ROOT_PACKAGE, packageName)
            } else {
                null
            }
        }
    }

    private fun checkInstrumentationArtifacts(): List<Finding> {
        val processMaps = readProcFile(decoded(116, 43, 41, 52, 56, 116, 40, 62, 55, 61, 116, 54, 58, 43, 40))
            ?: return emptyList()
        val normalizedMaps = processMaps.lowercase(Locale.US)

        return instrumentationMarkers.mapNotNull { marker ->
            if (normalizedMaps.contains(marker)) {
                Finding(FindingType.INSTRUMENTATION_ARTIFACT, marker)
            } else {
                null
            }
        }
    }

    private fun isDebuggerAttached(): Boolean {
        return Debug.isDebuggerConnected() || Debug.waitingForDebugger()
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0),
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(packageName, 0)
            }
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        } catch (_: RuntimeException) {
            false
        }
    }

    private fun systemProperty(key: String): String? {
        return try {
            val systemProperties = Class.forName("android.os.SystemProperties")
            val get = systemProperties.getMethod("get", String::class.java)
            get.invoke(null, key) as? String
        } catch (_: ReflectiveOperationException) {
            null
        } catch (_: RuntimeException) {
            null
        }
    }

    private fun readProcFile(path: String): String? {
        return try {
            File(path).takeIf { it.exists() }?.readText()
        } catch (_: RuntimeException) {
            null
        }
    }

    private fun decoded(vararg encoded: Int): String {
        return encoded
            .map { (it xor OBFUSCATION_KEY).toChar() }
            .joinToString(separator = "")
    }

    private val rootFilePaths: List<String> = listOf(
        decoded(116, 40, 34, 40, 47, 62, 54, 116, 58, 43, 43, 116, 8, 46, 43, 62, 41, 46, 40, 62, 41, 117, 58, 43, 48),
        decoded(116, 40, 57, 50, 53, 116, 40, 46),
        decoded(116, 40, 34, 40, 47, 62, 54, 116, 57, 50, 53, 116, 40, 46),
        decoded(116, 40, 34, 40, 47, 62, 54, 116, 35, 57, 50, 53, 116, 40, 46),
        decoded(116, 63, 58, 47, 58, 116, 55, 52, 56, 58, 55, 116, 35, 57, 50, 53, 116, 40, 46),
        decoded(116, 63, 58, 47, 58, 116, 55, 52, 56, 58, 55, 116, 57, 50, 53, 116, 40, 46),
        decoded(116, 40, 34, 40, 47, 62, 54, 116, 40, 63, 116, 35, 57, 50, 53, 116, 40, 46),
        decoded(116, 40, 34, 40, 47, 62, 54, 116, 57, 50, 53, 116, 61, 58, 50, 55, 40, 58, 61, 62, 116, 40, 46),
        decoded(116, 63, 58, 47, 58, 116, 55, 52, 56, 58, 55, 116, 40, 46),
        decoded(116, 40, 46, 116, 57, 50, 53, 116, 40, 46),
        decoded(116, 40, 34, 40, 47, 62, 54, 116, 57, 50, 53, 116, 117, 62, 35, 47, 116, 117, 40, 46),
        decoded(116, 40, 34, 40, 47, 62, 54, 116, 46, 40, 41, 116, 44, 62, 118, 53, 62, 62, 63, 118, 41, 52, 52, 47, 116, 40, 46),
        decoded(116, 56, 58, 56, 51, 62, 116, 40, 46),
        decoded(116, 63, 58, 47, 58, 116, 58, 63, 57, 116, 54, 58, 60, 50, 40, 48),
        decoded(116, 40, 57, 50, 53, 116, 117, 54, 58, 60, 50, 40, 48),
    )

    private val rootPackages: List<String> = listOf(
        decoded(56, 52, 54, 117, 47, 52, 43, 49, 52, 51, 53, 44, 46, 117, 54, 58, 60, 50, 40, 48),
        decoded(62, 46, 117, 56, 51, 58, 50, 53, 61, 50, 41, 62, 117, 40, 46, 43, 62, 41, 40, 46),
        decoded(56, 52, 54, 117, 53, 52, 40, 51, 46, 61, 52, 46, 117, 58, 53, 63, 41, 52, 50, 63, 117, 40, 46),
        decoded(56, 52, 54, 117, 48, 52, 46, 40, 51, 50, 48, 63, 46, 47, 47, 58, 117, 40, 46, 43, 62, 41, 46, 40, 62, 41),
        decoded(56, 52, 54, 117, 47, 51, 50, 41, 63, 43, 58, 41, 47, 34, 117, 40, 46, 43, 62, 41, 46, 40, 62, 41),
        decoded(56, 52, 54, 117, 34, 62, 55, 55, 52, 44, 62, 40, 117, 40, 46),
        decoded(56, 52, 54, 117, 48, 50, 53, 60, 41, 52, 52, 47, 117, 48, 50, 53, 60, 46, 40, 62, 41),
        decoded(56, 52, 54, 117, 48, 50, 53, 60, 52, 117, 41, 52, 52, 47),
        decoded(56, 52, 54, 117, 33, 58, 56, 51, 40, 43, 52, 53, 60, 117, 47, 62, 54, 43, 41, 52, 52, 47, 41, 62, 54, 52, 45, 62, 49, 57),
        decoded(56, 52, 54, 117, 41, 58, 54, 63, 41, 52, 50, 63, 117, 58, 43, 43, 42, 46, 58, 41, 58, 53, 47, 50, 53, 62),
    )

    private val instrumentationMarkers: List<String> = listOf(
        decoded(61, 41, 50, 63, 58),
        decoded(60, 46, 54, 118, 49, 40, 118, 55, 52, 52, 43),
        decoded(60, 58, 63, 60, 62, 47),
        decoded(41, 62, 117, 61, 41, 50, 63, 58, 117, 40, 62, 41, 45, 62, 41),
        decoded(55, 50, 53, 49, 62, 56, 47, 52, 41),
        decoded(54, 58, 60, 50, 40, 48),
        decoded(33, 34, 60, 50, 40, 48),
        decoded(35, 43, 52, 40, 62, 63),
        decoded(40, 46, 57, 40, 47, 41, 58, 47, 62),
    )

    private const val OBFUSCATION_KEY = 91
}
