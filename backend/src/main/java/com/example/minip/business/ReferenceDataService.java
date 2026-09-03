package com.example.minip.business;

import com.example.minip.auth.Role;
import com.example.minip.auth.RoleRepository;
import com.example.minip.auth.UserAccount;
import com.example.minip.auth.UserAccountRepository;
import com.example.minip.auth.UserRole;
import com.example.minip.auth.UserRoleId;
import com.example.minip.auth.UserRoleRepository;
import com.example.minip.config.ActorRole;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReferenceDataService {
    private static final BigDecimal DEFAULT_COMMISSION_RATE = new BigDecimal("0.7000");

    private final UserAccountRepository users;
    private final RoleRepository roles;
    private final UserRoleRepository userRoles;
    private final BusinessRepository businesses;
    private final BusinessMemberRepository members;
    private final BusinessContractRepository contracts;

    public ReferenceDataService(UserAccountRepository users, RoleRepository roles,
                                UserRoleRepository userRoles, BusinessRepository businesses,
                                BusinessMemberRepository members, BusinessContractRepository contracts) {
        this.users = users;
        this.roles = roles;
        this.userRoles = userRoles;
        this.businesses = businesses;
        this.members = members;
        this.contracts = contracts;
    }

    @Transactional
    public UserAccount ensureUser(Long userId, ActorRole actorRole) {
        UserAccount user = users.findById(userId).orElseGet(() -> users.save(
            new UserAccount(userId, "user-" + userId + "@fitly.local", "FITLY User " + userId)
        ));
        Role role = roles.findByName(toDatabaseRole(actorRole))
            .orElseGet(() -> roles.save(new Role(toDatabaseRole(actorRole))));
        UserRoleId userRoleId = new UserRoleId(user.getId(), role.getId());
        if (!userRoles.existsById(userRoleId)) {
            userRoles.save(new UserRole(user.getId(), role.getId()));
        }
        return user;
    }

    @Transactional
    public BusinessContext ensureBusiness(Long businessId, BigDecimal commissionRate) {
        UserAccount owner = ensureUser(businessId, ActorRole.ROLE_PARTNER);
        Business business = businesses.findById(businessId).orElseGet(() -> businesses.save(
            new Business(businessId, "FITLY Partner " + businessId)
        ));
        BusinessMember member = members.findByBusinessIdAndUserId(businessId, owner.getId())
            .orElseGet(() -> members.save(new BusinessMember(business, owner)));
        BusinessContract contract = contracts
            .findFirstByBusinessIdAndStatusOrderByStartDateDesc(businessId, "ACTIVE")
            .orElseGet(() -> contracts.save(new BusinessContract(
                business, commissionRate == null ? DEFAULT_COMMISSION_RATE : commissionRate
            )));
        return new BusinessContext(business, member, contract);
    }

    @Transactional(readOnly = true)
    public BigDecimal activeCommissionRate(Long businessId) {
        return contracts.findFirstByBusinessIdAndStatusOrderByStartDateDesc(businessId, "ACTIVE")
            .map(BusinessContract::getCommissionRate)
            .orElse(BigDecimal.ZERO);
    }

    private String toDatabaseRole(ActorRole actorRole) {
        return switch (actorRole) {
            case ROLE_CUSTOMER -> "CUSTOMER";
            case ROLE_PARTNER -> "ENTERPRISE";
            case ROLE_ADMIN -> "ADMIN";
        };
    }

    public record BusinessContext(Business business, BusinessMember member, BusinessContract contract) {
    }
}
