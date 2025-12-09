package ingsis.snippet.dto;

import ingsis.snippet.entities.Snippet;

public record SnippetDetails(
    String id,
    String title,
    String description,
    String language,
    String extension,
    Snippet.Status lintStatus) {}
