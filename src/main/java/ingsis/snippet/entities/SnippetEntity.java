package ingsis.snippet.entities;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "snippets")
public class SnippetEntity {

  @Id
  @Column(length = 36)
  private String id = UUID.randomUUID().toString();

  private String name;

  @Column(length = 2000)
  private String description;

  private String language;

  private String version;

  @Column(columnDefinition = "TEXT")
  private String code;

  public SnippetEntity() {
    // Constructor vacío requerido por JPA
  }

  public SnippetEntity(
      String id, String name, String description, String language, String version, String code) {
    this.id = id != null ? id : UUID.randomUUID().toString();
    this.name = name;
    this.description = description;
    this.language = language;
    this.version = version;
    this.code = code;
  }

  // Getters y setters

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }
}
