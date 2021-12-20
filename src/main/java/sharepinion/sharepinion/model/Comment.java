package sharepinion.sharepinion.model;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table(name = "comment")
@NoArgsConstructor
@AllArgsConstructor
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
public class Comment implements Serializable{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    private String content;

    private int label;

    @NotNull
    private Date cmtdate;
    
    @NotNull
    private Time cmttime;

    @NotNull
    @OneToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "accID")
    private Account account;

    @ManyToOne
    @OneToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "prdID")
    private Product product;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "commentid")
    private List<Attribute> attributes;
}
