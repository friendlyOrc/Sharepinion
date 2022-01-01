package sharepinion.sharepinion.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import sharepinion.sharepinion.data.ProductRepository;
import sharepinion.sharepinion.data.SubCategoryRepository;
import sharepinion.sharepinion.data.AccountRepository;
import sharepinion.sharepinion.data.AttributeRepository;
import sharepinion.sharepinion.data.CommentRepository;
import sharepinion.sharepinion.data.FileUploadUtil;
import sharepinion.sharepinion.data.ProductImageRepository;
import sharepinion.sharepinion.model.Account;
import sharepinion.sharepinion.model.Attribute;
import sharepinion.sharepinion.model.Comment;
import sharepinion.sharepinion.model.Product;
import sharepinion.sharepinion.model.ProductImage;
import sharepinion.sharepinion.model.SubCategory;

@Controller
public class ProductController {

    private final AccountRepository accRepo;
    private final ProductRepository prdRepo;
    private final ProductImageRepository prdImgRepo;
    private final CommentRepository cmtRepo;
    private final AttributeRepository attrRepo;
    private final SubCategoryRepository subCateRepo;
    
    @Autowired
    public ProductController(Environment env, AccountRepository accRepo, ProductRepository prdRepo, 
                            ProductImageRepository prdImgRepo, CommentRepository cmtRepo, AttributeRepository attrRepo,
                            SubCategoryRepository subCateRepo) {
        this.prdRepo = prdRepo;
        this.prdImgRepo = prdImgRepo;
        this.cmtRepo = cmtRepo;
        this.accRepo = accRepo;
        this.attrRepo = attrRepo;
        this.subCateRepo = subCateRepo;
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

        ArrayList<Comment> cmtUser = new ArrayList<>();
        ArrayList<Comment> cmtShopee = new ArrayList<>();
        ArrayList<Comment> cmtLazada = new ArrayList<>();
        ArrayList<Comment> cmtTiki = new ArrayList<>();

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

            if(cmt.get(i).getAccount().getId() == -99999){
                cmtShopee.add(cmt.get(i));
            }else if(cmt.get(i).getAccount().getId() == -99991){
                cmtLazada.add(cmt.get(i));
            }else if(cmt.get(i).getAccount().getId() == -99992){
                cmtTiki.add(cmt.get(i));
            }else{
                cmtUser.add(cmt.get(i));
            }
        }


        ArrayList<Product> sameCate = prdRepo.findSameCate(prd.getSubCategory().getId(), id, 5);
        for(int i = 0; i < sameCate.size(); i++){
            ArrayList<ProductImage> img = prdImgRepo.getImage(sameCate.get(i).getId());
            sameCate.get(i).setImages(img);
        }
        
        model.addAttribute("cmt", cmt);
        model.addAttribute("cmtList", cmtUser);
        model.addAttribute("cmtShopeeList", cmtShopee);
        model.addAttribute("cmtLazadaList", cmtLazada);
        model.addAttribute("cmtTitkiList", cmtTiki);
        
        model.addAttribute("prd", prd);
        model.addAttribute("cmtPos", cmtPos);
        model.addAttribute("cmtNeg", cmtNeg);
        model.addAttribute("attrPos", (attrPos.size() > 10)? attrPos.subList(0, 9) : attrPos);
        model.addAttribute("attrNeg", (attrNeg.size() > 10)? attrNeg.subList(0, 9) : attrNeg);
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
        else if(labelRs.contains("Negative")){
            comment.setLabel(-1);
        } else{
            comment.setLabel(0);
        }

        cmtRepo.save(comment);

        for(int i = 0; i < root.get("attr").size(); i++){
            Attribute attr = new Attribute();
            String content = root.get("attr").get(i).toString();
            if(content.length() > 2){
                System.out.println(content);
                attr.setContent(content.substring(1, content.length() - 1));
                attr.setDisplay(1);
                attr.setCommentid(comment.getId());
                attrRepo.save(attr);
            }
        
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
                    attribute.setContent(attribute.getContent().toLowerCase());
                    attrPos1.add(attribute);
                }
            } 
            else{
                cmtNeg1.add(cmt1.get(i));
                for (Attribute attribute : attr) {
                    attribute.setContent(attribute.getContent().toLowerCase());
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
                    attribute.setContent(attribute.getContent().toLowerCase());
                    attrPos2.add(attribute);
                }
            } 
            else{
                cmtNeg2.add(cmt2.get(i));
                for (Attribute attribute : attr) {
                    attribute.setContent(attribute.getContent().toLowerCase());
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
            }else if(attrPos1.get(i).getContent().contains("bền")){
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
            }else if(attrPos2.get(i).getContent().contains("bền")){
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
            }else if(attrNeg1.get(i).getContent().contains("bền")){
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
            }else if(attrNeg2.get(i).getContent().contains("bền")){
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

        model.addAttribute("attrPos1", (attrPos1.size() > 10)? attrPos1.subList(0, 9) : attrPos1);
        model.addAttribute("attrNeg1", (attrNeg1.size() > 10)? attrNeg1.subList(0, 9) : attrNeg1);
        model.addAttribute("attrPos2", (attrPos2.size() > 10)? attrPos2.subList(0, 9) : attrPos2);
        model.addAttribute("attrNeg2", (attrNeg2.size() > 10)? attrNeg2.subList(0, 9) : attrNeg2);

        
        model.addAttribute("attrScreenPos1", (attrScreenPos1.size() > 10)? attrScreenPos1.subList(0, 9) : attrScreenPos1);
        model.addAttribute("attrScreenPos2", (attrScreenPos2.size() > 10)? attrScreenPos2.subList(0, 9) : attrScreenPos2);
        model.addAttribute("attrBattPos1", (attrBattPos1.size() > 10)? attrBattPos1.subList(0, 9) : attrBattPos1);
        model.addAttribute("attrBattPos2", (attrBattPos2.size() > 10)? attrBattPos2.subList(0, 9) : attrBattPos2);
        model.addAttribute("attrEndPos1", (attrEndPos1.size() > 10)? attrEndPos1.subList(0, 9) : attrEndPos1);
        model.addAttribute("attrEndPos2", (attrEndPos2.size() > 10)? attrEndPos2.subList(0, 9) : attrEndPos2);
        model.addAttribute("attrMemPos1", (attrMemPos1.size() > 10)? attrMemPos1.subList(0, 9) : attrMemPos1);
        model.addAttribute("attrMemPos2", (attrMemPos2.size() > 10)? attrMemPos2.subList(0, 9) : attrMemPos2);
        model.addAttribute("attrOtherPos1", (attrOtherPos1.size() > 10)? attrOtherPos1.subList(0, 9) : attrOtherPos1);
        model.addAttribute("attrOtherPos2", (attrOtherPos2.size() > 10)? attrOtherPos2.subList(0, 9) : attrOtherPos2);

        model.addAttribute("attrScreenNeg1", (attrScreenNeg1.size() > 10)? attrScreenNeg1.subList(0, 9) : attrScreenNeg1);
        model.addAttribute("attrScreenNeg2", (attrScreenNeg2.size() > 10)? attrScreenNeg2.subList(0, 9) : attrScreenNeg2);
        model.addAttribute("attrBattNeg1", (attrBattNeg1.size() > 10)? attrBattNeg1.subList(0, 9) : attrBattNeg1);
        model.addAttribute("attrBattNeg2", (attrBattNeg2.size() > 10)? attrBattNeg2.subList(0, 9) : attrBattNeg2);
        model.addAttribute("attrEndNeg1", (attrEndNeg1.size() > 10)? attrEndNeg1.subList(0, 9) : attrEndNeg1);
        model.addAttribute("attrEndNeg2", (attrEndNeg2.size() > 10)? attrEndNeg2.subList(0, 9) : attrEndNeg2);
        model.addAttribute("attrMemNeg1", (attrMemNeg1.size() > 10)? attrMemNeg1.subList(0, 9) : attrMemNeg1);
        model.addAttribute("attrMemNeg2", (attrMemNeg2.size() > 10)? attrMemNeg2.subList(0, 9) : attrMemNeg2);
        model.addAttribute("attrOtherNeg1", (attrOtherNeg1.size() > 10)? attrOtherNeg1.subList(0, 9) : attrOtherNeg1);
        model.addAttribute("attrOtherNeg2", (attrOtherNeg2.size() > 10)? attrOtherNeg2.subList(0, 9) : attrOtherNeg2);



        return "compare";
    }

    @GetMapping("/admin/product")
    public String prdAdmin( @RequestParam(required = false, name="msg") String msg, Model model, HttpSession session) {
        if (session.getAttribute("account") == null) {
            return "redirect:/admin/login";
        }

        model.addAttribute("page", "Product");
        model.addAttribute("title", "Quản lý sản phẩm");
        if(msg != null){
            model.addAttribute("msg", msg);
        }

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

        return "product_admin";

    }

    @GetMapping("/admin/add_product")
    public String addPrdAdmin(Model model, HttpSession session){
        if (session.getAttribute("account") == null) {
            return "redirect:/admin/login";
        }

        model.addAttribute("page", "Product");
        model.addAttribute("title", "Quản lý sản phẩm");

        ArrayList<SubCategory> subList = subCateRepo.findAllSubCategory();

        model.addAttribute("product", new Product());
        // model.addAttribute("subCategory", new SubCategory());
        model.addAttribute("images", new ArrayList<ProductImage>());
        model.addAttribute("subCate", subList);

        return "add_prd";
    }

    @PostMapping("/admin/add_product")
    public String addPrdAdminPost(@RequestParam("prd_images") List<MultipartFile> multipartFile, Product product, SubCategory subCate, Model model, HttpSession session) throws IOException{
        if (session.getAttribute("account") == null) {
            return "redirect:/admin/login";
        }
        try{
            int prdImgBaseID = prdImgRepo.getHighestID();
            int prdBaseID = prdRepo.getHighestID();
    
    
            // product.setImages(prdImg);
            product.setId(prdBaseID + 1);
            SubCategory sub = subCateRepo.findById(product.getSubCategory().getId());
    
            product.setSubCategory(sub);
    
            prdRepo.save(product);
            
            for(int i = 0; i < multipartFile.size(); i++){
                String fileName = StringUtils.cleanPath(multipartFile.get(i).getOriginalFilename());
                FileUploadUtil.saveFile("src/main/resources/static/img", fileName, multipartFile.get(i));
                prdImgRepo.save(new ProductImage(prdImgBaseID + 1 + i, fileName, prdBaseID + 1));
                System.out.println(fileName);
            }
    
            return "redirect:/admin/product?msg=added";
        }catch(Exception e){
            e.printStackTrace();
            model.addAttribute("msg", "fail");
            return "add_prd";
        }
    }

    @GetMapping("/admin/add_cmt/{id}")
    public String addCmtAdmin(@PathVariable int id, Model model, HttpSession session){
        if (session.getAttribute("account") == null) {
            return "redirect:/admin/login";
        }

        model.addAttribute("page", "Product");
        model.addAttribute("title", "Thêm đánh giá");

        return "add_prd_cmt";
    }

    @PostMapping("/admin/add_cmt/{id}")
    public String addCmtAdminPOST(@RequestParam("src") int type, @RequestParam("prd_cmt") MultipartFile file, @PathVariable int id, Model model, HttpSession session) throws IOException{
        if (session.getAttribute("account") == null) {
            return "redirect:/admin/login";
        }
        int srcID = -99999;
        if(type == 2){
            srcID = -99991;
        }else if(type == 3){
            srcID = -99992;
        }
        // FileInputStream inputStream = new FileInputStream();

		Workbook workbook = new XSSFWorkbook(file.getInputStream());

		// Read sheet inside the workbook by its name   

		Sheet sheet = workbook.getSheet("Sheet1"); 
        int rowCount = sheet.getLastRowNum() - sheet.getFirstRowNum();
		

        System.out.println("================" + rowCount);
		// Create a loop over all the rows of excel file to read it

		for (int i = 0; i < rowCount + 1; i++) {

			Row row = sheet.getRow(i);

			// Create a loop to print cell values in a row
            Comment cmt = new Comment();
            Account acc = accRepo.findOneAccount(srcID);
            System.out.print(row.getCell(0).getStringCellValue());
            cmt.setAccount(acc);
            cmt.setId(cmtRepo.getHighestID() + 1);
            cmt.setContent(row.getCell(0).getStringCellValue());
            cmt.setProduct(prdRepo.findProduct(id));
            cmt.setLabel(0);
            cmt.setCmtdate(new java.sql.Date(Calendar.getInstance().getTime().getTime()));
            cmt.setCmttime(new java.sql.Time(Calendar.getInstance().getTime().getTime()));

            cmtRepo.save(cmt);
            
            //CALL API SECTION
            String apURL = "http://127.0.0.1:8000/api/classify";

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            JSONObject commentObject = new JSONObject();

            commentObject.put("content", cmt.getContent());
            commentObject.put("label", "none");
            commentObject.put("attr", new JSONArray().put(new Object()));

            HttpEntity<String> request = 
            new HttpEntity<String>(commentObject.toString(), headers);
            
            String cmtResultAsJsonStr = restTemplate.postForObject(apURL, request, String.class);

            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode root = objectMapper.readTree(cmtResultAsJsonStr);
            String labelRs = root.get("label").toString();

            if(labelRs.contains("Positive"))
                cmt.setLabel(1);
            else cmt.setLabel(-1);

            cmtRepo.save(cmt);

            for(int j = 0; j < root.get("attr").size(); j++){
                Attribute attr = new Attribute();
                String content = root.get("attr").get(j).toString();
                if(content.length() > 2){
                    attr.setContent(content.substring(1, content.length() - 1));
                    attr.setDisplay(1);
                    attr.setCommentid(cmt.getId());
                    attrRepo.save(attr);
                }
            }
		}
        
        return "redirect:/admin/product?msg=cmt_added";

    }

    @GetMapping("/admin/comment")
    public String commentAdmin(@RequestParam(required = false, name="msg") String msg, Model model, HttpSession session) throws JsonMappingException, JsonProcessingException {
        if (session.getAttribute("account") == null) {
            return "redirect:/admin/login";
        }

        model.addAttribute("page", "Comment");
        model.addAttribute("title", "Quản lý đánh giá ");

        if(msg != null){
            model.addAttribute("msg", msg);
        }
        //CALL API SECTION
        String apURL = "http://127.0.0.1:8000/api/cmtnum";

        RestTemplate restTemplate = new RestTemplate();

        String cmtResultAsJsonStr = restTemplate.getForObject(apURL, String.class);
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode root = objectMapper.readTree(cmtResultAsJsonStr);
        int sum = Integer.parseInt(root.get("sum").toString());

        ArrayList<Comment> cmtList = cmtRepo.getAllCmt();

        for(int i = 0; i < cmtList.size(); i++){
            cmtList.get(i).setAccount(accRepo.findOneAccount(cmtList.get(i).getAccount().getId()));
            cmtList.get(i).setProduct(prdRepo.findProduct(cmtList.get(i).getProduct().getId()));
        }
        
        ArrayList<Comment> nonTrained = cmtRepo.getNonTrainedCmt();

        System.out.print(cmtList.size());
        model.addAttribute("cmtList", cmtList);
        model.addAttribute("sum", sum);
        model.addAttribute("nontrained", nonTrained);

        return "comment_admin";

    }

    @GetMapping("/product/search")
    public String searchPrd(@RequestParam("prdsearch") String search, Model model, HttpSession session){
        
        model.addAttribute("page", "ProductList");
        model.addAttribute("title", "Kết quả tìm kiếm " + search);

        ArrayList<Product> prdList = prdRepo.findProductByName(search);
        
        ArrayList<Integer> cmtList = new ArrayList<>();
        for(int i = 0; i < prdList.size(); i++){
            ArrayList<ProductImage> images = prdImgRepo.getImage(prdList.get(i).getId());
            cmtList.add(cmtRepo.getCmtNumber(prdList.get(i).getId()));
            prdList.get(i).setImages(images);
        }
        
        System.out.print(prdList.size());
        model.addAttribute("prdList", prdList);
        model.addAttribute("cmtList", cmtList);
        model.addAttribute("search", search);

        return "productlist";
    }

}
