package com.itau.authorizer.authorization.application.adapter.mock.schemaregistry

import io.mockk.spyk
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import org.apache.tomcat.util.net.openssl.OpenSSLStatus.setVersion
import org.springframework.cloud.stream.schema.registry.SchemaReference
import org.springframework.cloud.stream.schema.registry.SchemaRegistrationResponse
import org.springframework.cloud.stream.schema.registry.client.SchemaRegistryClient

abstract class MockSchemaRegistryClient : SchemaRegistryClient {

    private val schemas = ConcurrentHashMap<String, MutableMap<Int, String>>()
    private val subjectVersions = ConcurrentHashMap<String, Int>()
    private val schemaIds = ConcurrentHashMap<Int, Pair<String, String>>()
    private var nextId = AtomicInteger(0)

    companion object {
        val INSTANCE = spyk<MockSchemaRegistryClient>()
    }

    override fun register(subject: String, schema: String, version: String?): SchemaRegistrationResponse {
        val currentVersion = subjectVersions.getOrPut(subject) { 0 } + 1
        subjectVersions[subject] = currentVersion

        schemas.getOrPut(subject) { ConcurrentHashMap() }[currentVersion] = schema

        val id = nextId.incrementAndGet()
        schemaIds[id] = Pair(subject, schema)

        return SchemaRegistrationResponse().apply {
            setId(id)
            setVersion(1)
            schemaReference = SchemaReference(subject, currentVersion, "application/*+avro")
        }
    }

}
