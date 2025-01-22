package dtupay.services.facade.domain.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
public class Token {

    private UUID id;

    public Token() {
    }

    public Token(UUID id) {
        this.id = id;
    }

    public static Token random() {
        return new Token(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return "Token{" + id +'}';
    }
}
