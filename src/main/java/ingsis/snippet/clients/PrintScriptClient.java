package ingsis.snippet.clients;

import ingsis.snippet.dto.ValidationResultDTO;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PrintScriptClient {

  private final RestTemplate restTemplate;
  private final String validateUrl;

  public PrintScriptClient(
      RestTemplate restTemplate,
      @Value("${printscript.validate.url:http://localhost:8081/validate}") String validateUrl) {
    this.restTemplate = restTemplate;
    this.validateUrl = validateUrl;
  }

  public ValidationResultDTO validateSnippet(String code, String version) {
    try {
      Map<String, String> body = new HashMap<>();
      body.put("code", code);
      body.put("version", version);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

      ResponseEntity<ValidationResultDTO> resp =
          restTemplate.postForEntity(validateUrl, request, ValidationResultDTO.class);

      return resp.getBody() != null
          ? resp.getBody()
          : new ValidationResultDTO(false, "No response", 0, 0);
    } catch (Exception e) {
      return new ValidationResultDTO(
          false, "Error comunicando con PrintScriptManager: " + e.getMessage(), 0, 0);
    }
  }
}
