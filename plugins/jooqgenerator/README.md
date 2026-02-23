# Jooq models generator

add to build.gradle
```kotlin
jooqGenerator {
    changeLogPath = "$projectDir/src/main/resources/db/changelog/db.changelog-master.xml"
    outputSchema = "public"
    inputSchema = "public"
    modelsPackage = "your.project.package"
}
```