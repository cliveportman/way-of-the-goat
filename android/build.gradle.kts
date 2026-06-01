// Top-level build file. Plugins are declared here (apply false) so each
// version is resolved once and applied per-module.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.detekt) apply false
}
