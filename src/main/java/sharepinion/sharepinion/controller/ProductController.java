package sharepinion.sharepinion.controller;

import java.util.ArrayList;
import java.util.Calendar;

import javax.servlet.http.HttpSession;


import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;



import sharepinion.sharepinion.data.ProductRepository;
import sharepinion.sharepinion.data.AccountRepository;
import sharepinion.sharepinion.data.AttributeRepository;
import sharepinion.sharepinion.data.CommentRepository;
import sharepinion.sharepinion.data.ProductImageRepository;
import sharepinion.sharepinion.model.Account;
import sharepinion.sharepinion.model.Attribute;
import sharepinion.sharepinion.model.Comment;
import sharepinion.sharepinion.model.Product;
import sharepinion.sharepinion.model.ProductImage;

@Controller
public class ProductController {

    private final AccountRepository accRepo;
    private final ProductRepository prdRepo;
    private final ProductImageRepository prdImgRepo;
    private final CommentRepository cmtRepo;
    private final AttributeRepository attrRepo;
    
    @Autowired
    public ProductController(Environment env, AccountRepository accRepo, ProductRepository prdRepo, ProductImageRepository prdImgRepo, CommentRepository cmtRepo, AttributeRepository attrRepo) {
        this.prdRepo = prdRepo;
        this.prdImgRepo = prdImgRepo;
        this.cmtRepo = cmtRepo;
        this.accRepo = accRepo;
        this.attrRepo = attrRepo;
    }

    @GetMapping("/product/{id}")
    public String home(@PathVariable int id, Model model, HttpSession session) {

        model.addAttribute("page", "Product");
        model.addAttribute("title", "Trang chủ");

        Product prd = prdRepo.findProduct(id);
        ArrayList<ProductImage> images = prdImgRepo.getImage(id);
        prd.setImages(images);

        model.addAttribute("comment", new Comment());

        ArrayList<Comment> cmt = cmtRepo.getCmtViaPrd(id);

        ArrayList<Comment> cmtPos = new ArrayList<>();
        ArrayList<Comment> cmtNeg = new ArrayList<>();
        ArrayList<Attribute> attrPos = new ArrayList<>();
        ArrayList<Attribute> attrNeg = new ArrayList<>();

        for(int i = 0; i < cmt.size(); i++){
            Account a = accRepo.findAccountByComment(cmt.get(i).getId());
            cmt.get(i).setAccount(a);

            ArrayList<Attribute> attr = attrRepo.getCmtAttr(cmt.get(i).getId());
            cmt.get(i).setAttributes(attr);
            
            if(cmt.get(i).getLabel() == 1){
                cmtPos.add(cmt.get(i));
                for (Attribute attribute : attr) {
                    attrPos.add(attribute);
                }
            } 
            else{
                cmtNeg.add(cmt.get(i));
                for (Attribute attribute : attr) {
                    attrNeg.add(attribute);
                }
            } 
        }


        ArrayList<Product> sameCate = prdRepo.findSameCate(prd.getSubCategory().getId(), id, 5);
        for(int i = 0; i < sameCate.size(); i++){
            ArrayList<ProductImage> img = prdImgRepo.getImage(sameCate.get(i).getId());
            sameCate.get(i).setImages(img);
        }
        
        model.addAttribute("cmtList", cmt);
        model.addAttribute("prd", prd);
        model.addAttribute("cmtPos", cmtPos);
        model.addAttribute("cmtNeg", cmtNeg);
        model.addAttribute("attrPos", (attrPos.size() > 10)? attrPos.subList(0, 10) : attrPos);
        model.addAttribute("attrNeg", (attrNeg.size() > 10)? attrNeg.subList(0, 10) : attrNeg);
        model.addAttribute("sameCate", sameCate);
        return "product";
    }

    @PostMapping("/product/{id}")
    public String comment(@PathVariable int id, Comment comment, Model model, HttpSession session) throws JsonMappingException, JsonProcessingException {

        if (session.getAttribute("account") == null) {
            return "redirect:/login";
        }

        comment.setAccount((Account)session.getAttribute("account"));

        Product prd = prdRepo.findProduct(id);
        
        comment.setProduct(prd);
        comment.setCmtdate(new java.sql.Date(Calendar.getInstance().getTime().getTime()));
        comment.setCmttime(new java.sql.Time(Calendar.getInstance().getTime().getTime()));

        prd.setLastcmtdate(comment.getCmtdate());
        prd.setLastcmttime(comment.getCmttime());
        prdRepo.save(prd);

        comment.setLabel(0);
        comment.setId(cmtRepo.getHighestID() + 1);
        long pre = cmtRepo.count();

        cmtRepo.save(comment);

        //CALL API SECTION
        String apURL = "http://127.0.0.1:8000/api/classify";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject commentObject = new JSONObject();

        commentObject.put("content", comment.getContent());
        commentObject.put("label", "none");
        commentObject.put("attr", new JSONArray().put(new Object()));

        HttpEntity<String> request = 
        new HttpEntity<String>(commentObject.toString(), headers);
        
        String cmtResultAsJsonStr = restTemplate.postForObject(apURL, request, String.class);

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode root = objectMapper.readTree(cmtResultAsJsonStr);
        String labelRs = root.get("label").toString();

        if(labelRs.contains("Positive"))
            comment.setLabel(1);
        else comment.setLabel(-1);

        cmtRepo.save(comment);

        for(int i = 0; i < root.get("attr").size(); i++){
            Attribute attr = new Attribute();
            String content = root.get("attr").get(i).toString();
            attr.setContent(content.substring(1, content.length() - 1));
            attr.setDisplay(1);
            attr.setCommentid(comment.getId());
            attrRepo.save(attr);
        
        }
		// System.out.println(root.get("attr").get(0));

        
        if(pre >= cmtRepo.count()){
            return "redirect:/product/" + id + "?cmt=error";
        }else {
            return "redirect:/product/" + id + "?cmt=ok";
        }
    }


    @GetMapping("/compare/{id1}/{id2}")
    public String compare(@PathVariable int id1,@PathVariable int id2, Model model, HttpSession session) {

        model.addAttribute("page", "Compare");
        model.addAttribute("title", "Trang chủ");

        Product prd1 = prdRepo.findProduct(id1);
        Product prd2 = prdRepo.findProduct(id2);

        ArrayList<ProductImage> images1 = prdImgRepo.getImage(id1);
        ArrayList<ProductImage> images2 = prdImgRepo.getImage(id2);

        prd1.setImages(images1);
        prd2.setImages(images2);

        ArrayList<Comment> cmt1 = cmtRepo.getCmtViaPrd(id1);
        ArrayList<Comment> cmt2 = cmtRepo.getCmtViaPrd(id2);

        ArrayList<Comment> cmtPos1 = new ArrayList<>();
        ArrayList<Comment> cmtNeg1 = new ArrayList<>();
        ArrayList<Attribute> attrPos1 = new ArrayList<>();
        ArrayList<Attribute> attrNeg1 = new ArrayList<>();

        ArrayList<Comment> cmtPos2 = new ArrayList<>();
        ArrayList<Comment> cmtNeg2 = new ArrayList<>();
        ArrayList<Attribute> attrPos2 = new ArrayList<>();
        ArrayList<Attribute> attrNeg2 = new ArrayList<>();

        for(int i = 0; i < cmt1.size(); i++){
            Account a = accRepo.findAccountByComment(cmt1.get(i).getId());
            cmt1.get(i).setAccount(a);

            ArrayList<Attribute> attr = attrRepo.getCmtAttr(cmt1.get(i).getId());
            cmt1.get(i).setAttributes(attr);
            
            if(cmt1.get(i).getLabel() == 1){
                cmtPos1.add(cmt1.get(i));
                for (Attribute attribute : attr) {
                    attrPos1.add(attribute);
                }
            } 
            else{
                cmtNeg1.add(cmt1.get(i));
                for (Attribute attribute : attr) {
                    attrNeg1.add(attribute);
                }
            } 
        }

        for(int i = 0; i < cmt2.size(); i++){
            Account a = accRepo.findAccountByComment(cmt2.get(i).getId());
            cmt2.get(i).setAccount(a);

            ArrayList<Attribute> attr = attrRepo.getCmtAttr(cmt2.get(i).getId());
            cmt2.get(i).setAttributes(attr);
            
            if(cmt2.get(i).getLabel() == 1){
                cmtPos2.add(cmt2.get(i));
                for (Attribute attribute : attr) {
                    attrPos2.add(attribute);
                }
            } 
            else{
                cmtNeg2.add(cmt2.get(i));
                for (Attribute attribute : attr) {
                    attrNeg2.add(attribute);
                }
            } 
        }

        ArrayList<String> attrScreenPos1 = new ArrayList<>();
        ArrayList<String> attrScreenPos2 = new ArrayList<>();
        ArrayList<String> attrBattPos1 = new ArrayList<>();
        ArrayList<String> attrBattPos2 = new ArrayList<>();
        ArrayList<String> attrEndPos1 = new ArrayList<>();
        ArrayList<String> attrEndPos2 = new ArrayList<>();
        ArrayList<String> attrMemPos1 = new ArrayList<>();
        ArrayList<String> attrMemPos2 = new ArrayList<>();
        ArrayList<String> attrOtherPos1 = new ArrayList<>();
        ArrayList<String> attrOtherPos2 = new ArrayList<>();

        ArrayList<String> attrScreenNeg1 = new ArrayList<>();
        ArrayList<String> attrScreenNeg2 = new ArrayList<>();
        ArrayList<String> attrBattNeg1 = new ArrayList<>();
        ArrayList<String> attrBattNeg2 = new ArrayList<>();
        ArrayList<String> attrEndNeg1 = new ArrayList<>();
        ArrayList<String> attrEndNeg2 = new ArrayList<>();
        ArrayList<String> attrMemNeg1 = new ArrayList<>();
        ArrayList<String> attrMemNeg2 = new ArrayList<>();
        ArrayList<String> attrOtherNeg1 = new ArrayList<>();
        ArrayList<String> attrOtherNeg2 = new ArrayList<>();
        
        for(int i = 0; i < attrPos1.size(); i++){
            if(attrPos1.get(i).getContent().contains("màn hình") || attrPos1.get(i).getContent().contains("man hinh")
            || attrPos1.get(i).getContent().contains("phân giải")){
                attrScreenPos1.add(attrPos1.get(i).getContent());
            }else if(attrPos1.get(i).getContent().contains("pin")){
                attrBattPos1.add(attrPos1.get(i).getContent());
            }else if(attrPos1.get(i).getContent().contains("dung lượng") || attrPos1.get(i).getContent().contains("bộ nhớ") 
                    || attrPos1.get(i).getContent().contains("dung luong") || attrPos1.get(i).getContent().contains("ram")){
                attrMemPos1.add(attrPos1.get(i).getContent());
            }else if(attrPos1.get(i).getContent().contains("độ bền")){
                attrEndPos1.add(attrPos1.get(i).getContent());
            }else{
                attrOtherPos1.add(attrPos1.get(i).getContent());
            }
        }

        for(int i = 0; i < attrPos2.size(); i++){
            if(attrPos2.get(i).getContent().contains("màn hình") || attrPos2.get(i).getContent().contains("man hinh")
            || attrPos2.get(i).getContent().contains("phân giải")){
                attrScreenPos2.add(attrPos2.get(i).getContent());
            }else if(attrPos2.get(i).getContent().contains("pin")){
                attrBattPos2.add(attrPos2.get(i).getContent());
            }else if(attrPos2.get(i).getContent().contains("dung lượng") || attrPos2.get(i).getContent().contains("bộ nhớ") 
                    || attrPos2.get(i).getContent().contains("dung luong") || attrPos2.get(i).getContent().contains("ram")){
                attrMemPos2.add(attrPos2.get(i).getContent());
            }else if(attrPos2.get(i).getContent().contains("độ bền")){
                attrEndPos2.add(attrPos2.get(i).getContent());
            }else{
                attrOtherPos2.add(attrPos2.get(i).getContent());
            }
        }

        
        for(int i = 0; i < attrNeg1.size(); i++){
            if(attrNeg1.get(i).getContent().contains("màn hình") || attrNeg1.get(i).getContent().contains("man hinh")
            || attrNeg1.get(i).getContent().contains("phân giải")){
                attrScreenNeg1.add(attrNeg1.get(i).getContent());
            }else if(attrNeg1.get(i).getContent().contains("pin")){
                attrBattNeg1.add(attrNeg1.get(i).getContent());
            }else if(attrNeg1.get(i).getContent().contains("dung lượng") || attrNeg1.get(i).getContent().contains("bộ nhớ") 
                    || attrNeg1.get(i).getContent().contains("dung luong") || attrNeg1.get(i).getContent().contains("ram")){
                attrMemNeg1.add(attrNeg1.get(i).getContent());
            }else if(attrNeg1.get(i).getContent().contains("độ bền")){
                attrEndNeg1.add(attrNeg1.get(i).getContent());
            }else{
                attrOtherNeg1.add(attrNeg1.get(i).getContent());
            }
        }

        
        for(int i = 0; i < attrNeg2.size(); i++){
            if(attrNeg2.get(i).getContent().contains("màn hình") || attrNeg2.get(i).getContent().contains("man hinh")
            || attrNeg2.get(i).getContent().contains("phân giải")){
                attrScreenNeg2.add(attrNeg2.get(i).getContent());
            }else if(attrNeg2.get(i).getContent().contains("pin")){
                attrBattNeg2.add(attrNeg2.get(i).getContent());
            }else if(attrNeg2.get(i).getContent().contains("dung lượng") || attrNeg2.get(i).getContent().contains("bộ nhớ") 
                    || attrNeg2.get(i).getContent().contains("dung luong") || attrNeg2.get(i).getContent().contains("ram")){
                attrMemNeg2.add(attrNeg2.get(i).getContent());
            }else if(attrNeg2.get(i).getContent().contains("độ bền")){
                attrEndNeg2.add(attrNeg2.get(i).getContent());
            }else{
                attrOtherNeg2.add(attrNeg2.get(i).getContent());
            }
        }


        
        model.addAttribute("cmtList1", cmt1);
        model.addAttribute("cmtList2", cmt2);
        model.addAttribute("prd1", prd1);
        model.addAttribute("prd2", prd2);
        model.addAttribute("cmtPos1", cmtPos1);
        model.addAttribute("cmtPos2", cmtPos2);
        model.addAttribute("cmtNeg1", cmtNeg1);
        model.addAttribute("cmtNeg2", cmtNeg2);

        model.addAttribute("attrPos1", (attrPos1.size() > 10)? attrPos1.subList(0, 10) : attrPos1);
        model.addAttribute("attrNeg1", (attrNeg1.size() > 10)? attrNeg1.subList(0, 10) : attrNeg1);
        model.addAttribute("attrPos2", (attrPos2.size() > 10)? attrPos2.subList(0, 10) : attrPos2);
        model.addAttribute("attrNeg2", (attrNeg2.size() > 10)? attrNeg2.subList(0, 10) : attrNeg2);

        
        model.addAttribute("attrScreenPos1", (attrScreenPos1.size() > 10)? attrScreenPos1.subList(0, 10) : attrScreenPos1);
        model.addAttribute("attrScreenPos2", (attrNeg1.size() > 10)? attrScreenPos2.subList(0, 10) : attrScreenPos2);
        model.addAttribute("attrBattPos1", (attrBattPos1.size() > 10)? attrBattPos1.subList(0, 10) : attrBattPos1);
        model.addAttribute("attrBattPos2", (attrBattPos2.size() > 10)? attrBattPos2.subList(0, 10) : attrBattPos2);
        model.addAttribute("attrEndPos1", (attrEndPos1.size() > 10)? attrEndPos1.subList(0, 10) : attrEndPos1);
        model.addAttribute("attrEndPos2", (attrEndPos2.size() > 10)? attrEndPos2.subList(0, 10) : attrEndPos2);
        model.addAttribute("attrMemPos1", (attrMemPos1.size() > 10)? attrMemPos1.subList(0, 10) : attrMemPos1);
        model.addAttribute("attrMemPos2", (attrMemPos2.size() > 10)? attrMemPos2.subList(0, 10) : attrMemPos2);
        model.addAttribute("attrOtherPos1", (attrOtherPos1.size() > 10)? attrOtherPos1.subList(0, 10) : attrOtherPos1);
        model.addAttribute("attrOtherPos2", (attrOtherPos2.size() > 10)? attrOtherPos2.subList(0, 10) : attrOtherPos2);

        model.addAttribute("attrScreenNeg1", (attrScreenNeg1.size() > 10)? attrScreenNeg1.subList(0, 10) : attrScreenNeg1);
        model.addAttribute("attrScreenNeg2", (attrScreenNeg2.size() > 10)? attrScreenNeg2.subList(0, 10) : attrScreenNeg2);
        model.addAttribute("attrBattNeg1", (attrBattNeg1.size() > 10)? attrBattNeg1.subList(0, 10) : attrBattNeg1);
        model.addAttribute("attrBattNeg2", (attrBattNeg2.size() > 10)? attrBattNeg2.subList(0, 10) : attrBattNeg2);
        model.addAttribute("attrEndNeg1", (attrEndNeg1.size() > 10)? attrEndNeg1.subList(0, 10) : attrEndNeg1);
        model.addAttribute("attrEndNeg2", (attrEndNeg2.size() > 10)? attrEndNeg2.subList(0, 10) : attrEndNeg2);
        model.addAttribute("attrMemNeg1", (attrMemNeg1.size() > 10)? attrMemNeg1.subList(0, 10) : attrMemNeg1);
        model.addAttribute("attrMemNeg2", (attrMemNeg2.size() > 10)? attrMemNeg2.subList(0, 10) : attrMemNeg2);
        model.addAttribute("attrOtherNeg1", (attrOtherNeg1.size() > 10)? attrOtherNeg1.subList(0, 10) : attrOtherNeg1);
        model.addAttribute("attrOtherNeg2", (attrOtherNeg2.size() > 10)? attrOtherNeg2.subList(0, 10) : attrOtherNeg2);



        return "compare";
    }

}
