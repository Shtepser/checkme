package checkme.web.commonExtensions

import checkme.web.solution.handlers.FetchingCheckError
import com.fasterxml.jackson.databind.ObjectMapper
import org.http4k.core.*

fun ObjectMapper.sendBadRequestError(message: Any? = null): Response {
    val errorMessage = message ?: FetchingCheckError.UNKNOWN_DATABASE_ERROR
    return Response(Status.BAD_REQUEST)
        .body(this.writeValueAsString(mapOf("error" to errorMessage)))
}

fun ObjectMapper.sendOKResponse(data: Any?): Response =
    Response(Status.OK)
        .body(this.writeValueAsString(data))

fun ObjectMapper.sendStatusUnauthorized(message: Any) =
    Response(Status.UNAUTHORIZED)
        .body(this.writeValueAsString(mapOf("error" to message)))

fun ObjectMapper.sendStatusCreated(data: Any) = Response(Status.CREATED).body(this.writeValueAsString(data))

fun ObjectMapper.sendStatusNotFound(message: Any) = Response(Status.NOT_FOUND).body(this.writeValueAsString(mapOf("error" to message)))
