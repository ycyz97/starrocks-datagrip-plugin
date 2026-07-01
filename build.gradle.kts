plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = "com.github.ycyz.starrocks.datagrip"
version = "2.0.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")

    intellijPlatform {
        create("DB", "251.28774.27")

        bundledPlugin("com.intellij.modules.json")
        bundledPlugin("com.intellij.platform.images")
        bundledPlugin("intellij.charts")
        bundledPlugin("intellij.grid.plugin")
        bundledPlugin("com.intellij.database")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "251"
        }
    }
    pluginVerification {
        ides {
            ide("DB-251.28774.27")
        }
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    register("validateRewriteScenarios") {
        group = "verification"
        description = "Validates StarRocks rewrite SQL scenario fixtures."

        val testDataDir = layout.projectDirectory.dir("src/testData/sql")
        val manifestFile = testDataDir.file("scenarios.properties")

        inputs.file(manifestFile)
        inputs.file(testDataDir.file("README.md"))

        doLast {
            val scenarios = manifestFile.asFile.readLines()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") }
                .map { line ->
                    val parts = line.split("=", limit = 2)
                    check(parts.size == 2) { "Invalid scenario manifest line: $line" }
                    val fileName = parts[0].trim()
                    val metadata = parts[1].split("|", limit = 2)
                    check(metadata.size == 2) { "Invalid scenario metadata for $fileName" }
                    val milestone = metadata[0].trim()
                    val features = metadata[1].split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    Triple(fileName, milestone, features)
                }
            check(scenarios.isNotEmpty()) { "No StarRocks rewrite scenarios declared." }

            val expectedFiles = scenarios.map { it.first }
            val missingFiles = expectedFiles.filterNot { testDataDir.file(it).asFile.isFile }
            check(missingFiles.isEmpty()) {
                "Missing StarRocks rewrite SQL fixtures: ${missingFiles.joinToString()}"
            }

            val readme = testDataDir.file("README.md").asFile.readText()
            val undocumentedFiles = expectedFiles.filterNot { readme.contains("`$it`") }
            check(undocumentedFiles.isEmpty()) {
                "Missing StarRocks rewrite fixture documentation: ${undocumentedFiles.joinToString()}"
            }

            val undocumentedMilestones = scenarios.map { it.second }.distinct().filterNot { readme.contains("`$it`") }
            check(undocumentedMilestones.isEmpty()) {
                "Missing StarRocks rewrite milestone documentation: ${undocumentedMilestones.joinToString()}"
            }

            val undocumentedFeatures = scenarios.flatMap { it.third }.distinct().filterNot { readme.contains("`$it`") }
            check(undocumentedFeatures.isEmpty()) {
                "Missing StarRocks rewrite feature documentation: ${undocumentedFeatures.joinToString()}"
            }
        }
    }

    register<JavaExec>("validateStarRocksScenarios") {
        group = "verification"
        description = "Runs native StarRocks parser and local context scenario checks."

        dependsOn("testClasses")
        classpath = sourceSets["test"].runtimeClasspath
        mainClass.set("com.github.ycyz.starrocks.datagrip.StarRocksScenarioValidator")
        args(layout.projectDirectory.asFile.absolutePath)
    }

    named("check") {
        dependsOn("validateRewriteScenarios", "validateStarRocksScenarios")
    }
}
