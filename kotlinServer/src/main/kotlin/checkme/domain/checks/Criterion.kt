package checkme.domain.checks

import com.fasterxml.jackson.annotation.JsonProperty

data class Criterion(
    val description: String,
    val score: Int,
    val test: TestConfig,
    val message: String,
    @JsonProperty("special_marker")
    val specialMarker: SpecialCriteriaMarker?,
)

enum class SpecialCriteriaMarker {
    NULL,
    BEFORE_EACH,
    BEFORE_ALL,
    AFTER_EACH,
    AFTER_ALL,
}
