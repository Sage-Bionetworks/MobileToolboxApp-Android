import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

allprojects {
    repositories {

        google()
        jcenter()

        maven(url = "http://repo-maven.sagebridge.org/")
        maven(url = "https://kotlin.bintray.com/kotlinx/")
        maven(url = "https://dl.bintray.com/heliumfoot/maven")
        maven(url = "https://dl.bintray.com/readdle/maven")
        maven(url = "https://dl.bintray.com/sage-bionetworks/AssessmentModel-KotlinNative")
        maven(url = "https://dl.bintray.com/sage-bionetworks/BridgeClientKMM")
        maven(url = "https://dl.bintray.com/ekito/koin")

        maven {

            name = "GitHubPackages"

            url = uri("https://maven.pkg.github.com/MobileToolbox/MobileToolboxAndroid")
            credentials {

                username = gradleLocalProperties(rootProject.rootDir).getProperty("gpr.user") ?: System.getenv("GPR_USER")
                password = gradleLocalProperties(rootProject.rootDir).getProperty("gpr.key") ?: System.getenv("GPR_API_KEY")
            }
        }

        repositories {
            maven {

                name = "GitHubPackages"

                url = uri("https://maven.pkg.github.com/MobileToolbox/MobileToolboxNavigation")
                credentials {

                    gradleLocalProperties(rootProject.rootDir).getProperty("gpr.user") ?: System.getenv("GPR_USER")
                    gradleLocalProperties(rootProject.rootDir).getProperty("gpr.key") ?: System.getenv("GPR_API_KEY")
                }
            }
        }

    }
}

//task clean(type: Delete) {
//    delete rootProject.buildDir
//}