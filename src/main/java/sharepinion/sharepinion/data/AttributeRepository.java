package sharepinion.sharepinion.data;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
// import org.springframework.data.repository.query.Param;

import sharepinion.sharepinion.model.Attribute;


public interface AttributeRepository extends CrudRepository<Attribute, Long> {
    @Query(value = "SELECT * FROM `attribute` WHERE commentid = ?1", nativeQuery = true)
    ArrayList<Attribute> getCmtAttr(int id);
}
