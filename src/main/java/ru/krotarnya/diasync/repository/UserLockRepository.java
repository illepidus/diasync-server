package ru.krotarnya.diasync.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.krotarnya.diasync.model.UserLock;

public interface UserLockRepository extends JpaRepository<UserLock, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserLock u WHERE u.userId = :userId")
    Optional<UserLock> lockUser(@Param("userId") String userId);
}
