package sharepinion.sharepinion.data;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import sharepinion.sharepinion.model.Account;


public interface AccountRepository extends CrudRepository<Account, Long> {
    @Query(value = "SELECT * FROM `account`", nativeQuery = true)
    ArrayList<Account> getAll();

    @Query(value = "SELECT * FROM `account` WHERE email = ?1 and password = ?2", nativeQuery = true)
    ArrayList<Account> findAccount(String un, String pw);

    @Query(value = "SELECT * FROM `account` WHERE idcard = ?1", nativeQuery = true)
    ArrayList<Account> findAccountByIdcard(String card);

    @Query(value = "SELECT * FROM `account` WHERE email = ?1", nativeQuery = true)
    ArrayList<Account> findAccountByEmail(String un);

    @Query(value = "SELECT * FROM `account` WHERE id = ?1", nativeQuery = true)
    Account findOneAccount(int i);

    @Query(value = "SELECT * FROM `account` WHERE `name` LIKE ?1", nativeQuery = true)
    ArrayList<Account> findByName(@Param("uname") String uname);

    @Query(value = "SELECT * FROM `account`, `comment` WHERE `comment`.id = ?1 AND `comment`.accID = `account`.id;", nativeQuery = true)
    Account findAccountByComment(int cmtId);
}