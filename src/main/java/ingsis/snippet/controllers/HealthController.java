package ingsis.snippet.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping // (sin prefijo)
public class HealthController {
  @GetMapping("/ping")
  public ResponseEntity<String> ping() {
    System.out.println(">>> PING HIT <<<");
    return ResponseEntity.ok("pong 🟢");
  }
}
