// build.gradle.kts (project-level)
buildscript {
    val kotlinVersion by extra("1.8.10")
    val composeVersion by extra("1.4.3")
    val hiltVersion by extra("2.45")

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.4.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("com.google.dagger:hilt-android-gradle-plugin:$hiltVersion")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}