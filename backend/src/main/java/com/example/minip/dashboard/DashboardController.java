package com.example.minip.dashboard;
import com.example.minip.catalog.*;
import com.example.minip.config.*;
import com.example.minip.laundry.*;
import com.example.minip.rental.*;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api")
public class DashboardController {
 private final ProductRepository products;private final ProductVariantRepository variants;private final RentalOrderRepository orders;private final LaundryInspectionRepository inspections;private final RoleGuard roles;
 public DashboardController(ProductRepository products,ProductVariantRepository variants,RentalOrderRepository orders,LaundryInspectionRepository inspections,RoleGuard roles){this.products=products;this.variants=variants;this.orders=orders;this.inspections=inspections;this.roles=roles;}
 @GetMapping("/admin/dashboard") public AdminDashboard admin(@RequestHeader("X-Actor-Role")String role){roles.require(role,ActorRole.ROLE_ADMIN);List<RentalOrder> all=orders.findAll();long rented=all.stream().filter(v->v.getStatus()==RentalOrder.Status.RENTED).count();long returns=all.stream().filter(v->v.getStatus()==RentalOrder.Status.RETURN_REQUESTED).count();return new AdminDashboard(products.count(),all.size(),rented,returns,inspections.count(),all.stream().sorted((a,b)->b.getCreatedAt().compareTo(a.getCreatedAt())).limit(10).toList());}
 @GetMapping("/partner/dashboard") public PartnerDashboard partner(@RequestHeader("X-Actor-Role")String role,@RequestParam Long partnerId){roles.require(role,ActorRole.ROLE_PARTNER,ActorRole.ROLE_ADMIN);List<Product> ps=products.findAllByPartnerIdOrderByCreatedAtDesc(partnerId);List<RentalOrder> os=orders.findAllByVariantProductPartnerIdOrderByCreatedAtDesc(partnerId);long stock=ps.stream().flatMap(p->variants.findAllByProductId(p.getId()).stream()).mapToLong(ProductVariant::getAvailableStock).sum();BigDecimal revenue=os.stream().map(RentalOrder::getRentalAmount).reduce(BigDecimal.ZERO,BigDecimal::add);long renting=os.stream().filter(v->v.getStatus()==RentalOrder.Status.RENTED).count();return new PartnerDashboard(partnerId,ps.size(),renting,stock,revenue,ps.stream().limit(10).toList());}
 public record AdminDashboard(long productCount,long orderCount,long rentingCount,long returnWaitingCount,long inspectionCount,List<RentalOrder> recentOrders){}
 public record PartnerDashboard(Long partnerId,long productCount,long rentingProductCount,long availableStock,BigDecimal rentalRevenue,List<Product> recentProducts){}
}
