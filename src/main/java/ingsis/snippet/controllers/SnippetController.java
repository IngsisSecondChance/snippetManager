package ingsis.snippet.controllers;

import ingsis.snippet.dto.SnippetDTO;
import ingsis.snippet.exceptions.InvalidSnippetException;
import ingsis.snippet.services.SnippetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/snippets")
public class SnippetController {

    private final SnippetService snippetService;

    public SnippetController(SnippetService snippetService) {
        this.snippetService = snippetService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadSnippet(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("language") String language,
            @RequestParam("version") String version) {
        try {
            SnippetDTO created = snippetService.createSnippetFromFile(file, name, description, language, version);
            return ResponseEntity.ok(created);
        } catch (InvalidSnippetException ex) {
            return ResponseEntity.badRequest().body(ex.getDetails());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error interno: " + e.getMessage());
        }
    }
}
