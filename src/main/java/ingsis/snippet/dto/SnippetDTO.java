package ingsis.snippet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class SnippetDTO {

  @NotBlank(message = "Title is required")
  private String title;

  private String description;

  @NotBlank(message = "Language is required")
  private String language;

  @NotBlank(message = "Extension is required")
  private String extension;

  @NotBlank(message = "Code is required")
  private String code;

  public SnippetDTO(
      String code, String title, String description, String language, String extension) {
    this.title = title;
    this.description = description;
    this.language = language;
    this.extension = extension;
    this.code = code;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    SnippetDTO other = (SnippetDTO) obj;
    return java.util.Objects.equals(title, other.title)
        && java.util.Objects.equals(description, other.description)
        && java.util.Objects.equals(language, other.language)
        && java.util.Objects.equals(extension, other.extension)
        && java.util.Objects.equals(code, other.code);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(title, description, language, extension, code);
  }
}
