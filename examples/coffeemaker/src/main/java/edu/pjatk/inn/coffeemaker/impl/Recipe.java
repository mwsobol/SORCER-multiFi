package edu.pjatk.inn.coffeemaker.impl;

import sorcer.core.context.ServiceContext;
import sorcer.service.Context;
import sorcer.service.ContextException;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * Class that represents coffee recipe.
 * @author   Sarah & Mike
 */
public class Recipe implements Serializable {
    private String name;
    private int price;
    private int amtCoffee;
    private int amtMilk;
    private int amtSugar;
    private int amtChocolate;
    
    public Recipe() {
    	this.name = "";
    	this.price = 0;
    	this.amtCoffee = 0;
    	this.amtMilk = 0;
    	this.amtSugar = 0;
    	this.amtChocolate = 0;
    }
    
    /**
	 * @return   Returns the amtChocolate.
	 */
    public int getAmtChocolate() {
		return amtChocolate;
	}

    /**
	 * AmtChocolate represents the amount of chocolate in the recipe.
	 * It has default value 0, set in Recipe no argument constructor.
	 * Sets amtChocolate, if provided value is greater equal 0.
	 *
	 * @param amtChocolate   The amtChocolate to setValue.
	 */
    public void setAmtChocolate(int amtChocolate) {
		if (amtChocolate >= 0) {
			this.amtChocolate = amtChocolate;
		} 
	}
    /**
	 * @return   Returns the amtCoffee.
	 */
    public int getAmtCoffee() {
		return amtCoffee;
	}

	/**
	 * AmtCoffee represents the amount of coffee in the recipe.
	 * It has default value 0, set in Recipe no argument constructor.
	 * Sets amtCoffee, if provided value is greater equal 0.
	 *
	 * @param amtCoffee   The amtCoffee to setValue.
	 */
    public void setAmtCoffee(int amtCoffee) {
		if (amtCoffee >= 0) {
			this.amtCoffee = amtCoffee;
		} 
	}
    /**
	 * @return   Returns the amtMilk.
	 */
    public int getAmtMilk() {
		return amtMilk;
	}

	/**
	 * AmtMilk represents the amount of milk in the recipe.
	 * It has default value 0, set in Recipe no argument constructor.
	 * Sets amtMilk, if provided value is greater equal 0.
	 *
	 * @param amtMilk   The amtMilk to setValue.
	 */
    public void setAmtMilk(int amtMilk) {
		if (amtMilk >= 0) {
			this.amtMilk = amtMilk;
		} 
	}
    /**
	 * @return   Returns the amtSugar.
	 */
    public int getAmtSugar() {
		return amtSugar;
	}

	/**
	 * AmtSugar represents the amount of milk in the recipe.
	 * It has default value 0, set in Recipe no argument constructor.
	 * Sets amtSugar, if provided value is greater equal 0.
	 *
	 * @param amtSugar   The amtSugar to setValue.
	 */
    public void setAmtSugar(int amtSugar) {
		if (amtSugar >= 0) {
			this.amtSugar = amtSugar;
		} 
	}
    /**
	 * @return   Returns the name.
	 */
    public String getName() {
		return name;
	}

	/**
	 * Name represents the name of the recipe.
	 * It has default value "", set in Recipe no argument constructor.
	 * Sets name, if provided value is not null.
	 *
	 * @param name   The name to setValue.
	 */
    public void setName(String name) {
    	if(name != null) {
    		this.name = name;
    	}
	}

	/**
	 * @return Returns the price.
	 */
    public int getPrice() {
		return price;
	}


	/**
	 * Price represents the price in the recipe.
	 * It has default value 0, set in Recipe no argument constructor.
	 * Sets price, if provided value is greater equal 0.
	 *
	 * @param price   The price to setValue.
	 */
    public void setPrice(int price) {
		if (price >= 0) {
			this.price = price;
		} 
	}

	/**
	 * Compares name of given recipe to name
	 *
	 * @param r Recipe to compare to
	 * @return result of comparison of names
	 */
    public boolean equals(Recipe r) {
        if((this.name).equals(r.getName())) {
            return true;
        }
        return false;
    }

	/**
	 * @return Name of recipe
	 */
	public String toString() {
    	return name;
    }

	/**
	 * Creates new Recipe instance, passes values of given context to it
	 * @param context Context instance to take values from
	 * @return Newly created Recipe
	 * @throws ContextException thrown if RemoteException is caught during setting recipe values
	 */
	static public Recipe getRecipe(Context context) throws ContextException {
		Recipe r = new Recipe();
		try {
			r.name = (String)context.getValue("key");
			r.price = (int)context.getValue("price");
			r.amtCoffee = (int)context.getValue("amtCoffee");
			r.amtMilk = (int)context.getValue("amtMilk");
			r.amtSugar = (int)context.getValue("amtSugar");
			r.amtChocolate = (int)context.getValue("amtChocolate");
		} catch (RemoteException e) {
			throw new ContextException(e);
		}
		return r;
	}

	/**
	 * Creates new ServiceContext instance, passes values of given recipe to it
	 *
	 * @param recipe Recipe instance to take values from
	 * @return Newly created ServiceContext
	 * @throws ContextException
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
