package sharepinion.sharepinion.data;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
// import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import sharepinion.sharepinion.model.Product;


public interface ProductRepository extends CrudRepository<Product, Long> {
    @Query(value = "SELECT * FROM `product` order by id DESC", nativeQuery = true)
    ArrayList<Product> allProduct();

    @Query(value = "SELECT * FROM `product` WHERE name LIKE %?1%", nativeQuery = true)
    ArrayList<Product> findProductByName(String name);

    @Query(value = "SELECT * FROM `product` WHERE id = ?1", nativeQuery = true)
    Product findProduct(int id);
    
    @Query(value = "SELECT * FROM `product` WHERE subcateid = ?1", nativeQuery = true)
    ArrayList<Product> findProductsByCategory(int catid);

    @Query(value = "SELECT * from `product` WHERE subcateid = ?1 and id <> ?2 LIMIT ?3", nativeQuery = true)
    ArrayList<Product> findSameCate(int cate, int id, int limit);

    @Query(value = "SELECT * FROM `product`, `comment`WHERE `comment`.id = ?1 AND `comment`.prdID = `product`.id;", nativeQuery = true)
    Product getProductViaCmt(int cmtId);

    @Query(value = "SELECT id FROM `product` order by id DESC LIMIT 1", nativeQuery = true)
    int getHighestID();
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM `product` WHERE id = ?1", nativeQuery = true)
    void deletePrd(int id);
}