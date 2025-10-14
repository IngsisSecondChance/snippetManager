package ingsis.snippet.entities

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "snippets")
class SnippetEntity(

    @Id
    @Column(length = 36)
    var id: String = UUID.randomUUID().toString(),

    var name: String? = null,

    @Column(length = 2000)
    var description: String? = null,

    var language: String? = null,

    var version: String? = null,

    @Column(columnDefinition = "TEXT")
    var code: String? = null
)
