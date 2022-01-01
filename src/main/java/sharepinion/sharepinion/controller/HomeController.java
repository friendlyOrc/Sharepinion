package sharepinion.sharepinion.controller;

import java.util.ArrayList;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import sharepinion.sharepinion.data.ProductRepository;
import sharepinion.sharepinion.data.SubCategoryRepository;
import sharepinion.sharepinion.data.CategoryRepository;
import sharepinion.sharepinion.data.CommentRepository;
import sharepinion.sharepinion.data.ProductImageRepository;
import sharepinion.sharepinion.model.Category;
import sharepinion.sharepinion.model.Product;
import sharepinion.sharepinion.model.ProductImage;
import sharepinion.sharepinion.model.SubCategory;

@Controller
public class HomeController {

    private final ProductRepository prdRepo;
    private final CommentRepository cmtRepo;
    private final ProductImageRepository prdImgRepo;
    private final CategoryRepository cateRepo;
    private final SubCategoryRepository subCateRepo;
    
    @Autowired
    public HomeController(Environment env, ProductRepository prdRepo, 
                        ProductImageRepository prdImgRepo, CommentRepository cmtRepo, CategoryRepository cateRepo, SubCategoryRepository subCateRepo) {
        this.prdRepo = prdRepo;
        this.prdImgRepo = prdImgRepo;
        this.cmtRepo = cmtRepo;
        this.cateRepo = cateRepo;
        this.subCateRepo = subCateRepo;
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {

        model.addAttribute("page", "Home");
        model.addAttribute("title", "Trang chủ");

        ArrayList<Product> prdList = prdRepo.allProduct();
        ArrayList<Integer> cmtList = new ArrayList<>();
        for(int i = 0; i < prdList.size(); i++){
            ArrayList<ProductImage> images = prdImgRepo.getImage(prdList.get(i).getId());
            cmtList.add(cmtRepo.getCmtNumber(prdList.get(i).getId()));
            prdList.get(i).setImages(images);
        }

        ArrayList<Category> cat = (ArrayList<Category>) cateRepo.findAll();
        for(int i = 0; i < cat.size(); i++){
            ArrayList<SubCategory> sub = subCateRepo.findAllSubCategory(cat.get(i).getId());
            cat.get(i).setSubcategory(sub);
        }
        
        System.out.print(prdList.size());
        model.addAttribute("prdList", prdList);
        model.addAttribute("cmtList", cmtList);
        model.addAttribute("cate", cat);
        return "index";

    }
    @GetMapping("/admin")
    public String home_admin(Model model, HttpSession session) {
        if (session.getAttribute("account") == null) {
            return "redirect:/admin/login";
        }

        model.addAttribute("page", "Home");
        model.addAttribute("title", "Trang chủ");

        int prdNum = prdRepo.allProduct().size();
        int cmtNum = cmtRepo.getAllCmtNumber();
        int cmtPosNum = cmtRepo.getAllCmtPosNumber();
        int cmtNegNum = cmtRepo.getAllCmtNegNumber();
        
        model.addAttribute("prdNum", prdNum);
        model.addAttribute("cmtNum", cmtNum);
        model.addAttribute("cmtPosNum", cmtPosNum);
        model.addAttribute("cmtNegNum", cmtNegNum);
        return "index_admin";

    }

}
