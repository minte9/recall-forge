package dev.recallforge.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import dev.recallforge.domain.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    
    Optional<UserAccount> findByProviderAndProviderUserId(
        String provider, 
        String providerUserId
    );
}
