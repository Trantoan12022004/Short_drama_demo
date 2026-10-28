plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

val forceCmakeBuild = providers.gradleProperty("security.forceCmake")
    .map { it.toBoolean() }
    .getOrElse(false)
val hasPrebuiltNative = file("src/main/jniLibs").exists()
val usePrebuiltNative = hasPrebuiltNative && !forceCmakeBuild

android {
    namespace = "com.sulotion.security"
    compileSdk = 36
    ndkVersion = "28.2.13676358"

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        if (!usePrebuiltNative) {
            externalNativeBuild {
                cmake {
                    cppFlags += "-std=c++17"
                }
            }
        }
    }

    if (!usePrebuiltNative) {
        externalNativeBuild {
            cmake {
                path = file("src/main/cpp/CMakeLists.txt")
                version = "3.22.1"
            }
        }
    }

    sourceSets {
        getByName("main") {
            if (!usePrebuiltNative) {
                jniLibs.directories.clear()
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("com.google.android.material:material:1.11.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
}
