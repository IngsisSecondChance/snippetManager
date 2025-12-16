package ingsis.snippet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RunSnippetDTO {

  @NotBlank(message = "snippetId is required")
  private String snippetId;

  @NotNull(message = "Inputs are required")
  private List<String> inputs;
}
