package com.sa1zer.gradleplugins.jooqgenerator

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries

abstract class GenerateModelsTask : DefaultTask() {

    @Input
    val changeLogPath: Property<String> = project.objects.property(String::class.java)

    @Input
    val inputSchema: Property<String> = project.objects.property(String::class.java)

    @Input
    val outputSchema: Property<String> = project.objects.property(String::class.java)

    @Input
    val modelsPackage: Property<String> = project.objects.property(String::class.java)

    @OutputDirectory
    val outputDir: Property<Path> = project.objects.property(Path::class.java)

    @TaskAction
    fun generate() {
        if (outputDir.get().exists() && outputDir.get().listDirectoryEntries().isNotEmpty()) {
            logger.lifecycle("Models already generated")
            return
        }

        val jooqGenerator = JooqGenerator(
            changeLogPath = changeLogPath.get(),
            inputSchema = inputSchema.get(),
            outputSchema = outputSchema.get(),
            modelsPackage = modelsPackage.get(),
            outputDir = outputDir.get(),
        )

        logger.lifecycle("Generating jooq generator")
        jooqGenerator.run()
        logger.lifecycle("Jooq models generated")
    }
}