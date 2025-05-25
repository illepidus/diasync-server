package ru.krotarnya.diasync.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_locks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserLock {
    @Id
    private String userId;
}
