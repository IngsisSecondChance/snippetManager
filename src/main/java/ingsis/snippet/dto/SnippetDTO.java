package ingsis.snippet.dto;

import java.util.Objects;

public class SnippetDTO {

    private final String id;
    private final String name;
    private final String description;
    private final String language;
    private final String version;

    public SnippetDTO(String id, String name, String description, String language, String version) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.language = language;
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLanguage() {
        return language;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SnippetDTO)) return false;
        SnippetDTO that = (SnippetDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(language, that.language) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, language, version);
    }

    @Override
    public String toString() {
        return "SnippetDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", language='" + language + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
