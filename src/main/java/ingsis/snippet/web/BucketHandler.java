package ingsis.snippet.web;

import ingsis.snippet.dto.Response;
import ingsis.snippet.errorDTO.Error;
import ingsis.snippet.services.RestTemplateService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class BucketHandler {
  private final RestTemplate bucketWebClient;

  public BucketHandler(RestTemplateService bucketRestTemplate) {
    this.bucketWebClient = bucketRestTemplate.getRestTemplate();
  }

  public Response<Void> put(String path, String text, String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", token);
    HttpEntity<String> request = new HttpEntity<>(text, headers);
    try {
      // Use exchange to include headers (body is the request entity)
      ResponseEntity<Void> resp =
          bucketWebClient.exchange("/v1/asset/" + path, HttpMethod.PUT, request, Void.class);
      return Response.withData(null);
    } catch (HttpClientErrorException e) {
      return Response.withError(new Error<>(e.getStatusCode().value(), "Internal Server Error"));
    }
  }

  public Response<String> get(String path, String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", token);
    HttpEntity<Void> request = new HttpEntity<>(headers);
    try {
      return Response.withData(
          bucketWebClient.getForObject("/v1/asset/" + path, String.class, request));
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode().value() == 404) {
        return Response.withError(new Error<>(404, "Not Found"));
      }
      return Response.withError(new Error<>(500, "Internal Server Error"));
    }
  }

  public Response<Void> delete(String path, String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", token);
    HttpEntity<Void> request = new HttpEntity<>(headers);
    try {
      bucketWebClient.delete("/v1/asset/" + path, request);
      return Response.withData(null);
    } catch (HttpClientErrorException e) {
      return Response.withError(new Error<>(500, "Internal Server Error"));
    }
  }
}
