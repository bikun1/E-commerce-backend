package com.example.ecommerce.backend.service;

import com.example.ecommerce.backend.dto.request.PaginationRequest;
import com.example.ecommerce.backend.dto.request.UpdateUserRequest;
import com.example.ecommerce.backend.dto.request.UserFilterDTO;
import com.example.ecommerce.backend.dto.response.PagedResponse;
import com.example.ecommerce.backend.dto.response.UserDTO;
import com.example.ecommerce.backend.dto.response.UserResponse;

public interface UserService {

    PagedResponse<UserResponse> getAllUsers(UserFilterDTO filter, PaginationRequest pagination);

    UserDTO getUserById(Long id);

    UserDTO updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);

    void restoreUser(Long id);
}