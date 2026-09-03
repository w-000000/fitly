package com.example.minip.wardrobe;

import com.example.minip.config.*;
import java.io.IOException;
import java.util.List;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController @RequestMapping("/api/wardrobe/items")
public class WardrobeController {
    private final WardrobeItemRepository items; private final RoleGuard roles;
    public WardrobeController(WardrobeItemRepository items,RoleGuard roles){this.items=items;this.roles=roles;}
    @PostMapping(consumes=MediaType.MULTIPART_FORM_DATA_VALUE) @ResponseStatus(HttpStatus.CREATED)
    public View create(@RequestHeader("X-Actor-Role") String role,@RequestHeader("X-User-Id") Long userId,
        @RequestParam String name,@RequestParam String category,@RequestParam(required=false) String color,
        @RequestParam(required=false) String season,@RequestParam(required=false) String style,
        @RequestPart MultipartFile image) throws IOException {
        roles.require(role,ActorRole.ROLE_CUSTOMER);
        if(image.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"옷 사진이 필요합니다.");
        return view(items.save(new WardrobeItem(userId,name,category,color,season,style,image.getOriginalFilename(),image.getContentType(),image.getBytes())));
    }
    @GetMapping public List<View> list(@RequestHeader("X-Actor-Role") String role,@RequestHeader("X-User-Id") Long userId){
        roles.require(role,ActorRole.ROLE_CUSTOMER);return items.findAllByCustomerIdOrderByCreatedAtDesc(userId).stream().map(this::view).toList();}
    @GetMapping("/{id}/image") public ResponseEntity<byte[]> image(@RequestHeader("X-Actor-Role") String role,@RequestHeader("X-User-Id") Long userId,@PathVariable Long id){
        roles.require(role,ActorRole.ROLE_CUSTOMER);WardrobeItem item=own(id,userId);return ResponseEntity.ok().contentType(MediaType.parseMediaType(item.getImageContentType())).body(item.getImageData());}
    @PatchMapping("/{id}") @Transactional public View update(@RequestHeader("X-Actor-Role") String role,@RequestHeader("X-User-Id") Long userId,@PathVariable Long id,@RequestBody UpdateRequest r){
        roles.require(role,ActorRole.ROLE_CUSTOMER);WardrobeItem item=own(id,userId);item.update(r.name(),r.category(),r.color(),r.season(),r.style());return view(item);}
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@RequestHeader("X-Actor-Role") String role,@RequestHeader("X-User-Id") Long userId,@PathVariable Long id){roles.require(role,ActorRole.ROLE_CUSTOMER);items.delete(own(id,userId));}
    private WardrobeItem own(Long id,Long userId){WardrobeItem v=items.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"옷장 항목을 찾을 수 없습니다."));if(!v.getCustomerId().equals(userId))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"본인의 옷만 관리할 수 있습니다.");return v;}
    private View view(WardrobeItem v){return new View(v.getId(),v.getName(),v.getCategory(),v.getColor(),v.getSeason(),v.getStyle(),"/api/wardrobe/items/"+v.getId()+"/image",v.getCreatedAt());}
    public record UpdateRequest(String name,String category,String color,String season,String style){}
    public record View(Long id,String name,String category,String color,String season,String style,String imageUrl,java.time.Instant createdAt){}
}
