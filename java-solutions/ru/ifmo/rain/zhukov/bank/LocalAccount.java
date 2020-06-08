package ru.ifmo.rain.zhukov.bank;

public class LocalAccount extends AbstractAccount {
    public LocalAccount(final String id) {
        super(id, 0);
    }

    LocalAccount(RemoteAccount remoteAccount) {
        super(remoteAccount.getId(), remoteAccount.getAmount());
    }
}
