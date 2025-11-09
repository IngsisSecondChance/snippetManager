package ingsis.snippet.dto;

public class SnippetCodeDetails {
    private final String author;
    private final String id;
    private final String title;
    private final String description;
    private final String language;
    private final String extension;
    private final String code;

    public SnippetCodeDetails(String author, String id, String title, String description,
                              String language, String extension, String code) {
        this.author = author;
        this.id = id;
        this.title = title;
        this.description = description;
        this.language = language;
        this.extension = extension;
        this.code = code;
    }

    public String getAuthor() { return author; }
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLanguage() { return language; }
    public String getExtension() { return extension; }
    public String getCode() { return code; }
}
