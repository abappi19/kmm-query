import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.kotlin.serialization)
    id("maven-publish")
}

group = "io.github.abappi19"
version = "1.0.0"

kotlin {
    jvm()
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser{
            testTask {
                useKarma {
                    useSafari()
                }
            }
        }

        // ...
        binaries.executable()
    }

    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    linuxX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                //put your multiplatform dependencies here
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.google.gson)

                // Crypto hash
                implementation(project.dependencies.platform("org.kotlincrypto.hash:bom:0.7.0"))
                implementation(libs.hash.md)

            }
        }
        val wasmJsMain by getting {
            dependencies {
                // Wasm-specific dependencies
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

android {
    namespace = "io.github.abappi19.kmm_query"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates(group.toString(), "kmm_query", version.toString())

    pom {
        name = "KMM Query"
        description = "KMM Query is a query library that with lot of options."
        inceptionYear = "2025"
        url = "https://github.com/abappi19/kmm-query/"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "abappi19"
                name = "Muhammad Bappi"
                url = "https://github.com/abappi19"
            }
        }
        scm {
            url = "https://github.com/abappi19/kmm-query/"
            connection = "scm:git:git://github.com/abappi19/kmm-query.git"
            developerConnection = "scm:git:ssh://git@github.com/abappi19/kmm-query.git"
        }
    }
}
