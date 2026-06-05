package checkme.web.solution.forms

import checkme.domain.forms.CheckResult

data class CheckWithAllData(
    val id: String,
    val date: String,
    val status: String,
    val result: Map<String, CheckResult>?,
    val user: UserNameSurnameForAllResults,
    val task: TaskNameForAllResults,
    val totalScore: Int,
)

data class CheckWithTaskData(
    val id: String,
    val date: String,
    val status: String,
    val task: TaskNameForAllResults,
)

data class TaskNameForAllResults(
    val name: String,
)

data class TaskIdAndName(
    val id: String,
    val name: String,
)

data class UserNameSurnameForAllResults(
    val name: String,
    val surname: String,
)

data class UserDataForUsersList(
    val id: String,
    val login: String,
    val name: String,
    val surname: String,
)

data class TaskWithBestResult(
    val taskId: String,
    val taskName: String,
    val highestScore: Int,
    val bestSolution: Int,
)

data class BundleWithTaskAndBestResult(
    val bundleName: String,
    val taskWithBestResult: MutableList<TaskWithBestResult>,
)
