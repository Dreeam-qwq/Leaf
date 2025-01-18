import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    `maven-publish`
    id("io.papermc.paperweight.patcher") version "2.0.0-beta.14"
}

val paperMavenPublicUrl = "https://repo.papermc.io/repository/maven-public/"
val leafMavenPublicUrl = "https://maven.nostal.ink/repository/maven-snapshots/"

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    repositories {
        mavenCentral()
        maven(paperMavenPublicUrl)
        maven(leafMavenPublicUrl)
        maven("https://ci.pluginwiki.us/plugin/repository/everything/") // Leaf Config - ConfigurationMaster-API
    }

    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release = 21
        options.isFork = true
        options.forkOptions.memoryMaximumSize = "6g"
    }
    tasks.withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }
    tasks.withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }
    tasks.withType<Test> {
        testLogging {
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
            events(TestLogEvent.STANDARD_OUT)
        }
    }

    extensions.configure<PublishingExtension> {
        repositories {
            maven(leafMavenPublicUrl) {
                name = "leaf"

                credentials.username = "dreeam"
                credentials.password = "dreeam123"
            }
        }
    }
}

paperweight {
    upstreams.register("gale") {
        repo = github("Dreeam-qwq", "Gale")
        ref = providers.gradleProperty("galeCommit")

        patchFile {
            path = "gale-server/build.gradle.kts"
            outputFile = file("leaf-server/build.gradle.kts")
            patchFile = file("leaf-server/build.gradle.kts.patch")
        }
        patchFile {
            path = "gale-api/build.gradle.kts"
            outputFile = file("leaf-api/build.gradle.kts")
            patchFile = file("leaf-api/build.gradle.kts.patch")
        }
        patchRepo("paperApi") {
            upstreamPath = "paper-api"
            patchesDir = file("leaf-api/paper-patches")
            outputDir = file("paper-api")
        }
        patchDir("galeApi") {
            upstreamPath = "gale-api"
            excludes = listOf("build.gradle.kts", "build.gradle.kts.patch", "paper-patches")
            patchesDir = file("leaf-api/gale-patches")
            outputDir = file("gale-api")
        }
    }
}
