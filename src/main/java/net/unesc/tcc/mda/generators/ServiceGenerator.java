package net.unesc.tcc.mda.generators;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Modifier;
import net.unesc.tcc.mda.core.MdaModel;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

public class ServiceGenerator implements MdaGenerator {

	@Override
	public JavaFile generate(String packageName, MdaModel model) {
		String entity = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, model.getName());
		String serviceName = StringUtils.capitalize(entity) + "Service";
		TypeSpec typeSpec = TypeSpec.classBuilder(serviceName)
			.addAnnotation(Service.class)
			.addModifiers(Modifier.PUBLIC)
			.build();
		return JavaFile.builder(packageName + ".services", typeSpec).build();
	}
}
