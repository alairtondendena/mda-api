package net.unesc.tcc.mda.core;

import lombok.Getter;

@Getter
public class MdaAttributeTypes {
	private String value;
	private String description;

	public MdaAttributeTypes(String value, String description) {
		this.value = value;
		this.description = description;
	}
}
