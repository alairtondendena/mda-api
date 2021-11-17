package net.unesc.tcc.mda.resources;

import java.util.Set;
import lombok.Getter;
import net.unesc.tcc.mda.core.MdaAttributeTypes;
import net.unesc.tcc.mda.core.MdaModel;

@Getter
public class MdaModelResponse {
	private Set<MdaModel> models;
	private Set<MdaAttributeTypes> types;

	public MdaModelResponse(Set<MdaModel> models, Set<MdaAttributeTypes> types) {
		this.models = models;
		this.types = types;
	}
}
