package ingsis.snippet.repositories;

import ingsis.snippet.entities.Test;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRepository extends JpaRepository<Test, String> {
  List<Test> findBySnippetId(String snippetId);
}
