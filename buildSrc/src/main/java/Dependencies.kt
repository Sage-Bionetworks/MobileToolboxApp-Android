object Versions {
    val kotlin = "1.5.30"
    val dagger = "2.21"
    const val koin = "3.1.1"
    const val glide = "4.11.0"
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
}