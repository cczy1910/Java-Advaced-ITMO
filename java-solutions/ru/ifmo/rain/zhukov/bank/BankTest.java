package ru.ifmo.rain.zhukov.bank;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.util.Random;

public class BankTest {
    private static final String NAME = "Name";
    private static final String SURNAME = "Surname";
    private static final String PASSPORT_ID = "123";
    private static final String ACC = "acc";

    private static int port = Registry.REGISTRY_PORT;

    private Bank bank;

    @BeforeClass
    public static void beforeClass() throws RemoteException {
        try {
            LocateRegistry.createRegistry(port);
        } catch (ExportException ignored) {
            port = 8889;
            LocateRegistry.createRegistry(port);
        }
    }

    @Before
    public void before() throws RemoteException, MalformedURLException, NotBoundException {
        try {
            Bank remoteBank = new RemoteBank(8888);
            Naming.rebind("//localhost:" + port + "/bank", remoteBank);
        } catch (ExportException e) {
            Bank remoteBank = new RemoteBank(8899);
            Naming.rebind("//localhost:" + port + "/bank", remoteBank);
        }
        bank = (Bank) Naming.lookup("//localhost:" + port + "/bank");
    }

    private void checkAccount(String expectedId, int expectedAmount, Account actual) throws RemoteException {
        Assert.assertEquals(expectedId, actual.getId());
        Assert.assertEquals(expectedAmount, actual.getAmount());
    }

    private void checkPerson(String expectedName, String expectedSurname,
                             String expectedPassportId, Person actual) throws RemoteException {
        Assert.assertEquals(expectedName, actual.getName());
        Assert.assertEquals(expectedSurname, actual.getSurname());
        Assert.assertEquals(expectedPassportId, actual.getPassportId());
    }

    @Test
    public void testSinglePersonIdentity() throws RemoteException {
        Assert.assertNull(bank.getPerson(PASSPORT_ID, true));
        Person person = bank.createPerson(NAME, SURNAME, PASSPORT_ID);
        Assert.assertNotNull(person);
        checkPerson(NAME, SURNAME, PASSPORT_ID, person);
        checkPerson(NAME, SURNAME, PASSPORT_ID, bank.getPerson(PASSPORT_ID, true));
        checkPerson(NAME, SURNAME, PASSPORT_ID, bank.getPerson(PASSPORT_ID, false));
        final String secondName = NAME + "qwerty";
        final String secondSurname = SURNAME + "asdfgh";
        Person second = bank.createPerson(secondName, secondSurname, PASSPORT_ID);
        checkPerson(NAME, SURNAME, PASSPORT_ID, second);
        checkPerson(NAME, SURNAME, PASSPORT_ID, bank.getPerson(PASSPORT_ID, true));
        checkPerson(NAME, SURNAME, PASSPORT_ID, bank.getPerson(PASSPORT_ID, false));
    }

    private void testSinglePersonAccounts(Person person) throws RemoteException {
        for (int i = 1; i <= 10; i++) {
            String accountId = ACC + i;
            Assert.assertNull(person.getAccount(accountId));
            Account account = person.createAccount(accountId);
            Assert.assertNotNull(account);
            checkAccount(PASSPORT_ID + ":" + accountId, 0, account);
        }
        int[] values = new Random().ints().limit(10).map(x -> Math.abs(x) % 10000).toArray();
        for (int i = 1; i <= 10; i++) {
            String accountId = ACC + i;
            Account account = person.getAccount(accountId);
            Assert.assertNotNull(account);
            checkAccount(PASSPORT_ID + ":" + accountId, 0, account);
            account.setAmount(values[i - 1]);
        }
        for (int i = 1; i <= 10; i++) {
            String accountId = ACC + i;
            Account account = person.getAccount(accountId);
            checkAccount(PASSPORT_ID + ":" + accountId, values[i - 1], account);
        }
    }

    @Test
    public void testRemotePersonAccounts() throws RemoteException {
        bank.createPerson(NAME, SURNAME, PASSPORT_ID);
        Person person = bank.getPerson(PASSPORT_ID, false);
        testSinglePersonAccounts(person);
    }

    @Test
    public void testLocalPersonAccounts() throws RemoteException {
        bank.createPerson(NAME, SURNAME, PASSPORT_ID);
        Person person = bank.getPerson(PASSPORT_ID, true);
        testSinglePersonAccounts(person);
    }

    @Test
    public void testRemoteToRemoteVisibility() throws RemoteException {
        bank.createPerson(NAME, SURNAME, PASSPORT_ID);
        Person firstRemote = bank.getPerson(PASSPORT_ID, false);
        Person secondRemote = bank.getPerson(PASSPORT_ID, false);
        Account account = firstRemote.createAccount(ACC);
        Account otherAccount = secondRemote.getAccount(ACC);
        Assert.assertNotNull(otherAccount);
        checkAccount(PASSPORT_ID + ":" + ACC, 0, otherAccount);
        account.setAmount(100);
        checkAccount(PASSPORT_ID + ":" + ACC, 100, otherAccount);
        otherAccount.setAmount(200);
        checkAccount(PASSPORT_ID + ":" + ACC, 200, account);
        account.setAmount(300);
        checkAccount(PASSPORT_ID + ":" + ACC, 300, otherAccount);
    }

    @Test
    public void testRemoteToLocalVisibility() throws RemoteException {
        bank.createPerson(NAME, SURNAME, PASSPORT_ID);
        Person remote = bank.getPerson(PASSPORT_ID, false);
        Person local = bank.getPerson(PASSPORT_ID, true);
        Account remoteAccount = remote.createAccount(ACC);
        Account localAccount = local.getAccount(ACC);
        Assert.assertNull(localAccount);

        local = bank.getPerson(PASSPORT_ID, true);
        localAccount = local.getAccount(ACC);
        remoteAccount.setAmount(100);
        Assert.assertNotNull(localAccount);
        checkAccount(PASSPORT_ID + ":" + ACC, 0, localAccount);

        local = bank.getPerson(PASSPORT_ID, true);
        remoteAccount.setAmount(200);
        localAccount = local.getAccount(ACC);
        checkAccount(PASSPORT_ID + ":" + ACC, 100, localAccount);

        local = bank.getPerson(PASSPORT_ID, true);
        remoteAccount.setAmount(300);
        remoteAccount.setAmount(400);
        remoteAccount.setAmount(500);
        localAccount = local.getAccount(ACC);
        checkAccount(PASSPORT_ID + ":" + ACC, 200, localAccount);

        local = bank.getPerson(PASSPORT_ID, true);
        localAccount = local.getAccount(ACC);
        checkAccount(PASSPORT_ID + ":" + ACC, 500, localAccount);
    }

    @Test
    public void testLocalVisibility() throws RemoteException {
        bank.createPerson(NAME, SURNAME, PASSPORT_ID);

        Person remote = bank.getPerson(PASSPORT_ID, false);
        Person local = bank.getPerson(PASSPORT_ID, true);
        Person otherLocal = bank.getPerson(PASSPORT_ID, true);

        local.createAccount(ACC);
        Account remoteAccount = remote.getAccount(ACC);
        Account otherLocalAccount = otherLocal.getAccount(ACC);
        Assert.assertNull(remoteAccount);
        Assert.assertNull(otherLocalAccount);

        remote = bank.getPerson(PASSPORT_ID, false);
        otherLocal = bank.getPerson(PASSPORT_ID, true);
        remoteAccount = remote.getAccount(ACC);
        otherLocalAccount = otherLocal.getAccount(ACC);
        Assert.assertNull(remoteAccount);
        Assert.assertNull(otherLocalAccount);

        remoteAccount = remote.createAccount(ACC);
        remoteAccount.setAmount(100);
        local = bank.getPerson(PASSPORT_ID, true);
        Account localAccount = local.getAccount(ACC);
        Assert.assertNotNull(localAccount);
        localAccount.setAmount(200);
        localAccount.setAmount(300);

        remote = bank.getPerson(PASSPORT_ID, false);
        otherLocal = bank.getPerson(PASSPORT_ID, true);
        remoteAccount = remote.getAccount(ACC);
        otherLocalAccount = otherLocal.getAccount(ACC);
        checkAccount(PASSPORT_ID + ":" + ACC, 100, remoteAccount);
        checkAccount(PASSPORT_ID + ":" + ACC, 100, otherLocalAccount);
    }

    @Test
    public void testMultiplePersons() throws RemoteException {
        int[] ids = new Random().ints().map(x -> Math.abs(x) % 100000 + 1).distinct().limit(10).toArray();
        for (int i = 1; i <= 10; i++) {
            String passportId = Integer.toString(ids[i - 1]);
            Assert.assertNull(bank.getPerson(Integer.toString(ids[i - 1]), true));
            Person person = bank.createPerson(NAME + i, SURNAME + i, passportId);
            Assert.assertNotNull(person);
            checkPerson(NAME + i, SURNAME + i, passportId, person);
            checkPerson(NAME + i, SURNAME + i,
                    passportId, bank.getPerson(passportId, true));
            checkPerson(NAME + i, SURNAME + i, passportId,
                    bank.getPerson(passportId, false));
        }

        int[] values = new Random().ints().map(x -> Math.abs(x) % 10000 + 1).distinct().limit(10).toArray();
        for (int i = 1; i <= 10; i++) {
            String passportId = Integer.toString(ids[i - 1]);
            final String name = NAME + "qwerty";
            final String surname = SURNAME + "asdfgh";
            Person person = bank.createPerson(name, surname, passportId);
            Assert.assertNotNull(person);
            checkPerson(NAME + i, SURNAME + i, passportId, person);
            checkPerson(NAME + i, SURNAME + i, passportId,
                    bank.getPerson(passportId, true));
            checkPerson(NAME + i, SURNAME + i, passportId,
                    bank.getPerson(passportId, true));
            Account account = person.createAccount(ACC);
            Assert.assertNotNull(account);
            account.setAmount(values[i - 1]);
        }

        for (int i = 1; i <= 10; i++) {
            String passportId = Integer.toString(ids[i - 1]);
            Person person = bank.getPerson(passportId, false);
            Assert.assertNotNull(person);
            Account account = person.getAccount(ACC);
            Assert.assertNotNull(account);
            checkAccount(passportId + ":" + ACC, values[i - 1], account);
        }
    }
}