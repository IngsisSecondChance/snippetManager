package ingsis.snippet.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import ingsis.snippet.dto.Response;
import ingsis.snippet.dto.TestDTO;
import ingsis.snippet.errorDTO.Error;
import ingsis.snippet.services.SnippetServiceTest;
import ingsis.snippet.services.TestService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@MockitoSettings(strictness = Strictness.LENIENT)
// @ExtendWith(MockitoExtension.class)
@SpringBootTest
public class TestControllerTest {

  @Autowired private TestController testController;

  @MockBean private TestService testService;

  private String token;

  @BeforeEach
  void setUp() {
    token = SnippetServiceTest.securityConfig(this);
  }

  @Test
  void testCreateTest() {
    TestDTO testDTO = new TestDTO();
    testDTO.setId("snippetId");
    testDTO.setTitle("title");
    testDTO.setInputQueue(List.of("input"));
    testDTO.setOutputQueue(List.of("output"));

    // Mock éxito
    when(testService.createTest(testDTO, token)).thenReturn(Response.withData("testId"));
    assertEquals("testId", testController.createTest(testDTO, token).getBody());

    // Mock error
    when(testService.createTest(testDTO, token))
        .thenReturn(Response.withError(new Error<>(400, "error")));
    assertEquals(400, testController.createTest(testDTO, token).getStatusCode().value());
  }

  @Test
  void testGetTestsForSnippet() {
    when(testService.getTestsForSnippet("snippetId", token))
        .thenReturn(Response.withData(List.of()));
    assertEquals(
        200, testController.getTestsForSnippet("snippetId", token).getStatusCode().value());

    when(testService.getTestsForSnippet("snippetId", token))
        .thenReturn(Response.withError(new Error<>(400, "error")));
    assertEquals(
        400, testController.getTestsForSnippet("snippetId", token).getStatusCode().value());
  }

  @Test
  void testUpdateTest() {
    TestDTO testDTO = new TestDTO();
    testDTO.setId("snippetId");
    testDTO.setTitle("title");
    testDTO.setInputQueue(List.of("input"));
    testDTO.setOutputQueue(List.of("output"));

    when(testService.updateTest(testDTO, token)).thenReturn(Response.withData(null));
    assertEquals(200, testController.updateTest(testDTO, token).getStatusCode().value());

    when(testService.updateTest(testDTO, token))
        .thenReturn(Response.withError(new Error<>(400, "error")));
    assertEquals(400, testController.updateTest(testDTO, token).getStatusCode().value());
  }

  @Test
  void testDeleteTest() {
    when(testService.deleteTest("testId", token)).thenReturn(Response.withData(null));
    assertEquals(200, testController.deleteTest("testId", token).getStatusCode().value());

    when(testService.deleteTest("testId", token))
        .thenReturn(Response.withError(new Error<>(400, "error")));
    assertEquals(400, testController.deleteTest("testId", token).getStatusCode().value());
  }

  @Test
  void testRunTest() {
    TestDTO testDTO = new TestDTO();
    testDTO.setId("snippetId");
    testDTO.setTitle("title");
    testDTO.setInputQueue(List.of("input"));
    testDTO.setOutputQueue(List.of("output"));

    when(testService.runTest(testDTO, token)).thenReturn(Response.withData(null));
    assertEquals(200, testController.runTest(testDTO, token).getStatusCode().value());

    when(testService.runTest(testDTO, token))
        .thenReturn(Response.withError(new Error<>(400, "error")));
    assertEquals(400, testController.runTest(testDTO, token).getStatusCode().value());
  }
}
