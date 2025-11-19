package ingsis.snippet.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ingsis.snippet.dto.GrantResponse;
import ingsis.snippet.dto.PaginatedUsers;
import ingsis.snippet.dto.Response;
import ingsis.snippet.dto.ShareSnippetDTO;
import ingsis.snippet.errorDTO.Error;
import ingsis.snippet.services.RestTemplateService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class PermissionsManagerHandler {

  private final RestTemplate permissionsWebClient;
  private final ObjectMapper objectMapper;

  @Autowired
  public PermissionsManagerHandler(
      RestTemplateService permissionsRestTemplate, ObjectMapper objectMapper) {
    this.permissionsWebClient = permissionsRestTemplate.getRestTemplate();
    this.objectMapper = objectMapper;
  }

  public Response<String> saveRelation(String token, String snippetId, String path) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", token);
    HttpEntity<String> requestPermissions = new HttpEntity<>(snippetId, headers);
    try {
      postRequest(permissionsWebClient, path, requestPermissions, Void.class);
      return Response.withData(null);
    } catch (HttpClientErrorException e) {
      return Response.withError(
          new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
    }
  }

  public Response<String> checkPermissions(String snippetId, String token, String path) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", token);
    HttpEntity<Void> requestPermissions = new HttpEntity<>(headers);
    Map<String, String> params = Map.of("snippetId", snippetId);
    try {
      getRequest(permissionsWebClient, path, requestPermissions, Void.class, params);
      return Response.withData(null);
    } catch (HttpClientErrorException e) {
      return Response.withError(
          new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
    }
  }

  public Response<String> deleteRelation(String snippetId, String path, String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", token);
    HttpEntity<String> requestPermissions = new HttpEntity<>(snippetId, headers);
    try {
      deleteRequest(permissionsWebClient, path, requestPermissions, Void.class);
      return Response.withData(null);
    } catch (HttpClientErrorException e) {
      return Response.withError(
          new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
    }
  }

  public Response<String> shareSnippet(String token, ShareSnippetDTO shareSnippetDTO, String path) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", token);
    HttpEntity<Object> requestPermissions = new HttpEntity<>(shareSnippetDTO, headers);
    try {
      postRequest(permissionsWebClient, path, requestPermissions, Void.class);
      return Response.withData(null);
    } catch (HttpClientErrorException e) {
      return Response.withError(
          new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
    }
  }

  public Response<List<GrantResponse>> getSnippetRelationships(String token, String filterType) {
    HttpHeaders header = new HttpHeaders();
    header.set("Authorization", token);
    HttpEntity<Void> requestPermissions = new HttpEntity<>(header);
    try {
      String response =
          getRequest(
              permissionsWebClient,
              "snippets/get/relationships",
              requestPermissions,
              String.class,
              Map.of("filterType", filterType));
      List<GrantResponse> snippetIds = objectMapper.readValue(response, new TypeReference<>() {});
      return Response.withData(snippetIds);
    } catch (HttpClientErrorException e) {
      return Response.withError(
          new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
    } catch (JsonProcessingException e) {
      return Response.withError(new Error<>(400, "Error parsing response"));
    }
  }

  public Response<PaginatedUsers> getSnippetUsers(
      String token, String prefix, Integer page, Integer pageSize) {
    HttpHeaders header = new HttpHeaders();
    header.set("Authorization", token);
    HttpEntity<Void> requestPermissions = new HttpEntity<>(header);
    try {
      String response =
          getRequest(
              permissionsWebClient,
              "snippets/paginated",
              requestPermissions,
              String.class,
              Map.of("page", page.toString(), "pageSize", pageSize.toString(), "prefix", prefix));
      PaginatedUsers paginatedUsers = objectMapper.readValue(response, PaginatedUsers.class);
      return Response.withData(paginatedUsers);
    } catch (HttpClientErrorException e) {
      return Response.withError(
          new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
    } catch (Exception e) {
      return Response.withError(new Error<>(400, "Error parsing response"));
    }
  }

  public Response<String> getSnippetAuthor(String snippetId, String token) {
    HttpHeaders header = new HttpHeaders();
    header.set("Authorization", token);
    HttpEntity<Void> requestPermissions = new HttpEntity<>(header);
    try {
      String response =
          getRequest(
              permissionsWebClient,
              "snippets/get/author",
              requestPermissions,
              String.class,
              Map.of("snippetId", snippetId));
      return Response.withData(response);
    } catch (HttpClientErrorException e) {
      return Response.withError(
          new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
    }
  }

  public Response<List<String>> getAllSnippets(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", token);
    HttpEntity<Void> request = new HttpEntity<>(headers);
    try {
      String response =
          getRequest(
              permissionsWebClient, "/snippets/get/all/edit", request, String.class, Map.of());
      List<String> snippetIds = objectMapper.readValue(response, new TypeReference<>() {});
      return Response.withData(snippetIds);
    } catch (HttpClientErrorException e) {
      return Response.withError(
          new Error<>(e.getStatusCode().value(), e.getResponseBodyAsString()));
    } catch (Exception e) {
      return Response.withError(new Error<>(400, "Failed to get snippets"));
    }
  }

  // Implementaciones locales para reemplazar las llamadas a RequestExecutor
  private <T> T postRequest(
      RestTemplate client, String path, HttpEntity<?> request, Class<T> responseType) {
    return client.exchange(path, HttpMethod.POST, request, responseType).getBody();
  }

  private <T> T getRequest(
      RestTemplate client,
      String path,
      HttpEntity<?> request,
      Class<T> responseType,
      Map<String, String> params) {
    return client.exchange(path, HttpMethod.GET, request, responseType, params).getBody();
  }

  private <T> T deleteRequest(
      RestTemplate client, String path, HttpEntity<?> request, Class<T> responseType) {
    return client.exchange(path, HttpMethod.DELETE, request, responseType).getBody();
  }
}
