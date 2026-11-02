package com.peter.device_service;

import com.peter.device_service.entity.Device;
import com.peter.device_service.model.DeviceType;
import com.peter.device_service.repository.DeviceRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class DeviceServiceApplicationTests {

	public static final int NumberOfDevice = 200;
	public static final int Users = 10;
	@Autowired
	private DeviceRepository deviceRepository;

	@Test
	void contextLoads() {
	}

	@Disabled
	@Test
	void createDevice() {
		for (int i = 1; i<= NumberOfDevice; i++) {
			var device = Device.builder()
					.name("device"+i)
					.type(DeviceType.values()[i % DeviceType.values().length])
					.location("location"+ ((i%3)+1))
					.user_id((long)((i % Users)+1))
					.build();

			deviceRepository.save(device);
		}

		log.info("Device Repository has been populated");
	}
}
