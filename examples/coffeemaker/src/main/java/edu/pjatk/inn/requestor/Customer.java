package edu.pjatk.inn.requestor;

import edu.pjatk.inn.coffeemaker.*;
import sorcer.service.*;

import static sorcer.co.operator.*;
import static sorcer.eo.operator.*;
import static sorcer.eo.operator.context;

public class Customer {
    private Mogram giveFeedback() throws Exception {
        Context mocha = context(val("key", "mocha"), val("price", 100),
                val("amtCoffee", 10), val("amtMilk", 11),
                val("amtSugar", 10), val("amtChocolate", 11));

        Task order = task("order", sig("makeOrder", OrderService.class), context(
                val("recipe/key", "mocha"),
                val("order/dateTime", "01-02-2021 20:05:00"),
                val("order/quantity", 5),
                val("order/price"),
                val("recipe", mocha)));

        Task payment = task("payment", sig("payWithBlik", PaymentService.class), context(
                val("payment/blikCode", "1657145"),
                val("payment/dateTime", "02-02-2021 20:05:20"),
                val("payment/price"),
                val("payment/status")));

        Task makeCoffee = task("makeCoffee", sig("makeCoffee", CoffeeService.class), context(
                val("coffee/paid", 320),
                val("coffee/status"),
                val("coffee/change")));

        Task delivery = task("delivery", sig("deliver", Delivery.class), context(
                val("location", "PJATK"),
                val("room", "A10"),
                val("delivery/paid"),
                val("delivery/status")));

        Task feedback = task("feedback", sig("giveFeedback", FeedbackService.class), context(
                val("feedback/grade", 10),
                val("feedback/description", "Tasty!"),
                val("feedback/status")));

        Job orderCoffee = job(order, payment, makeCoffee, delivery, feedback,
                pipe(outPoint(order, "order/price"), inPoint(payment, "payment/price")),
                pipe(outPoint(payment, "payment/status"), inPoint(makeCoffee, "coffee/status")),
                pipe(outPoint(makeCoffee, "coffee/change"), inPoint(delivery, "delivery/paid")),
                pipe(outPoint(delivery, "delivery/status"), inPoint(feedback, "feedback/status")));

        return orderCoffee;
    }
}
