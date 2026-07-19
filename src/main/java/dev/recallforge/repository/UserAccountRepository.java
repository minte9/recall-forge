package dev.recallforge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dev.recallforge.domain.UserAccount;
import dev.recallforge.dto.AdminUsersStatsResponse;

import java.util.Optional;
import java.util.List;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByProviderAndProviderUserId(
            String provider,
            String providerUserId);

    @Query("""
        select new dev.recallforge.dto.AdminUsersStatsResponse(
            u.id,
            u.email,
            u.displayName, 
            (u.role = dev.recallforge.domain.Role.ADMIN),
            count(distinct t.id),
            count(distinct r.id),
            coalesce(sum(r.totalTokens), 0)
        )
        from UserAccount u
        left join MarkdownFile mf on mf.user.id = u.id
        left join Topic t on t.markdownFile.id = mf.id
        left join Review r on r.topic.id = t.id
        group by u.id, u.email, u.role
        order by u.email asc
    """)
    List<AdminUsersStatsResponse> findAdminUserStats();
}
