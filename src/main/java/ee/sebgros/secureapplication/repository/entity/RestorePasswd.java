package ee.sebgros.secureapplication.repository.entity;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Setter
@Getter
public class RestorePasswd {
	@Id
	@Column(length = 36, unique = true, nullable = false)
	private String id = UUID.randomUUID().toString();

	private Date validTime;

	@ManyToOne
	private User user;
}
