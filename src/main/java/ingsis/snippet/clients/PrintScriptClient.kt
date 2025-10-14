package ingsis.snippet.clients

import ingsis.snippet.dto.ValidationResultDTO
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class PrintScriptClient(
    private val restTemplate: RestTemplate,
    @Value("\${printscript.validate.url:http://localhost:8081/validate}")
    private val validateUrl: String
) {
    fun validateSnippet(code: String, version: String): ValidationResultDTO {
        return try {
            val body = mapOf("code" to code, "version" to version)
            val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
            val request = HttpEntity(body, headers)

            val resp = restTemplate.postForEntity(validateUrl, request, ValidationResultDTO::class.java)
            resp.body ?: ValidationResultDTO(false, "No response", 0, 0)
        } catch (e: Exception) {
            ValidationResultDTO(false, "Error comunicando con PrintScriptManager: ${e.message}", 0, 0)
        }
    }
}
