plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("kotlin-android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    compileSdk = 33

    defaultConfig {
        applicationId = "org.sagebionetworks.research.mobiletoolbox.app"
        minSdk = 23
        targetSdk = 33
        versionCode = 40
        versionName = "0.28.$versionCode"

        multiDexEnabled = true
        multiDexKeepFile = File("multidex-config.txt")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["useTestStorageService"] = "true"

        ndk {
            abiFilters.addAll(setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
            debugSymbolLevel = "FULL"
        }
        vectorDrawables {
            useSupportLibrary = true
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
    flavorDimensions += "version"
    productFlavors {
        create("prod") {
            dimension = "version"
        }
        create("staging") {
            dimension = "version"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
        }
    }
    packagingOptions {
        jniLibs {
            pickFirsts += setOf("**/*.so")
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
    }

    buildFeatures.viewBinding = true
    ndkVersion = "22.1.7171670"
    androidResources {
        noCompress += listOf("pdf")
    }
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
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    implementation("org.sagebionetworks.bridge.kmm:bridge-client:${Versions.bridgeClient}")
    implementation("org.sagebionetworks.bridge.kmm:bridge-client-presentation:${Versions.bridgeClient}")
    implementation("org.sagebionetworks.bridge.kmm:assessmentmodel-sdk:${Versions.bridgeClient}")

    implementation("org.sagebionetworks.research.kmm:passiveData:${Versions.passiveData}")

    // MTB dependencies
    implementation("com.readdle.swift.java.codegen:annotations:0.8.2")
    implementation(Deps.MTB.glide)
    kapt(Deps.MTB.glide_kapt)

    implementation("edu.northwestern.mobiletoolbox:assessments_provider:1.5.8")

    val assessmentVersion = "0.12.0"
    implementation("org.sagebionetworks.assessmentmodel:presentation:$assessmentVersion")
    implementation("org.sagebionetworks.assessmentmodel:assessmentModel:$assessmentVersion")
    implementation("org.sagebionetworks.motorcontrol:MotorControl:0.0.3")

    // Kermit
    implementation("co.touchlab:kermit:${Versions.kermit}")
    implementation("co.touchlab:kermit-crashlytics:${Versions.kermit}")

    // Koin
    implementation(Deps.Koin.core)
    implementation(Deps.Koin.android)
    implementation(Deps.Koin.androidWorkManager)

    // Kotlin
    implementation(kotlin("stdlib-jdk7", Versions.kotlin))
    implementation(kotlin("reflect", Versions.kotlin))
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutines}") 
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlinCoroutines}")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinxSerializationJson}")


    // Firebase crashlytics
    implementation("com.google.firebase:firebase-crashlytics-ktx:18.3.6")

    // Android
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("com.android.support:multidex:1.0.3")

    implementation("com.google.android.material:material:1.4.0")

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

    implementation("androidx.compose.ui:ui:${rootProject.extra["compose_version"]}")
    implementation("androidx.compose.material:material:${rootProject.extra["compose_version"]}")
    implementation("androidx.compose.ui:ui-tooling-preview:${rootProject.extra["compose_version"]}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.0")
    implementation("androidx.activity:activity-compose:1.6.1")

    debugImplementation("androidx.compose.ui:ui-tooling:${rootProject.extra["compose_version"]}")

    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")

    //For support Instant now() for API <26
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.kotlinCoroutines}")

    val espressoVersion = "3.5.1"
    androidTestImplementation("androidx.arch.core:core-testing:2.1.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:$espressoVersion")
    androidTestImplementation( "androidx.test.espresso:espresso-contrib:$espressoVersion")
    androidTestImplementation("org.hamcrest:hamcrest:2.2")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")

    androidTestUtil("androidx.test.services:test-services:1.4.2")

    // Test rules and transitive dependencies:
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${rootProject.extra["compose_version"]}")
    // Needed for createAndroidComposeRule, but not createComposeRule:
    debugImplementation("androidx.compose.ui:ui-test-manifest:${rootProject.extra["compose_version"]}")
}
