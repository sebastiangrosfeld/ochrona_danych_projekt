package ee.sebgros.secureapplication.repository.repositories;

import ee.sebgros.secureapplication.repository.entity.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {
	List<LoginLog> findAllByUserId(Long user);
}
