package net.unesc.tcc.mda.resources;

import java.util.Set;
import lombok.Getter;
import net.unesc.tcc.mda.core.MdaAttributeTypes;
import net.unesc.tcc.mda.core.MdaModel;

@Getter
public class MdaProjectResponse {
	private String name;
	private Set<MdaModel> models;
	private Set<MdaAttributeTypes> types;

	public MdaProjectResponse(String name, Set<MdaModel> models, Set<MdaAttributeTypes> types) {
		this.name = name;
		this.models = models;
		this.types = types;
	}
}
