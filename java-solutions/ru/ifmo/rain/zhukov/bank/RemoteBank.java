package ru.ifmo.rain.zhukov.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteBank extends UnicastRemoteObject implements Bank {
    private final int port;
    private final ConcurrentMap<String, RemotePerson> persons = new ConcurrentHashMap<>();

    public RemoteBank(final int port) throws RemoteException {
        super(port);
        this.port = port;
    }

    @Override
    public synchronized Person createPerson(String name, String surname, String passportId) throws RemoteException {
        if (name == null || surname == null || passportId == null) {
            return null;
        }
        final RemotePerson person = new RemotePerson(name, surname, passportId, port);
        if (persons.putIfAbsent(passportId, person) == null) {
            return person;
        } else {
            return getPerson(passportId, false);
        }
    }

    @Override
    public synchronized Person getPerson(String passportId, boolean local) throws RemoteException {
        if (passportId == null) {
            return null;
        }
        if (local) {
            RemotePerson person = persons.get(passportId);
            if (person == null) {
                return null;
            }
            return new LocalPerson(person);
        } else {
            return persons.get(passportId);
        }
    }
}
