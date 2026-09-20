package com.banking.util;

import java.io.*;
import java.math.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** Versioned university rules; annual simple interest, HALF_UP currency rounding. */
public final class DemoRules {
    private static final Properties VALUES=new Properties();
    static {try(InputStream in=DemoRules.class.getResourceAsStream("/demo-rules.properties")){if(in==null)throw new IOException("Demo rules missing");VALUES.load(in);}catch(IOException e){throw new ExceptionInInitializerError(e);}}
    private DemoRules(){}
    public static BigDecimal number(String key){return new BigDecimal(System.getProperty("bank.demo."+key,VALUES.getProperty(key)));}
    public static int integer(String key){return number(key).intValueExact();}
    public static BigDecimal amount(String kind,String value){
        BigDecimal amount;
        try{amount=Input.money(new BigDecimal(value));}catch(RuntimeException e){throw new IllegalArgumentException("Enter a valid positive amount with at most two decimal places.");}
        if(amount.compareTo(number(kind+".minAmount"))<0 || amount.compareTo(number(kind+".maxAmount"))>0)throw new IllegalArgumentException("Amount must be between "+number(kind+".minAmount")+" and "+number(kind+".maxAmount")+".");
        return amount;
    }
    public static int months(String kind,String value){int months=Input.id(value);if(months>integer(kind+".maxMonths"))throw new IllegalArgumentException("Term exceeds "+integer(kind+".maxMonths")+" months.");return months;}
    public static BigDecimal totalLoan(BigDecimal principal,BigDecimal annualRate,int months){return principal.add(principal.multiply(annualRate).multiply(BigDecimal.valueOf(months)).divide(new BigDecimal("1200"),2,RoundingMode.HALF_UP)).setScale(2,RoundingMode.HALF_UP);}
    public static List<BigDecimal> installments(BigDecimal principal,BigDecimal rate,int months){
        BigDecimal total=totalLoan(principal,rate,months),regular=total.divide(BigDecimal.valueOf(months),2,RoundingMode.DOWN);
        List<BigDecimal> values=new ArrayList<>(Collections.nCopies(months,regular));values.set(months-1,total.subtract(regular.multiply(BigDecimal.valueOf(months-1))));return values;
    }
    public static BigDecimal payout(BigDecimal principal,BigDecimal rate,LocalDate start,LocalDate maturity,LocalDate today){
        if(today.isBefore(maturity))return principal;
        long days=ChronoUnit.DAYS.between(start,maturity);
        return principal.add(principal.multiply(rate).multiply(BigDecimal.valueOf(days)).divide(new BigDecimal("36500"),2,RoundingMode.HALF_UP));
    }
}
