package ingsis.snippet.repositories

import ingsis.snippet.entities.SnippetEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SnippetRepository : JpaRepository<SnippetEntity, String>
