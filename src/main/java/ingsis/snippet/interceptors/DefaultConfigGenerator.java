package ingsis.snippet.interceptors;

import ingsis.snippet.repositories.FormatConfigRepository;
import ingsis.snippet.repositories.LintingConfigRepository;
import ingsis.snippet.services.ConfigService;
import ingsis.snippet.utils.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class DefaultConfigGenerator implements HandlerInterceptor {

  @Autowired private ConfigService configService;

  @Autowired private FormatConfigRepository formatConfigRepository;

  @Autowired private LintingConfigRepository lintingConfigRepository;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    // ignore if it goes to http://localhost:8080/swagger-ui/index.html#/
    if (request.getRequestURI().matches(".*swagger-ui.*")
        || request.getRequestURI().matches(".*v3/api-docs.*")) {
      return true;
    }

    try {
      // Verificar si existe el header de autorización
      String authHeader = request.getHeader("authorization");
      if (authHeader == null || authHeader.length() <= 7) {
        // Si no hay token, permitir que la request continúe
        // El sistema de seguridad de Spring manejará la autenticación
        return true;
      }

      String token = authHeader.substring(7);
      String userId = TokenUtils.decodeToken(token).get("userId");

      // Verificar si el userId es válido
      if (userId == null || userId.isEmpty()) {
        return true;
      }

      // Si ya existen ambas configuraciones, continuar
      if (formatConfigRepository.existsById(userId) && lintingConfigRepository.existsById(userId)) {
        return true;
      }

      // Generar configuraciones por defecto si no existen
      configService.generateDefaultLintingConfig(userId, token);
      configService.generateDefaultFormatConfig(userId, token);
      return true;
    } catch (Exception e) {
      // Log del error para debugging
      System.err.println("Error en DefaultConfigGenerator: " + e.getMessage());
      e.printStackTrace();
      // Permitir que la request continúe en caso de error
      // así el controller puede manejar la lógica de negocio normalmente
      return true;
    }
  }
}
