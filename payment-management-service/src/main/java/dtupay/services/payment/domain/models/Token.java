package dtupay.services.payment.domain.models;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Objects;
import java.util.UUID;

@EqualsAndHashCode
@Value
public class Token {

    public Token(){
        this.id = UUID.randomUUID();
    }
    UUID id;


    public Token(UUID id) { this.id = id; }


    public static Token random() { return new Token(UUID.randomUUID()); }

    @Override
    public String toString() {
        return "Token{" + id +'}';
    }
}

