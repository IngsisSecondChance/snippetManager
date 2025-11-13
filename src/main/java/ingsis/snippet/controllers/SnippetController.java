package ingsis.snippet.controllers;

import static ingsis.snippet.utils.Utils.checkMediaType;

import ingsis.snippet.dto.SnippetCodeDetails;
import ingsis.snippet.dto.SnippetDTO;
import ingsis.snippet.dto.UpdateSnippetDTO;
import ingsis.snippet.dto.Tuple;
import com.printScript.snippetService.DTO.ShareSnippetDTO;
//import ingsis.snippet.dto.SnippetCodeDetails;
import ingsis.snippet.dto.Response;
import ingsis.snippet.exceptions.InvalidSnippetException;
import ingsis.snippet.services.SnippetService;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/snippets")
public class SnippetController {

  private final SnippetService snippetService;

  public SnippetController(SnippetService snippetService) {
    this.snippetService = snippetService;
  }

  // === NUEVO: Guardar snippet (JSON) ===
  @PostMapping("/save")
  public ResponseEntity<Object> saveSnippet(
          @RequestBody SnippetDTO snippetDTO,
          @RequestHeader("Authorization") String token) {

    Response<SnippetCodeDetails> response = snippetService.saveSnippet(snippetDTO, token);
    if (response.isError()) {
      return new ResponseEntity<>(
              response.getError().body(),
              HttpStatusCode.valueOf(response.getError().code())
      );
    }
    return ResponseEntity.ok(response.getData());
  }


  // === NUEVO: Guardar snippet desde archivo ===
  @PostMapping("/save/file")
  public ResponseEntity<Object> saveSnippetFile(
          @RequestParam MultipartFile file,
          @RequestParam String title,
          @RequestParam String description,
          @RequestParam String language,
          @RequestParam String version,
          @RequestHeader("Authorization") String token) {

    ResponseEntity<Object> mediaTypeCheck = ingsis.snippet.utils.Utils.checkMediaType(file.getContentType());
    if (mediaTypeCheck != null) {
      return mediaTypeCheck;
    }

    try {
      String code = new String(file.getBytes());
      SnippetDTO snippetDTO = new SnippetDTO(code, title, description, language, version);
      Response<SnippetCodeDetails> response = snippetService.saveSnippet(snippetDTO, token);
      if (response.isError()) {
        return new ResponseEntity<>(
                response.getError().body(),
                HttpStatusCode.valueOf(response.getError().code())
        );
      }
      return ResponseEntity.ok(response.getData());
    } catch (Exception e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  // === EXISTENTE: Subir snippet básico ===
  @PostMapping("/upload")
  public ResponseEntity<?> uploadSnippet(
          @RequestParam("file") MultipartFile file,
          @RequestParam("name") String name,
          @RequestParam(value = "description", required = false) String description,
          @RequestParam("language") String language,
          @RequestParam("version") String version) {
    try {
      SnippetDTO created =
              snippetService.createSnippetFromFile(file, name, description, language, version);
      return ResponseEntity.ok(created);
    } catch (InvalidSnippetException ex) {
      return ResponseEntity.badRequest().body(ex.getDetails());
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body("Error interno: " + e.getMessage());
    }
  }

  @PostMapping("/update")
  public ResponseEntity<Object> updateSnippet(@RequestBody UpdateSnippetDTO updateSnippetDTO,
      @RequestHeader("Authorization") String token) {
      Response<SnippetCodeDetails> response = snippetService.updateSnippet(updateSnippetDTO, token);
        if (response.isError()) {
          return new ResponseEntity<>(response.getError().body(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok(response.getData());
  }

  @GetMapping("/download")
  public ResponseEntity<Object> downloadSnippet(@RequestParam String snippetId,
        @RequestHeader("Authorization") String token) {
        Response<SnippetService.Tuple> response = snippetService.downloadSnippet(snippetId, token);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().body(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=" + response.getData().name())
                .body(response.getData().code());
    }

  @PostMapping("/share")
    public ResponseEntity<Object> shareSnippet(@RequestBody ShareSnippetDTO shareSnippetDTO,
            @RequestHeader("Authorization") String token) {
        Response<SnippetCodeDetails> response = snippetService.shareSnippet(shareSnippetDTO, token);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().body(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok(response.getData());
    }

  @GetMapping("/ping")
  public ResponseEntity<String> ping() {
    return ResponseEntity.ok("pong 🟢");
  }
}
