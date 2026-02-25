package com.paypipe.payment_gateway.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotBlank(message = "User ID cannot be blank")
    private String userId;

    @NotNull(message = "Amount cannot be null")
    @Min(value=1,message = "Amount must be at least 1 dollar")
    private Double amount;

    @NotNull(message = "Currency cannot be blank")
    private String currency; //USD,INR

}
