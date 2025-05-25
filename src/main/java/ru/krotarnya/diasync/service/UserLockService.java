package ru.krotarnya.diasync.service;

import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.krotarnya.diasync.model.UserLock;
import ru.krotarnya.diasync.repository.UserLockRepository;

@Service
public class UserLockService {
    private final UserLockRepository repository;

    public UserLockService(UserLockRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Optional<UserLock> lockUser(String userId) {
        return repository.lockUser(userId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createLockFastAndSafe(String userId) {
        repository.saveAndFlush(new UserLock(userId));
    }

    @Transactional
    public void lockOrCreate(String userId) {
        while (true) {
            if (lockUser(userId).isPresent()) {
                return;
            }

            try {
                createLockFastAndSafe(userId);
            } catch (DataIntegrityViolationException ignored) {
                // Another thread already inserted — continue
            }
        }
    }
}
