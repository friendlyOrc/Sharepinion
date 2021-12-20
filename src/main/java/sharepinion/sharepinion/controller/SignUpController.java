package sharepinion.sharepinion.controller;

import java.util.ArrayList;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import sharepinion.sharepinion.data.AccountRepository;
import sharepinion.sharepinion.model.Account;


@Controller
public class SignUpController {

    private final AccountRepository accRepo;
    private ValidateFunction val;

    @Autowired
    public SignUpController(Environment env, AccountRepository accRepo, ValidateFunction val) {
        this.accRepo = accRepo;
        this.val = val;
    }

    @GetMapping("/signup")
    public String members(Model model, HttpSession session) {
        model.addAttribute("page", "Signup");
        model.addAttribute("title", "Đăng kí thành viên mới");

        model.addAttribute("account", new Account());

        return "signup";
    }

    @PostMapping("/signup")
    public String postMember(Account account, Model model, HttpSession session) {

        boolean rs = false;
        
        model.addAttribute("account", account);
        
        if (!val.specialKey(account.getName()) || val.isContainsNumber(account.getName())) {
            model.addAttribute("msg", "name");
        } else if (account.getName().length() > 50) {
            model.addAttribute("msg", "longName");
        }  else if (!val.specialKey(account.getIdcard())) {
            model.addAttribute("msg", "cccd");
        } else if (!val.verifyId(account.getIdcard())) {
            model.addAttribute("msg", "cccdWord");
        }  else if (!val.validateEmail(account.getEmail())) {
            model.addAttribute("msg", "email");
        } else {
            ArrayList<Account> accList = accRepo.findAccountByIdcard(account.getIdcard());
            ArrayList<Account> accList2 = accRepo.findAccountByEmail(account.getEmail());

            if (accList2.size() != 0) {
                model.addAttribute("msg", "dupEmail");
            } else if (accList.size() != 0){
                model.addAttribute("msg", "dup");
            }else {


                account.setRole(1);
                account.setAvatar("img.jpg");

                accRepo.save(account);
                rs = true;
            }
        }

        if (rs) {
            model.addAttribute("page", "Login");

            model.addAttribute("title", "Đăng nhập");
            return "redirect:/login?msg=success";
        } else {
            return "signup";
        }
    }
}
