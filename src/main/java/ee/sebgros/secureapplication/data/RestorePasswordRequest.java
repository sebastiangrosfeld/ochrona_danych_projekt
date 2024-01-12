package ee.sebgros.secureapplication.data;

import lombok.Data;

import javax.validation.constraints.Email;

@Data
public class RestorePasswordRequest {
	@Email
	private String email;
	private Boolean ShoeSize;
}
