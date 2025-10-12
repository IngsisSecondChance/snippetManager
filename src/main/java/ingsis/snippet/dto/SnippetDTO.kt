package ingsis.snippet.dto

data class SnippetDTO(
    val id: String,
    val name: String,
    val description: String?,
    val language: String,
    val version: String
)
