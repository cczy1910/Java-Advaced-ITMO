package ru.ifmo.rain.zhukov.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Bank extends Remote {
    Person createPerson(String name, String surname, String passportId) throws RemoteException;

    Person getPerson(String passportId, boolean local) throws RemoteException;
}
