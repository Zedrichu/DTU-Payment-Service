package dtupay.services.token.domain.models;

import lombok.Value;

import java.util.Objects;
import java.util.UUID;

@Value
public class Token {

    public UUID getId() {
        return id;
    }

    UUID id;

    public Token(UUID id) { this.id = id; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Token that = (Token) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }


    public static Token random() { return new Token(UUID.randomUUID()); }

    @Override
    public String toString() {
        return "Token{" + id +'}';
    }
}
