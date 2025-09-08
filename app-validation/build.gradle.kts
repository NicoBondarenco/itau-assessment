import kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit.BRANCH
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit.INSTRUCTION
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit.LINE
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType.APPLICATION

kotlin {
    jvmToolchain(libs.versions.jdk.get().toInt())
}

dependencies {
    implementation(platform(libs.spring.google.grpc))
    implementation(projects.appCommon)
    implementation(libs.bundles.spring.cloud.aws)
    implementation(libs.spring.boot.grpc.starter)
    implementation(libs.bundles.grpc.all)
    implementation(libs.aws.netty.nio.client)
    implementation(libs.bundles.micrometer.all)
    implementation(libs.spring.boot.starter.actuator)
    testImplementation(libs.bundles.grpc.test.all)
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
                    "com.itau.authorizer.validation.infrastructure.*",
                    "com.itau.authorizer.validation.application.adapter.out.grpc.*",
                    "com.itau.authorizer.validation.application.adapter.in.grpc.*",
                    "com.itau.authorizer.validation.ApplicationKt",
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
