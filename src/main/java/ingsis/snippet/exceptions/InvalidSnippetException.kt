package ingsis.snippet.exceptions

import ingsis.snippet.dto.ValidationResultDTO

class InvalidSnippetException(
    val details: ValidationResultDTO
) : RuntimeException("Snippet inválido")
