package com.sa1zer.gradleplugins.jooqgenerator

import liquibase.Contexts
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.DirectoryResourceAccessor
import org.gradle.api.GradleException
import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Database
import org.jooq.meta.jaxb.Generate
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Jdbc
import org.jooq.meta.jaxb.Target
import org.testcontainers.containers.PostgreSQLContainer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.DriverManager
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

class JooqGenerator(
    val changeLogPath: String,
    val inputSchema: String,
    val outputSchema: String,
    val modelsPackage: String,
    val outputDir: Path,
) {

    fun run() {
        val postgreSQLContainer = createContainer(outputSchema)
        postgreSQLContainer.start()

        try {
            applyMigrations(changeLogPath, postgreSQLContainer)
            generateModels(postgreSQLContainer, inputSchema, outputSchema, modelsPackage, outputDir)
        } catch (e: Exception) {
            throw e
        } finally {
            postgreSQLContainer.stop()
        }
    }

    private fun generateModels(
        postgreSQLContainer: PostgreSQLContainer<*>,
        inputSchema: String,
        outputSchema: String,
        modelsPackage: String,
        outputDir: Path
    ) {
        Files.createDirectories(outputDir)

        print("JOOQ OUTPUT PATH = ${outputDir.absolutePathString()}")

        val config = Configuration()
            .withJdbc(
                Jdbc()
                    .withDriver("org.postgresql.Driver")
                    .withUrl(postgreSQLContainer.jdbcUrl)
                    .withUser(postgreSQLContainer.username)
                    .withPassword(postgreSQLContainer.password)
            ).withGenerator(
                Generator()
                    .withDatabase(
                        Database()
                            .withName("org.jooq.meta.postgres.PostgresDatabase")
                            .withInputSchema(inputSchema)
                            .withOutputSchema(outputSchema)
                            .withExcludes("databasechangelog|databasechangeloglock")
                    )
                    .withGenerate(
                        Generate()
                            .withDeprecated(false)
                            .withRecords(true)
                            .withImmutablePojos(true)
                            .withFluentSetters(true)
                            .withJavaTimeTypes(true)
                    )
                    .withTarget(
                        Target()
                            .withPackageName(modelsPackage)
                            .withDirectory(outputDir.absolutePathString())
                    )
            )
        GenerationTool.generate(config)
    }

    private fun createContainer(schema: String): PostgreSQLContainer<*> {
        runCatching {
            Class.forName("org.postgresql.Driver")
        }.getOrElse {
            throw GradleException("PostgreSQL driver not found")
        }


        return PostgreSQLContainer("postgres:17-alpine")
            .apply {
                withDatabaseName("jooq_db")
                withUsername("jooq_user")
                withPassword("jooq_password")
                withEnv("TESTCONTAINERS_RYUK_DISABLED", "true")
                withEnv("default_schema", schema)
            }
    }

    private fun applyMigrations(changeLogPath: String, postgreSQLContainer: PostgreSQLContainer<*>) {
        val changeLogFile = Paths.get(changeLogPath)
        if (!changeLogFile.exists()) {
            throw GradleException("ChangeLog file does not exist: ${changeLogFile.toAbsolutePath()}")
        }

        val db = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
            JdbcConnection(
                DriverManager.getConnection(
                    postgreSQLContainer.jdbcUrl,
                    postgreSQLContainer.username,
                    postgreSQLContainer.password
                )
            )
        )

        val liquibase = Liquibase(
            changeLogFile.fileName.toString(),
            DirectoryResourceAccessor(changeLogFile.parent),
            db
        )

        liquibase.update(Contexts())
    }
}