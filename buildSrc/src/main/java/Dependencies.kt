object Versions {
    val kotlin = "1.4.10"
    val dagger = "2.21"
    const val koin = "3.0.1-alpha-3"
}


object Deps {
    object Koin {
        val core = "org.koin:koin-core:${Versions.koin}"
        val test = "org.koin:koin-test:${Versions.koin}"
        val android = "org.koin:koin-android:${Versions.koin}"
        val androidViewModel = "org.koin:koin-androidx-viewmodel:${Versions.koin}"
        val androidWorkManager = "org.koin:koin-androidx-workmanager:${Versions.koin}"
    }
}