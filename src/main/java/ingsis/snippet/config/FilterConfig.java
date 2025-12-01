package ingsis.snippet.config;

import ingsis.snippet.filters.CorrelationIdFilters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

  @Bean
  public CorrelationIdFilters correlationIdFilter() {
    return new CorrelationIdFilters();
  }
}
