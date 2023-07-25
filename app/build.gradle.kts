import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.android.tools.build.apkzlib.sign.SigningExtension
import com.android.tools.build.apkzlib.sign.SigningOptions
import com.android.tools.build.apkzlib.zfile.ZFiles
import com.android.tools.build.apkzlib.zip.AlignmentRules
import com.android.tools.build.apkzlib.zip.CompressionMethod
import com.android.tools.build.apkzlib.zip.ZFile
import com.android.tools.build.apkzlib.zip.ZFileOptions
import org.jetbrains.changelog.markdownToHTML
import java.io.FileInputStream
import java.security.KeyStore
import java.security.cert.X509Certificate
import java.util.UUID

//plugins {
//    alias(libs.plugins.agp.app)
//    alias(libs.plugins.kotlin)
//    alias(libs.plugins.protobuf)
//    alias(libs.plugins.lsplugin.resopt)
//    alias(libs.plugins.lsplugin.jgit)
//    alias(libs.plugins.lsplugin.apksign)
//    alias(libs.plugins.lsplugin.apktransform)
//    alias(libs.plugins.lsplugin.cmaker)
//}


@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("build-logic.android.application")
    alias(libs.plugins.changelog)
    alias(libs.plugins.ksp)
    alias(libs.plugins.license)
    alias(libs.plugins.serialization)
    alias(libs.plugins.aboutlibraries)
}

val currentBuildUuid = UUID.randomUUID().toString()
println("Current build ID is $currentBuildUuid")

val releaseTime = Common.getReleaseTime()
println("Current Release Time is $releaseTime")


val ccacheExecutablePath = Common.findInPath("ccache")

if (ccacheExecutablePath != null) {
    println("Found ccache at $ccacheExecutablePath")
} else {
    println("No ccache found.")
}


android {
    namespace = "cn.martinkay.wechatroaming"
    ndkVersion = Version.getNdkVersion(project)

    defaultConfig {
        applicationId = "cn.martinkay.wechatroaming"
        buildConfigField("String", "BUILD_UUID", "\"$currentBuildUuid\"")
        buildConfigField("long", "BUILD_TIMESTAMP", "${System.currentTimeMillis()}L")
        buildConfigField("String", "VERSION_DATE_TIME", "\"$releaseTime\"")

        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true

        externalNativeBuild {
            cmake {
                ccacheExecutablePath?.let {
                    arguments += listOf(
                        "-DCMAKE_C_COMPILER_LAUNCHER=$it",
                        "-DCMAKE_CXX_COMPILER_LAUNCHER=$it",
                        "-DNDK_CCACHE=$it",
                        "-DANDROID_CCACHE=$it",
                    )
                }

                val flags = arrayOf(
                    "-Qunused-arguments",
                    "-fno-rtti",
                    "-fvisibility=hidden",
                    "-fvisibility-inlines-hidden",
                    "-fno-omit-frame-pointer",
                    "-Wno-unused-value",
                    "-Wno-unused-variable",
                    "-Wno-unused-command-line-argument",
                    "-DMMKV_DISABLE_CRYPT",
                )
                cppFlags("-std=c++17", *flags)
                cFlags("-std=c18", *flags)
                // 需要在这里添加才能添加到lib中
                // libwechatroaming.so
                targets += "wechatroaming"
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = File(projectDir, "src/main/cpp/CMakeLists.txt")
            version = Version.getCMakeVersion(project)
        }
    }

    buildTypes {
        getByName("release") {
//            isDebuggable = true
            isShrinkResources = false
            isMinifyEnabled = false
            proguardFiles("proguard-rules.pro")
            kotlinOptions.suppressWarnings = true
            val ltoCacheFlags = listOf(
                "-flto=thin",
                "-Wl,--thinlto-cache-policy,cache_size_bytes=300m",
                "-Wl,--thinlto-cache-dir=${buildDir.absolutePath}/.lto-cache",
            )
            val releaseFlags = arrayOf(
                "-ffunction-sections",
                "-fdata-sections",
                "-Wl,--gc-sections",
                "-Oz",
                "-Wl,--exclude-libs,ALL",
                "-DNDEBUG",
            )
            externalNativeBuild.cmake {
                // 是-D 不要少了D否则血的教训
                arguments += "-DWECHATROAMING_VERSION=${defaultConfig.versionName}"
                cFlags += releaseFlags
                cppFlags += releaseFlags
                cFlags += ltoCacheFlags
                cppFlags += ltoCacheFlags
            }
        }
        getByName("debug") {
            @Suppress("ChromeOsAbiSupport")
            ndk.abiFilters += arrayOf("arm64-v8a", "armeabi-v7a")
            isCrunchPngs = false
            proguardFiles("proguard-rules.pro")
            val debugFlags = arrayOf<String>(
//                "-DMODULE_SIGNATURE=E7A8AEB0A1431D12EB04BF1B7FC31960",
//                "-DTEST_SIGNATURE",
            )
            externalNativeBuild.cmake {
                // 是-D 不要少了D否则血的教训
                arguments += "-DWECHATROAMING_VERSION=${Version.versionName}.debug"
                cFlags += debugFlags
                cppFlags += debugFlags
            }
        }
    }


    // 避免资源冲突 0x00-0xff
    androidResources {
        additionalParameters("--allow-reserved-package-id", "--package-id", "0x39")
    }


    packagingOptions {
        resources.excludes.addAll(
            arrayOf(
                "META-INF/**",
                "kotlin/**",
                "**.bin",
                "kotlin-tooling-metadata.json"
            )
        )
    }

    buildFeatures {
        prefab = true
        aidl = true
        buildConfig = true
        dataBinding = true
        viewBinding = true
    }

    buildFeatures.viewBinding = true

    lint {
        checkDependencies = true
    }

    kotlinOptions {
        freeCompilerArgs += listOf(
            "-Xno-call-assertions",
            "-Xno-receiver-assertions",
            "-Xno-param-assertions",
        )
    }

    applicationVariants.all {
        val variantCapped = name.capitalize()
        val mergeAssets = tasks.getByName("merge${variantCapped}Assets")
//        mergeAssets.dependsOn(generateEulaAndPrivacy)
//        mergeAssets.dependsOn("data${variantCapped}Descriptor")
    }


}

kotlin {
    sourceSets.configureEach {
        kotlin.srcDir("$buildDir/generated/ksp/$name/kotlin/")
    }
}

licenseReport {
    generateCsvReport = false
    generateHtmlReport = false
    generateJsonReport = true
    generateTextReport = false

    copyCsvReportToAssets = false
    copyHtmlReportToAssets = false
}





configurations.all {
    exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk7")
    exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
}

dependencies {

    implementation(libs.androidx.appcompat)
    implementation(libs.protobuf.kotlin)
    implementation(libs.protobuf.java)
    compileOnly(libs.protobuf.protoc)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.coroutines.android)
    implementation(libs.kotlin.coroutines.jdk)
    implementation(libs.androidx.documentfile)
    implementation(libs.cxx)
    implementation(libs.apache.commons)
    implementation(projects.libs.mmkv)
    ksp(projects.libs.ksp)
    compileOnly(libs.xposed)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.browser)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.common)
    implementation(libs.lifecycle.runtime)
    implementation(libs.hiddenapibypass)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.material)
    implementation(libs.flexbox)
    implementation(libs.colorpicker)
    implementation(libs.material.dialogs.core)
    implementation(libs.material.dialogs.input)
    implementation(libs.ezXHelper)
    // festival title
    implementation(libs.confetti)
    implementation(libs.weatherView)
    implementation(libs.appcenter.analytics)
    implementation(libs.appcenter.crashes)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.sealedEnum.runtime)
    ksp(libs.sealedEnum.ksp)

    implementation(libs.dexKit)
    implementation(libs.androidx.swipeRefreshLayout)
    implementation(libs.flycoTabLayout)
    implementation(libs.netty)
    implementation(libs.fastjson)
    implementation(libs.guava)
    implementation(libs.androidx.resourceinspection)
    implementation(libs.apache.commons)
}


val adb: String = androidComponents.sdkComponents.adb.get().asFile.absolutePath
val packageName = "cn.martinkay.wechatroaming"
val killBs = tasks.register<Exec>("killBs") {
    group = "wechatroaming"
    commandLine(adb, "shell", "am", "force-stop", packageName)
    isIgnoreExitValue = true
}

val openBs = tasks.register<Exec>("openBs") {
    group = "wechatroaming"
    commandLine(adb, "shell", "am", "start", "$(pm resolve-activity --components $packageName)")
    isIgnoreExitValue = true
}

val restartBs = tasks.register<Exec>("restartBs") {
    group = "wechatroaming"
    commandLine(adb, "shell", "am", "start", "$(pm resolve-activity --components $packageName)")
    isIgnoreExitValue = true
}.dependsOn(killBs)

androidComponents.onVariants { variant ->
    val variantCapped = variant.name.capitalize()
    task("install${variantCapped}AndRestartQQ") {
        group = "wechatroaming"
        dependsOn(":app:install$variantCapped")
        finalizedBy(restartBs)
    }
//    task("data${variantCapped}Descriptor") {
//        inputs.file("${buildDir}/reports/licenses/license${variantCapped}Report.json")
//        outputs.file("${projectDir}/src/main/assets/open_source_licenses.json")
//        dependsOn("license${variantCapped}Report")
//
//        doFirst {
//            val input = inputs.files.singleFile
//            val output = outputs.files.singleFile
//            this.runCatching {
//                output.writeText(Licenses.transform(input.readText()))
//            }
//        }
//    }
}

tasks.register<task.ReplaceIcon>("replaceIcon") {
    group = "wechatroaming"
    projectDir.set(project.projectDir)
    commitHash = Common.getGitHeadRefsSuffix(rootProject)
    config()
}.also { tasks.preBuild.dependsOn(it) }

tasks.register<Delete>("cleanCxxIntermediates") {
    group = "wechatroaming"
    delete(file(".cxx"))
}.also { tasks.clean.dependsOn(it) }

tasks.register("checkGitSubmodule") {
    group = "wechatroaming"
    val projectDir = rootProject.projectDir
    doLast {
        listOf(
            "libs/mmkv/MMKV/Core",
        ).forEach {
            val submoduleDir = File(projectDir, it.replace('/', File.separatorChar))
            if (!submoduleDir.exists()) {
                throw IllegalStateException(
                    "submodule dir not found: $submoduleDir" +
                            "\nPlease run 'git submodule init' and 'git submodule update' manually."
                )
            }
        }
    }
}.also { tasks.preBuild.dependsOn(it) }

val synthesizeDistReleaseApksCI by tasks.registering {
    group = "build"
    // use :app:assembleRelease output apk as input
    dependsOn(":app:packageRelease")
    inputs.files(tasks.named("packageRelease").get().outputs.files)
    val srcApkDir =
        File(project.buildDir, "outputs" + File.separator + "apk" + File.separator + "release")
    if (srcApkDir !in tasks.named("packageRelease").get().outputs.files) {
        val msg = "srcApkDir should be in packageRelease outputs, srcApkDir: $srcApkDir, " +
                "packageRelease outputs: ${tasks.named("packageRelease").get().outputs.files.files}"
        logger.error(msg)
    }
    // output name format: "BlackSpider-v${defaultConfig.versionName}-${productFlavors.first().name}.apk"
    val outputAbiVariants = mapOf(
        "arm32" to arrayOf("armeabi-v7a"),
        "arm64" to arrayOf("arm64-v8a"),
        "armAll" to arrayOf("armeabi-v7a", "arm64-v8a"),
        "universal" to arrayOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
    )
    val versionName = android.defaultConfig.versionName
    val outputDir = File(project.buildDir, "outputs" + File.separator + "ci")
    // declare output files
    outputAbiVariants.forEach { (variant, _) ->
        val outputName = "wechatroaming-v${versionName}-${variant}.apk"
        outputs.file(File(outputDir, outputName))
    }
    val signConfig = android.signingConfigs.findByName("release")
    val minSdk = android.defaultConfig.minSdk!!
    doLast {
        if (signConfig == null) {
            logger.error("Task :app:synthesizeDistReleaseApksCI: No release signing config found, skip signing")
        }
        val requiredAbiList = listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        outputDir.mkdir()
        val options = com.android.tools.build.apkzlib.zip.ZFileOptions().apply {
            alignmentRule = com.android.tools.build.apkzlib.zip.AlignmentRules.constantForSuffix(".so", 4096)
            noTimestamps = true
            autoSortFiles = true
        }
        require(srcApkDir.exists()) { "srcApkDir not found: $srcApkDir" }
        // srcApkDir should have one apk file
        val srcApkFiles =
            srcApkDir.listFiles()?.filter { it.isFile && it.name.endsWith(".apk") }
                ?: emptyList()
        require(srcApkFiles.size == 1) { "input apk should have one apk file, but found ${srcApkFiles.size}" }
        val inputApk = srcApkFiles.single()
        val startTime = System.currentTimeMillis()
        com.android.tools.build.apkzlib.zip.ZFile.openReadOnly(inputApk).use { srcApk ->
            // check whether all required abis are in the apk
            requiredAbiList.forEach { abi ->
                val path = "lib/$abi/libwechatroaming.so"
                require(srcApk.get(path) != null) { "input apk should contain $path, but not found" }
            }
            outputAbiVariants.forEach { (variant, abis) ->
                val outputApk = File(outputDir, "wechatroaming-v${versionName}-${variant}.apk")
                if (outputApk.exists()) {
                    outputApk.delete()
                }
                com.android.tools.build.apkzlib.zfile.ZFiles.apk(outputApk, options).use { dstApk ->
                    if (signConfig != null) {
                        val keyStore =
                            KeyStore.getInstance(signConfig.storeType
                                ?: KeyStore.getDefaultType())
                        FileInputStream(signConfig.storeFile!!).use {
                            keyStore.load(it, signConfig.storePassword!!.toCharArray())
                        }
                        val protParam =
                            KeyStore.PasswordProtection(signConfig.keyPassword!!.toCharArray())
                        val keyEntry = keyStore.getEntry(signConfig.keyAlias!!, protParam)
                        val privateKey = keyEntry as KeyStore.PrivateKeyEntry
                        val signingOptions = com.android.tools.build.apkzlib.sign.SigningOptions.builder()
                            .setMinSdkVersion(minSdk)
                            .setV1SigningEnabled(minSdk < 24)
                            .setV2SigningEnabled(true)
                            .setKey(privateKey.privateKey)
                            .setCertificates(privateKey.certificate as X509Certificate)
                            .setValidation(com.android.tools.build.apkzlib.sign.SigningOptions.Validation.ASSUME_INVALID)
                            .build()
                        com.android.tools.build.apkzlib.sign.SigningExtension(signingOptions).register(dstApk)
                    }
                    // add input apk to the output apk
                    srcApk.entries().forEach { entry ->
                        val cdh = entry.centralDirectoryHeader
                        val name = cdh.name
                        val isCompressed =
                            cdh.compressionInfoWithWait.method != com.android.tools.build.apkzlib.zip.CompressionMethod.STORE
                        if (name.startsWith("lib/")) {
                            val abi = name.substring(4).split('/').first()
                            if (abis.contains(abi)) {
                                dstApk.add(name, entry.open(), isCompressed)
                            }
                        } else if (name.startsWith("META-INF/com/android/")) {
                            // drop gradle version
                        } else {
                            // add all other entries to the output apk
                            dstApk.add(name, entry.open(), isCompressed)
                        }
                    }
                    dstApk.update()
                }
            }
        }
        val endTime = System.currentTimeMillis()
        logger.info("Task :app:synthesizeDistReleaseApksCI: completed in ${endTime - startTime}ms")
    }
}


// 最终用户许可和隐私
val generateEulaAndPrivacy by tasks.registering {
    inputs.files("${rootDir}/LICENSE.md", "${rootDir}/PRIVACY_LICENSE.md")
    outputs.file("${projectDir}/src/main/assets/eulaAndPrivacy.html")

    doFirst {
        val html = inputs.files.map { markdownToHTML(it.readText()) }
        outputs.files.forEach {
            val output = buildString {
                append("<!DOCTYPE html><head><meta charset=\"UTF-8\"></head><body><html>")
                html.forEach(::append)
                append("</body></html>")
            }.lines().joinToString("")
            it.writeText(output)
        }
    }
}
