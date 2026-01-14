package com.example.demo4.controller;

import com.example.demo4.dto.request.PaginationRequest;
import com.example.demo4.dto.request.UpdateUserRequest;
import com.example.demo4.dto.request.UserFilterDTO;
import com.example.demo4.dto.response.ApiResponse;
import com.example.demo4.dto.response.PagedResponse;
import com.example.demo4.dto.response.UserDTO;
import com.example.demo4.dto.response.UserResponse;
import com.example.demo4.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management APIs")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Returns paginated users with optional filters")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllUsers(
            @ModelAttribute UserFilterDTO filter,
            @RequestParam(defaultValue = PaginationRequest.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = PaginationRequest.DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = PaginationRequest.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = PaginationRequest.DEFAULT_SORT_DIR) String sortDir) {

        var result = userService.getAllUsers(filter, new PaginationRequest(page, size, sortBy, sortDir));
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MODERATOR', 'ADMIN')")
    @Operation(summary = "Get user by id", description = "Returns user details by id")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("User retrieved successfully", userService.getUserById(id)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user", description = "Updates user profile and role information")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("User updated successfully", userService.updateUser(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Soft deletes user account by id")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Restore user", description = "Restores a previously deleted user account")
    public ResponseEntity<ApiResponse<Void>> restoreUser(@PathVariable Long id) {
        userService.restoreUser(id);
        return ResponseEntity.ok(ApiResponse.success("User restored successfully"));
    }
}