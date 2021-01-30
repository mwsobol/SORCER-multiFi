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
import sorcer.service.ContextException;

import static org.junit.Assert.*;


@RunWith(SorcerTestRunner.class)
@ProjectContext("examples/coffeemaker")
public class RecipeTest {
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
     * Resolution
    * In this scenario only 3 recipies are allowed at max.
    */
    @Test
    public void testAddRecipe() {
        assertTrue(coffeeMaker.addRecipe(espresso));
        assertTrue(coffeeMaker.addRecipe(macchiato));
        assertTrue(coffeeMaker.addRecipe(americano));
        assertFalse(coffeeMaker.addRecipe(mocha)); //Adds 4-th recipe
    }

    /**
     * Resolution
     * The recipe didn't removed from the recipe array when deleteRecipe was called.
     * THe coffee maker should retun false if it is not deleting anything.
     */
    @Test
    public void testDeleteRecipe() {
        assertFalse(coffeeMaker.deleteRecipe(espresso));
        assertTrue(coffeeMaker.addRecipe(espresso));
//        assertTrue(coffeeMaker.deleteRecipe("espresso")); in documentation deletion should be BY BEVERAGE NAME
        assertTrue(coffeeMaker.deleteRecipe(espresso));
        assertEquals(coffeeMaker.getRecipeForName("esspresso"), null);
        assertFalse(coffeeMaker.deleteRecipe(espresso)); // Should return false, returns true instead
    }

    /**
     * Resolution
     * Two recipies with same name is redundant which is not allowed.
     */
    @Test
    public void testEditRecipe() {

        Recipe mocha2 = new Recipe();
        mocha2.setName("mocha");
        mocha2.setPrice(40);
        mocha2.setAmtCoffee(7);
        mocha2.setAmtMilk(1);
        mocha2.setAmtSugar(2);
        mocha2.setAmtChocolate(0);

        assertTrue(coffeeMaker.addRecipe(espresso));
        assertTrue(coffeeMaker.addRecipe(mocha));
        assertFalse(coffeeMaker.editRecipe(espresso, mocha2)); // Allows changing espresso into mocha2.
        // Same name Mocha2 is "mocha" is already used by mocha in list.
    }
}