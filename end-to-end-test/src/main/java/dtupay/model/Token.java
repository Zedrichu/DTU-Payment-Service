package dtupay.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.UUID;

@ToString
@EqualsAndHashCode
@Value
public class Token {

    public Token(){
        this.id = UUID.randomUUID();
    }
    UUID id;

    public Token(UUID id) { this.id = id; }

    public static Token random() { return new Token(UUID.randomUUID()); }
}
