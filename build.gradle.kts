import java.time.Year
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.4.0"
    id("org.jetbrains.compose") version "1.11.1"
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.0"
    id("com.google.devtools.ksp") version "2.3.10"
}

group = "net.tactware.worldweaver"
version = "1.0.0"

val appPublisher = "Kerry Bisset"
val appHomepage = "https://github.com/kmbisset89/WorldWeaver"
val appCopyright = "Copyright © ${Year.now()} $appPublisher"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.compose.material3:material3:1.11.0-alpha07")
    implementation("org.jetbrains.compose.material:material-icons-extended:1.11.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2")
    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.sqlite:sqlite-bundled:2.5.2")
    implementation("io.insert-koin:koin-core:4.1.1")
    implementation("ovh.plrapps:mapcompose-mp:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    ksp("androidx.room:room-compiler:2.7.2")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

compose.desktop {
    application {
        mainClass = "net.tactware.worldweaver.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Deb)
            packageName = "WorldWeaver"
            packageVersion = "1.0.0"
            description = "World Weaver — tabletop campaign manager ($appHomepage)"
            vendor = appPublisher
            copyright = appCopyright
            licenseFile.set(project.file("LICENSE"))

            windows {
                menuGroup = "World Weaver"
            }

            macOS {
                bundleID = "io.github.kmbisset89.worldweaver"
                infoPlist {
                    extraKeysRawXml = """
                        <key>NSHumanReadableCopyright</key>
                        <string>$appCopyright</string>
                    """.trimIndent()
                }
            }

            linux {
                debMaintainer = "$appPublisher <kmbisset89@users.noreply.github.com>"
            }
        }
    }
}

kotlin {
    jvmToolchain(17)
}
