package checkme.domain.checks

import com.fasterxml.jackson.annotation.JsonProperty


data class Criterion(
    val description: String,
    val score: Int,
    val test: TestConfig,
    val message: String,
    @JsonProperty("special_marker")
    val specialMarker: SpecialCriteriaMarker?
)

enum class SpecialCriteriaMarker(val code: String) {
    NULL_MARKER("null"),
    BEFORE_EACH("beforeEach"),
    BEFORE_ALL("beforeAll"),
    AFTER_EACH("afterEach"),
    AFTER_ALL("afterAll");

    companion object {
        fun specialCriteriaMarkerFromCode(code: String): SpecialCriteriaMarker? {
            return SpecialCriteriaMarker.entries.find { it.code == code }
        }
    }
}
