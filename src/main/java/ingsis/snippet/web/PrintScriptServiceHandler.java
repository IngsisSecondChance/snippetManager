package ingsis.snippet.web;

import java.util.List;

import ingsis.snippet.dto.TestData;
import ingsis.snippet.dto.ValidationRequest;
import ingsis.snippet.dto.LintRequest;
import ingsis.snippet.dto.TestDataRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ingsis.snippet.dto.Response;
import ingsis.snippet.errorDTO.Error;
import ingsis.snippet.errorDTO.ErrorMessage;
import ingsis.snippet.services.RestTemplateService;

@Component
public class PrintScriptServiceHandler {

    private final RestTemplate printScriptWebClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public PrintScriptServiceHandler(RestTemplateService printScriptRestTemplate, ObjectMapper objectMapper) {
        this.printScriptWebClient = printScriptRestTemplate.getRestTemplate();
        this.objectMapper = objectMapper;
    }

    public Response<String> validateCode(String code, String version, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<ValidationRequest> requestPrintScript = new HttpEntity<>(new ValidationRequest(code, version), headers);
        try {
            printScriptWebClient.postForEntity("/runner/validate", requestPrintScript, Void.class);
            return Response.withData(null);
        } catch (HttpClientErrorException e) {
            return getValidationErrors(e);
        }
    }

    private Response<String> getValidationErrors(HttpClientErrorException e) {
        try {
            List<ErrorMessage> errorMessages = objectMapper.readValue(e.getResponseBodyAsString(),
                    new TypeReference<>() {
                    });
            return Response.withError(new Error<>(e.getStatusCode().value(), errorMessages));
        } catch (JsonProcessingException ex) {
            return Response.withError(new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
        }
    }

    public Response<Void> executeTest(String snippetId, String version, List<String> inputs, List<String> expected,
                      String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
    HttpEntity<TestDataRequest> requestPrintScript = new HttpEntity<>(new TestDataRequest(snippetId, version, inputs, expected),
        headers);
        try {
            printScriptWebClient.postForEntity("/runner/test", requestPrintScript, Void.class);
            return Response.withData(null);
        } catch (HttpClientErrorException e) {
            return Response.withError(new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
        }
    }


}