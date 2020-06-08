package ru.ifmo.rain.zhukov.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemotePerson extends AbstractPerson {
    private final int port;
    private final ConcurrentMap<String, RemoteAccount> accounts = new ConcurrentHashMap<>();

    public RemotePerson(String name, String surname, String passportId, int port) {
        super(name, surname, passportId);
        this.port = port;
    }

    @Override
    public synchronized Account getAccount(final String id) {
        if (id == null) {
            return null;
        }
        return accounts.get(id);
    }

    @Override
    public synchronized Account createAccount(final String id) throws RemoteException {
        if (id == null) {
            return null;
        }
        final RemoteAccount account = new RemoteAccount(passportId + ":" + id);
        if (accounts.putIfAbsent(id, account) == null) {
            UnicastRemoteObject.exportObject(account, port);
            return account;
        } else {
            return getAccount(id);
        }
    }

    synchronized ConcurrentMap<String, LocalAccount> accountsCopy() {
        ConcurrentMap<String, LocalAccount> copy = new ConcurrentHashMap<>();
        for (String id : accounts.keySet()) {
            copy.put(id, new LocalAccount(accounts.get(id)));
        }
        return copy;
    }
}
