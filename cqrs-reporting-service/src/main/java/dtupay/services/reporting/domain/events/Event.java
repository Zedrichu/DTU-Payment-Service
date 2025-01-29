package dtupay.services.reporting.domain.events;

import dtupay.services.reporting.utilities.intramessaging.Message;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

public abstract class Event implements Message, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static long versionCount = 1;

    @Getter
    private final long version = versionCount++;

    public abstract String getId();
}
