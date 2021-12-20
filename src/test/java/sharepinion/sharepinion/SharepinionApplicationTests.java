package sharepinion.sharepinion;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;


@SpringBootTest
class SharepinionApplicationTests {

	@Test
	void contextLoads() throws JSONException, JsonMappingException, JsonProcessingException {
		//CALL API SECTION
        String createPersonUrl = "http://127.0.0.1:8000/api/classify";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject personJsonObject = new JSONObject();
        personJsonObject.put("content", "Sản phẩm tệ");
        personJsonObject.put("label", "none");
        personJsonObject.put("attr", new JSONArray().put(new Object()));

        HttpEntity<String> request = 
        new HttpEntity<String>(personJsonObject.toString(), headers);
        
        String personResultAsJsonStr = restTemplate.postForObject(createPersonUrl, 
		request, String.class);

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode root = objectMapper.readTree(personResultAsJsonStr);

		assertNotNull(root);

		System.out.println(root);
	}
	

}
