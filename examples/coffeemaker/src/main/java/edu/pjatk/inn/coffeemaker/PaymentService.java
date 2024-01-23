package edu.pjatk.inn.coffeemaker;

import sorcer.service.Context;
import sorcer.service.ContextException;

import java.rmi.RemoteException;

public interface PaymentService {
    Context payWithCash(Context context) throws ContextException, RemoteException;

    Context payWithCard(Context context) throws ContextException, RemoteException;

    Context payWithBlik(Context context) throws ContextException, RemoteException;
}
