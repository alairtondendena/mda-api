package net.unesc.tcc.mda.core;

import io.swagger.annotations.ApiModelProperty;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MdaModel {
	@NotBlank
	@ApiModelProperty(required = true)
	private String id;
	@NotBlank
	@ApiModelProperty(required = true)
	private String name;
	@Valid
	private Set<MdaMetaModel> attributes;

	public MdaModel(String id, String name, Set<MdaMetaModel> attributes) {
		this.id = id;
		this.name = name;
		this.attributes = attributes;
	}
}
