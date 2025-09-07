import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.lang) apply false
    alias(libs.plugins.kotlinx.kover) apply false
    alias(libs.plugins.spring.kotlin.plugin) apply false
    alias(libs.plugins.spring.framework.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    java
}

group = "com.itau.authorizer"

java.sourceCompatibility = JavaVersion.valueOf(libs.versions.target.get())
java.targetCompatibility = JavaVersion.valueOf(libs.versions.target.get())

allprojects {

    val libs = rootProject.libs

    apply {
        plugin(libs.plugins.kotlin.lang.get().pluginId)
        plugin(libs.plugins.kotlinx.kover.get().pluginId)
        plugin(libs.plugins.spring.kotlin.plugin.get().pluginId)
        plugin(libs.plugins.spring.framework.boot.get().pluginId)
        plugin(libs.plugins.spring.dependency.management.get().pluginId)
    }

    group = "com.itau.authorizer"

    java.sourceCompatibility = JavaVersion.valueOf(libs.versions.target.get())
    java.targetCompatibility = JavaVersion.valueOf(libs.versions.target.get())

    dependencies {
        implementation(platform(libs.spring.cloud.dependency))
        implementation(platform(libs.spring.cloud.aws))

        implementation(libs.bundles.kotlin.stdlib.all)
        implementation(libs.bundles.kotlinx.coroutines.all)
        implementation(libs.bundles.spring.boot.base)
        implementation(libs.bundles.logback.all)
        implementation(libs.bundles.jackson.all)

        testImplementation(libs.bundles.junit.all)

        testRuntimeOnly(libs.junit.jupiter.launcher)
    }

    tasks.withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.valueOf(libs.versions.jvm.get()))
            freeCompilerArgs.addAll(
                "-Xjsr305=strict",
                "-Xcontext-receivers",
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-java-parameters",
                "-Xconcurrent-gc"
            )
        }
    }

    tasks.test {
        useJUnitPlatform()
        systemProperty("file.encoding", "UTF-8")
        systemProperty("user.timezone", "UTC")
        maxHeapSize = "1g"

        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }

        finalizedBy("koverXmlReport", "koverHtmlReport")
    }

    configurations {
        all {
            exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
        }
    }

}
