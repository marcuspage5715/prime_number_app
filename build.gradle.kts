// Top-level build file. Plugins are declared here without applying them, so each
// module opts in to exactly what it needs (and versions stay in one place:
// gradle/libs.versions.toml).
plugins {
    alias(libs.plugins.android.application) apply false
    // kotlin.android is intentionally absent: AGP 9 has built-in Kotlin support.
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
