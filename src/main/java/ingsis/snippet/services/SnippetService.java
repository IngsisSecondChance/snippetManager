package ingsis.snippet.services;
import java.util.Optional;
import ingsis.snippet.dto.Response;
import ingsis.snippet.dto.SnippetCodeDetails;
import ingsis.snippet.dto.SnippetDTO;
import ingsis.snippet.dto.UpdateSnippetDTO;
import ingsis.snippet.dto.Tuple;
import ingsis.snippet.dto.Validation;
import ingsis.snippet.entities.Snippet;
import ingsis.snippet.errorDTO.Error;
import com.printScript.snippetService.DTO.ShareSnippetDTO;
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

  // Use the current entity class and setters
  Snippet snippet = new Snippet();
  snippet.setTitle(title);
  snippet.setDescription(snippetDTO.getDescription());
  snippet.setLanguage(language);
  snippet.setExtension(extension);

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

  public Response<SnippetCodeDetails> updateSnippet(UpdateSnippetDTO updateSnippetDTO, String token) {
        log.info("updateSnippet was called");
        Set<ConstraintViolation<UpdateSnippetDTO>> violations = validation.validate(updateSnippetDTO);
        if (!violations.isEmpty()) {
            return Response.withError(getViolationsMessageError(violations));
        }

        String snippetId = updateSnippetDTO.getSnippetId();
        String title = updateSnippetDTO.getTitle();
        String language = updateSnippetDTO.getLanguage();
        String extension = updateSnippetDTO.getExtension();
        String code = updateSnippetDTO.getCode();

        Optional<Snippet> snippetOptional = snippetRepository.findById(snippetId);
        if (snippetOptional.isEmpty()) {
            return Response.withError(new Error<>(404, "Snippet not found"));
        }

        Response<String> permissionsResponse = permissionsManagerHandler.checkPermissions(snippetId, token,
                "/snippets/can-edit");
        if (permissionsResponse.isError())
            return Response.withError(permissionsResponse.getError());

        if (language.equals("printscript")) {
            Response<String> printScriptResponse = printScriptServiceHandler.validateCode(code, "1.1", token);
            if (printScriptResponse.isError())
                return Response.withError(printScriptResponse.getError());
        }
        Snippet snippet = snippetOptional.get();
  snippet.setTitle(title);
  snippet.setDescription(updateSnippetDTO.getDescription());
  snippet.setLanguage(language);
  snippet.setExtension(extension);
  snippet.setLintStatus(Snippet.Status.IN_PROGRESS);
  snippet.setFormatStatus(Snippet.Status.IN_PROGRESS);

        try {
            snippetRepository.save(snippet);
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Response.withError(new Error<>(500, e.getMessage()));
        }

        bucketHandler.put("snippets/" + snippetId, code, token);

  // Trigger any async events (stubbed for now)
  generateEvents(token, snippetId, snippet, language);
        snippetRepository.save(snippet);

        String author = permissionsManagerHandler.getSnippetAuthor(snippetId, token).getData();
        String codeSnippet = bucketHandler.get("snippets/" + snippetId, token).getData();

        SnippetCodeDetails snippetDetails = new SnippetCodeDetails();
        snippetDetails.setId(snippetId);
        snippetDetails.setTitle(title);
        snippetDetails.setCode(codeSnippet);
        snippetDetails.setDescription(updateSnippetDTO.getDescription());
        snippetDetails.setLanguage(language);
        snippetDetails.setExtension(extension);
  // DTO expects a String lint status; convert enum to name
  snippetDetails.setLintStatus(snippet.getLintStatus() != null ? snippet.getLintStatus().name() : null);
        snippetDetails.setAuthor(author);
        return Response.withData(snippetDetails);
    }

     public Response<SnippetCodeDetails> getSnippetDetails(String snippetId, String token) {
        log.info("getSnippetDetails was called");
        Optional<Snippet> snippetOpt = snippetRepository.findById(snippetId);
        if (snippetOpt.isEmpty()) {
            return Response.withError(new Error<>(404, "Snippet not found"));
        }

        Response<String> permissionsResponse = permissionsManagerHandler.checkPermissions(snippetId, token,
                "/snippets/has-access");
        if (permissionsResponse.isError())
            return Response.withError(permissionsResponse.getError());

        Response<String> authorResponse = permissionsManagerHandler.getSnippetAuthor(snippetId, token);
        if (authorResponse.isError())
            return Response.withError(authorResponse.getError());

        Snippet snippet = snippetOpt.get();

        Response<String> response = bucketHandler.get("snippets/" + snippetId, token);
        if (response.isError())
            return Response.withError(response.getError());

        String code = response.getData();
        String extension = snippet.getExtension();
        String language = snippet.getLanguage();

  Snippet.Status lintStatus = snippet.getLintStatus();

        SnippetCodeDetails snippetDetails = new SnippetCodeDetails();
        snippetDetails.setCode(code);
        snippetDetails.setLanguage(language);
        snippetDetails.setAuthor(authorResponse.getData());
        snippetDetails.setExtension(extension);
        snippetDetails.setDescription(snippet.getDescription());
  snippetDetails.setLintStatus(lintStatus != null ? lintStatus.name() : null);
        snippetDetails.setId(snippetId);
        snippetDetails.setTitle(snippet.getTitle());

        return Response.withData(snippetDetails);
    }
    
  
  // Business logic: download snippet content and filename as a Tuple
  public Response<Tuple> downloadSnippet(String snippetId, String token) {
    log.info("downloadSnippet was called");
    Response<String> permissionsResponse = permissionsManagerHandler.checkPermissions(snippetId, token,
        "/snippets/has-access");
    if (permissionsResponse.isError())
      return Response.withError(permissionsResponse.getError());

    var snippetOpt = snippetRepository.findById(snippetId);
    if (snippetOpt.isEmpty())
      return Response.withError(new Error<>(404, "Snippet not found"));

    var snippet = snippetOpt.get();
  String extension = snippet.getExtension();

    Response<String> response = bucketHandler.get("snippets/" + snippetId, token);
    if (response.isError())
      return Response.withError(response.getError());

    Tuple tuple = new Tuple(response.getData(), snippet.getTitle().replace(" ", "_") + "." + extension);
    return Response.withData(tuple);
  }

  // Crea un SnippetDTO a partir de un archivo; lanza InvalidSnippetException si es inválido
  public SnippetDTO createSnippetFromFile(MultipartFile file, String name, String description, String language, String version) {
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

  public record Tuple(String code, String name) {
  }

  // Small stub to trigger lint/format events asynchronously.
  // For now, just log and return — real implementation can enqueue jobs or call other services.
  private void generateEvents(String token, String snippetId, Snippet snippet, String language) {
    log.info("generateEvents called for snippet {} language={}", snippetId, language);
    // Placeholder: in a full implementation this could publish messages to a queue or call other services.
  }

  // Share a snippet with another user by delegating to the PermissionsManagerHandler
  public Response<SnippetCodeDetails> shareSnippet(ShareSnippetDTO shareSnippetDTO, String token) {
    String snippetId = shareSnippetDTO.getSnippetId();
    Optional<Snippet> snippetOpt = snippetRepository.findById(snippetId);
    if (snippetOpt.isEmpty()) {
      return Response.withError(new Error<>(404, "Snippet not found"));
    }

    Response<String> pmResponse = permissionsManagerHandler.shareSnippet(token, shareSnippetDTO, "/snippets/share");
    if (pmResponse.isError()) {
      return Response.withError(pmResponse.getError());
    }

    // Return snippet details as confirmation
    Snippet snippet = snippetOpt.get();
    String author = permissionsManagerHandler.getSnippetAuthor(snippetId, token).getData();
    Response<String> codeResp = bucketHandler.get("snippets/" + snippetId, token);
    if (codeResp.isError()) {
      return Response.withError(codeResp.getError());
    }

    SnippetCodeDetails details = new SnippetCodeDetails(author, snippetId, snippet.getTitle(), snippet.getDescription(), snippet.getLanguage(), snippet.getExtension(), codeResp.getData());
    return Response.withData(details);
  }

}
