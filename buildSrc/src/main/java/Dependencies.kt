object Versions {
    const val kotlin = "1.5.30"
    const val dagger = "2.21"
    const val koin = "3.1.1"
    const val glide = "4.12.0"
    const val napier = "2.1.0"
    const val bridgeClient = "0.3.1"
    const val passiveData = "0.2.0"
    const val kotlinCoroutines = "1.5.1-native-mt"
    const val kotlinxSerializationJson = "1.3.0"
}


object Deps {
    object Koin {
        val core = "io.insert-koin:koin-core:${Versions.koin}"
        val test = "io.insert-koin:koin-test:${Versions.koin}"
        val android = "io.insert-koin:koin-android:${Versions.koin}"
        val androidViewModel = "io.insert-koin:koin-androidx-viewmodel:${Versions.koin}"
        val androidWorkManager = "io.insert-koin:koin-androidx-workmanager:${Versions.koin}"
    }

    object MTB {
        val glide = "com.github.bumptech.glide:glide:${Versions.glide}"
        val glide_kapt = "com.github.bumptech.glide:compiler:${Versions.glide}"
    }

    object Napier {
        val napier = "io.github.aakira:napier:${Versions.napier}"
    }
}