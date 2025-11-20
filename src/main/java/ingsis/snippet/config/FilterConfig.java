package ingsis.snippet.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ingsis.snippet.filters.CorrelationIdFilters;

@Configuration
public class FilterConfig {

    @Bean
    public CorrelationIdFilters correlationIdFilter() {
        return new CorrelationIdFilters();
    }
}
