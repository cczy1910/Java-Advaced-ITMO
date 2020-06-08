package ru.ifmo.rain.zhukov.bank;

import java.io.Serializable;

public abstract class AbstractAccount implements Account, Serializable {
    protected String id;
    protected int amount;

    protected AbstractAccount(String id, int amount) {
        this.id = id;
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public synchronized int getAmount() {
        return amount;
    }

    public synchronized void setAmount(final int amount) {
        this.amount = amount;
    }
}
