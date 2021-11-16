package net.unesc.tcc.mda.generators;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.lang.model.element.Modifier;
import net.unesc.tcc.mda.core.MdaModel;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

public class ServiceGenerator implements MdaGenerator {

	@Override
	public void generate(MdaModel model) {
		String entity = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, model.getName());
		String serviceName = StringUtils.capitalize(entity) + "Service";
		TypeSpec typeSpec = TypeSpec.classBuilder(serviceName)
			.addAnnotation(Service.class)
			.addModifiers(Modifier.PUBLIC)
			.build();
		JavaFile javaFile = JavaFile.builder("com.exemple.demo.services", typeSpec).build();
		String path = "target" + File.separator + "generated-mda-projects" + File.separator + "teste" + File.separator + "demo" + File.separator + "src" +
			File.separator + "main" + File.separator + "java" + File.separator + "com" + File.separator + "example" + File.separator + "demo" +
			File.separator + "services";

		File directory = new File(path);
		if (!directory.exists()) {
			directory.mkdir();
		}

		try {
			FileWriter file = new FileWriter(new File(path + File.separator + serviceName + ".java"));
			file.write(javaFile.toString());
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
