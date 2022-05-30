package com.revolut.kompot.navigable.screen

import java.io.Serializable

sealed class DomainError

object NoInternetConnection : DomainError()

/**
 * Don't use kotlin's object with sealed class
 */
interface DomainStatePropertyError : Serializable

data class UnknownError(val throwable: Throwable) : DomainStatePropertyError

fun Throwable?.toUnknownError(): DomainStatePropertyError? = this?.let { error ->
    UnknownError(error)
}