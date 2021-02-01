package edu.pjatk.inn.coffeemaker;

import sorcer.service.Context;
import sorcer.service.ContextException;

import java.rmi.RemoteException;

public interface OrderService {
    Context makeOrder(Context context) throws ContextException, RemoteException;

    Context getOrder(Context context) throws ContextException, RemoteException;

    Context scheduleOrder(Context context) throws ContextException, RemoteException;

    Context putOrderByGeolocation(Context context) throws ContextException, RemoteException;

    Context saveToFavorites(Context context) throws ContextException, RemoteException;

}
