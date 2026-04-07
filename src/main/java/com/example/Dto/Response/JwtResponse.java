package com.example.Dto.Response;

import lombok.Data;

@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Integer id;
    private String userName;
    private String fullname;
    private String role;

    // Constructor để tạo nhanh đối tượng trả về
    public JwtResponse(String accessToken, Integer id, String userName, String fullname, String role) {
        this.token = accessToken;
        this.id = id;
        this.userName = userName;
        this.fullname = fullname;
        this.role = role;
    }
}