package sharepinion.sharepinion.controller;

import java.util.ArrayList;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import sharepinion.sharepinion.data.AccountRepository;
import sharepinion.sharepinion.data.CommentRepository;
import sharepinion.sharepinion.data.ProductRepository;
import sharepinion.sharepinion.model.Account;
import sharepinion.sharepinion.model.Comment;
import sharepinion.sharepinion.model.Product;

import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class ProfileController {
    
    private final AccountRepository accRepo;
    private final ProductRepository prdRepo;
    private final CommentRepository cmtRepo;
    private ValidateFunction val;

    @Autowired
    public ProfileController(Environment env, AccountRepository accRepo, ValidateFunction val, CommentRepository cmtRepo, ProductRepository prdRepo) {
        this.accRepo = accRepo;
        this.val = val;
        this.cmtRepo = cmtRepo;
        this.prdRepo = prdRepo;
    }

    @GetMapping("/profile")
    public String profile(Model model, HttpSession session) {
        if (session.getAttribute("account") == null) {
            return "redirect:/login";
        }
        model.addAttribute("page", "Profile");
        model.addAttribute("title", "Thông tin tài khoản");
        Account userAcc = (Account) session.getAttribute("account");

        model.addAttribute("userAcc", userAcc);

        System.out.print(userAcc.getEmail());
        model.addAttribute("account", new Account());
        
        ArrayList<Comment> cmt = cmtRepo.getCmtViaAcc(userAcc.getId());
        ArrayList<Product> prdCmtList = new ArrayList<>();
        for(int i = 0; i < cmt.size(); i++){
            prdCmtList.add(prdRepo.getProductViaCmt(cmt.get(i).getId()));
        } 

        model.addAttribute("cmtList", cmt);
        model.addAttribute("prdCmt", prdCmtList);

        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(Account account, Model model, HttpSession session) {
        if (session.getAttribute("account") == null) {
            return "redirect:/login";
        }
        Account userAcc = (Account) session.getAttribute("account");

        System.out.print(account.getName());

        boolean rs = false;
        account.setAvatar(userAcc.getAvatar());
        account.setRole(userAcc.getRole());
        account.setId(userAcc.getId());
    
        model.addAttribute("account", account);
        
        ArrayList<Account> accList = accRepo.findAccount(account.getEmail(), account.getPassword());
        
        if(accList.size() == 0){
            model.addAttribute("msg", "pw_error");
        }else{
            if (!val.specialKey(account.getName()) || val.isContainsNumber(account.getName())) {
                model.addAttribute("msg", "name");
            } else if (account.getName().length() > 50) {
                model.addAttribute("msg", "longName");
            }  else if (!val.specialKey(account.getIdcard())) {
                model.addAttribute("msg", "cccd");
            } else if (!val.verifyId(account.getIdcard())) {
                model.addAttribute("msg", "cccdWord");
            }  else {
                ArrayList<Account> accList2 = accRepo.findAccountByIdcard(account.getIdcard());
    
                if (accList2.size() != 0) {
                    model.addAttribute("msg", "dupEmail");
                } else {
                    accRepo.save(account);
                    session.setAttribute("account", account);
                    rs = true;
                }
            }
        }

        if (rs) {
            model.addAttribute("msg", "success");
        }
        
        return "profile";
    }
    
    @GetMapping("/admin/user")
    public String user(Model model, HttpSession session) {
        if (session.getAttribute("account") == null) {
            return "redirect:/admin/login";
        }else if( ((Account) session.getAttribute("account")).getRole() == 0){
            return "redirect:/";
        }

        model.addAttribute("page", "Member");
        model.addAttribute("title", "Thông tin tài khoản");

        ArrayList<Account> userAcc = accRepo.getAll();

        model.addAttribute("userAcc", userAcc);

        return "user_admin";
    }
}
