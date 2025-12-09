package ingsis.snippet.dto;

import ingsis.snippet.entities.Snippet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SnippetCodeDetails {
  private String author;
  private String id;
  private String title;
  private String description;
  private String language;
  private String extension;
  private String code;
  private Snippet.Status lintStatus;
}
