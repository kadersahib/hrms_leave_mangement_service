package net.tetradtech.hrms_leave_service.client;

import net.tetradtech.hrms_leave_service.dto.UserDTO;
import net.tetradtech.hrms_leave_service.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class UserServiceClient {

    @Autowired
    private RestTemplate restTemplate;

    private final String BASE_URL = "http://localhost:8080/api/users";

    public UserDTO getUserById(Long id) {
        try {
            ResponseEntity<ApiResponse<UserDTO>> response = restTemplate.exchange(
                    BASE_URL + "/" + id,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<UserDTO>>() {}
            );
            return response.getBody().getData();
        } catch (HttpClientErrorException.NotFound e) {
            // Return null so service layer can throw a custom exception
            return null;
        }
    }
    public List<UserDTO> getAllUsers() {
        try {
            ResponseEntity<ApiResponse<List<UserDTO>>> response = restTemplate.exchange(
                    BASE_URL,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<List<UserDTO>>>() {}
            );
            return response.getBody().getData();
        } catch (Exception e) {
            System.out.println("Error fetching all users: " + e.getMessage());
            return List.of();
        }
    }

}

