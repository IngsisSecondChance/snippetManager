package ingsis.snippet.controllers;

import java.util.List;

import ingsis.snippet.dto.Response;
import ingsis.snippet.dto.TestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ingsis.snippet.services.TestService;

@RestController
@RequestMapping("/test")
public class TestController {

    private final TestService testService;

    @Autowired
    public TestController(TestService testService) {
        this.testService = testService;
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createTest(@RequestBody TestDTO testDTO,
            @RequestHeader("Authorization") String token) {
        Response<String> response = testService.createTest(testDTO, token);
        System.out.println(token);
        if (response.isError()) {
            return new ResponseEntity<>(response.getError().body(), HttpStatusCode.valueOf(response.getError().code()));
        }
        return ResponseEntity.ok(response.getData());
    }

}