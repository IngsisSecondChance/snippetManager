package ingsis.snippet.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaginationAndDetails {
    Integer page;
    Integer page_size;
    Integer count;
    private List<SnippetCodeDetails> snippetCodeDetails;
}
