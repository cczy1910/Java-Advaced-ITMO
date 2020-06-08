package ru.ifmo.rain.zhukov.bank;

import java.util.concurrent.ConcurrentMap;

public class LocalPerson extends AbstractPerson {
    private final ConcurrentMap<String, LocalAccount> accounts;

    LocalPerson(RemotePerson remotePerson) {
        super(remotePerson.getName(), remotePerson.getSurname(), remotePerson.getPassportId());
        this.accounts = remotePerson.accountsCopy();
    }

    @Override
    public synchronized Account getAccount(String id) {
        if (id == null) {
            return null;
        }
        return accounts.get(id);
    }

    @Override
    public synchronized Account createAccount(String id) {
        if (id == null) {
            return null;
        }
        if (accounts.containsKey(id)) {
            return accounts.get(id);
        } else {
            LocalAccount localAccount = new LocalAccount(passportId + ":" + id);
            accounts.put(id, localAccount);
            return localAccount;
        }
    }
}
