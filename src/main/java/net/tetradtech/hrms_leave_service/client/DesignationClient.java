package net.tetradtech.hrms_leave_service.client;

import net.tetradtech.hrms_leave_service.dto.DesignationDTO;
import net.tetradtech.hrms_leave_service.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DesignationClient {

    @Autowired
    private RestTemplate restTemplate;

    private final String BASE_URL = "http://localhost:8081/api/designations";

    public DesignationDTO getDesignationById(Long id) {
        try {
            ResponseEntity<ApiResponse<DesignationDTO>> response = restTemplate.exchange(
                    BASE_URL + "/" + id,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<DesignationDTO>>() {}
            );
            return response.getBody().getData();
        } catch (Exception e) {
            return null;
        }
    }
}