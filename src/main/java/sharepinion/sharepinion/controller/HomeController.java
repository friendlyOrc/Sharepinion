package sharepinion.sharepinion.controller;

import java.util.ArrayList;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import sharepinion.sharepinion.data.ProductRepository;
import sharepinion.sharepinion.data.CommentRepository;
import sharepinion.sharepinion.data.ProductImageRepository;
import sharepinion.sharepinion.model.Product;
import sharepinion.sharepinion.model.ProductImage;

@Controller
public class HomeController {

    private final ProductRepository prdRepo;
    private final CommentRepository cmtRepo;
    private final ProductImageRepository prdImgRepo;
    
    @Autowired
    public HomeController(Environment env, ProductRepository prdRepo, ProductImageRepository prdImgRepo, CommentRepository cmtRepo) {
        this.prdRepo = prdRepo;
        this.prdImgRepo = prdImgRepo;
        this.cmtRepo = cmtRepo;
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
        
        System.out.print(prdList.size());
        model.addAttribute("prdList", prdList);
        model.addAttribute("cmtList", cmtList);
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
