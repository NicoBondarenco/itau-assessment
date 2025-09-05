package com.itau.authorizer.common.util.extension

fun String.toSnakeCase(): String = this.replace("(?<=.)[A-Z]".toRegex(), "_$0").lowercase()
