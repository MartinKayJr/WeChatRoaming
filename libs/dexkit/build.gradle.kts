@file:Suppress("UnstableApiUsage")

plugins {
    id("build-logic.android.library")
}

android {
    namespace = "io.luckypray.dexkit"
    sourceSets {
        val main by getting
        main.apply {
            manifest.srcFile("AndroidManifest.xml")
            java.setSrcDirs(listOf("DexKit/dexkit/src/main/java"))
        }
    }
}
