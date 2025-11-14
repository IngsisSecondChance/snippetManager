package ingsis.snippet.repositories;

import ingsis.snippet.entities.Snippet;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SnippetRepository extends JpaRepository<Snippet, String> {
    List<Snippet> findByIdInAndTitleStartingWith(List<String> ids, String prefix, Pageable pageable);

}
