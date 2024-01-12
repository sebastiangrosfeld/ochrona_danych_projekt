package ee.sebgros.secureapplication.repository.repositories;

import ee.sebgros.secureapplication.repository.entity.ActivateAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivateAccRepository extends JpaRepository<ActivateAccount, String> {

}
