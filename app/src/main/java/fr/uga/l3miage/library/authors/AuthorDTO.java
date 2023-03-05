package fr.uga.l3miage.library.authors;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AuthorDTO(
                Long id,
                @NotNull @NotBlank String fullName) {
}
