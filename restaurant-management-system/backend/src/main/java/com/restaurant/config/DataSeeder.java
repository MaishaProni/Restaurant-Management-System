package com.restaurant.config;

import com.restaurant.model.*;
import com.restaurant.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepo;
    private final TableRepository tableRepo;
    private final MenuCategoryRepository categoryRepo;
    private final MenuItemRepository menuItemRepo;
    private final IngredientRepository ingredientRepo;
    private final RecipeItemRepository recipeItemRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepo.count() > 0) return; // Already seeded

        log.info("Seeding initial data...");
        seedUsers();
        seedTables();
        seedMenuAndInventory();
        log.info("Seeding complete. Login credentials: manager/password123, waiter/password123, kitchen/password123, cashier/password123");
    }

    private void seedUsers() {
        String hash = passwordEncoder.encode("password123");
        userRepo.saveAll(List.of(
            createUser("manager", hash, "Alice Manager",  Role.MANAGER),
            createUser("waiter",  hash, "Bob Waiter",     Role.WAITER),
            createUser("kitchen", hash, "Chef Charlie",   Role.KITCHEN),
            createUser("cashier", hash, "Diana Cashier",  Role.CASHIER)
        ));
    }

    private void seedTables() {
        for (int i = 1; i <= 10; i++) {
            RestaurantTable t = new RestaurantTable();
            t.setTableNumber(i);
            t.setCapacity(i % 3 == 0 ? 6 : 4);
            t.setStatus(TableStatus.AVAILABLE);
            tableRepo.save(t);
        }
    }

    private void seedMenuAndInventory() {
        MenuCategory appetizers = saveCategory("Appetizers");
        MenuCategory mains      = saveCategory("Main Course");
        MenuCategory beverages  = saveCategory("Beverages");
        MenuCategory desserts   = saveCategory("Desserts");

        Ingredient chicken  = saveIngredient("Chicken Breast",        20.0, "kg",  5.0);
        Ingredient flour    = saveIngredient("All-Purpose Flour",      15.0, "kg",  3.0);
        Ingredient pasta    = saveIngredient("Pasta",                  10.0, "kg",  2.0);
        Ingredient beef     = saveIngredient("Beef Patty",              8.0, "kg",  2.0);
        Ingredient sauce    = saveIngredient("Tomato Sauce",            5.0, "ltr", 1.0);
        Ingredient cheese   = saveIngredient("Cheese",                  4.0, "kg",  1.0);
        Ingredient eggs     = saveIngredient("Eggs",                   30.0, "pcs", 10.0);
        Ingredient choc     = saveIngredient("Chocolate",               3.0, "kg",  0.5);
        Ingredient cream    = saveIngredient("Cream",                   2.0, "ltr", 0.5);
        Ingredient wrapper  = saveIngredient("Spring Roll Wrapper",    50.0, "pcs", 10.0);

        MenuItem springRolls  = saveItem("Spring Rolls",     4.99,  appetizers);
        MenuItem wings        = saveItem("Chicken Wings",    7.99,  appetizers);
        MenuItem grilledChick = saveItem("Grilled Chicken", 14.99, mains);
        MenuItem carbonara    = saveItem("Pasta Carbonara",  12.99, mains);
        MenuItem burger       = saveItem("Beef Burger",      11.99, mains);
        saveItem("Soft Drink",       1.99, beverages);
        saveItem("Fresh Juice",      3.49, beverages);
        MenuItem cake         = saveItem("Chocolate Cake",   5.99, desserts);

        saveRecipe(springRolls,  wrapper, 3.0);
        saveRecipe(springRolls,  sauce,   0.1);
        saveRecipe(wings,        chicken, 0.3);
        saveRecipe(grilledChick, chicken, 0.25);
        saveRecipe(carbonara,    pasta,   0.2);
        saveRecipe(carbonara,    eggs,    2.0);
        saveRecipe(carbonara,    cheese,  0.05);
        saveRecipe(burger,       beef,    0.2);
        saveRecipe(burger,       flour,   0.1);
        saveRecipe(cake,         choc,    0.1);
        saveRecipe(cake,         cream,   0.1);
        saveRecipe(cake,         flour,   0.2);
    }

    private User createUser(String username, String hash, String name, Role role) {
        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(hash);
        u.setFullName(name);
        u.setRole(role);
        u.setActive(true);
        return u;
    }

    private MenuCategory saveCategory(String name) {
        MenuCategory c = new MenuCategory();
        c.setName(name);
        return categoryRepo.save(c);
    }

    private Ingredient saveIngredient(String name, double stock, String unit, double threshold) {
        Ingredient i = new Ingredient();
        i.setName(name);
        i.setCurrentStock(stock);
        i.setUnit(unit);
        i.setLowStockThreshold(threshold);
        return ingredientRepo.save(i);
    }

    private MenuItem saveItem(String name, double price, MenuCategory cat) {
        MenuItem m = new MenuItem();
        m.setName(name);
        m.setPrice(price);
        m.setCategory(cat);
        m.setIsAvailable(true);
        return menuItemRepo.save(m);
    }

    private void saveRecipe(MenuItem item, Ingredient ingredient, double qty) {
        RecipeItem r = new RecipeItem();
        r.setMenuItem(item);
        r.setIngredient(ingredient);
        r.setQuantityRequired(qty);
        recipeItemRepo.save(r);
    }
}
