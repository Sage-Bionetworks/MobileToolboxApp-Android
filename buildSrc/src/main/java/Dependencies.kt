object Versions {
    const val kotlin = "1.7.0"
    const val koin = "3.1.5"
    const val glide = "4.12.0"
    const val kermit = "1.0.0"
    const val bridgeClient = "0.14.2"
    const val passiveData = "0.5.1"
    const val kotlinCoroutines = "1.5.1-native-mt"
    const val kotlinxSerializationJson = "1.3.0"
}


object Deps {
    object Koin {
        const val core = "io.insert-koin:koin-core:${Versions.koin}"
        const val test = "io.insert-koin:koin-test:${Versions.koin}"
        const val android = "io.insert-koin:koin-android:${Versions.koin}"
        const val androidViewModel = "io.insert-koin:koin-androidx-viewmodel:${Versions.koin}"
        const val androidWorkManager = "io.insert-koin:koin-androidx-workmanager:${Versions.koin}"
    }

    object MTB {
        const val glide = "com.github.bumptech.glide:glide:${Versions.glide}"
        const val glide_kapt = "com.github.bumptech.glide:compiler:${Versions.glide}"
    }

}