package net.unesc.tcc.mda.resources;

import io.swagger.annotations.ApiModelProperty;
import java.util.Set;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import net.unesc.tcc.mda.core.MdaModel;

@Data
public class MdaModelRequest {
	@NotBlank
	@ApiModelProperty(required = true)
	private String name;
	private Set<MdaModel> models;
}
