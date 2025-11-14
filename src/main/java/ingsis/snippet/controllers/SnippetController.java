package ingsis.snippet.controllers;

import static ingsis.snippet.utils.Utils.checkMediaType;

import ingsis.snippet.dto.*;
import ingsis.snippet.dto.SnippetCodeDetails;
import ingsis.snippet.exceptions.InvalidSnippetException;
import ingsis.snippet.services.SnippetService;

import ingsis.snippet.utils.TokenUtils;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/snippets")
public class SnippetController {

  private final SnippetService snippetService;

  public SnippetController(SnippetService snippetService) {
    this.snippetService = snippetService;
  }

  //Guardar snippet (JSON) ===
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
  @GetMapping("/get/all")
  public ResponseEntity<Object> getAccessibleSnippets(@RequestHeader("Authorization") String token,
                                                      @RequestParam(required = false) String relation, @RequestParam Integer page,
                                                      @RequestParam Integer page_size, @RequestParam String prefix) {
    Response<PaginationAndDetails> response = snippetService.getAccessibleSnippets(token, relation, page, page_size,
            prefix);
    if (response.isError()) {
      return new ResponseEntity<>(response.getError().body(), HttpStatusCode.valueOf(response.getError().code()));
    }
    return new ResponseEntity<>(response.getData(), HttpStatus.OK);
  }

  @GetMapping("/get/users")
  public ResponseEntity<Object> getSnippetUsers(@RequestHeader("Authorization") String token,
                                                @RequestParam String prefix, @RequestParam Integer page, @RequestParam Integer page_size) {
    Response<PaginatedUsers> response = snippetService.getSnippetUsers(token, prefix, page, page_size);
    String userId = TokenUtils.decodeToken(token.substring(7)).get("userId");
    if (response.isError()) {
      return new ResponseEntity<>(response.getError().body(), HttpStatus.valueOf(response.getError().code()));
    }
    PaginatedUsers paginatedUsers = response.getData();
    List<User> filteredUsers = paginatedUsers.getUsers().stream().filter(user -> !user.getId().equals(userId))
            .toList();
    paginatedUsers.setUsers(filteredUsers);

    return new ResponseEntity<>(paginatedUsers, HttpStatus.OK);
  }


  //Guardar snippet desde archivo ===
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

  //Subir snippet básico ===
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

  @PostMapping("/update/file")
  public ResponseEntity<Object> updateSnippetFile(
          @RequestParam MultipartFile file,
          @RequestParam String snippetId,
          @RequestParam String title,
          @RequestParam String description,
          @RequestParam String language,
          @RequestParam String version,
          @RequestHeader("Authorization") String token) {

    ResponseEntity<Object> mediaTypeCheck = checkMediaType(file.getContentType());
    if (mediaTypeCheck != null) {
      return mediaTypeCheck;
    }

    try {
      String code = new String(file.getBytes());
      UpdateSnippetDTO updateSnippetDTO =
              new UpdateSnippetDTO(code, snippetId, title, description, language, version);

      Response<SnippetCodeDetails> response =
              snippetService.updateSnippet(updateSnippetDTO, token);

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
  @GetMapping("/details")
  public ResponseEntity<Object> getSnippetDetails(
          @RequestParam String snippetId,
          @RequestHeader("Authorization") String token) {

    Response<SnippetCodeDetails> response =
            snippetService.getSnippetDetails(snippetId, token);

    if (response.isError()) {
      return new ResponseEntity<>(
              response.getError().body(),
              HttpStatusCode.valueOf(response.getError().code()));
    }

    return ResponseEntity.ok(response.getData());
  }

  @DeleteMapping("/delete")
  public ResponseEntity<Object> deleteSnippet(
          @RequestParam String snippetId,
          @RequestHeader("Authorization") String token) {

    Response<String> response = snippetService.deleteSnippet(snippetId, token);
    if (response.isError()) {
      return new ResponseEntity<>(
              response.getError().body(),
              HttpStatusCode.valueOf(response.getError().code()));
    }
    return ResponseEntity.ok(response.getData());
  }

  /*
  @GetMapping("/get/users")
  public ResponseEntity<Object> getSnippetUsers(
          @RequestHeader("Authorization") String token,
          @RequestParam String prefix,
          @RequestParam Integer page,
          @RequestParam Integer page_size) {

    Response<PaginatedUsers> response =
            snippetService.getSnippetUsers(token, prefix, page, page_size);

    if (response.isError()) {
      return new ResponseEntity<>(
              response.getError().body(),
              HttpStatus.valueOf(response.getError().code()));
    }

    PaginatedUsers paginatedUsers = response.getData();
    String userId = TokenUtils.decodeToken(token.substring(7)).get("userId");

    paginatedUsers.setUsers(
            paginatedUsers.getUsers().stream()
                    .filter(u -> !u.getId().equals(userId))
                    .toList()
    );

    return new ResponseEntity<>(paginatedUsers, HttpStatus.OK);
  }
|*/
  @GetMapping("/ping")
  public ResponseEntity<String> ping() {
    return ResponseEntity.ok("pong 🟢");
  }
}
