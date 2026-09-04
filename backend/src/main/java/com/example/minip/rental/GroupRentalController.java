package com.example.minip.rental;
import com.example.minip.config.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/group-rentals")
public class GroupRentalController{
 private final GroupRentalRequestRepository requests;private final RoleGuard roles;
 public GroupRentalController(GroupRentalRequestRepository requests,RoleGuard roles){this.requests=requests;this.roles=roles;}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public GroupRentalRequest create(@RequestHeader("X-Actor-Role")String role,@RequestHeader("X-User-Id")Long userId,@Valid @RequestBody CreateRequest r){roles.require(role,ActorRole.ROLE_CUSTOMER);if(r.endDate().isBefore(r.startDate()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"이용 종료일은 시작일과 같거나 이후여야 합니다.");if((r.items()==null||r.items().isEmpty())&&(r.requestedItems()==null||r.requestedItems().isBlank()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"요청 품목이 필요합니다.");if(r.items()!=null&&!r.items().isEmpty())return requests.save(new GroupRentalRequest(userId,r.purpose(),r.startDate(),r.endDate(),r.headcount(),r.items().stream().map(v->new GroupRentalItem(v.category(),v.quantity())).toList(),r.contactName(),r.contactPhone()));return requests.save(new GroupRentalRequest(userId,r.purpose(),r.startDate(),r.endDate(),r.headcount(),r.requestedItems(),r.contactName(),r.contactPhone()));}
 @GetMapping("/mine") public List<GroupRentalRequest> mine(@RequestHeader("X-Actor-Role")String role,@RequestHeader("X-User-Id")Long userId){roles.require(role,ActorRole.ROLE_CUSTOMER);return requests.findAllByCustomerIdOrderByCreatedAtDesc(userId);}
 @GetMapping public List<GroupRentalRequest> all(@RequestHeader("X-Actor-Role")String role){roles.require(role,ActorRole.ROLE_ADMIN);return requests.findAll();}
 public record CreateRequest(@NotBlank String purpose,@NotNull LocalDate startDate,@NotNull LocalDate endDate,@Min(2)int headcount,String requestedItems,List<@Valid ItemRequest> items,@NotBlank String contactName,@NotBlank String contactPhone){}
 public record ItemRequest(@NotBlank String category,@Min(1)int quantity){}
}
