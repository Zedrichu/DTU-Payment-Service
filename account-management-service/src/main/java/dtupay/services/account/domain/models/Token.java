package dtupay.services.account.domain.models;


import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.Objects;
import java.util.UUID;

@Value
@EqualsAndHashCode
@ToString
public class Token {

    public Token(){
        this.id = UUID.randomUUID();
    }
    UUID id;


    public Token(UUID id) { this.id = id; }

    public static Token random() { return new Token(UUID.randomUUID()); }
}


