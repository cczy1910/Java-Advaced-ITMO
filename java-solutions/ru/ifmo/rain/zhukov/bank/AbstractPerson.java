package ru.ifmo.rain.zhukov.bank;

import java.io.Serializable;

public abstract class AbstractPerson implements Person, Serializable {
    protected final String name;
    protected final String surname;
    protected final String passportId;

    protected AbstractPerson(String name, String surname, String passportId) {
        this.name = name;
        this.surname = surname;
        this.passportId = passportId;
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
}
