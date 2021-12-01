package net.unesc.tcc.mda.core;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import net.unesc.tcc.mda.core.enums.DataType;

@Data
public class MdaMetaModel {
	@NotBlank
	@ApiModelProperty(required = true)
	private String id;
	@NotBlank
	@ApiModelProperty(required = true)
	private String name;
	private String columnDefault;
	private boolean isPrimaryKey;
	private boolean isRequired;
	@NotNull
	@ApiModelProperty(required = true)
	private DataType dataType;
	private Integer length;
	private Integer numScale;
	private String description;

	public MdaMetaModel(String id, String name, String columnDefault, boolean isPrimaryKey, boolean isRequired, DataType dataType, Integer length,
		Integer numScale,
		String description) {
		this.id = id;
		this.name = name;
		this.columnDefault = columnDefault;
		this.isPrimaryKey = isPrimaryKey;
		this.isRequired = isRequired;
		this.dataType = dataType;
		this.length = length;
		this.numScale = numScale;
		this.description = description;
	}
}