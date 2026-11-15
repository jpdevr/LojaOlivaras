package br.com.olivaras.api.order.entity;

import java.time.Duration;

public enum PaymentMethod {
    PIX(Duration.ofMinutes(5)),
    BOLETO(Duration.ofMinutes(15)),
    CREDIT_CARD(Duration.ofMinutes(2));

    private final Duration simulationDelay;

    PaymentMethod(Duration simulationDelay) { this.simulationDelay = simulationDelay; }

    public Duration simulationDelay() { return simulationDelay; }
}
