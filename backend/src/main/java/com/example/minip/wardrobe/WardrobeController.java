package com.example.minip.wardrobe;

import com.example.minip.business.ReferenceDataService;
import com.example.minip.config.ActorRole;
import com.example.minip.config.RoleGuard;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/wardrobe/items")
public class WardrobeController {
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;
    private final WardrobeItemRepository items;
    private final RoleGuard roles;
    private final ReferenceDataService references;
    public WardrobeController(WardrobeItemRepository items, RoleGuard roles, ReferenceDataService references) {
        this.items = items; this.roles = roles; this.references = references;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ItemView create(@RequestHeader("X-Actor-Role") String role, @RequestHeader("X-User-Id") Long userId,
                           @Valid @RequestPart("metadata") Metadata metadata,
                           @RequestPart("image") MultipartFile image) throws IOException {
        roles.require(role, ActorRole.ROLE_CUSTOMER);
        validateImage(image);
        references.ensureUser(userId, ActorRole.ROLE_CUSTOMER);
        WardrobeItem saved = items.save(new WardrobeItem(userId, metadata.name(), metadata.category(), metadata.color(),
            metadata.season(), metadata.description(), safeFileName(image), image.getContentType(), image.getBytes()));
        return ItemView.from(saved);
    }

    @GetMapping @Transactional(readOnly = true)
    public List<ItemView> list(@RequestHeader("X-Actor-Role") String role, @RequestHeader("X-User-Id") Long userId) {
        roles.require(role, ActorRole.ROLE_CUSTOMER);
        return items.findAllByUserIdOrderByCreatedAtDesc(userId).stream().map(ItemView::from).toList();
    }

    @GetMapping("/{id}") @Transactional(readOnly = true)
    public ItemView get(@RequestHeader("X-Actor-Role") String role, @RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        roles.require(role, ActorRole.ROLE_CUSTOMER); return ItemView.from(owned(id, userId));
    }

    @GetMapping("/{id}/image") @Transactional(readOnly = true)
    public ResponseEntity<byte[]> image(@RequestHeader("X-Actor-Role") String role, @RequestHeader("X-User-Id") Long userId,
                                        @PathVariable Long id) {
        roles.require(role, ActorRole.ROLE_CUSTOMER);
        WardrobeItem item = owned(id, userId);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(item.getImageContentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + item.getOriginalFileName() + "\"")
            .body(item.getImageData());
    }

    @PatchMapping("/{id}") @Transactional
    public ItemView update(@RequestHeader("X-Actor-Role") String role, @RequestHeader("X-User-Id") Long userId,
                           @PathVariable Long id, @Valid @RequestBody Metadata metadata) {
        roles.require(role, ActorRole.ROLE_CUSTOMER);
        WardrobeItem item = owned(id, userId);
        item.update(metadata.name(), metadata.category(), metadata.color(), metadata.season(), metadata.description());
        return ItemView.from(item);
    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @Transactional
    public void delete(@RequestHeader("X-Actor-Role") String role, @RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        roles.require(role, ActorRole.ROLE_CUSTOMER); items.delete(owned(id, userId));
    }

    private WardrobeItem owned(Long id, Long userId) {
        WardrobeItem item = items.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "옷장 항목을 찾을 수 없습니다."));
        if (!item.getCustomerId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 옷만 관리할 수 있습니다.");
        return item;
    }
    private void validateImage(MultipartFile image) {
        if (image.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사진 파일이 필요합니다.");
        if (image.getSize() > MAX_IMAGE_SIZE) throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "사진은 10MB 이하여야 합니다.");
        if (image.getContentType() == null || !image.getContentType().startsWith("image/"))
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "이미지 파일만 업로드할 수 있습니다.");
    }
    private String safeFileName(MultipartFile image) {
        String value = image.getOriginalFilename();
        if (value == null || value.isBlank()) return "wardrobe-image";
        return value.replace("\\", "/").substring(value.replace("\\", "/").lastIndexOf('/') + 1).replace("\"", "");
    }

    public record Metadata(@NotBlank @Size(max = 100) String name, @NotBlank @Size(max = 50) String category,
                           @Size(max = 50) String color, @Size(max = 100) String season,
                           @Size(max = 1000) String description) {}
    public record ItemView(Long id, String name, String category, String color, String season, String description,
                           String imageUrl, Instant createdAt, Instant updatedAt) {
        static ItemView from(WardrobeItem item) {
            return new ItemView(item.getId(), item.getName(), item.getCategory(), item.getColor(), item.getSeason(),
                item.getDescription(), "/api/wardrobe/items/" + item.getId() + "/image", item.getCreatedAt(), item.getUpdatedAt());
        }
    }
}
