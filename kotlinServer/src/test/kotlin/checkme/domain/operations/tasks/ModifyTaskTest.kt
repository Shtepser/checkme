package checkme.domain.operations.tasks

import checkme.db.notExistingId
import checkme.db.validTasks
import checkme.domain.models.Task
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.UUID

class ModifyTaskTest : FunSpec({
    isolationMode = IsolationMode.InstancePerTest

    val validTasks = validTasks

    val selectTaskByIdMock: (UUID) -> Task? = { id -> validTasks.find { it.id == id } }
    val selectTaskByIdNullMock: (UUID) -> Task? = { null }
    // возможно не правильно изменил
    val deleteTaskMock: (UUID) -> Int? = { id -> validTasks.indexOf(validTasks.find { it.id == id }) }
    val deleteNullTaskMock: (UUID) -> Int? = { null }
    val updateTaskActualityMock: (Task) -> Task? =
        { task -> validTasks.find { it.id == task.id }?.copy(isActual = task.isActual) }

    val deleteTask = RemoveTask(selectTaskByIdMock, deleteTaskMock)
    val deleteTaskIdNotExist = RemoveTask(selectTaskByIdNullMock, deleteNullTaskMock)
    val deleteTaskNullDelete = RemoveTask(selectTaskByIdMock, deleteNullTaskMock)
    val updateTaskActuality = ModifyTaskActuality(updateTaskActualityMock)

    test("Task can be deleted if task exists") {
        deleteTask(validTasks.first()).shouldBeSuccess()
    }

    test("Task cant be deleted if task doesn't exists") {
        deleteTaskIdNotExist(
            validTasks.first()
                .copy(id = notExistingId)
        ).shouldBeFailure(TaskRemovingError.TASK_NOT_EXISTS)
    }

    test("Task cant be deleted if operations remove task returns null") {
        deleteTaskNullDelete(validTasks.first()).shouldBeFailure(TaskRemovingError.UNKNOWN_DELETE_ERROR)
    }

    test("Task actuality can be changed") {
        updateTaskActuality(validTasks.first().copy(isActual = false)).shouldBeSuccess() shouldBe validTasks.first()
            .copy(isActual = false)
        updateTaskActuality(validTasks.first().copy(isActual = true)).shouldBeSuccess() shouldBe validTasks.first()
            .copy(isActual = true)
    }

    test("Task actuality cant be changed if task doesnt exists") {
        updateTaskActuality(validTasks.first().copy(id = notExistingId, isActual = false))
            .shouldBeFailure(TaskModifyActualityError.UNKNOWN_DATABASE_ERROR)
    }
})
