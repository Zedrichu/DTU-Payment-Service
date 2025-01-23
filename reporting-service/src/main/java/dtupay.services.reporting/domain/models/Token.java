package dtupay.services.reporting.domain.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;

import java.util.UUID;

@Getter
@Value
@EqualsAndHashCode
public class Token {

    UUID id;

    public Token(UUID id) { this.id = id; }

    public static Token random() { return new Token(UUID.randomUUID()); }

    @Override
    public String toString() {
        return "Token{" + id +'}';
    }
}
