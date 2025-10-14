package ingsis.snippet.services

import ingsis.snippet.clients.PrintScriptClient
import ingsis.snippet.dto.SnippetDTO
import ingsis.snippet.dto.ValidationResultDTO
import ingsis.snippet.entities.SnippetEntity
import ingsis.snippet.exceptions.InvalidSnippetException
import ingsis.snippet.repositories.SnippetRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.charset.StandardCharsets

@Service
class SnippetService(
    private val snippetRepository: SnippetRepository,
    private val printScriptClient: PrintScriptClient
) {
    fun createSnippetFromFile(
        file: MultipartFile,
        name: String,
        description: String?,
        language: String,
        version: String
    ): SnippetDTO {
        try {
            val code = String(file.bytes, StandardCharsets.UTF_8)
            val validation: ValidationResultDTO = printScriptClient.validateSnippet(code, version)

            if (!validation.valid) {
                throw InvalidSnippetException(validation)
            }

            val entity = SnippetEntity(
                name = name,
                description = description,
                language = language,
                version = version,
                code = code
            )
            snippetRepository.save(entity)

            return SnippetDTO(
                id = entity.id,
                name = entity.name ?: "",
                description = entity.description,
                language = entity.language ?: "",
                version = entity.version ?: ""
            )
        } catch (e: IOException) {
            throw RuntimeException("Error leyendo archivo subido", e)
        }
    }
}
