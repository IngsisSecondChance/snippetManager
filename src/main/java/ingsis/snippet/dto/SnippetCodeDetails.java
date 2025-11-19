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
  // Use a simple String for lint status to avoid depending on external/renamed Snippet class
  private Snippet.Status lintStatus;

  // Convenience constructor used by service when lintStatus is not available yet
  public SnippetCodeDetails(
      String author,
      String id,
      String title,
      String description,
      String language,
      String extension,
      String code) {
    this.author = author;
    this.id = id;
    this.title = title;
    this.description = description;
    this.language = language;
    this.extension = extension;
    this.code = code;
    this.lintStatus = null;
  }
}
