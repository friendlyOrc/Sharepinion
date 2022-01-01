package sharepinion.sharepinion.data;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
// import org.springframework.data.repository.query.Param;

import sharepinion.sharepinion.model.Comment;


public interface CommentRepository extends CrudRepository<Comment, Long> {
    @Query(value = "SELECT * FROM `comment` WHERE prdid = ?1 order by id DESC", nativeQuery = true)
    ArrayList<Comment> getCmtViaPrd(int id);
    
    @Query(value = "SELECT * FROM `comment` WHERE accid = ?1 order by id DESC", nativeQuery = true)
    ArrayList<Comment> getCmtViaAcc(int id);

    @Query(value = "SELECT id FROM `comment` order by id DESC LIMIT 1", nativeQuery = true)
    int getHighestID();

    @Query(value = "SELECT COUNT(id) from `comment` WHERE prdID = ?1", nativeQuery = true)
    Integer getCmtNumber(int id);

    @Query(value = "SELECT * from `comment`", nativeQuery = true)
    ArrayList<Comment> getAllCmt();

    @Query(value = "SELECT COUNT(id) from `comment`", nativeQuery = true)
    Integer getAllCmtNumber();

    @Query(value = "SELECT * from `comment` WHERE trained = 0", nativeQuery = true)
    ArrayList<Comment> getNonTrainedCmt();

    @Query(value = "SELECT COUNT(id) from `comment` WHERE label = 1", nativeQuery = true)
    Integer getAllCmtPosNumber();

    @Query(value = "SELECT COUNT(id) from `comment` WHERE label = -1", nativeQuery = true)
    Integer getAllCmtNegNumber();
}