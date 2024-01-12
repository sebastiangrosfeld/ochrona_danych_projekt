package ee.sebgros.secureapplication.data;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class AddPlayerAccess {
	@NotEmpty
	@Size(min = 4, max = 32)
	@Pattern(regexp = "\\A\\p{ASCII}*\\z", message = "only ascii characters")
	private String name;
}
