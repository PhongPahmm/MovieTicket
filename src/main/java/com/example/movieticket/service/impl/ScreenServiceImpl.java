package com.example.movieticket.service.impl;

import com.example.movieticket.dto.request.ScreenRequest;
import com.example.movieticket.dto.response.ScreenResponse;
import com.example.movieticket.exception.AppException;
import com.example.movieticket.exception.ErrorCode;
import com.example.movieticket.model.Screen;
import com.example.movieticket.repository.ScreenRepository;
import com.example.movieticket.service.ScreenService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ScreenServiceImpl implements ScreenService {
    ScreenRepository screenRepository;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ScreenResponse createScreen(ScreenRequest request) {
        var screen = Screen.builder()
                .name(request.getName())
                .totalSeats(request.getTotalSeats())
                .is3D(request.getIs3D())
                .active(true)
                .build();
        return mapToScreenResponse(screenRepository.save(screen));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ScreenResponse updateScreen(Integer screenId, ScreenRequest request) {
        var screen = screenRepository.findById(screenId)
                .orElseThrow(()-> new AppException(ErrorCode.SCREEN_NOT_FOUND));

        if(request.getName() != null) screen.setName(request.getName());
        if(request.getTotalSeats() != null) screen.setTotalSeats(request.getTotalSeats());
        if(request.getIs3D() != null) screen.setIs3D(request.getIs3D());
        if(request.getActive() != null) screen.setActive(request.getActive());

        return mapToScreenResponse(screenRepository.save(screen));
    }

    @Override
    public ScreenResponse getScreeById(Integer id) {
        var screen = screenRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.SCREEN_NOT_FOUND));

        return mapToScreenResponse(screen);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<ScreenResponse> getAllScreens() {
        return screenRepository.findAll()
                .stream().map(this::mapToScreenResponse)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ScreenResponse changeScreenStatus(Integer id, Boolean isActive) {
        var screen = screenRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.SCREEN_NOT_FOUND));
        screen.setActive(isActive);
        return mapToScreenResponse(screenRepository.save(screen));
    }

    private ScreenResponse mapToScreenResponse(Screen screen) {
        return ScreenResponse.builder()
                .id(screen.getId())
                .name(screen.getName())
                .totalSeats(screen.getTotalSeats())
                .is3D(screen.getIs3D())
                .createdAt(screen.getCreatedAt())
                .updatedAt(screen.getUpdatedAt())
                .active(screen.getActive())
                .build();
    }

}
