package ingsis.snippet.services;

import ingsis.snippet.clients.PrintScriptClient;
import ingsis.snippet.dto.Response;
import ingsis.snippet.dto.SnippetCodeDetails;
import ingsis.snippet.dto.SnippetDTO;
import ingsis.snippet.dto.ValidationResultDTO;
import ingsis.snippet.entities.SnippetEntity;
import ingsis.snippet.errorDTO.Error;
import ingsis.snippet.exceptions.InvalidSnippetException;
import ingsis.snippet.repositories.SnippetRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.logging.Logger;

import ingsis.snippet.web.PermissionsManagerHandler;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jdk.jshell.Snippet;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;


@Service
public class SnippetService {

  @Autowired
  private PermissionsManagerHandler permissionsManagerHandler;

  @Autowired
  private PrintScriptServiceHandler printScriptServiceHandler;

  private final ProducerInterface lintProducer;

  private final ProducerInterface formatProducer;

  private final Validator validation = Validation.buildDefaultValidatorFactory().getValidator();

  private final Logger log = LoggerFactory.getLogger(SnippetService.class);

  @Autowired
  public SnippetService(ProducerInterface lintProducer, ProducerInterface formatProducer) {
    this.lintProducer = lintProducer;
    this.formatProducer = formatProducer;
  }

  @Transactional
  public Response<SnippetCodeDetails> saveSnippet(SnippetDTO snippetDTO, String token) {
    log.info("saveSnippet was called");
    Set<ConstraintViolation<SnippetDTO>> violations = validation.validate(snippetDTO);
    if (!violations.isEmpty()) {
      return Response.withError(getViolationsMessageError(violations));
    }

    String title = snippetDTO.getTitle();
    String language = snippetDTO.getLanguage();
    String extension = snippetDTO.getExtension();
    String code = snippetDTO.getCode();

    Snippet snippet = new Snippet();
    snippet.setTitle(title);
    snippet.setDescription(snippetDTO.getDescription());
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

    String snippetId = snippet.getId();

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

  @Transactional
  public Response<SnippetCodeDetails> saveSnippet(SnippetDTO snippetDTO, String token) {
    log.info("saveSnippet was called");
    Set<ConstraintViolation<SnippetDTO>> violations = validation.validate(snippetDTO);
    if (!violations.isEmpty()) {
      return Response.withError(getViolationsMessageError(violations));
    }

    String title = snippetDTO.getTitle();
    String language = snippetDTO.getLanguage();
    String extension = snippetDTO.getExtension();
    String code = snippetDTO.getCode();

    Snippet snippet = new Snippet();
    snippet.setTitle(title);
    snippet.setDescription(snippetDTO.getDescription());
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

    String snippetId = snippet.getId();

    Response<String> permissionsResponse = permissionsManagerHandler.saveRelation(token, snippetId,
            "/snippets/save/relationship");
    if (permissionsResponse.isError()) {
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      return Response.withError(permissionsResponse.getError());
    }
    if (language.equals("printscript")) {
      Response<String> printScriptResponse = printScriptServiceHandler.validateCode(code, "1.1", token);
      if (printScriptResponse.isError()) {
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return Response.withError(printScriptResponse.getError());
      }
    }

    Response<Void> response = bucketHandler.put("snippets/" + snippetId, code, token);
    if (response.isError())
      return Response.withError(response.getError());

    generateEvents(token, snippetId, snippet, language);

    String author = permissionsManagerHandler.getSnippetAuthor(snippetId, token).getData();

    return Response.withData(new SnippetCodeDetails(author, snippetId, title, snippetDTO.getDescription(), language,
            extension, code, snippet.getLintStatus()));
  }
}}
