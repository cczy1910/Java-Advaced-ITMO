package ru.ifmo.rain.zhukov.bank;

import java.io.Serializable;
import java.util.concurrent.ConcurrentMap;

public class LocalPerson implements Person, Serializable {
    private final String name;
    private final String surname;
    private final String passportId;
    private final ConcurrentMap<String, LocalAccount> accounts;

    LocalPerson(RemotePerson remotePerson) {
        this.name = remotePerson.getName();
        this.surname = remotePerson.getSurname();
        this.passportId = remotePerson.getPassportId();
        this.accounts = remotePerson.accountsCopy();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSurname() {
        return surname;
    }

    @Override
    public String getPassportId() {
        return passportId;
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
