package sharepinion.sharepinion.data;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
// import org.springframework.data.repository.query.Param;

import sharepinion.sharepinion.model.ProductImage;


public interface ProductImageRepository extends CrudRepository<ProductImage, Long> {
    @Query(value = "SELECT * FROM `prdImage` WHERE productID = ?1", nativeQuery = true)
    ArrayList<ProductImage> getImage(int id);

    @Query(value = "SELECT id FROM `prdImage` order by id DESC LIMIT 1", nativeQuery = true)
    int getHighestID();

}