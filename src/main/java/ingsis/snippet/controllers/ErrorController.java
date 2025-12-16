package ingsis.snippet.controllers;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/error")
public class ErrorController {

  private final Logger log = LoggerFactory.getLogger(ErrorController.class);

  @GetMapping
  public ResponseEntity<Map<String, String>> triggerError() {
    Map<String, String> body = Map.of("error", "Intentional test error");
    log.error(body.toString());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }
}
