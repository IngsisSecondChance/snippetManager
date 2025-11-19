package ingsis.snippet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SnippetPermissionGrantResponse {
  private String snippetId;
  private String author;
}
