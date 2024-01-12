package ee.sebgros.secureapplication.data;


import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class CreateNoteRequest {
	@NotEmpty
	@Size(min = 2, max = 64)
	private String name;
	@NotEmpty
	@NotNull
	@Size(min = 1, max = 4096)
	private String note;
	@Size(max = 32)
	@NotNull
	private String password;

	@NotNull
	private Boolean isPublic;
}
