package com.example.movieticket.controller;


import com.example.movieticket.dto.request.UserCreationRequest;
import com.example.movieticket.dto.request.UserUpdateRequest;
import com.example.movieticket.dto.response.PageResponse;
import com.example.movieticket.dto.response.ResponseData;
import com.example.movieticket.dto.response.UserResponse;
import com.example.movieticket.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;
    @PostMapping("")
    public UserResponse createUser(@RequestBody UserCreationRequest request){
        return userService.createUser(request);
    }

    @PutMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseData<UserResponse> updateUser(@PathVariable("userId") int userId, @ModelAttribute UserUpdateRequest request){
        return ResponseData.<UserResponse>builder()
                .code(200)
                .message("User updated")
                .data(userService.updateUser(userId, request))
                .build();
    }
    @GetMapping("")
    public ResponseData<PageResponse<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        return ResponseData.<PageResponse<UserResponse>>builder()
                .code(200)
                .data(userService.getAllUsers(page, size))
                .message("Successfully retrieved all users")
                .build();
    }

    @GetMapping("/{userId}")
    public ResponseData<UserResponse> getUserById(@PathVariable int userId){
        return ResponseData.<UserResponse>builder()
                .code(200)
                .data(userService.getUserById(userId))
                .message("Successfully retrieved user")
                .build();
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable int userId){
        userService.deleteUser(userId);
    }
    @PutMapping("status/{userId}")
    public ResponseData<UserResponse> changeUserStatus(@PathVariable int userId, @RequestParam boolean isActive){
        return ResponseData.<UserResponse>builder()
                .code(200)
                .message("User status changed")
                .data(userService.changeUserStatus(userId, isActive))
                .build();
    }
}
