package sharepinion.sharepinion.data;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import sharepinion.sharepinion.model.SubCategory;


public interface SubCategoryRepository extends CrudRepository<SubCategory, Long> {

    @Query(value = "SELECT * FROM `sub_category` WHERE catid = ?1", nativeQuery = true)
    ArrayList<SubCategory> findAllSubCategory(int id);

    @Query(value = "SELECT * FROM `sub_category`", nativeQuery = true)
    ArrayList<SubCategory> findAllSubCategory();
    
    @Query(value = "SELECT * FROM `sub_category` WHERE name LIKE ?1", nativeQuery = true)
    ArrayList<SubCategory> findASubCategoryByName(@Param("name") String name);

}