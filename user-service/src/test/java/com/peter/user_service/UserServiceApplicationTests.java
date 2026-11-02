package com.peter.user_service;

import com.peter.user_service.entity.User;
import com.peter.user_service.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class UserServiceApplicationTests {

	@Autowired
	private UserRepository userRepository;

	@Test
	void contextLoads() {
	}

	@Disabled
	@Test
	void createUser() {
		for (int i=1;i<=200;i++) {
			User user = User.builder()
					.name("User"+i)
					.surname("Surname"+i)
					.email("user"+i+"@gmail.com")
					.address(i+"Example St")
					.alerting(i % 2 ==0)
					.energyAlertingThreshold(1000.0+i)
					.build();
			userRepository.save(user);
		}
		log.info("UserRepository has been populated");
	}
}
