plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

android {
    compileSdkVersion(30)
    buildToolsVersion("29.0.3")

    defaultConfig {
        applicationId = "org.sagebionetworks.research.mtb.app"
        minSdkVersion(23)
        targetSdkVersion(30)
        versionCode = 12
        versionName = "0.7.$versionCode"

        multiDexEnabled = true
        multiDexKeepFile = File("multidex-config.txt")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.addAll(setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
            debugSymbolLevel = "FULL"
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("x86", "x86_64", "armeabi", "armeabi-v7a", "mips", "mips64", "arm64-v8a")
            isUniversalApk = true
        }
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

    packagingOptions {
        pickFirst("**/*.so")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures.viewBinding = true
    ndkVersion = "22.1.7171670"
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // Sage dependencies
    implementation("org.sagebionetworks:BridgeDataUploadUtils:0.2.6") {
        exclude(group = "joda-time", module = "joda-time")
        exclude(group = "org.bouncycastle")
        exclude(group = "com.madgag.spongycastle") //pkix renamed to bcpkix-jdk15on, causes dupes
    }
    implementation("com.madgag.spongycastle:core:1.58.0.0")
    implementation("com.madgag.spongycastle:prov:1.58.0.0")
    // marked api due to propagation of CMSException
    implementation("com.madgag.spongycastle:bcpkix-jdk15on:1.58.0.0")
    implementation("net.danlew:android.joda:2.9.9.4")
    implementation("androidx.recyclerview:recyclerview:1.2.0")

    val bridgeClientKmmVersion = "0.2.19"
    implementation("org.sagebionetworks.bridge.kmm:bridge-client:$bridgeClientKmmVersion")
    implementation("org.sagebionetworks.bridge.kmm:bridge-client-presentation:$bridgeClientKmmVersion")
    implementation("org.sagebionetworks.bridge.kmm:assessmentmodel-sdk:$bridgeClientKmmVersion")

    // MTB dependencies
    implementation("com.readdle.swift.java.codegen:annotations:0.8.2")
    implementation(Deps.MTB.glide)
    kapt(Deps.MTB.glide_kapt)
//    implementation("edu.northwestern.mobiletoolbox:mtbnavigation:0.4.3-SNAPSHOT")

    implementation("edu.northwestern.mobiletoolbox:mtb-common-ui:0.1.34")
    implementation("edu.northwestern.mobiletoolbox:memory-for-sequences:0.1.35")
    implementation("edu.northwestern.mobiletoolbox:dimensional_change_card_sort:0.1.33")
    implementation("edu.northwestern.mobiletoolbox:picture_sequence_memory:0.1.11")
    implementation("edu.northwestern.mobiletoolbox:flanker:0.1.19")
    implementation("edu.northwestern.mobiletoolbox:spelling:0.1.16")
    implementation("edu.northwestern.mobiletoolbox:vocabulary:0.1.16")
    implementation("edu.northwestern.mobiletoolbox:number_match:0.1.16")
    implementation("edu.northwestern.mobiletoolbox:fname:0.1.16")
    implementation("edu.northwestern.mobiletoolbox:dichotomous_engine:0.1.16")

    val assessment_version = "0.4.6"
    implementation("org.sagebionetworks.assessmentmodel:presentation:$assessment_version")
    implementation("org.sagebionetworks.assessmentmodel:assessmentModel:$assessment_version")

    // Koin
    implementation(Deps.Koin.core)
    implementation(Deps.Koin.android)
    implementation(Deps.Koin.androidWorkManager)

    //I think this can be removed once we remove bridge-client and dagger -nbrown 03/09/21
    configurations {
        all { exclude(group = "com.google.guava", module = "listenablefuture") }
    }


    // Kotlin
    implementation(kotlin("stdlib-jdk7", Versions.kotlin))
    implementation(kotlin("reflect", Versions.kotlin))
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.2.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3-native-mt") {
        version {
            strictly("1.4.3-native-mt")
        }
    }

    // Android
    implementation("androidx.appcompat:appcompat:1.3.0")
    implementation("com.android.support:multidex:1.0.3")

    implementation("com.google.android.material:material:1.3.0")

    implementation("androidx.constraintlayout:constraintlayout:2.0.4")

    implementation("androidx.core:core-ktx:1.5.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.3.1")
    implementation("androidx.activity:activity-ktx:1.2.3")
    implementation("androidx.fragment:fragment-ktx:1.3.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.5")

    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    //For support Instant now() for API <26
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}
