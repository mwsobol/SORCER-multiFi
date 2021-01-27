package edu.pjatk.inn.coffeemaker.impl;

import sorcer.core.context.ServiceContext;
import sorcer.service.Context;
import sorcer.service.ContextException;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * @author   Sarah & Mike
 */
public class Recipe implements Serializable {

	/** Name of the recipe */
    private String name;
    /** Price of the recipe */
    private int price;
    /** Amount of coffee in the recipe */
    private int amtCoffee;
    /** Amount of milk in the recipe */
    private int amtMilk;
    /** Amount of sugar in the recipe */
    private int amtSugar;
    /** Amount of chocolate in the recipe */
    private int amtChocolate;

	/**
	 * Constructor for the recipe
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
	 * @return   Returns the amtChocolate.
	 */
    public int getAmtChocolate() {
		return amtChocolate;
	}
    /**
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
	 * @param name   The name to setValue.
	 */
    public void setName(String name) {
    	if(name != null) {
    		this.name = name;
    	}
	}
    /**
	 * @return   Returns the price.
	 */
    public int getPrice() {
		return price;
	}
    /**
	 * @param price   The price to setValue.
	 */
    public void setPrice(int price) {
		if (price >= 0) {
			this.price = price;
		} 
	}

	/**
	 * Compares the Recipe with another Recipe, returning a boolean value.
	 * @param r   Recipe to compare to.
	 * @return    True if both objects have the same name, false otherwise.
	 */
    public boolean equals(Recipe r) {
        if((this.name).equals(r.getName())) {
            return true;
        }
        return false;
    }

	/**
	 * Get a string representation of the Recipe.
	 * @return   Name of the recipe.
	 */
	public String toString() {
    	return name;
    }

	/**
	 * Creates a Recipe object based on the given Context.
	 * @param context   Context object holding information about the recipe.
	 * @return   A new Recipe object with the data provided by the Context.
	 * @throws ContextException   When Context is unreachable.
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
	 * Creates a Context object based on the given Recipe.
	 * @param recipe   Recipe object to be stored in a new Context.
	 * @return   Context object storing information about the given Recipe.
	 * @throws ContextException   When Context cannot be created.
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
