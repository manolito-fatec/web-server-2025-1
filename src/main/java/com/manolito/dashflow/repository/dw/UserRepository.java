package com.manolito.dashflow.repository.dw;

import com.manolito.dashflow.entity.dw.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u.id FROM User u WHERE u.originalId = :originalId")
    Long getUserIdByOriginalId(@Param("originalId") String originalId);
}
