package ru.ifmo.rain.zhukov.bank;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Client {
    static boolean validateArgs(String[] args) {
        if (args.length > 5 || args.length == 1 || args.length == 2) {
            System.out.println("Usage: Client [Name Surname PassportId [AccountId [AmountChange]]]");
            return false;
        }
        for (String s : args) {
            if (s == null) {
                System.out.println("Null arguments are illegal");
                return false;
            }

            if (s.length() == 0) {
                System.out.println("Zero-length arguments are illegal");
                return false;
            }
        }
        return true;
    }

    public static void main(final String... args) throws RemoteException {
        final Bank bank;
        try {
            bank = (Bank) Naming.lookup("//localhost/bank");
        } catch (final NotBoundException e) {
            System.out.println("Bank is not bound");
            return;
        } catch (final MalformedURLException e) {
            System.out.println("Bank URL is invalid");
            return;
        }


        if (!validateArgs(args)) {
            return;
        }

        String name = "Zakhar";
        String surname = "Zhukov";
        String passportId = "1";
        String accountId = "1";
        int amountChange = 100;

        if (args.length > 0) {
            name = args[0];
            surname = args[1];
            passportId = args[2];
            if (args.length > 3) {
                accountId = args[3];
                if (args.length > 4) {
                    try {
                        amountChange = Integer.parseInt(args[4]);
                    } catch (NumberFormatException e) {
                        System.out.println("Wrong amount change format");
                        return;
                    }
                }
            }
        }

        Person person = bank.getPerson(passportId, false);
        if (person == null) {
            System.out.println("Creating person");
            person = bank.createPerson(name, surname, passportId);
        } else {
            if (!person.getName().equals(name) || !person.getSurname().equals(surname)) {
                System.out.println("Incorrect identity");
                return;
            }
            System.out.println("Person already exists");
        }
        System.out.println("Person identity: " + person.getName() + " " + person.getSurname() + " : " + person.getPassportId());
        Account account = person.getAccount(accountId);
        if (account == null) {
            System.out.println("Creating account");
            account = person.createAccount(accountId);
        } else {
            System.out.println("Account already exists");
        }
        System.out.println("Account id: " + account.getId());
        int accountAmount = account.getAmount();
        System.out.println("Money: " + accountAmount);
        if (accountAmount + amountChange < 0) {
            System.out.println("Sorry, you have no enough money");
            return;
        }
        System.out.println("Adding money");
        account.setAmount(accountAmount + amountChange);
        System.out.println("Money: " + account.getAmount());
    }
}
