package org.acme.domain.model;

public record Payment(String customerId, String merchantId, int amount) {
}
