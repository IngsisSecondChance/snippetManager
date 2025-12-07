package ingsis.snippet.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaginationAndDetails {
  private Integer page;
  private Integer page_size;
  private Integer count;
  private List<SnippetCodeDetails> snippetCodeDetails;
}