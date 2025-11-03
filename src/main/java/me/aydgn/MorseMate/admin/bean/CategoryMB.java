package me.aydgn.MorseMate.admin.bean;

import com.github.adminfaces.template.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.entity.Category;
import me.aydgn.MorseMate.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

/**
 * JSF Managed Bean for Category CRUD operations.
 * Uses AdminFaces framework for admin panel UI.
 */
@Named
@ViewScoped
@Data
@Slf4j
public class CategoryMB implements Serializable {

    private static final long serialVersionUID = 1L;

    @Autowired
    private CategoryRepository categoryRepository;

    private List<Category> categories;
    private Category selectedCategory;
    private Category category = new Category();

    @PostConstruct
    public void init() {
        loadCategories();
    }

    public void loadCategories() {
        try {
            categories = categoryRepository.findAll();
            log.info("Loaded {} categories", categories.size());
        } catch (Exception e) {
            log.error("Error loading categories", e);
            throw new BusinessException("Failed to load categories");
        }
    }

    @Transactional
    public void save() {
        try {
            if (category.getId() == null) {
                categoryRepository.save(category);
                log.info("Created new category: {}", category.getName());
            } else {
                categoryRepository.save(category);
                log.info("Updated category: {}", category.getName());
            }
            loadCategories();
            clear();
        } catch (Exception e) {
            log.error("Error saving category", e);
            throw new BusinessException("Failed to save category");
        }
    }

    @Transactional
    public void delete() {
        try {
            if (selectedCategory != null && selectedCategory.getId() != null) {
                categoryRepository.deleteById(selectedCategory.getId());
                log.info("Deleted category: {}", selectedCategory.getName());
                loadCategories();
                clear();
            }
        } catch (Exception e) {
            log.error("Error deleting category", e);
            throw new BusinessException("Failed to delete category. Make sure there are no related lessons.");
        }
    }

    public void edit(Category cat) {
        this.category = new Category();
        this.category.setId(cat.getId());
        this.category.setName(cat.getName());
        this.category.setDescription(cat.getDescription());
        this.category.setDisplayOrder(cat.getDisplayOrder());
        this.category.setIconUrl(cat.getIconUrl());
        this.category.setIsActive(cat.getIsActive());
    }

    public void clear() {
        category = new Category();
        category.setIsActive(true); // Default to active
        category.setDisplayOrder(0);
    }

    public boolean isEditing() {
        return category != null && category.getId() != null;
    }
}
