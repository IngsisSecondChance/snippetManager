package ingsis.snippet.services;

import ingsis.snippet.clients.PrintScriptClient;
import ingsis.snippet.dto.SnippetDTO;
import ingsis.snippet.dto.ValidationResultDTO;
import ingsis.snippet.entities.SnippetEntity;
import ingsis.snippet.exceptions.InvalidSnippetException;
import ingsis.snippet.repositories.SnippetRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SnippetService {

  private final SnippetRepository snippetRepository;
  private final PrintScriptClient printScriptClient;

  public SnippetService(SnippetRepository snippetRepository, PrintScriptClient printScriptClient) {
    this.snippetRepository = snippetRepository;
    this.printScriptClient = printScriptClient;
  }

  public SnippetDTO createSnippetFromFile(
      MultipartFile file, String name, String description, String language, String version) {

    try {
      String code = new String(file.getBytes(), StandardCharsets.UTF_8);
      ValidationResultDTO validation = printScriptClient.validateSnippet(code, version);

      if (!validation.isValid()) {
        throw new InvalidSnippetException(validation);
      }

      SnippetEntity entity = new SnippetEntity();
      entity.setName(name);
      entity.setDescription(description);
      entity.setLanguage(language);
      entity.setVersion(version);
      entity.setCode(code);

      snippetRepository.save(entity);

      return new SnippetDTO(
          entity.getId(),
          entity.getName() != null ? entity.getName() : "",
          entity.getDescription(),
          entity.getLanguage() != null ? entity.getLanguage() : "",
          entity.getVersion() != null ? entity.getVersion() : "");

    } catch (IOException e) {
      throw new RuntimeException("Error leyendo archivo subido", e);
    }
  }
}
