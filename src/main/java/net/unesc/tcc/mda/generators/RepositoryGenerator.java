package net.unesc.tcc.mda.generators;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.Modifier;
import net.unesc.tcc.mda.core.MdaMetaModel;
import net.unesc.tcc.mda.core.MdaModel;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public class RepositoryGenerator implements MdaGenerator {

	@Override
	public JavaFile generate(String packageName, MdaModel model) {
		MdaMetaModel pkMetaModel = model.getAttributes().stream().filter(MdaMetaModel::isPrimaryKey).findFirst().orElse(null);
		if (Objects.isNull(pkMetaModel)) {
			return null;
		}
		String entity = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, model.getName());
		String repositoryName = StringUtils.capitalize(entity) + "Repository";
		AtomicReference<Class> pkClass = new AtomicReference<>(Long.class);
		model.getAttributes().stream().filter(MdaMetaModel::isPrimaryKey).findFirst().ifPresent(f -> pkClass.set(f.getDataType().getType()));
		TypeSpec typeSpec = TypeSpec.interfaceBuilder(repositoryName)
			.addAnnotation(Repository.class)
			.addModifiers(Modifier.PUBLIC)
			.addSuperinterface(ParameterizedTypeName.get(ClassName.get(JpaRepository.class),
				ClassName.get(packageName + ".entities", StringUtils.capitalize(entity)),
				ClassName.get(pkClass.get())))
			.build();
		return JavaFile.builder(packageName + ".repositories", typeSpec).build();
	}
}
