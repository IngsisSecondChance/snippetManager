package ingsis.snippet.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import ingsis.snippet.entities.FormatConfig;

public interface FormatConfigRepository extends JpaRepository<FormatConfig, String> {
}