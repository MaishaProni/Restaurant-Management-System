package com.restaurant.repository;

import com.restaurant.model.MenuCategory;
import com.restaurant.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {
    List<MenuItem> findByCategory(MenuCategory category);
    List<MenuItem> findByIsAvailableTrue();
}
