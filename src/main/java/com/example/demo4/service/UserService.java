package com.example.demo4.service;

import com.example.demo4.dto.request.PaginationRequest;
import com.example.demo4.dto.request.UpdateUserRequest;
import com.example.demo4.dto.request.UserFilterDTO;
import com.example.demo4.dto.response.PagedResponse;
import com.example.demo4.dto.response.UserDTO;
import com.example.demo4.dto.response.UserResponse;

public interface UserService {

    PagedResponse<UserResponse> getAllUsers(UserFilterDTO filter, PaginationRequest pagination);

    UserDTO getUserById(Long id);

    UserDTO updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);

    void restoreUser(Long id);
}