package com.restaurant.service;

import com.restaurant.dto.MenuCategoryRequest;
import com.restaurant.dto.MenuItemRequest;
import com.restaurant.dto.RecipeItemRequest;
import com.restaurant.exception.BusinessException;
import com.restaurant.exception.ResourceNotFoundException;
import com.restaurant.model.Ingredient;
import com.restaurant.model.MenuCategory;
import com.restaurant.model.MenuItem;
import com.restaurant.model.RecipeItem;
import com.restaurant.repository.IngredientRepository;
import com.restaurant.repository.MenuCategoryRepository;
import com.restaurant.repository.MenuItemRepository;
import com.restaurant.repository.RecipeItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuCategoryRepository categoryRepo;
    private final MenuItemRepository itemRepo;
    private final IngredientRepository ingredientRepo;
    private final RecipeItemRepository recipeItemRepo;

    public List<MenuCategory> getAllCategories() {
        return categoryRepo.findAll();
    }

    public MenuCategory createCategory(MenuCategoryRequest req) {
        if (categoryRepo.existsByName(req.getName())) {
            throw new BusinessException("Category already exists: " + req.getName());
        }
        MenuCategory cat = new MenuCategory();
        cat.setName(req.getName());
        return categoryRepo.save(cat);
    }

    public MenuCategory updateCategory(Integer id, MenuCategoryRequest req) {
        MenuCategory cat = categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        cat.setName(req.getName());
        return categoryRepo.save(cat);
    }

    public void deleteCategory(Integer id) {
        if (!categoryRepo.existsById(id)) throw new ResourceNotFoundException("Category not found");
        categoryRepo.deleteById(id);
    }

    public List<MenuItem> getAllItems() {
        return itemRepo.findAll();
    }

    public List<MenuItem> getAvailableItems() {
        return itemRepo.findByIsAvailableTrue();
    }

    public MenuItem createItem(MenuItemRequest req) {
        MenuCategory cat = categoryRepo.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        MenuItem item = new MenuItem();
        item.setName(req.getName());
        item.setPrice(req.getPrice());
        item.setCategory(cat);
        item.setIsAvailable(req.getIsAvailable() != null ? req.getIsAvailable() : true);
        return itemRepo.save(item);
    }

    public MenuItem updateItem(Integer id, MenuItemRequest req) {
        MenuItem item = itemRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        MenuCategory cat = categoryRepo.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        item.setName(req.getName());
        item.setPrice(req.getPrice());
        item.setCategory(cat);
        item.setIsAvailable(req.getIsAvailable() != null ? req.getIsAvailable() : true);
        return itemRepo.save(item);
    }

    public void deleteItem(Integer id) {
        if (!itemRepo.existsById(id)) throw new ResourceNotFoundException("Menu item not found");
        itemRepo.deleteById(id);
    }

    // ── Recipe (ingredient links) ──────────────────────────────────────────────

    public List<RecipeItem> getRecipeItems(Integer menuItemId) {
        MenuItem item = itemRepo.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        return recipeItemRepo.findByMenuItem(item);
    }

    @Transactional
    public RecipeItem addRecipeItem(Integer menuItemId, RecipeItemRequest req) {
        if (req.getIngredientId() == null || req.getQuantityRequired() == null) {
            throw new BusinessException("ingredientId and quantityRequired are required");
        }
        if (req.getQuantityRequired() <= 0) {
            throw new BusinessException("quantityRequired must be greater than 0");
        }
        MenuItem item = itemRepo.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        Ingredient ingredient = ingredientRepo.findById(req.getIngredientId())
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found"));
        RecipeItem ri = new RecipeItem();
        ri.setMenuItem(item);
        ri.setIngredient(ingredient);
        ri.setQuantityRequired(req.getQuantityRequired());
        return recipeItemRepo.save(ri);
    }

    @Transactional
    public void deleteRecipeItem(Integer recipeItemId) {
        if (!recipeItemRepo.existsById(recipeItemId))
            throw new ResourceNotFoundException("Recipe item not found");
        recipeItemRepo.deleteById(recipeItemId);
    }
}
