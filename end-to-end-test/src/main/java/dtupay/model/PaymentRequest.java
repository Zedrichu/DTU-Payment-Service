package dtupay.model;
//TODO: Payment request should taken token object
public record PaymentRequest(String merchantId, String token, int amount) {
}
