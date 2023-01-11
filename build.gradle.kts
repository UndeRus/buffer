// import dev.petuska.npm.publish.extension.domain.NpmAccess
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest
buildscript {
    extra.apply {
        set("kotlin_version", "1.8.0")
    }
}
plugins {
    id("dev.petuska.npm.publish") version "3.2.0"
    kotlin("multiplatform") version "1.8.0"
    id("com.android.library")
    id("io.codearte.nexus-staging") version "0.30.0"
    `maven-publish`
    signing
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    id("org.jlleitschuh.gradle.ktlint-idea") version "11.0.0"
}

val libraryVersionPrefix: String by project
group = "com.ditchoom"
version = "$libraryVersionPrefix.0-SNAPSHOT"
val libraryVersion = if (System.getenv("GITHUB_RUN_NUMBER") != null) {
    "$libraryVersionPrefix${(Integer.parseInt(System.getenv("GITHUB_RUN_NUMBER")) - 40)}"
} else {
    "${libraryVersionPrefix}0-SNAPSHOT"
}

repositories {
    google()
    mavenCentral()
    maven { setUrl("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers/") }
}

kotlin {
    android {
        publishLibraryVariants("release")
    }
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }
    js(IR) {
        moduleName = "buffer-kt"
        binaries.library()
        browser()
        nodejs()
    }

    macosX64()
    macosArm64()
    linuxX64()
    ios()
    iosX64()
    iosSimulatorArm64()
    tasks.getByName<KotlinNativeSimulatorTest>("iosSimulatorArm64Test") {
        deviceId = "iPhone 14"
    }
    watchosX64()
    watchosArm64()
    watchosSimulatorArm64()
    tvos()
    tvosSimulatorArm64()

    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
            }
        }
        val jvmMain by getting {
        }
        val jvmTest by getting {
            kotlin.srcDir("src/commonJvmTest/kotlin")
        }

        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin-wrappers:kotlin-web:1.0.0-pre.467")
            }
        }
        val jsTest by getting
        val macosX64Main by getting
        val macosX64Test by getting
        val macosArm64Main by getting
        val macosArm64Test by getting
        val linuxX64Main by getting
        val linuxX64Test by getting
        val iosMain by getting
        val iosTest by getting
        val iosX64Main by getting
        val iosX64Test by getting
        val iosSimulatorArm64Main by getting
        val iosSimulatorArm64Test by getting
        val watchosArm64Main by getting
        val watchosArm64Test by getting
        val watchosX64Main by getting
        val watchosX64Test by getting
        val watchosSimulatorArm64Main by getting
        val watchosSimulatorArm64Test by getting
        val tvosMain by getting
        val tvosTest by getting
        val tvosSimulatorArm64Main by getting
        val tvosSimulatorArm64Test by getting

        val nativeMain by sourceSets.creating {
            dependsOn(commonMain)
            linuxX64Main.dependsOn(this)
        }
        val nativeTest by sourceSets.creating {
            dependsOn(commonTest)
            linuxX64Test.dependsOn(this)
        }

        val appleMain by sourceSets.creating {
            dependsOn(commonMain)
            macosX64Main.dependsOn(this)
            macosArm64Main.dependsOn(this)
            iosMain.dependsOn(this)
            iosX64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            watchosArm64Main.dependsOn(this)
            watchosX64Main.dependsOn(this)
            watchosSimulatorArm64Main.dependsOn(this)
            tvosMain.dependsOn(this)
            tvosSimulatorArm64Main.dependsOn(this)
        }

        val appleTest by sourceSets.creating {
            dependsOn(commonTest)
            macosX64Test.dependsOn(this)
            macosArm64Test.dependsOn(this)
            iosTest.dependsOn(this)
            iosX64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
            watchosArm64Test.dependsOn(this)
            watchosX64Test.dependsOn(this)
            watchosSimulatorArm64Test.dependsOn(this)
            tvosTest.dependsOn(this)
            tvosSimulatorArm64Test.dependsOn(this)
        }

        val androidMain by getting
        val androidTest by getting {
            kotlin.srcDir("src/commonJvmTest/kotlin")
        }
        val androidAndroidTest by getting {
            dependsOn(commonTest)
            kotlin.srcDir("src/commonJvmTest/kotlin")
            kotlin.srcDir("src/commonTest/kotlin")
        }
    }
}

android {
    compileSdk = 33
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 9
        targetSdk = 33
    }
    namespace = "$group.${rootProject.name}"
}

val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

(System.getenv("GITHUB_REPOSITORY"))?.let {
    if (System.getenv("GITHUB_REF") == "refs/heads/main") {
        signing {
            useInMemoryPgpKeys(
                "56F1A973",
                System.getenv("GPG_SECRET"),
                System.getenv("GPG_SIGNING_PASSWORD")
            )
            sign(publishing.publications)
        }
    }

    val ossUser = System.getenv("SONATYPE_NEXUS_USERNAME")
    val ossPassword = System.getenv("SONATYPE_NEXUS_PASSWORD")

    val publishedGroupId: String by project
    val libraryName: String by project
    val libraryDescription: String by project
    val siteUrl: String by project
    val gitUrl: String by project
    val licenseName: String by project
    val licenseUrl: String by project
    val developerOrg: String by project
    val developerName: String by project
    val developerEmail: String by project
    val developerId: String by project

    project.group = publishedGroupId
    project.version = libraryVersion

    publishing {
        publications.withType(MavenPublication::class) {
            groupId = publishedGroupId
            version = libraryVersion

            artifact(tasks["javadocJar"])

            pom {
                name.set(libraryName)
                description.set(libraryDescription)
                url.set(siteUrl)

                licenses {
                    license {
                        name.set(licenseName)
                        url.set(licenseUrl)
                    }
                }
                developers {
                    developer {
                        id.set(developerId)
                        name.set(developerName)
                        email.set(developerEmail)
                    }
                }
                organization {
                    name.set(developerOrg)
                }
                scm {
                    connection.set(gitUrl)
                    developerConnection.set(gitUrl)
                    url.set(siteUrl)
                }
            }
        }

        repositories {
            maven("https://oss.sonatype.org/service/local/staging/deploy/maven2/") {
                name = "sonatype"
                credentials {
                    username = ossUser
                    password = ossPassword
                }
            }
        }
    }

    nexusStaging {
        username = ossUser
        password = ossPassword
        packageGroup = publishedGroupId
    }
}
// if ("refs/heads/main".equals(System.getenv("GITHUB_REF"), ignoreCase = true)) {
// npmPublish {
//    registries {
//        register("npmjs") {
//            uri.set("https://registry.npmjs.org")
//            authToken.set(System.getenv("NPM_ACCESS_TOKEN"))
//        }
//    }
//    readme.set(rootDir.resolve("Readme.md"))
//    organization.set("ditchoom")
//    access.set(NpmAccess.PUBLIC)
//    version.set(libraryVersion)
//    packages.named("js") {
//        packageName.set("buffer-kt")
//    }
// }
// }

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
}
