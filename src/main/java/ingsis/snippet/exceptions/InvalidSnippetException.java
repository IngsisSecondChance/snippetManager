package ingsis.snippet.exceptions;

import ingsis.snippet.dto.ValidationResultDTO;

public class InvalidSnippetException extends RuntimeException {

    private final ValidationResultDTO details;

    public InvalidSnippetException(ValidationResultDTO details) {
        super("Snippet inválido");
        this.details = details;
    }

    public ValidationResultDTO getDetails() {
        return details;
    }
}
