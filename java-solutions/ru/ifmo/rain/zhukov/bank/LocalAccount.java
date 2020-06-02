package ru.ifmo.rain.zhukov.bank;

import java.io.Serializable;

public class LocalAccount implements Account, Serializable {
    private final String id;
    private int amount;

    public LocalAccount(final String id) {
        this.id = id;
        amount = 0;
    }

    LocalAccount(RemoteAccount remoteAccount) {
        this.id = remoteAccount.getId();
        this.amount = remoteAccount.getAmount();
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
