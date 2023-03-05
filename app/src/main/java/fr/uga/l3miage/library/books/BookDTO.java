package fr.uga.l3miage.library.books;

import fr.uga.l3miage.library.authors.AuthorDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.Collection;

import org.springframework.boot.context.properties.bind.DefaultValue;

public record BookDTO(
        @NotBlank @NotNull Long id,
        @NotBlank @NotNull String title,
        @Digits(integer = 13, fraction = 0) @Min(value = 1000000000L) @Max(value = 9999999999999L) long isbn,
        @NotBlank String publisher,
        @Digits(integer = 4, fraction = 0) @Min(value = -9999) @Max(value = 9999) @NotNull short year,
        @DefaultValue("french") @Pattern(regexp = "^(french|english)$") @NotBlank @NotNull String language,

        Collection<AuthorDTO> authors) {
}
