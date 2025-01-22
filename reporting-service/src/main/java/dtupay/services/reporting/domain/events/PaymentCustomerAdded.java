package dtupay.services.reporting.domain.events;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import messaging.Event;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PaymentCustomerAdded extends Event {
    private int amount;
    private String token;
    private String merchantId;

}


