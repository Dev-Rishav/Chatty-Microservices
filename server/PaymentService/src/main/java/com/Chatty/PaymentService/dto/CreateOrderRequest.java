package com.Chatty.PaymentService.dto;

    import lombok.Getter;
    import lombok.Setter;

    import java.time.LocalDateTime;

    @Getter
    @Setter
    public class CreateOrderRequest {
        private int amount;
        private String currency;
        private String recipientId;
        private LocalDateTime date;

        public CreateOrderRequest() {
            this.date = LocalDateTime.now();
        }
    }