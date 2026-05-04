package se.mo.xarbetemonolitisk.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UiPageController {

    @GetMapping("/users")
    public String usersPage() {
        return "forward:/users.html";
    }

    @GetMapping("/orders")
    public String ordersPage() {
        return "forward:/orders.html";
    }

    @GetMapping("/products-ui")
    public String productsPage() {
        return "forward:/products.html";
    }
}
