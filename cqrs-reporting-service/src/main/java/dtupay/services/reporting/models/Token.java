package dtupay.services.reporting.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.Value;

import java.io.Serializable;
import java.util.UUID;

@Getter
@EqualsAndHashCode
@ToString
@Value
public class Token implements Serializable {

    public Token(){
        this.id = UUID.randomUUID();
    }
    UUID id;

    public Token(UUID id) { this.id = id; }

    public static Token random() { return new Token(UUID.randomUUID()); }
}
