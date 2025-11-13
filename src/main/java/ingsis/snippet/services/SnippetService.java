package ingsis.snippet.services;

import ingsis.snippet.dto.Response;
import ingsis.snippet.dto.SnippetCodeDetails;
import ingsis.snippet.dto.SnippetDTO;
import ingsis.snippet.dto.Validation;
import ingsis.snippet.entities.SnippetEntity;
import ingsis.snippet.errorDTO.Error;
import java.util.Set;
import ingsis.snippet.repositories.SnippetRepository;
import ingsis.snippet.web.BucketHandler;
import ingsis.snippet.web.PermissionsManagerHandler;
import ingsis.snippet.web.PrintScriptServiceHandler;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static ingsis.snippet.utils.Utils.getViolationsMessageError;


@Service
public class SnippetService {

  @Autowired
  private SnippetRepository snippetRepository;

  @Autowired
  private BucketHandler bucketHandler;

  @Autowired
  private PermissionsManagerHandler permissionsManagerHandler;

  @Autowired
  private PrintScriptServiceHandler printScriptServiceHandler;

  private static final Logger log = LoggerFactory.getLogger(SnippetService.class);

  private final Validator validation = jakarta.validation.Validation
      .buildDefaultValidatorFactory().getValidator();


  @Transactional
  public Response<SnippetCodeDetails> saveSnippet(SnippetDTO snippetDTO, String token) {
    log.info("saveSnippet was called");
    Set<ConstraintViolation<SnippetDTO>> violations = validation.validate(snippetDTO);
    if (!violations.isEmpty()) {
      return Response.<SnippetCodeDetails>withError(getViolationsMessageError(violations));
    }

    String title = snippetDTO.getTitle();
    String language = snippetDTO.getLanguage();
    String extension = snippetDTO.getExtension();
    String code = snippetDTO.getCode();

    SnippetEntity snippet = new SnippetEntity();
    snippet.setName(title);
    snippet.setDescription(snippetDTO.getDescription());
    snippet.setLanguage(language);
    snippet.setVersion(extension);
    snippet.setCode(code);

    try {
      snippetRepository.save(snippet);
    } catch (Exception e) {
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      return Response.<SnippetCodeDetails>withError(new Error<>(500, e.getMessage()));
    }

    String snippetId = snippet.getId();

    Response<String> permissionsResponse = permissionsManagerHandler.saveRelation(token, snippetId,
            "/snippets/save/relationship");
    if (permissionsResponse.isError()) {
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      return Response.<SnippetCodeDetails>withError(permissionsResponse.getError());
    }
    if (language.equals("printscript")) {
      Response<String> printScriptResponse = printScriptServiceHandler.validateCode(code, "1.1", token);
      if (printScriptResponse.isError()) {
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return Response.<SnippetCodeDetails>withError(printScriptResponse.getError());
      }
    }

    Response<Void> response = bucketHandler.put("snippets/" + snippetId, code, token);
    if (response.isError())
      return Response.<SnippetCodeDetails>withError(response.getError());

    String author = permissionsManagerHandler.getSnippetAuthor(snippetId, token).getData();

    return Response.<SnippetCodeDetails>withData(new SnippetCodeDetails(author, snippetId, title, snippetDTO.getDescription(), language,
            extension, code));
  }

  // Crea un SnippetDTO a partir de un archivo; lanza InvalidSnippetException si es inválido
  public SnippetDTO createSnippetFromFile(MultipartFile file, String name, String description, String language, String version) {
    // Comprueba tipo de media
    var mediaCheck = ingsis.snippet.utils.Utils.checkMediaType(file.getContentType());
    if (mediaCheck != null) {
      throw new ingsis.snippet.exceptions.InvalidSnippetException(new Validation("Unsupported file type", version));
    }

    try {
      String code = new String(file.getBytes(), StandardCharsets.UTF_8);
      SnippetDTO snippetDTO = new SnippetDTO(code, name, description, language, version);
      Set<ConstraintViolation<SnippetDTO>> violations = validation.validate(snippetDTO);
      if (!violations.isEmpty()) {
        StringBuilder sb = new StringBuilder();
        for (ConstraintViolation<SnippetDTO> v : violations) {
          if (sb.length() > 0) sb.append("; ");
          sb.append(v.getPropertyPath()).append(": ").append(v.getMessage());
        }
        throw new ingsis.snippet.exceptions.InvalidSnippetException(new Validation(sb.toString(), version));
      }
      return snippetDTO;
    } catch (IOException e) {
      throw new ingsis.snippet.exceptions.InvalidSnippetException(new Validation("Unable to read file: " + e.getMessage(), version));
    }
  }

}
