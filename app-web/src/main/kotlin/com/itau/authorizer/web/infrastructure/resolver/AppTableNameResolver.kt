package com.itau.authorizer.web.infrastructure.resolver

import com.itau.authorizer.common.util.extension.toSnakeCase
import io.awspring.cloud.dynamodb.DynamoDbTableNameResolver
import org.springframework.stereotype.Component

@Component
class AppTableNameResolver : DynamoDbTableNameResolver {

    override fun <T : Any?> resolve(
        clazz: Class<T?>
    ): String = clazz.simpleName.replace("DynamoDB", "").toSnakeCase()

}
