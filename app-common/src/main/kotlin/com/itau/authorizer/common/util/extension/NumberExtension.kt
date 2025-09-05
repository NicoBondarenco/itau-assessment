package com.itau.authorizer.common.util.extension

import java.math.BigDecimal
import java.math.RoundingMode.HALF_EVEN

fun BigDecimal.toAmountFormat(): String = this.setScale(2, HALF_EVEN).toString()
