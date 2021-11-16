package net.unesc.tcc.mda.core;

import io.swagger.annotations.ApiModelProperty;
import java.util.Set;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MdaModel {
	@NotBlank
	@ApiModelProperty(required = true)
	private String name;
	private Set<MdaMetaModel> attributes;

	public MdaModel(String name, Set<MdaMetaModel> attributes) {
		this.name = name;
		this.attributes = attributes;
	}
}
