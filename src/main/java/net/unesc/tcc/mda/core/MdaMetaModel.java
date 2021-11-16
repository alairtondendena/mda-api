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
	private String name;
	private String columnDefault;
	private boolean isPrimaryKey;
	private boolean isNullable;
	@NotNull
	@ApiModelProperty(required = true)
	private DataType dataType;
	private Integer charLength;
	private Integer numPrecision;
	private Integer numScale;
	private String description;

	public MdaMetaModel(String name, String columnDefault, boolean isPrimaryKey, boolean isNullable, DataType dataType, Integer charLength, Integer numPrecision,
		Integer numScale, String description) {
		this.name = name;
		this.columnDefault = columnDefault;
		this.isPrimaryKey = isPrimaryKey;
		this.isNullable = isNullable;
		this.dataType = dataType;
		this.charLength = charLength;
		this.numPrecision = numPrecision;
		this.numScale = numScale;
		this.description = description;
	}
}