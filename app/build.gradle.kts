import com.google.protobuf.gradle.*

plugins {
    alias(libs.plugins.agp.app)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.lsplugin.resopt)
    alias(libs.plugins.lsplugin.jgit)
    alias(libs.plugins.lsplugin.apksign)
    alias(libs.plugins.lsplugin.apktransform)
    alias(libs.plugins.lsplugin.cmaker)
}

val appVerCode = jgit.repo()?.commitCount("refs/remotes/origin/master") ?: 0
val appVerName: String by rootProject


apksign {
    storeFileProperty = "releaseStoreFile"
    storePasswordProperty = "releaseStorePassword"
    keyAliasProperty = "releaseKeyAlias"
    keyPasswordProperty = "releaseKeyPassword"
}

apktransform {
    copy {
        when (it.buildType) {
            "release" -> file("${it.name}/WeChatRoaming_${appVerName}.apk")
            else -> null
        }
    }
}

cmaker {
    default {
        targets("wechatroaming")
        abiFilters("armeabi-v7a", "arm64-v8a", "x86")
        arguments += "-DANDROID_STL=none"
        cppFlags += "-Wno-c++2b-extensions"
    }

    buildTypes {
        arguments += "-DDEBUG_SYMBOLS_PATH=${project.buildDir.absolutePath}/symbols/${it.name}"
    }
}


android {
    namespace = "cn.martinkay.wechatroaming"
    compileSdk = 33
    buildToolsVersion = "33.0.2"
    ndkVersion = "25.2.9519653"

    buildFeatures {
        prefab = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "'cn.martinkay.wechatroaming'"
        minSdk = 24
        targetSdk = 33  // Target Android T
        versionCode = appVerCode
        versionName = appVerName
    }


    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles("proguard-rules.pro")
        }
    }


    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_11)
        targetCompatibility(JavaVersion.VERSION_11)
    }

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf(
            "-Xno-param-assertions",
            "-Xno-call-assertions",
            "-Xno-receiver-assertions",
            "-opt-in=kotlin.RequiresOptIn",
            "-language-version=2.0",
        )
    }

    sourceSets {
        named("main") {
            proto {
                srcDir("src/main/proto")
                include("**/*.proto")
            }
        }
    }

    packaging {
        resources {
            excludes += "**"
        }
    }

    lint {
        checkReleaseBuilds = false
    }

    dependenciesInfo {
        includeInApk = false
    }

    androidResources {
        additionalParameters += arrayOf("--allow-reserved-package-id", "--package-id", "0x23")
    }

//    externalNativeBuild {
//        cmake {
//            path("src/main/jni/CMakeLists.txt")
//            version = "3.22.1+"
//        }
//    }
}


protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                id("java") {
                    option("lite")
                }
                id("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

configurations.all {
    exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk7")
    exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
}

dependencies {
    compileOnly(libs.xposed)
    implementation(libs.androidx.appcompat)
    implementation(libs.protobuf.kotlin)
    implementation(libs.protobuf.java)
    compileOnly(libs.protobuf.protoc)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.coroutines.android)
    implementation(libs.kotlin.coroutines.jdk)
    implementation(libs.androidx.documentfile)
    implementation(libs.cxx)
}

val adbExecutable: String = androidComponents.sdkComponents.adb.get().asFile.absolutePath

val restartBiliBili = task("restartBiliBili").apply {
    doLast {
        exec {
            commandLine(adbExecutable, "shell", "am", "force-stop", "tv.danmaku.bili")
        }
        exec {
            commandLine(
                adbExecutable,
                "shell",
                "am",
                "start",
                "$(pm resolve-activity --components tv.danmaku.bili)"
            )
        }
    }
}

afterEvaluate {
    tasks.getByPath("installDebug").finalizedBy(restartBiliBili)
}