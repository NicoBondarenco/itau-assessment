import kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit.BRANCH
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit.INSTRUCTION
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit.LINE
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType.APPLICATION
import org.apache.avro.compiler.specific.SpecificCompiler.FieldVisibility

plugins {
    alias(libs.plugins.gradle.avro.plugin)
}

kotlin {
    jvmToolchain(libs.versions.jdk.get().toInt())
}

dependencies {
    implementation(platform(libs.spring.google.grpc))
    implementation(projects.appCommon)
    implementation(libs.bundles.spring.cloud.kafka)
    implementation(libs.bundles.spring.cloud.dynamodb)
    implementation(libs.spring.boot.grpc.starter)
    implementation(libs.bundles.grpc.all)
    implementation(libs.bundles.kafka.all)
    implementation(libs.bundles.micrometer.all)
}

generateAvro {
    encoding = "UTF-8"
    fieldVisibility = FieldVisibility.PRIVATE
    noSetters = true
    addNullSafeAnnotations.set(true)
    stringType = true
    addExtraOptionalGetters = true
    useBigDecimal = true
}

kover {
    currentProject {
        sources {
            excludedSourceSets.addAll("test")
        }
    }
    reports {
        filters {
            excludes {
                classes(
                    "com.itau.authorizer.authorization.configuration.*",
                )
            }
        }
        total {
            verify {
                onCheck = false
                rule("Branch Coverage of Tests must be more than 95%") {
                    disabled = false
                    groupBy = APPLICATION
                    bound {
                        aggregationForGroup = COVERED_PERCENTAGE
                        coverageUnits = BRANCH
                        minValue = 95
                    }
                }
                rule("Line Coverage of Tests must be more than 95%") {
                    disabled = false
                    groupBy = APPLICATION
                    bound {
                        aggregationForGroup = COVERED_PERCENTAGE
                        coverageUnits = LINE
                        minValue = 95
                    }
                }
                rule("Instruction Coverage of Tests must be more than 95%") {
                    disabled = false
                    groupBy = APPLICATION
                    bound {
                        aggregationForGroup = COVERED_PERCENTAGE
                        coverageUnits = INSTRUCTION
                        minValue = 95
                    }
                }
            }
            xml {
                onCheck = false
            }
            html {
                onCheck = false
            }
        }
    }
}

sonar {
    properties {
        property("sonar.sources", file("$projectDir/src/main/kotlin/"))
        property("sonar.tests", file("$projectDir/src/test/kotlin/"))
        property("sonar.projectName", "Authorizer Validation")
        property("sonar.projectKey", "authorizer-validation")
        property("sonar.login", "")
        property("sonar.host.url", "")
        property("sonar.coverage.jacoco.xmlReportPaths", "${layout.buildDirectory.get()}/reports/kover/report.xml")
        property("sonar.verbose", true)
        property("sonar.qualitygate.wait", true)
        property(
            "sonar.exclusions", listOf(
                "**/com/itau/authorizer/authorization/configuration**",
            ).joinToString(separator = ",")
        )
    }
}
