import com.google.protobuf.gradle.id
import kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit.BRANCH
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit.INSTRUCTION
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit.LINE
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType.APPLICATION

plugins {
    alias(libs.plugins.google.grpc.protobuf)
}

kotlin {
    jvmToolchain(libs.versions.jdk.get().toInt())
}

dependencies{
    implementation(platform(libs.spring.google.grpc))
    implementation(libs.bundles.spring.cloud.aws)
    implementation(libs.spring.boot.grpc.starter)
    implementation(libs.bundles.grpc.all)
    implementation(libs.bundles.micrometer.all)
}

protobuf {
    protoc {
        artifact = libs.google.grpc.protoc.compiler.get().toString()
    }
    plugins {
        id("grpc") {
            artifact = libs.google.grpc.protoc.java.get().toString()
        }
        id("grpckt") {
            artifact = "${libs.google.grpc.protoc.kotlin.get()}:jdk8@jar"
        }

    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
            it.builtins {
                id("kotlin")
            }
        }
    }
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
                    "com.itau.authorizer.common.application.model.*",
                    "com.itau.authorizer.authorization.application.adapter.in.grpc.*",
                    "com.itau.authorizer.authorization.application.adapter.out.grpc.*",
                    "com.itau.authorizer.common.domain.*",
                    "com.itau.authorizer.common.infrastructure.configuration.*",
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

tasks.named("bootJar") {
    enabled = false
}

tasks.named("bootRun") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
    archiveClassifier = ""
}
