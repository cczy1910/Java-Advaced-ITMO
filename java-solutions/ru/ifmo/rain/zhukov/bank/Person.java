package ru.ifmo.rain.zhukov.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Person extends Remote {
    String getName() throws RemoteException;

    String getSurname() throws RemoteException;

    String getPassportId() throws RemoteException;

    Account getAccount(String id) throws RemoteException;

    Account createAccount(String id) throws RemoteException;
}