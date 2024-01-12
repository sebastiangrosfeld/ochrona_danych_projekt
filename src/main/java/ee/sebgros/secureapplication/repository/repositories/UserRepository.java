package ee.sebgros.secureapplication.repository.repositories;

import ee.sebgros.secureapplication.repository.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
	Optional<User> findFirstByUsernameIgnoreCase(String username);

	Optional<User> findFirstByEmailIgnoreCase(String email);

}
