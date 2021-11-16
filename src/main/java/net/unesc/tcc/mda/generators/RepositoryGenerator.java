package net.unesc.tcc.mda.generators;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.Modifier;
import net.unesc.tcc.mda.core.MdaMetaModel;
import net.unesc.tcc.mda.core.MdaModel;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;

public class RepositoryGenerator implements MdaGenerator {

	@Override
	public void generate(MdaModel model) {
		String entity = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, model.getName());
		String repositoryName = StringUtils.capitalize(entity) + "Repository";
		AtomicReference<Class> pkClass = new AtomicReference<>(Long.class);
		model.getAttributes().stream().filter(MdaMetaModel::isPrimaryKey).findFirst().ifPresent( f -> pkClass.set(f.getDataType().getType()));
		TypeSpec typeSpec = TypeSpec.interfaceBuilder(repositoryName)
			.addAnnotation(Repository.class)
			.addModifiers(Modifier.PUBLIC)
			.addSuperinterface(ParameterizedTypeName.get(ClassName.get(JpaRepository.class),
				ClassName.get("com.exemple.demo.entities", StringUtils.capitalize(entity)),
				ClassName.get(pkClass.get())))
			.build();
		JavaFile javaFile = JavaFile.builder("com.exemple.demo.repositories", typeSpec).build();
		String path = "target" + File.separator + "generated-mda-projects" + File.separator + "teste" + File.separator + "demo" + File.separator + "src" +
			File.separator + "main" + File.separator + "java" + File.separator + "com" + File.separator + "example" + File.separator + "demo" +
			File.separator + "repositories";

		File directory = new File(path);
		if (!directory.exists()) {
			directory.mkdir();
		}

		try {
			FileWriter file = new FileWriter(new File(path + File.separator + repositoryName + ".java"));
			file.write(javaFile.toString());
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
