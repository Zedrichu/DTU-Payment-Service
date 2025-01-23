package dtupay.services.reporting.utilities;

import dtupay.services.reporting.annotations.ClassAuthor;
import dtupay.services.reporting.annotations.MethodAuthor;

@ClassAuthor(author = "Adrian Ursu", stdno = "s240160")
public enum EventTypes {
    BANK_TRANSFER_CONFIRMED("BankTransferConfirmed"),

    CUSTOMER_REPORT_REQUESTED("CustomerReportRequested"),
    MERCHANT_REPORT_REQUESTED("MerchantReportRequested"),
    MANAGER_REPORT_REQUESTED("ManagerReportRequested"),

    CUSTOMER_REPORT_GENERATED("CustomerReportGenerated"),
    MERCHANT_REPORT_GENERATED("MerchantReportGenerated"),
    MANAGER_REPORT_GENERATED("ManagerReportGenerated");

    private final String topic;

    EventTypes(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    @MethodAuthor(author = "Adrian Ursu", stdno = "s240160")
    public static EventTypes fromTopic(String topic) {
        for (EventTypes eventType : values()) {
            if (eventType.topic.equals(topic)) {
              return eventType;
            }
        }
        throw new IllegalArgumentException("No matching EventTypes for topic: " + topic);
    }

    @Override
    public String toString() {
        return topic;
    }
}