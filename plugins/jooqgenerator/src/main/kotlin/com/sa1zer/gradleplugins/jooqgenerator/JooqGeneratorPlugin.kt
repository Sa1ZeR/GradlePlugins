package com.sa1zer.gradleplugins.jooqgenerator

import com.sa1zer.gradleplugins.jooqgenerator.JooqConstant.JOOQ_GENERATE_TASK_NAME
import com.sa1zer.gradleplugins.jooqgenerator.JooqConstant.JOOQ_PLUGIN_NAME
import com.sa1zer.gradleplugins.jooqgenerator.JooqConstant.JOOQ_REGENERATE_TASK_NAME
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer

class JooqGeneratorPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create(JOOQ_PLUGIN_NAME, JooqGeneratorExtension::class.java)
        val outputDir = project.layout.buildDirectory.get().asFile.toPath().resolve("generated-src/jooq/main")

        println("JOOQ GENERATOR DEBUG: output dir = ${outputDir.toAbsolutePath()}")

        project.afterEvaluate {
            val generateTask = project.tasks.register(JOOQ_GENERATE_TASK_NAME, GenerateModelsTask::class.java) {
                it.group = JOOQ_PLUGIN_NAME
                it.changeLogPath.set(extension.changeLogPath)
                it.inputSchema.set(extension.inputSchema)
                it.outputSchema.set(extension.outputSchema)
                it.modelsPackage.set(extension.modelsPackage)
                it.outputDir.set(outputDir)
            }

            project.tasks.register(JOOQ_REGENERATE_TASK_NAME, RegenerateModelsTask::class.java) {
                it.group = JOOQ_PLUGIN_NAME
                it.changeLogPath.set(extension.changeLogPath)
                it.inputSchema.set(extension.inputSchema)
                it.outputSchema.set(extension.outputSchema)
                it.modelsPackage.set(extension.modelsPackage)
                it.outputDir.set(outputDir)
            }

            project.extensions.getByType(SourceSetContainer::class.java).named("main") { it.java.srcDir(outputDir.toFile()) }

            project.tasks.matching { it.name == "compileKotlin" || it.name == "compileJava" }
                .configureEach { it.dependsOn(generateTask) }
        }
    }
}