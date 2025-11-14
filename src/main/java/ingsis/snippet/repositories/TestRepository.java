package ingsis.snippet.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ingsis.snippet.entities.Test;

public interface TestRepository extends JpaRepository<Test, String> {
    List<Test> findBySnippetId(String snippetId);
}
