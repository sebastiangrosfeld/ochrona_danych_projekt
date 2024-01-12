package ee.sebgros.secureapplication.data;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class ResetPasswdRequest {
	@NotEmpty
	@Size(min = 8, max = 32)
	@Pattern(regexp = "\\A\\p{ASCII}*\\z", message = "only ascii characters")
	@Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{8,32}$",
			message = "Password must contain at least one: small letter, capital letter, number and one char from set '@#$%'")
	private String password;

	private Boolean isNotOk;

}
