package dtupay.services.reporting.utilities;

import dtupay.services.reporting.annotations.ClassAuthor;
import lombok.Getter;

@Getter
@ClassAuthor(author = "Adrian Ursu", stdno = "s240160")
public enum EventTypes {
    PAYMENT_INITIATED("PaymentInitiated"),

    CUSTOMER_ACCOUNT_VERIFIED("CustomerAccountVerified"),

    MERCHANT_ACCOUNT_VERIFIED("MerchantAccountVerified"),

    BANK_TRANSFER_CONFIRMED("BankTransferConfirmed"),
    BANK_TRANSFER_FAILED("BankTransferFailed"),

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

    @Override
    public String toString() {
        return topic;
    }
}