package edu.pjatk.inn.coffeemaker;


import edu.pjatk.inn.coffeemaker.impl.CoffeeMaker;
import edu.pjatk.inn.coffeemaker.impl.Inventory;
import edu.pjatk.inn.coffeemaker.impl.Recipe;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sorcer.test.ProjectContext;
import org.sorcer.test.SorcerTestRunner;
import sorcer.service.Context;
import sorcer.service.ContextException;

import static org.junit.Assert.*;

@RunWith(SorcerTestRunner.class)
@ProjectContext("examples/coffeemaker")
public class InventoryTest {
    private final static Logger logger = LoggerFactory.getLogger(CoffeeMakerTest.class);

    private CoffeeMaker coffeeMaker;
    private Inventory inventory;
    private Recipe espresso, mocha, macchiato, americano;

    @Before
    public void setUp() throws ContextException {
        coffeeMaker = new CoffeeMaker();
        inventory = coffeeMaker.checkInventory();

        espresso = new Recipe();
        espresso.setName("espresso");
        espresso.setPrice(50);
        espresso.setAmtCoffee(6);
        espresso.setAmtMilk(1);
        espresso.setAmtSugar(1);
        espresso.setAmtChocolate(0);

        mocha = new Recipe();
        mocha.setName("mocha");
        mocha.setPrice(100);
        mocha.setAmtCoffee(8);
        mocha.setAmtMilk(1);
        mocha.setAmtSugar(1);
        mocha.setAmtChocolate(2);

        macchiato = new Recipe();
        macchiato.setName("macchiato");
        macchiato.setPrice(40);
        macchiato.setAmtCoffee(7);
        macchiato.setAmtMilk(1);
        macchiato.setAmtSugar(2);
        macchiato.setAmtChocolate(0);

        americano = new Recipe();
        americano.setName("americano");
        americano.setPrice(40);
        americano.setAmtCoffee(7);
        americano.setAmtMilk(1);
        americano.setAmtSugar(2);
        americano.setAmtChocolate(0);
    }

    /**
     * FIXED
     * Positive level of sugar check seems to be wrong in the addinventory method it should be '> 0' used instead of '< 0'
          */
    @Test
    public void testAddInventory(){
        inventory.setCoffee(90);
        inventory.setSugar(80);
        inventory.setMilk(70);
        inventory.setChocolate(100);
        assertEquals(coffeeMaker.checkInventory().getCoffee(), 90);
        assertEquals(coffeeMaker.checkInventory().getMilk(), 70);
        assertEquals(coffeeMaker.checkInventory().getSugar(), 80);
        assertEquals(coffeeMaker.checkInventory().getChocolate(), 100);

        assertFalse(coffeeMaker.addInventory(-20,0,0,0));
        assertTrue(coffeeMaker.addInventory(20,0,0,0));
        assertEquals(coffeeMaker.checkInventory().getCoffee(), 110);

        assertFalse(coffeeMaker.addInventory(0,-20,0,0));
        assertTrue(coffeeMaker.addInventory(0,20,0,0));
        assertEquals(coffeeMaker.checkInventory().getMilk(), 90);

        assertFalse(coffeeMaker.addInventory(0,0,-20,0));
        assertTrue(coffeeMaker.addInventory(0,0,20,0)); // sugar adding gave an error
        assertEquals(coffeeMaker.checkInventory().getSugar(), 100);

        assertFalse(coffeeMaker.addInventory(0,0,0,-20));
        assertTrue(coffeeMaker.addInventory(0,0,0,20));
        assertEquals(coffeeMaker.checkInventory().getChocolate(), 120);
    }

    /**
     * Resolution
     * S: Amount of chocolate is increasing instead of decreasing in the coffee method.
     * 1. While the coffee is dispensed it is not decreasing the inventory
     */
    @Test
    public void testMakeCoffe() {
        inventory.setChocolate(100);
        inventory.setCoffee(100);
        inventory.setSugar(100);
        inventory.setMilk(100);
        assertEquals(coffeeMaker.checkInventory().getChocolate(), 100);
        assertEquals(coffeeMaker.checkInventory().getCoffee(), 100);
        assertEquals(coffeeMaker.checkInventory().getSugar(), 100);
        assertEquals(coffeeMaker.checkInventory().getMilk(), 100);

        coffeeMaker.makeCoffee(mocha, 100);
        assertEquals(coffeeMaker.checkInventory().getChocolate(), 100-mocha.getAmtChocolate());
        // After purchasing mocha amount of chocolate in machine should decrease.
    }

    /**
     * This looks perfect
     */
    @Test
    public void testCheckInventory(){

        inventory.setChocolate(100);
        inventory.setCoffee(100);
        inventory.setSugar(100);
        inventory.setMilk(100);
        assertEquals(coffeeMaker.checkInventory().getChocolate(), 100);
        assertEquals(coffeeMaker.checkInventory().getCoffee(), 100);
        assertEquals(coffeeMaker.checkInventory().getSugar(), 100);
        assertEquals(coffeeMaker.checkInventory().getMilk(), 100);
    }

    /**
     * This looks perfect
     */
    @Test
    public void testPurchase(){
        inventory.setChocolate(0);
        inventory.setCoffee(0);
        inventory.setSugar(0);
        inventory.setMilk(0);

        assertEquals(coffeeMaker.checkInventory().getMilk(), 0);

        assertFalse(coffeeMaker.checkInventory().enoughIngredients(mocha));
        assertEquals(coffeeMaker.makeCoffee(espresso, 100), 100); //returned money, coffe wasn't made. OK

        inventory.setChocolate(100);
        inventory.setCoffee(100);
        inventory.setSugar(100);
        inventory.setMilk(100);
        assertEquals(coffeeMaker.makeCoffee(mocha, 120), 20); //coffe made, returned 120-100=20 OK
        assertEquals(coffeeMaker.makeCoffee(mocha, 100), 0);
        assertEquals(coffeeMaker.makeCoffee(mocha, 99), 99);
    }
}