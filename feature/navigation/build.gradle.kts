// :feature:navigation — Android lib. Authenticated navigation shell (bottom nav) hosting the app's
// top-level destinations. Hosts Home (:feature:auth) directly; the Profile route (002) is a route id
// only — the :feature:profile module is intentionally NOT a dependency here so 003 stays independently
// buildable/mergeable without 002 (FR-006 graceful degradation; the seam is resolved at the merge gate).
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.mirabilis.feature.navigation"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core-ui"))
    implementation(project(":feature:auth")) // hosts Home (001) + reuses AuthStartState gating type
    implementation(project(":feature:profile")) // hosts the Profile screen (002) at the shared route

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.navigation.compose)

    testImplementation(libs.junit)

    // Instrumented Compose-nav behavior (plan.md: nav behavior is instrumented)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
