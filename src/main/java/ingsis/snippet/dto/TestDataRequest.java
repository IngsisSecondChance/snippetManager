package ingsis.snippet.dto;

import java.util.List;

public record TestDataRequest(
    String snippetId, String version, List<String> inputs, List<String> expected) {}
