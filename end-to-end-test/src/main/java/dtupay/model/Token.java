package dtupay.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.Objects;
import java.util.UUID;

@ToString
@Value
public class Token {

    public Token(){
        this.id = UUID.randomUUID();
    }
    UUID id;

    public Token(UUID id) { this.id = id; }

    public static Token random() { return new Token(UUID.randomUUID()); }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return Objects.equals(id, token.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
