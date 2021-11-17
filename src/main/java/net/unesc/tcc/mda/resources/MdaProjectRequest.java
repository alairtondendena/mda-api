package net.unesc.tcc.mda.resources;

import io.swagger.annotations.ApiModelProperty;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import net.unesc.tcc.mda.core.MdaModel;

@Data
public class MdaProjectRequest {
	@NotBlank
	@ApiModelProperty(required = true)
	private String group;
	@NotBlank
	@ApiModelProperty(required = true)
	private String artifact;
	@NotBlank
	@ApiModelProperty(required = true)
	private String name;
	@NotBlank
	@ApiModelProperty(required = true)
	private String packageName;
	@NotNull
	@Valid
	private MdaDatabaseCredentialsRequest databaseCredentials;
	@Valid
	private Set<MdaModel> models;
}
