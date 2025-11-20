package ingsis.snippet.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import ingsis.snippet.entities.LintConfig;

public interface LintingConfigRepository extends JpaRepository<LintConfig, String> {
}
