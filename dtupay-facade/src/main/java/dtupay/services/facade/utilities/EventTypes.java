package dtupay.services.facade.utilities;

import dtupay.services.facade.annotations.ClassAuthor;

@ClassAuthor(author = "Adrian Ursu", stdno = "s240160")
public enum EventTypes {
    TOKENS_REQUESTED("TokensRequested"),
    TOKEN_ACCOUNT_VERIFIED("TokenAccountVerified"),
    TOKEN_GENERATION_FAILED("TokenGenerationFailed"),
    TOKENS_GENERATED("TokensGenerated"),

    CUSTOMER_REGISTRATION_REQUESTED("CustomerRegistrationRequested"),
    CUSTOMER_ACCOUNT_CREATED("CustomerAccountCreated"),
    CUSTOMER_ACCOUNT_CREATION_FAILED("CustomerAccountCreationFailed"),
    CUSTOMER_DEREGISTRATION_REQUESTED("CustomerDeregistrationRequested"),
    CUSTOMER_DEREGISTRATION_COMPLETED("CustomerDeRegistrationCompleted"),
    CUSTOMER_DEREGISTERED("CustomerDeregistered"),
    CUSTOMER_TOKENS_DELETED("CustomerTokensDeleted"),
    CUSTOMER_DELETED("CustomerDeleted"),
    CUSTOMER_DELETE_FAILED("CustomerDeleteFailed"),

    MERCHANT_REGISTRATION_REQUESTED("MerchantRegistrationRequested"),
    MERCHANT_ACCOUNT_CREATED("MerchantAccountCreated"),
    MERCHANT_ACCOUNT_CREATION_FAILED("MerchantAccountCreationFailed"),

    PAYMENT_INITIATED("PaymentInitiated"),

    BANK_TRANSFER_CONFIRMED("BankTransferConfirmed"),
    BANK_TRANSFER_FAILED("BankTransferFailed"),
    MERCHANT_DEREGISTRATION_REQUESTED("MerchantDeregistrationRequested"),
    MERCHANT_DELETED("MerchantDeleted"),
    MERCHANT_DELETED_FAILED("MerchantDeleteFailed"),

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
