package whenyourcar.gateway.whenyourcargateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import whenyourcar.gateway.whenyourcargateway.data.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    boolean existsUserByEmail(String email);
}
