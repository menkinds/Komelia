import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

group = "io.github.snd_r.komelia.infra.onnxruntime.jvm"
version = "unspecified"

kotlin {
    jvm {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.komeliaInfra.jni)
            api(projects.komeliaInfra.imageDecoder.vips)
            api(projects.komeliaInfra.onnxruntime.api)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlin.logging)
        }

        jvmMain.dependencies {
            implementation(libs.directories)
        }
    }
}
