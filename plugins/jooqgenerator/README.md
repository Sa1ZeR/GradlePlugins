# Jooq models generator

[![](https://jitpack.io/v/Sa1ZeR/gradleplugins.svg)](https://jitpack.io/#Sa1ZeR/gradleplugins)

Just add dependency

```kotlin
dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url = uri("https://jitpack.io") }
		}
	}

dependencies {
    implementation("com.github.Sa1ZeR:GradlePlugins:Tag")
}
```


add to build.gradle

```kotlin
jooqGenerator {
    changeLogPath = "$projectDir/src/main/resources/db/changelog/db.changelog-master.xml"
    outputSchema = "public"
    inputSchema = "public"
    modelsPackage = "your.project.package"
}
```