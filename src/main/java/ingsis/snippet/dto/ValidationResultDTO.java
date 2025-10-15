package ingsis.snippet.dto;

import lombok.Value;

@Value
public class ValidationResultDTO {
    boolean valid;
    String rule;
    int line;
    int column;
}
