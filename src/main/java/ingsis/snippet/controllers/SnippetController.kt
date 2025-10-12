package ingsis.snippet.controllers

import ingsis.snippet.dto.SnippetDTO
import ingsis.snippet.exceptions.InvalidSnippetException
import ingsis.snippet.services.SnippetService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/snippets")
class SnippetController(
    private val snippetService: SnippetService
) {

    @PostMapping("/upload")
    fun uploadSnippet(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("name") name: String,
        @RequestParam("description", required = false) description: String?,
        @RequestParam("language") language: String,
        @RequestParam("version") version: String
    ): ResponseEntity<Any> {
        return try {
            val created: SnippetDTO = snippetService.createSnippetFromFile(file, name, description, language, version)
            ResponseEntity.ok(created)
        } catch (ex: InvalidSnippetException) {
            ResponseEntity.badRequest().body(ex.details)
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body("Error interno: ${e.message}")
        }
    }
}
