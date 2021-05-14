plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    id("kotlin-android")
}

android {
    compileSdkVersion(30)
    buildToolsVersion("29.0.3")

    defaultConfig {
        applicationId = "org.sagebionetworks.research.mtb.app"
        minSdkVersion(23)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.0"

        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    buildFeatures.viewBinding = true
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // Sage dependencies
    implementation("org.sagebionetworks.bridge:android-sdk:0.5.0")
    val bridgeClientKmmVersion = "0.2.7"
    implementation("org.sagebionetworks.bridge.kmm:bridge-client:$bridgeClientKmmVersion")
    implementation("org.sagebionetworks.bridge.kmm:bridge-client-presentation:$bridgeClientKmmVersion")

    // MTB dependencies
    val mtbVersion = "0.1.18"
    implementation("edu.northwestern.mobiletoolbox:mtb-common-ui:$mtbVersion")
    implementation("edu.northwestern.mobiletoolbox:memory-for-sequences:$mtbVersion")
    implementation("edu.northwestern.mobiletoolbox:dimensional_change_card_sort:$mtbVersion")
    implementation("edu.northwestern.mobiletoolbox:mtbnavigation:0.1.31")
    implementation("com.readdle.swift.java.codegen:annotations:0.8.2")

    val assessment_version = "0.4.2"
    implementation("org.sagebionetworks.assessmentmodel:presentation:$assessment_version")
    implementation("org.sagebionetworks.assessmentmodel:assessmentModel:$assessment_version")


    // Koin
    implementation(Deps.Koin.core)
    implementation(Deps.Koin.android)
    implementation(Deps.Koin.androidWorkManager)

    //I think this can be removed once we remove bridge-client and dagger -nbrown 03/09/21
    configurations {
        all{ exclude( group="com.google.guava", module="listenablefuture")}
    }


    // Kotlin
    implementation(kotlin("stdlib-jdk7", Versions.kotlin))
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.2.0")

    // Android
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("com.android.support:multidex:1.0.3")

    implementation("com.google.android.material:material:1.3.0")

    implementation("androidx.constraintlayout:constraintlayout:2.0.1")

    implementation("androidx.core:core-ktx:1.3.1")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0")
    implementation("androidx.activity:activity-ktx:1.1.0")
    implementation("androidx.fragment:fragment-ktx:1.2.5")

    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    //For support Instant now() for API <26
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")

    testImplementation("junit:junit:4.13")

    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}