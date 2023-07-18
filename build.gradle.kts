import com.android.build.gradle.BaseExtension

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("build-logic.root-project")
    alias(libs.plugins.kotlin.jvm) apply false
}

tasks.register<Delete>("clean").configure {
    delete(rootProject.buildDir)
}
