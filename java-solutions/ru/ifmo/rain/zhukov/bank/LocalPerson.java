package ru.ifmo.rain.zhukov.bank;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentMap;

public class LocalPerson implements Person, Serializable {
    private final String name;
    private final String surname;
    private final String passportId;
    private final ConcurrentMap<String, LocalAccount> accounts;

    LocalPerson(RemotePerson remotePerson) throws RemoteException {
        this.name = remotePerson.getName();
        this.surname = remotePerson.getSurname();
        this.passportId = remotePerson.getPassportId();
        this.accounts = remotePerson.accountsCopy();
    }

    @Override
    public String getName() throws RemoteException {
        return name;
    }

    @Override
    public String getSurname() throws RemoteException {
        return surname;
    }

    @Override
    public String getPassportId() throws RemoteException {
        return passportId;
    }

    @Override
    public synchronized Account getAccount(String id) throws RemoteException {
        if (id == null) {
            return null;
        }
        return accounts.get(id);
    }

    @Override
    public synchronized Account createAccount(String id) throws RemoteException {
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
