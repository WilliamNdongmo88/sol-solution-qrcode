package will.dev.qrcodeApp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import will.dev.qrcodeApp.dto.*;
import will.dev.qrcodeApp.entity.User;
import will.dev.qrcodeApp.mapper.PdfMetadataMapper;
import will.dev.qrcodeApp.mapper.QrCodeMetadataMapper;
import will.dev.qrcodeApp.mapper.UserActionMapper;
import will.dev.qrcodeApp.mapper.UserMapper;
import will.dev.qrcodeApp.repository.PdfMetadataRepository;
import will.dev.qrcodeApp.repository.QrCodeMetadataRepository;
import will.dev.qrcodeApp.service.UserActionService;
import will.dev.qrcodeApp.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final UserActionService userActionService;
    private final PdfMetadataRepository pdfMetadataRepository;
    private final QrCodeMetadataRepository qrCodeMetadataRepository;
    private final UserMapper userMapper;
    private final UserActionMapper userActionMapper;
    private final PdfMetadataMapper pdfMetadataMapper;
    private final QrCodeMetadataMapper qrCodeMetadataMapper;

    @GetMapping("/profile")
    @PreAuthorize("hasAnyAuthority(\'USER\', \'MANAGER\', \'ADMIN\')")
    public ResponseEntity<UserDto> getUserProfile(@AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(userMapper.userToUserDto(user));
        } catch (RuntimeException e) {
            throw new RuntimeException("GET_PROFILE_ERROR: " + e.getMessage());        }
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyAuthority(\'USER\', \'MANAGER\', \'ADMIN\')")
    public ResponseEntity<UserDto> updateUserProfile(@AuthenticationPrincipal User user,
                                                @RequestBody UpdateProfileRequest request) {
        try{
            User updatedUser = userService.updateUserProfile(user.getId(), request.getNom(), request.getEmail());
            return ResponseEntity.ok(userMapper.userToUserDto(updatedUser));
        } catch (RuntimeException e) {
            throw new RuntimeException("UPDATE_PROFILE_ERROR: " + e.getMessage());
        }
    }

    @GetMapping("/actions")
    @PreAuthorize("hasAnyAuthority(\'USER\', \'MANAGER\', \'ADMIN\')")
    public ResponseEntity<List<UserActionDto>> getUserActions(@AuthenticationPrincipal User user) {
        List<UserActionDto> actions = userActionService.getUserActions(user.getId())
                .stream().map(userActionMapper::userActionToUserActionDto).collect(Collectors.toList());
        return ResponseEntity.ok(actions);
    }

    @GetMapping("/qrcodes")
    @PreAuthorize("hasAnyAuthority(\'USER\', \'MANAGER\', \'ADMIN\')")
    public ResponseEntity<List<QrCodeMetadataDto>> getUserQrCodes(@AuthenticationPrincipal User user) {
        List<QrCodeMetadataDto> qrCodes = qrCodeMetadataRepository.findByUserOrderByGenerationDateDesc(user)
                .stream().map(qrCodeMetadataMapper::qrCodeMetadataToQrCodeMetadataDto).collect(Collectors.toList());
        return ResponseEntity.ok(qrCodes);
    }

    @GetMapping("/pdfs")
    @PreAuthorize("hasAnyAuthority(\'USER\', \'MANAGER\', \'ADMIN\')")
    public ResponseEntity<List<PdfMetadataDto>> getUserPdfs(@AuthenticationPrincipal User user) {
        List<PdfMetadataDto> pdfs = pdfMetadataRepository.findByUserOrderByUploadDateDesc(user)
                .stream().map(pdfMetadataMapper::pdfMetadataToPdfMetadataDto).collect(Collectors.toList());
        return ResponseEntity.ok(pdfs);
    }
}


