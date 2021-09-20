import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.2.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
        classpath("org.jetbrains.kotlin:kotlin-serialization:${Versions.kotlin}")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven(url = "http://repo-maven.sagebridge.org/")
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

        resolutionStrategy.eachDependency(Action {
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