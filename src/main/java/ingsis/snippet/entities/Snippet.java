package ingsis.snippet.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Table
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Snippet {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(nullable = false)
  private String title;

  @Column private String description;

  @Column(nullable = false)
  private String language;

  @Column(nullable = false)
  private String extension;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private Status lintStatus = Status.UNKNOWN;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private Status formatStatus = Status.UNKNOWN;

  @OneToMany(mappedBy = "snippet", cascade = CascadeType.ALL)
  @Builder.Default
  private List<Test> tests = new ArrayList<>();

  public enum Status {
    IN_PROGRESS,
    COMPLIANT,
    NON_COMPLIANT,
    UNKNOWN
  }
}
