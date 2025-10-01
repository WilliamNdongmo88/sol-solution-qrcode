package will.dev.qrcodeApp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import will.dev.qrcodeApp.dto.CreateUserRequest;
import will.dev.qrcodeApp.dto.QrCodeMetadataDto;
import will.dev.qrcodeApp.dto.UpdateRoleRequest;
import will.dev.qrcodeApp.dto.UserActionDto;
import will.dev.qrcodeApp.dto.UserDto;
import will.dev.qrcodeApp.entity.QrCodeMetadata;
import will.dev.qrcodeApp.entity.User;
import will.dev.qrcodeApp.entity.UserAction;
import will.dev.qrcodeApp.mapper.QrCodeMetadataMapper;
import will.dev.qrcodeApp.mapper.UserActionMapper;
import will.dev.qrcodeApp.mapper.UserMapper;
import will.dev.qrcodeApp.repository.QrCodeMetadataRepository;
import will.dev.qrcodeApp.service.UserActionService;
import will.dev.qrcodeApp.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserService userService;
    private final UserActionService userActionService;
    private final QrCodeMetadataRepository qrCodeMetadataRepository;
    private final UserMapper userMapper;
    private final UserActionMapper userActionMapper;
    private final QrCodeMetadataMapper qrCodeMetadataMapper;

    @GetMapping("/users")
    @PreAuthorize("hasAnyAuthority(\'ADMIN\', \'MANAGER\')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        try{
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users.stream().map(userMapper::userToUserDto).collect(Collectors.toList()));
        } catch (RuntimeException e) {
            throw new RuntimeException("GET_ALL_USER_ERROR :: " + e.getMessage());
        }
    }

    @PostMapping("/users")
    @PreAuthorize("hasAuthority(\'ADMIN\')")
    public ResponseEntity<UserDto> createUser(@RequestBody CreateUserRequest request) {
        try {
            User newUser = userService.createUser(request.getNom(), request.getEmail(),
                    request.getPassword(), User.Role.valueOf(request.getRole().toUpperCase()));
            return ResponseEntity.ok(userMapper.userToUserDto(newUser));
        } catch (RuntimeException e) {
            throw new RuntimeException("CREATE_USER_ERROR :: " + e.getMessage());
        }
    }

    /**
     * Régénérer un code d'accès pour un utilisateur
     */
    @PutMapping("/users/{id}/access-code")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, String>> regenerateAccessCode(@PathVariable Long id) {
        try {
            String message = userService.regenerateAccessCode(id);
            return ResponseEntity.ok(Map.of("message",message));
        } catch (RuntimeException e) {
            throw new RuntimeException("RESET_ACCESS_CODE_ERROR :: " + e.getMessage());
        }
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDto> updateUserRole(@PathVariable Long id,
                                                @RequestBody UpdateRoleRequest request) {
        String role = request.getRole();
        User updatedUser = userService.changeUserRole(id, User.Role.valueOf(role));
        return ResponseEntity.ok(userMapper.userToUserDto(updatedUser));
    }

    @PutMapping("/users/{id}/status")
    @PreAuthorize("hasAuthority(\'ADMIN\')")
    public ResponseEntity<UserDto> toggleUserStatus(@PathVariable Long id) {
        User updatedUser = userService.toggleUserStatus(id);
        return ResponseEntity.ok(userMapper.userToUserDto(updatedUser));
    }

    @GetMapping("/actions")
    @PreAuthorize("hasAnyAuthority(\'ADMIN\', \'MANAGER\')")
    public ResponseEntity<List<UserActionDto>> getAllActions() {
        List<UserAction> actions = userActionService.getAllActions();
        return ResponseEntity.ok(actions.stream().map(userActionMapper::userActionToUserActionDto).collect(Collectors.toList()));
    }

    @DeleteMapping("/qrcodes/{uniqueId}")
    @PreAuthorize("hasAnyAuthority(\'ADMIN\', \'MANAGER\')")
    public ResponseEntity<Map<String, String>> deleteQrCode(@PathVariable String uniqueId) {
        qrCodeMetadataRepository.deleteByUniqueId(uniqueId);
        return ResponseEntity.ok(Map.of("message","QR code supprimé avec succès"));
    }

    @GetMapping("/qrcodes")
    @PreAuthorize("hasAnyAuthority(\'ADMIN\', \'MANAGER\')")
    public ResponseEntity<List<QrCodeMetadataDto>> getAllQrCodes() {
        List<QrCodeMetadata> qrCodes = qrCodeMetadataRepository.findAll();
        return ResponseEntity.ok(qrCodes.stream().map(qrCodeMetadataMapper::qrCodeMetadataToQrCodeMetadataDto).collect(Collectors.toList()));
    }
}


