plugins {
    java
    `maven-publish`
    id("io.papermc.paperweight.patcher") version "1.7.2-SNAPSHOT"
}

val paperMavenPublicUrl = "https://repo.papermc.io/repository/maven-public/"

repositories {
    mavenCentral()
    maven(paperMavenPublicUrl) {
        content { onlyForConfigurations(configurations.paperclip.name) }
    }
    maven("https://maven.nostal.ink/repository/maven-snapshots/")
}

dependencies {
    remapper("net.fabricmc:tiny-remapper:0.10.3:fat")
    decompiler("org.vineflower:vineflower:1.10.1")
    paperclip("io.papermc:paperclip:3.0.3") // Dreeam TODO - nostal's maven repo down
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
}

subprojects {
    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }
    tasks.withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }
    tasks.withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }

    repositories {
        mavenCentral()
        maven(paperMavenPublicUrl)
        maven("https://ci.pluginwiki.us/plugin/repository/everything/") // Leaf Config - ConfigurationMaster-API
    }
}

paperweight {
    serverProject.set(project(":leaf-server"))

    remapRepo.set("https://maven.fabricmc.net/")
    decompileRepo.set("https://maven.quiltmc.org/")

    useStandardUpstream("Gale") {
        url.set(github("Dreeam-qwq", "Gale"))
        ref.set(providers.gradleProperty("galeCommit"))

        withStandardPatcher {
            apiSourceDirPath.set("gale-api")
            serverSourceDirPath.set("gale-server")

            apiPatchDir.set(layout.projectDirectory.dir("patches/api"))
            apiOutputDir.set(layout.projectDirectory.dir("Leaf-API"))

            serverPatchDir.set(layout.projectDirectory.dir("patches/server"))
            serverOutputDir.set(layout.projectDirectory.dir("Leaf-Server"))
        }

        patchTasks.register("generatedApi") {
            isBareDirectory = true
            upstreamDirPath = "paper-api-generator/generated"
            patchDir = layout.projectDirectory.dir("patches/generated-api")
            outputDir = layout.projectDirectory.dir("paper-api-generator/generated")
        }
    }
}

tasks.generateDevelopmentBundle {
    apiCoordinates = "cn.dreeam.leaf:leaf-api"
    libraryRepositories.set(
        listOf(
            "https://repo.maven.apache.org/maven2/",
            paperMavenPublicUrl,
        )
    )
}

publishing {
    if (project.providers.gradleProperty("publishDevBundle").isPresent) {
        publications.create<MavenPublication>("devBundle") {
            artifact(tasks.generateDevelopmentBundle) {
                artifactId = "dev-bundle"
            }
        }
    }
}

allprojects {
    publishing {
        repositories {
            maven {
                name = "leaf"
                url = uri("https://maven.pkg.github.com/Winds-Studio/Leaf")

                credentials.username = System.getenv("REPO_PASSWORD")
                credentials.password = System.getenv("REPO_USER")
            }

            publications {
                register<MavenPublication>("gpr") {
                    from(components["java"])
                }
            }
        }
    }
}