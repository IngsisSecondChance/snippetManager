package ingsis.snippet.dto;

// Small immutable container for downloaded snippet (code + filename).
public record Tuple(String code, String name) {}
