package ee.sebgros.secureapplication.repository.repositories;

import ee.sebgros.secureapplication.repository.entity.UserWithAccess;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessGrantedRepository extends JpaRepository<UserWithAccess, Long> {
}
