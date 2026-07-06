plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.5.0"
    id("org.jetbrains.grammarkit") version "2022.3.2.2"
}

group = "com.github.ycyz.starrocks.datagrip"
version = "2.0.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

sourceSets {
    named("main") {
        java.srcDir(layout.projectDirectory.dir("generated/src/main/java"))
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

    register("validateGrammarSources") {
        group = "verification"
        description = "Validates StarRocks JFlex and Grammar-Kit grammar source assets."

        val flexFile = layout.projectDirectory.file("grammar/starrocks.flex")
        val bnfFile = layout.projectDirectory.file("grammar/starrocks.bnf")

        inputs.file(flexFile)
        inputs.file(bnfFile)

        doLast {
            check(flexFile.asFile.isFile) { "Missing parser lexer grammar: ${flexFile.asFile}" }
            check(bnfFile.asFile.isFile) { "Missing parser grammar: ${bnfFile.asFile}" }

            val flex = flexFile.asFile.readText()
            check("%class _StarRocksParserLexer" in flex) {
                "starrocks.flex must declare the generated parser lexer class."
            }
            check("StarRocksLexer" !in flex) {
                "Parser lexer grammar must not depend on the highlighting lexer."
            }
            listOf("SQL_IDENT", "SQL_STRING_TOKEN", "SQL_INTEGER_TOKEN", "SQL_SEMICOLON").forEach { token ->
                check(token in flex) { "starrocks.flex must map base SQL token $token." }
            }

            val bnf = bnfFile.asFile.readText()
            listOf(
                "script",
                "statement",
                "query_expression",
                "value_expression",
                "type_element",
                "cast_type",
                "table_column_list",
                "analytic_clause"
            ).forEach { rule ->
                check(Regex("""(?m)^$rule\s*::=""").containsMatchIn(bnf)) {
                    "starrocks.bnf must define required entry rule $rule."
                }
            }
            check("pin=" in bnf) { "starrocks.bnf must model pin points explicitly." }
            check("recoverWhile=" in bnf) { "starrocks.bnf must model recovery explicitly." }
            check("STATEMENT_SEGMENT" !in bnf && "statement_tail" !in bnf) {
                "starrocks.bnf must not define broad statement fallback segments."
            }
        }
    }

    named<org.jetbrains.grammarkit.tasks.GenerateLexerTask>("generateLexer") {
        group = "generation"
        description = "Generates the StarRocks parser lexer from grammar/starrocks.flex."

        dependsOn("validateGrammarSources")

        val flexFile = layout.projectDirectory.file("grammar/starrocks.flex")
        val generatedLexerDir = layout.projectDirectory.dir(
            "generated/src/main/java/com/github/ycyz/starrocks/datagrip/lang"
        )

        sourceFile.set(flexFile)
        targetOutputDir.set(generatedLexerDir)
        purgeOldFiles.set(true)
    }

    named<org.jetbrains.grammarkit.tasks.GenerateParserTask>("generateParser") {
        group = "generation"
        description = "Generates the StarRocks parser from grammar/starrocks.bnf."

        dependsOn("generateLexer")

        val bnfFile = layout.projectDirectory.file("grammar/starrocks.bnf")
        val generatedRoot = layout.projectDirectory.dir("generated/src/main/java")

        sourceFile.set(bnfFile)
        targetRootOutputDir.set(generatedRoot)
        pathToParser.set("/com/github/ycyz/starrocks/datagrip/lang/StarRocksGeneratedParser.java")
        pathToPsiRoot.set("/com/github/ycyz/starrocks/datagrip/lang/psi")
        purgeOldFiles.set(true)
    }

    named("compileKotlin") {
        dependsOn("generateParser")
    }

    named("compileJava") {
        dependsOn("generateLexer")
    }

    register("validateStarRocksFixtureManifest") {
        group = "verification"
        description = "Validates StarRocks SQL scenario fixture manifest and documentation."

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
            check(scenarios.isNotEmpty()) { "No StarRocks SQL scenarios declared." }

            val expectedFiles = scenarios.map { it.first }
            val missingFiles = expectedFiles.filterNot { testDataDir.file(it).asFile.isFile }
            check(missingFiles.isEmpty()) {
                "Missing StarRocks SQL fixtures: ${missingFiles.joinToString()}"
            }

            val readme = testDataDir.file("README.md").asFile.readText()
            val undocumentedFiles = expectedFiles.filterNot { readme.contains("`$it`") }
            check(undocumentedFiles.isEmpty()) {
                "Missing StarRocks fixture documentation: ${undocumentedFiles.joinToString()}"
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
        dependsOn("validateGrammarSources", "validateStarRocksFixtureManifest", "validateStarRocksScenarios")
    }
}
