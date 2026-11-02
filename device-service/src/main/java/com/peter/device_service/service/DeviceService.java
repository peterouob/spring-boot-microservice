package com.peter.device_service.service;

import com.peter.device_service.dto.DeviceDto;
import com.peter.device_service.entity.Device;
import com.peter.device_service.exception.DeviceNotFoundException;
import com.peter.device_service.repository.DeviceRepository;
import org.springframework.stereotype.Service;

@Service
public class DeviceService {
    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public DeviceDto createDevice(DeviceDto deviceDto) {
        Device device = Device.builder()
                .name(deviceDto.getName())
                .type(deviceDto.getType())
                .location(deviceDto.getLocation())
                .user_id(deviceDto.getUser_id())
                .build();
        return mapToDto(deviceRepository.save(device));
    }

    public DeviceDto getDeviceById(Long id) {
        return mapToDto(findDevice(id));
    }

    public void updateDevice(Long id, DeviceDto deviceDto) {
        Device device = findDevice(id);
        device.setName(deviceDto.getName());
        device.setType(deviceDto.getType());
        device.setLocation(deviceDto.getLocation());
        device.setUser_id(deviceDto.getUser_id());
        deviceRepository.save(device);
    }

    public void deleteDevice(Long id) {
        deviceRepository.delete(findDevice(id));
    }

    private Device findDevice(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found with id: " + id));
    }

    private DeviceDto mapToDto(Device device) {
        DeviceDto deviceDto = new DeviceDto();
        deviceDto.setId(device.getId());
        deviceDto.setName(device.getName());
        deviceDto.setLocation(device.getLocation());
        deviceDto.setType(device.getType());
        deviceDto.setUser_id(device.getUser_id());
        return deviceDto;
    }
}
