package ingsis.snippet.repositories;

import ingsis.snippet.entities.Snippet;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SnippetRepository extends JpaRepository<Snippet, String> {
  List<Snippet> findByIdInAndTitleStartingWith(List<String> ids, String prefix, Pageable pageable);
}
