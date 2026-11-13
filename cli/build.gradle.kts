import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Optional second front end. It reuses :core unchanged, which is the point of
// the refactor: the same verified logic answers both the app and the terminal,
// so the two can never disagree.
plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

application {
    mainClass.set("io.github.marcuspage5715.primechecker.cli.MainKt")
    applicationName = "prime-checker"
}

dependencies {
    implementation(project(":core"))
    testImplementation(libs.junit)
}
