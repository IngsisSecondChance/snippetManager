package ingsis.snippet.dto

data class ValidationResultDTO(
    val valid: Boolean,
    val rule: String,
    val line: Int,
    val column: Int
)
