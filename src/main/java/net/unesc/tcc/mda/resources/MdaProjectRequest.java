package net.unesc.tcc.mda.resources;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MdaProjectRequest {
	@NotBlank
	@ApiModelProperty(required = true)
	private String name;
	@NotBlank
	@ApiModelProperty(required = true)
	private String url;
	@NotBlank
	@ApiModelProperty(required = true)
	private String username;
	@NotBlank
	@ApiModelProperty(required = true)
	private String password;
}
