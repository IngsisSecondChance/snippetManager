package ingsis.snippet.exceptions;

import ingsis.snippet.dto.Validation;

public class InvalidSnippetException extends RuntimeException {

  private final Validation details;

  public InvalidSnippetException(Validation details) {
    super("Snippet inválido");
    this.details = details;
  }

  public Validation getDetails() {
    return details;
  }
}
