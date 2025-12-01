package ingsis.snippet.services;

import static ingsis.snippet.utils.Utils.getViolationsMessageError;

import ingsis.snippet.dto.*;
import ingsis.snippet.dto.ShareSnippetDTO;
import ingsis.snippet.entities.Snippet;
import ingsis.snippet.errorDTO.Error;
import ingsis.snippet.repositories.SnippetRepository;
import ingsis.snippet.web.BucketHandler;
import ingsis.snippet.web.PermissionsManagerHandler;
import ingsis.snippet.web.PrintScriptServiceHandler;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SnippetService {

  @Autowired private SnippetRepository snippetRepository;

  @Autowired private BucketHandler bucketHandler;

  @Autowired private PermissionsManagerHandler permissionsManagerHandler;

  @Autowired private PrintScriptServiceHandler printScriptServiceHandler;

  private static final Logger log = LoggerFactory.getLogger(SnippetService.class);

  private final Validator validation =
      jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();

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

    Response<String> permissionsResponse =
        permissionsManagerHandler.saveRelation(token, snippetId, "/snippets/save/relationship");
    if (permissionsResponse.isError()) {
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      return Response.<SnippetCodeDetails>withError(permissionsResponse.getError());
    }
    if (language.equals("printscript")) {
      Response<String> printScriptResponse =
          printScriptServiceHandler.validateCode(code, "1.1", token);
      if (printScriptResponse.isError()) {
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return Response.<SnippetCodeDetails>withError(printScriptResponse.getError());
      }
    }

    Response<Void> response = bucketHandler.put("snippets/" + snippetId, code, token);
    if (response.isError()) return Response.<SnippetCodeDetails>withError(response.getError());

    String author = permissionsManagerHandler.getSnippetAuthor(snippetId, token).getData();

    return Response.<SnippetCodeDetails>withData(
        new SnippetCodeDetails(
            author, snippetId, title, snippetDTO.getDescription(), language, extension, code));
  }

  public Response<SnippetCodeDetails> updateSnippet(
      UpdateSnippetDTO updateSnippetDTO, String token) {
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

    Response<String> permissionsResponse =
        permissionsManagerHandler.checkPermissions(snippetId, token, "/snippets/can-edit");
    if (permissionsResponse.isError()) return Response.withError(permissionsResponse.getError());

    if (language.equals("printscript")) {
      Response<String> printScriptResponse =
          printScriptServiceHandler.validateCode(code, "1.1", token);
      if (printScriptResponse.isError()) return Response.withError(printScriptResponse.getError());
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
    snippetDetails.setLintStatus(snippet.getLintStatus());
    snippetDetails.setAuthor(author);
    return Response.withData(snippetDetails);
  }

  public Response<SnippetCodeDetails> getSnippetDetails(String snippetId, String token) {
    log.info("getSnippetDetails was called");
    Optional<Snippet> snippetOpt = snippetRepository.findById(snippetId);
    if (snippetOpt.isEmpty()) {
      return Response.withError(new Error<>(404, "Snippet not found"));
    }

    Response<String> permissionsResponse =
        permissionsManagerHandler.checkPermissions(snippetId, token, "/snippets/has-access");
    if (permissionsResponse.isError()) return Response.withError(permissionsResponse.getError());

    Response<String> authorResponse = permissionsManagerHandler.getSnippetAuthor(snippetId, token);
    if (authorResponse.isError()) return Response.withError(authorResponse.getError());

    Snippet snippet = snippetOpt.get();

    Response<String> response = bucketHandler.get("snippets/" + snippetId, token);
    if (response.isError()) return Response.withError(response.getError());

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
    snippetDetails.setLintStatus(lintStatus);
    snippetDetails.setId(snippetId);
    snippetDetails.setTitle(snippet.getTitle());

    return Response.withData(snippetDetails);
  }

  public Response<String> deleteSnippet(String snippetId, String token) {
    log.info("deleteSnippet was called");
    Response<String> canEditResponse =
        permissionsManagerHandler.checkPermissions(snippetId, token, "/snippets/can-edit");

    if (canEditResponse.isError()) {
      Response<String> hasAccessResponse =
          permissionsManagerHandler.checkPermissions(snippetId, token, "/snippets/has-access");
      if (hasAccessResponse.isError()) {
        return Response.withError(hasAccessResponse.getError());
      }

      Response<String> deleteResponse =
          permissionsManagerHandler.deleteRelation(
              snippetId, "/snippets/delete/relationship", token);
      if (deleteResponse.isError()) return Response.withError(deleteResponse.getError());

      return Response.withData(null);
    }

    Response<String> deleteResponse =
        permissionsManagerHandler.deleteRelation(
            snippetId, "/snippets/delete/all-relationships", token);
    if (deleteResponse.isError()) return Response.withError(deleteResponse.getError());

    snippetRepository.deleteById(snippetId);

    Response<Void> response = bucketHandler.delete("snippets/" + snippetId, token);
    if (response.isError()) return Response.withError(response.getError());

    return Response.withData(null);
  }

  // Business logic: download snippet content and filename as a Tuple
  public Response<Tuple> downloadSnippet(String snippetId, String token) {
    log.info("downloadSnippet was called");
    Response<String> permissionsResponse =
        permissionsManagerHandler.checkPermissions(snippetId, token, "/snippets/has-access");
    if (permissionsResponse.isError()) return Response.withError(permissionsResponse.getError());

    var snippetOpt = snippetRepository.findById(snippetId);
    if (snippetOpt.isEmpty()) return Response.withError(new Error<>(404, "Snippet not found"));

    var snippet = snippetOpt.get();
    String extension = snippet.getExtension();

    Response<String> response = bucketHandler.get("snippets/" + snippetId, token);
    if (response.isError()) return Response.withError(response.getError());

    Tuple tuple =
        new Tuple(response.getData(), snippet.getTitle().replace(" ", "_") + "." + extension);
    return Response.withData(tuple);
  }

  // Crea un SnippetDTO a partir de un archivo; lanza InvalidSnippetException si es inválido
  public SnippetDTO createSnippetFromFile(
      MultipartFile file, String name, String description, String language, String version) {
    var mediaCheck = ingsis.snippet.utils.Utils.checkMediaType(file.getContentType());
    if (mediaCheck != null) {
      throw new ingsis.snippet.exceptions.InvalidSnippetException(
          new Validation("Unsupported file type", version));
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
        throw new ingsis.snippet.exceptions.InvalidSnippetException(
            new Validation(sb.toString(), version));
      }
      return snippetDTO;
    } catch (IOException e) {
      throw new ingsis.snippet.exceptions.InvalidSnippetException(
          new Validation("Unable to read file: " + e.getMessage(), version));
    }
  }

  public record Tuple(String code, String name) {}

  // Small stub to trigger lint/format events asynchronously.
  private void generateEvents(String token, String snippetId, Snippet snippet, String language) {
    log.info("generateEvents called for snippet {} language={}", snippetId, language);
  }

  public Response<SnippetCodeDetails> shareSnippet(ShareSnippetDTO shareSnippetDTO, String token) {
    log.info("shareSnippet was called");

    Set<ConstraintViolation<ShareSnippetDTO>> violations = validation.validate(shareSnippetDTO);
    if (!violations.isEmpty()) {
      return Response.withError(getViolationsMessageError(violations));
    }

    Response<String> permissionsResponse =
        permissionsManagerHandler.checkPermissions(
            shareSnippetDTO.getSnippetId(), token, "/snippets/has-access");
    if (permissionsResponse.isError()) return Response.withError(permissionsResponse.getError());

    Response<String> permissionsResponse2 =
        permissionsManagerHandler.shareSnippet(
            token, shareSnippetDTO, "/snippets/save/share/relationship");
    if (permissionsResponse2.isError()) {
      return Response.withError(permissionsResponse2.getError());
    }

    Snippet snippet = snippetRepository.findById(shareSnippetDTO.getSnippetId()).orElse(null);
    if (snippet == null) {
      return Response.withError(new Error<>(404, "Snippet not found"));
    }

    Response<String> authorResponse =
        permissionsManagerHandler.getSnippetAuthor(shareSnippetDTO.getSnippetId(), token);

    String code = bucketHandler.get("snippets/" + shareSnippetDTO.getSnippetId(), token).getData();
    SnippetCodeDetails snippetDetails = new SnippetCodeDetails();

    snippetDetails.setId(shareSnippetDTO.getSnippetId());
    snippetDetails.setTitle(snippet.getTitle());
    snippetDetails.setDescription(snippet.getDescription());
    snippetDetails.setLanguage(snippet.getLanguage());
    snippetDetails.setExtension(snippet.getExtension());
    snippetDetails.setAuthor(authorResponse.getData());
    snippetDetails.setCode(code);

    return Response.withData(snippetDetails);
  }

  public Response<PaginatedUsers> getSnippetUsers(
      String token, String prefix, Integer page, Integer PageSize) {
    log.info("getSnippetUsers was called");
    Response<PaginatedUsers> response =
        permissionsManagerHandler.getSnippetUsers(token, prefix, page, PageSize);

    if (response.isError()) {
      return Response.withError(response.getError());
    }
    return response;
  }

  public Response<String> getFormattedFile(String snippetId, String token) {
    log.info("getFormattedFile was called");
    Response<String> permissionsResponse =
        permissionsManagerHandler.checkPermissions(snippetId, token, "/snippets/has-access");
    if (permissionsResponse.isError()) return permissionsResponse;

    Optional<Snippet> snippetOptional = snippetRepository.findById(snippetId);
    if (snippetOptional.isEmpty()) {
      return Response.withError(new Error<>(404, "Snippet not found"));
    }

    Snippet snippet = snippetOptional.get();
    if (snippet.getFormatStatus() == Snippet.Status.IN_PROGRESS) {
      return Response.withError(new Error<>(400, "Format is in progress"));
    }

    Response<String> response;
    try {
      response = bucketHandler.get("formatted/" + snippetId, token);
      if (response.isError()) {
        return response;
      }
    } catch (Exception e) {
      return Response.withError(new Error<>(500, "Internal Server Error"));
    }
    return Response.withData(response.getData());
  }

  public Response<PaginationAndDetails> getAccessibleSnippets(
      String token, String relation, Integer page, Integer pageSize, String name) {
    log.info("getAccessibleSnippets was called");

    // sanitize pagination and filter inputs
    int pageNum = page == null || page < 0 ? 0 : page;
    int size = pageSize == null || pageSize <= 0 ? 10 : pageSize;
    String titlePrefix = name == null ? "" : name;

    Response<List<ingsis.snippet.dto.GrantResponse>> relationshipsResponse =
        permissionsManagerHandler.getSnippetRelationships(token, relation);
    if (relationshipsResponse.isError()) {
      return Response.withError(relationshipsResponse.getError());
    }

    List<ingsis.snippet.dto.GrantResponse> relationships = relationshipsResponse.getData();
    if (relationships == null || relationships.isEmpty()) {
      PaginationAndDetails empty = new PaginationAndDetails(pageNum, size, 0, List.of());
      return Response.withData(empty);
    }

    List<String> ids =
        relationships.stream().map(ingsis.snippet.dto.GrantResponse::getSnippetId).toList();
    Pageable pageable = PageRequest.of(pageNum, size);

    List<Snippet> snippets =
        snippetRepository.findByIdInAndTitleStartingWith(ids, titlePrefix, pageable);

    List<SnippetCodeDetails> snippetDetails = new java.util.ArrayList<>();
    for (Snippet snippet : snippets) {
      SnippetCodeDetails detail = new SnippetCodeDetails();
      Response<String> codeResp = bucketHandler.get("snippets/" + snippet.getId(), token);
      if (codeResp.isError()) {
        return Response.withError(codeResp.getError());
      }

      detail.setId(snippet.getId());
      detail.setTitle(snippet.getTitle());
      detail.setCode(codeResp.getData());
      detail.setDescription(snippet.getDescription());
      detail.setLanguage(snippet.getLanguage());
      detail.setExtension(snippet.getExtension());
      detail.setLintStatus(snippet.getLintStatus());

      String author =
          relationships.stream()
              .filter(rel -> rel.getSnippetId().equals(snippet.getId()))
              .map(ingsis.snippet.dto.GrantResponse::getAuthor)
              .findFirst()
              .orElse(null);
      detail.setAuthor(author);

      snippetDetails.add(detail);
    }

    PaginationAndDetails paginationAndDetails =
        new PaginationAndDetails(pageNum, size, relationships.size(), snippetDetails);
    return Response.withData(paginationAndDetails);
  }

  public Response<String> getFormattedFile(String snippetId, String token) {
    log.info("getFormattedFile was called");
    Response<String> permissionsResponse =
        permissionsManagerHandler.checkPermissions(snippetId, token, "/snippets/has-access");
    if (permissionsResponse.isError()) return permissionsResponse;

    Optional<Snippet> snippetOptional = snippetRepository.findById(snippetId);
    if (snippetOptional.isEmpty()) {
      return Response.withError(new Error<>(404, "Snippet not found"));
    }

    Snippet snippet = snippetOptional.get();
    if (snippet.getFormatStatus() == Snippet.Status.IN_PROGRESS) {
      return Response.withError(new Error<>(400, "Format is in progress"));
    }

    Response<String> response;
    try {
      response = bucketHandler.get("formatted/" + snippetId, token);
      if (response.isError()) {
        return response;
      }
    } catch (Exception e) {
      return Response.withError(new Error<>(500, "Internal Server Error"));
    }
    return Response.withData(response.getData());
  }
}
