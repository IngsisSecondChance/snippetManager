package ingsis.snippet.dto;

import java.util.List;

public record TestData(
    String snippetId, String version, List<String> inputs, List<String> expected) {}
