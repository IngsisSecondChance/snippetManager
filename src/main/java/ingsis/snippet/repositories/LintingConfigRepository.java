package ingsis.snippet.repositories;

import ingsis.snippet.entities.LintConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LintingConfigRepository extends JpaRepository<LintConfig, String> {}
