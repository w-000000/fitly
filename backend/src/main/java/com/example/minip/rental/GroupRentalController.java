package com.example.minip.rental;
import com.example.minip.business.ReferenceDataService;
import com.example.minip.config.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/group-rentals")
public class GroupRentalController{
 private final GroupRentalRequestRepository requests;private final RentalOrderRepository orders;private final RoleGuard roles;private final ReferenceDataService references;
 public GroupRentalController(GroupRentalRequestRepository requests,RentalOrderRepository orders,RoleGuard roles,ReferenceDataService references){this.requests=requests;this.orders=orders;this.roles=roles;this.references=references;}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) @Transactional public GroupRentalRequest create(@RequestHeader("X-Actor-Role")String role,@RequestHeader("X-User-Id")Long userId,@Valid @RequestBody CreateRequest r){
   roles.require(role,ActorRole.ROLE_CUSTOMER);
   if(r.endDate().isBefore(r.startDate()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"이용 종료일은 시작일과 같거나 이후여야 합니다.");
   List<GroupRentalItem> items = r.items() == null ? List.of() : r.items().stream().map(v -> new GroupRentalItem(v.category(), v.quantity())).toList();
   if(items.isEmpty() && (r.requestedItems() == null || r.requestedItems().isBlank()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"요청 품목이 필요합니다.");
   references.ensureUser(userId,ActorRole.ROLE_CUSTOMER);
   String requestedItems = items.isEmpty() ? r.requestedItems() : items.stream().map(v -> v.getCategory()+" "+v.getQuantity()+"개").reduce((a,b)->a+", "+b).orElse("");
   String summary=r.contactName()+"\n"+r.contactPhone()+"\n"+requestedItems;
   RentalOrder order=orders.save(new RentalOrder(userId,r.startDate(),r.endDate(),summary));
   return requests.save(new GroupRentalRequest(order,r.purpose(),r.headcount(),items));
 }
 @GetMapping("/mine") public List<GroupRentalRequest> mine(@RequestHeader("X-Actor-Role")String role,@RequestHeader("X-User-Id")Long userId){roles.require(role,ActorRole.ROLE_CUSTOMER);return requests.findAllForUser(userId);}
 @GetMapping public List<GroupRentalRequest> all(@RequestHeader("X-Actor-Role")String role){roles.require(role,ActorRole.ROLE_ADMIN);return requests.findAll();}
 public record CreateRequest(@NotBlank String purpose,@NotNull LocalDate startDate,@NotNull LocalDate endDate,@Min(2)int headcount,String requestedItems,List<@Valid ItemRequest> items,@NotBlank String contactName,@NotBlank String contactPhone){}
 public record ItemRequest(@NotBlank String category,@Min(1)int quantity){}
}
