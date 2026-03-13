package com.github.zeykrus.bankpet.model;

import java.util.HashMap;

public class BankInfo {
    private String name;
    private HashMap<BankCurrency, Boolean> currency;
    private BankCountry country;

    public BankInfo(String name, BankCountry country) {
        this.name = name;
        this.country = country;
        this.currency = new HashMap<>();

        for (BankCurrency curr : BankCurrency.values()) {
            currency.put(curr, false);
        }
    }

    public void setCurrencyAllowing(BankCurrency curr, Boolean allow) {
        if (curr != null) this.currency.put(curr, allow);
    }

    public boolean isAllowed(BankCurrency curr) {
        if (curr == null) return false;
        return currency.get(curr);
    }

    public String getName() {
        return name;
    }

    public HashMap<BankCurrency, Boolean> getCurrency() {
        return currency;
    }

    public BankCountry getCountry() {
        return country;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCountry(BankCountry country) {
        this.country = country;
    }
}