package com.banking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Authoritative completed payment fields; never populated from browser amounts/accounts. */
public record TransferReceipt(long paymentId, String reference, LocalDateTime timestamp,
        String senderAccount, String receiverAccount, BigDecimal amount, String type, String status) {
    public static String mask(String account) {
        return "**** " + (account.length()>4 ? account.substring(account.length()-4) : "****");
    }
}
