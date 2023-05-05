import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val compose_version by extra("1.4.0")
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.4.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
        classpath("org.jetbrains.kotlin:kotlin-serialization:${Versions.kotlin}")
        classpath("com.google.gms:google-services:4.3.15")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.4")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("http://repo-maven.sagebridge.org/")
            isAllowInsecureProtocol = true
        }
        maven(url = "https://sagebionetworks.jfrog.io/artifactory/mobile-sdks/")
        mavenLocal()

        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/MobileToolbox/MobileToolboxAndroid")
            credentials {

                username = gradleLocalProperties(rootProject.rootDir).getProperty("gpr.user")
                    ?: System.getenv("GPR_USER")
                password = gradleLocalProperties(rootProject.rootDir).getProperty("gpr.key")
                    ?: System.getenv("GPR_API_KEY")
            }
        }

        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/MobileToolbox/MobileToolboxNavigation")
            credentials {

                username = gradleLocalProperties(rootProject.rootDir).getProperty("gpr.user")
                    ?: System.getenv("GPR_USER")
                password = gradleLocalProperties(rootProject.rootDir).getProperty("gpr.key")
                    ?: System.getenv("GPR_API_KEY")
            }
        }

        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/heliumfoot/swift-java-codegen")
            credentials {
                username = gradleLocalProperties(rootProject.rootDir).getProperty("gpr.user")
                    ?: System.getenv("GPR_USER")
                password = gradleLocalProperties(rootProject.rootDir).getProperty("gpr.key")
                    ?: System.getenv("GPR_API_KEY")
            }
        }
    }

    configurations.all {

        resolutionStrategy.eachDependency({
            with (requested) {
                // remove after our transitive dependencies migrate
                if (group == "org.koin") {
                    useTarget( "io.insert-koin:${name}:${version}")
                    because("Koin moved groups")
                }
                if (group == "org.jetbrains.kotlin") {
                    if (name.startsWith("kotlin-stdlib")) {
                        useTarget( "org.jetbrains.kotlin:${name}:${Versions.kotlin}")

                    }
                }
            }
        })

    }

}

//task clean(type: Delete) {
//    delete rootProject.buildDir
//}