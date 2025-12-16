package ingsis.snippet.dto;

import java.util.List;

public record RunSnippetData(String snippetId, String version, List<String> inputs) {}
