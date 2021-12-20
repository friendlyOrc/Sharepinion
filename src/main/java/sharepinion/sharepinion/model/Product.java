package sharepinion.sharepinion.model;

import java.io.Serializable;
import java.sql.Time;
import java.util.Date;
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
@Table(name = "product")
@NoArgsConstructor
@AllArgsConstructor
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
public class Product implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    private String name;

    @NotNull
    private Long price;

    @NotNull
    private String des;

    @NotNull
    private int label;
    // -99 = not define
    // -2 = Very negative
    // -1 = mostly negative
    // 0 = Mixed 
    // 1 = Mostly Positive
    // 2 = Very Positive

    @NotNull
    @OneToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "subcateID")
    private SubCategory subCategory;
    
    @NotNull
    private Date lastcmtdate;

    @NotNull
    private Time lastcmttime;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "productID")
    private List<ProductImage> images;

}