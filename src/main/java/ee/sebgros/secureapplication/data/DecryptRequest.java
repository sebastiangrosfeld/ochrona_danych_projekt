package ee.sebgros.secureapplication.data;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class DecryptRequest {
	@Size(max = 32)
	private String password;
}
