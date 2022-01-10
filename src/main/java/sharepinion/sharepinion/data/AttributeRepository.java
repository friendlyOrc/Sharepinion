package sharepinion.sharepinion.data;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
// import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import sharepinion.sharepinion.model.Attribute;


public interface AttributeRepository extends CrudRepository<Attribute, Long> {
    @Query(value = "SELECT * FROM `attribute` WHERE commentid = ?1", nativeQuery = true)
    ArrayList<Attribute> getCmtAttr(int id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM `attribute` WHERE id = ?1", nativeQuery = true)
    void deleteAttr(int id);
}
