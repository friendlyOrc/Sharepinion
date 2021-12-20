package sharepinion.sharepinion.data;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import sharepinion.sharepinion.model.Category;


public interface CategoryRepository extends CrudRepository<Category, Long> {
    @Query(value = "SELECT * FROM `category`", nativeQuery = true)
    ArrayList<Category> allCategory();

    @Query(value = "SELECT * FROM `category` WHERE id = ?1", nativeQuery = true)
    ArrayList<Category> findACategoryByID(int id);
    
    @Query(value = "SELECT * FROM `category` WHERE name LIKE ?1", nativeQuery = true)
    ArrayList<Category> findACategoryByName(@Param("name") String name);

}