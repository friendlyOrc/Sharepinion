package sharepinion.sharepinion.controller;

import java.util.ArrayList;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import sharepinion.sharepinion.data.AccountRepository;
import sharepinion.sharepinion.model.Account;


@Controller
public class LoginController {

    private final AccountRepository accRepo;
    private ValidateFunction val;

    @Autowired
    public LoginController(Environment env, AccountRepository accRepo, ValidateFunction val) {
        this.accRepo = accRepo;
        this.val = val;
    }

    @GetMapping("/login")
    public String login(Model model, HttpSession session,  @RequestParam(required = false, name="msg") String msg) {
        if (session.getAttribute("account") != null) {
            return "redirect:/";
        }

        model.addAttribute("account", new Account());
        model.addAttribute("page", "Login");
        model.addAttribute("title", "Đăng nhập");

        model.addAttribute("msg", msg);
        
        return "login";
    }

    @GetMapping("/logout")
    public String logout(Model model, HttpSession session) {
        session.removeAttribute("account");
        return "redirect:/";
    }

    @PostMapping("/login")
    public String postLogin(Account account, Model model, HttpSession session) {
        if (val.specialKey(account.getEmail())) {
            ArrayList<Account> accList = accRepo.findAccount(account.getEmail(), account.getPassword());
            if (accList.size() == 1) {
                session.setAttribute("account", accList.get(0));
                return "redirect:/";
            } else {
                accList = accRepo.findAccountByEmail(account.getEmail());
                if (accList.size() == 0) {
                    model.addAttribute("msg", "username");
                } else {
                    model.addAttribute("msg", "password");
                }
                model.addAttribute("title", "Đăng nhập");
                return "login";
            }
        } else {
            model.addAttribute("msg", "speUsername");
        }

        model.addAttribute("title", "Đăng nhập");
        return "login";

    }
}
