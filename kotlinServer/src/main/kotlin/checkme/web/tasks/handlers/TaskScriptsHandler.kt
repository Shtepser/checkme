package checkme.web.tasks.handlers

import checkme.domain.models.User
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendStatusNotFound
import checkme.web.lenses.GeneralWebLenses.idOrNull
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.RequestContextLens
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class TaskScriptsHandler(
    private val userLens: RequestContextLens<User?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        val taskId = request.idOrNull()
        return when {
            user == null -> objectMapper.sendBadRequestError(ViewTaskError.USER_CANT_VIEW_THIS_TASK.errorText)
            taskId == null -> objectMapper.sendBadRequestError(ViewTaskError.NO_TASK_ID_ERROR.errorText)
            else -> tryFetchTaskScripts(
                nameDirectory = taskId,
                objectMapper = objectMapper,
            )
        }
    }
}

private fun tryFetchTaskScripts(
    nameDirectory: UUID,
    objectMapper: ObjectMapper,
): Response {
    val taskDir = File(
        "..$TASKS_DIR/$nameDirectory"
    )
    if (!taskDir.exists() || !taskDir.isDirectory) {
        return objectMapper.sendStatusNotFound("Task directory not found")
    }
    val files = taskDir.listFiles()?.filter { it.isFile } ?: emptyList()
    if (files.isEmpty()) {
        return objectMapper.sendStatusNotFound("Files not found")
    }
    val outputStream = ByteArrayOutputStream()
    ZipOutputStream(outputStream).use { zip ->
        files.forEach { file ->
            zip.putNextEntry(ZipEntry(file.name))
            file.inputStream().use { input ->
                input.copyTo(zip)
            }
            zip.closeEntry()
        }
    }
    val bytes = outputStream.toByteArray()
    return Response(Status.OK)
        .header("Content-Disposition", "filename=\"task_$nameDirectory.zip\"")
        .body(ByteArrayInputStream(bytes), bytes.size.toLong())
}
