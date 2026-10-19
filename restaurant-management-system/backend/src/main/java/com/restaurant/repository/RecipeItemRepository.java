package com.restaurant.repository;

import com.restaurant.model.MenuItem;
import com.restaurant.model.RecipeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecipeItemRepository extends JpaRepository<RecipeItem, Integer> {
    List<RecipeItem> findByMenuItem(MenuItem menuItem);
}
