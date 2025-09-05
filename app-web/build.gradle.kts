kotlin {
    jvmToolchain(libs.versions.jdk.get().toInt())
}

dependencies {
    implementation(projects.appCommon)
    implementation(libs.bundles.spring.cloud.aws)
}
