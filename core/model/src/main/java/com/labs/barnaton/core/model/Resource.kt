package com.labs.barnaton.core.model

sealed class ErrorType {
    data object NoInternet : ErrorType()
    data object Unauthorized : ErrorType()
    data object NotFound : ErrorType()
    data class ClientError(val code: Int, val message: String? = null) : ErrorType()
    data class ServerError(val code: Int, val message: String? = null) : ErrorType()
    data class Unknown(val message: String? = null) : ErrorType()
}

class ApiErrorException(
    val errorType: ErrorType,
    override val cause: Throwable? = null,
) : Exception(errorType.toString(), cause)

sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val errorType: ErrorType, val cause: Throwable? = null) : Resource<Nothing>()
}
