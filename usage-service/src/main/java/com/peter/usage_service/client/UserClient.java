package com.peter.usage_service.client;

import com.peter.usage_service.dto.UserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class UserClient {

    private final RestTemplate restTemplate;
    private final String userUrl;

    public UserClient(@Value("${user.service.url}") String userUrl) {
        this.restTemplate = new RestTemplate();
        this.userUrl = userUrl;
    }

    public UserDto getUserById(Long userId) {
        String url = UriComponentsBuilder
                .fromUriString(userUrl)
                .path("/{id}")
                .buildAndExpand(userId)
                .toUriString();

        ResponseEntity<UserDto> response = restTemplate.getForEntity(url, UserDto.class);
        return response.getBody();
    }
}
