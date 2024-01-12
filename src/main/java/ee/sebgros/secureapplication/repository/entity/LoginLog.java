package ee.sebgros.secureapplication.repository.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class LoginLog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 255)
	private String ip;

	@Column(length = 255)
	private String operation;

	@Column(length = 255)
	private String result;

	private String date;

	@ManyToOne
	private User user;
}
