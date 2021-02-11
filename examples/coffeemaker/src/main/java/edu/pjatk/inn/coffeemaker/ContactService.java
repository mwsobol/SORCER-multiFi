package edu.pjatk.inn.coffeemaker;

import sorcer.service.Context;
import sorcer.service.ContextException;

import java.rmi.RemoteException;

public interface ContactService {
    Context giveContact(Context context) throws ContextException, RemoteException;

    Context getAllContactsForRecipe(Context context) throws ContextException, RemoteException;
}
