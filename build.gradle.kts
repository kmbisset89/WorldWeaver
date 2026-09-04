import java.time.Year
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.4.0"
    id("org.jetbrains.compose") version "1.11.1"
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.0"
    id("com.google.devtools.ksp") version "2.3.10"
}

group = "io.github.kmbisset89.worldweaver"
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
    implementation("org.jetbrains.compose.components:components-resources-desktop:1.11.1")
    implementation("org.jetbrains.compose.material3:material3:1.11.0-alpha07")
    implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
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
        mainClass = "io.github.kmbisset89.worldweaver.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Deb)
            packageName = "WorldWeaver"
            packageVersion = version.toString()
            description = "World Weaver — tabletop campaign manager ($appHomepage)"
            vendor = appPublisher
            copyright = appCopyright
            licenseFile.set(project.file("LICENSE"))
            // jdk.crypto.ec was folded into java.base; requesting it breaks jlink on some JDK 21 builds.
            // java.instrument / java.prefs / jdk.unsupported: reported by suggestRuntimeModules.
            // java.sql / java.naming: Room's JVM runtime still touches JDBC/JNDI types during startup.
            // java.xml: ImageIO PNG metadata and java.sql both use JAXP.
            // jdk.security.auth: AWT file dialogs on Linux can need Unix principals.
            modules(
                "java.instrument",
                "java.naming",
                "java.prefs",
                "java.sql",
                "java.xml",
                "jdk.unsupported",
                "jdk.security.auth",
            )

            windows {
                iconFile.set(project.file("icons/icon.ico"))
                menuGroup = "World Weaver"
            }

            macOS {
                iconFile.set(project.file("icons/icon.icns"))
                bundleID = "io.github.kmbisset89.worldweaver"
                infoPlist {
                    extraKeysRawXml = """
                        <key>NSHumanReadableCopyright</key>
                        <string>$appCopyright</string>
                    """.trimIndent()
                }
            }

            linux {
                iconFile.set(project.file("icons/icon.png"))
                debMaintainer = "$appPublisher <kmbisset89@users.noreply.github.com>"
            }
        }
        // packageRelease* enables ProGuard shrinking by default. Room, Koin, bundled
        // SQLite, MapCompose, and Skiko still reference classes the shrinker cannot
        // resolve, which makes the Windows launcher report "Failed to launch JVM".
        buildTypes.release.proguard {
            isEnabled.set(false)
            obfuscate.set(false)
            optimize.set(false)
            joinOutputJars.set(false)
        }
    }
}

kotlin {
    jvmToolchain(17)
}

compose.resources {
    packageOfResClass = "io.github.kmbisset89.worldweaver.generated.resources"
    generateResClass = always
}

tasks.register("printPackageVersion") {
    group = "help"
    description = "Prints the Compose Desktop native package version."
    doLast {
        println(
            compose.desktop.application.nativeDistributions.packageVersion
                ?: version.toString(),
        )
    }
}
