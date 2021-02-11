package edu.pjatk.inn.coffeemaker.impl;

import sorcer.core.context.ServiceContext;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.Path;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * Class that represents a single recipe.
 * Aggregates functionalities, that are used to manage Recipe.
 * Recipe instance can be created using either {@link Recipe#Recipe} or {@link Recipe#getRecipe}
 * Context instance can be created using {@link Recipe#getContext}
 *
 * @author Sarah and Mike
 */
public class Recipe implements Serializable {
    private String name;
    private int price;
    private int amtCoffee;
    private int amtMilk;
    private int amtSugar;
    private int amtChocolate;

    /**
     * Default constructor, used to create a new instance of Recipe.
     * Initializes it with default values.
     */
    public Recipe() {
        this.name = "";
        this.price = 0;
        this.amtCoffee = 0;
        this.amtMilk = 0;
        this.amtSugar = 0;
        this.amtChocolate = 0;
    }

    /**
     * Provides the amount of chocolate of a particular instance of Recipe.
     *
     * @return Returns the amtChocolate.
     */
    public int getAmtChocolate() {
        return amtChocolate;
    }

    /**
     * Sets the amount of chocolate for a particular instance of Recipe.
     * Negative amount of chocolate is ignored.
     *
     * @param amtChocolate The amtChocolate to setValue. Negative value is ignored.
     */
    public void setAmtChocolate(int amtChocolate) {
        if (amtChocolate >= 0) {
            this.amtChocolate = amtChocolate;
        }
    }

    /**
     * Provides the amount of coffee of a particular instance of Recipe.
     *
     * @return Returns the amtCoffee.
     */
    public int getAmtCoffee() {
        return amtCoffee;
    }

    /**
     * Sets the amount of coffee for a particular instance of Recipe.
     * Negative amount of coffee is ignored.
     *
     * @param amtCoffee The amtCoffee to setValue.
     */
    public void setAmtCoffee(int amtCoffee) {
        if (amtCoffee >= 0) {
            this.amtCoffee = amtCoffee;
        }
    }

    /**
     * Provides the amount of milk of a particular instance of Recipe.
     *
     * @return Returns the amtMilk.
     */
    public int getAmtMilk() {
        return amtMilk;
    }

    /**
     * Sets the amount of milk for a particular instance of Recipe.
     * Negative amount of milk is ignored.
     *
     * @param amtMilk The amtMilk to setValue.
     */
    public void setAmtMilk(int amtMilk) {
        if (amtMilk >= 0) {
            this.amtMilk = amtMilk;
        }
    }

    /**
     * Provides the amount of sugar of a particular instance of Recipe.
     *
     * @return Returns the amtSugar.
     */
    public int getAmtSugar() {
        return amtSugar;
    }

    /**
     * Sets the amount of sugar for a particular instance of Recipe.
     * Negative amount of sugar is ignored.
     *
     * @param amtSugar The amtSugar to setValue.
     */
    public void setAmtSugar(int amtSugar) {
        if (amtSugar >= 0) {
            this.amtSugar = amtSugar;
        }
    }

    /**
     * Provides the name of a particular instance of Recipe.
     *
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name for a particular instance of Recipe.
     * Null value of name is ignored.
     *
     * @param name The name to setValue.
     */
    public void setName(String name) {
        if (name != null) {
            this.name = name;
        }
    }

    /**
     * Provides the price of a particular instance of Recipe.
     *
     * @return Returns the price.
     */
    public int getPrice() {
        return price;
    }

    /**
     * Sets the price for a particular instance of Recipe.
     * Negative price is ignored.
     *
     * @param price The price to setValue.
     */
    public void setPrice(int price) {
        if (price >= 0) {
            this.price = price;
        }
    }

    /**
     * Compares current instance of Recipe to another instance of Recipe based only on name.
     * Returns true if names are the same.
     * Returns false if names are different.
     *
     * @param r another instance of Recipe.
     * @return boolean result of comparison
     */
    public boolean equals(Recipe r) {
        if ((this.name).equals(r.getName())) {
            return true;
        }
        return false;
    }

    /**
     * Provides string representation of the instance of a Recipe.
     * Only name field is taken into account.
     *
     * @return Returns the name.
     */
    public String toString() {
        return name;
    }

    /**
     * Creates a new instance of Recipe based on the provided Context.
     *
     * @param   context 		 Provided Context.
     * @return  r                Instance of Recipe based on the provided Context
     * @throws  ContextException If remote method {@link Context#getValue} failed
     *                           or value name doesn't exist in provided Context.
	 * @see 	Context
	 */
    static public Recipe getRecipe(Context context) throws ContextException {
        Recipe r = new Recipe();
        try {
            r.name = (String) context.getValue("key");
            r.price = (int) context.getValue("price");
            r.amtCoffee = (int) context.getValue("amtCoffee");
            r.amtMilk = (int) context.getValue("amtMilk");
            r.amtSugar = (int) context.getValue("amtSugar");
            r.amtChocolate = (int) context.getValue("amtChocolate");
        } catch (RemoteException e) {
            throw new ContextException(e);
        }
        return r;
    }

    /**
     * Creates a new instance of Context based on the provided Recipe.
     *
     * @param 	recipe 			 Provided Recipe.
     * @return 	ctx              Instance of Context based on the provided Recipe.
     * @throws 	ContextException If remote method {@link Context#putValue} failed
     * @see 	Context
     */
    static public Context getContext(Recipe recipe) throws ContextException {
        Context cxt = new ServiceContext();
        cxt.putValue("key", recipe.getName());
        cxt.putValue("price", recipe.getPrice());
        cxt.putValue("amtCoffee", recipe.getAmtCoffee());
        cxt.putValue("amtMilk", recipe.getAmtMilk());
        cxt.putValue("amtSugar", recipe.getAmtSugar());
        cxt.putValue("amtChocolate", recipe.getAmtChocolate());
        return cxt;
    }


}
