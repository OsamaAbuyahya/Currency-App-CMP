package org.example.project.domain.mapper

import org.example.project.domain.model.Currency
import org.example.project.domain.model.CurrencyEntity

fun Currency.toEntity(): CurrencyEntity {
    val entity = CurrencyEntity()
    entity.code = this.code
    entity.value = this.value
    return entity
}

fun CurrencyEntity.toCurrency(): Currency {
    return Currency(code = this.code, value = this.value)
}