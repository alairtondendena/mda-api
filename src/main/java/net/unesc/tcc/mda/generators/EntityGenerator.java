package net.unesc.tcc.mda.generators;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.FieldSpec.Builder;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import net.unesc.tcc.mda.core.MdaMetaModel;
import net.unesc.tcc.mda.core.MdaModel;
import org.apache.commons.lang.StringUtils;

public class EntityGenerator implements MdaGenerator {

	@Override
	public void generate(MdaModel model) {
		String entity = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, model.getName());

		Set<FieldSpec> fields = new LinkedHashSet<>();
		for (MdaMetaModel metaModel : model.getAttributes()) {
			Builder fieldBuilder = FieldSpec
				.builder(metaModel.getDataType().getType(), CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, metaModel.getName()))
				.addModifiers(Modifier.PRIVATE);
			if (metaModel.isPrimaryKey()) {
				fieldBuilder.addAnnotation(AnnotationSpec.builder(Id.class).build());
				fieldBuilder.addAnnotation(AnnotationSpec.builder(GeneratedValue.class)
					.addMember("strategy", "$L", GenerationType.IDENTITY)
					.build());
			}
			fields.add(fieldBuilder.addAnnotation(
				AnnotationSpec.builder(Column.class).addMember("name", "$S", metaModel.getName()).build()
			).build());
		}

		TypeSpec typeSpec = TypeSpec.classBuilder(StringUtils.capitalize(entity))
			.addModifiers(Modifier.PUBLIC)
			.addAnnotation(Data.class)
			.addAnnotation(Entity.class)
			.addAnnotation(AnnotationSpec.builder(Table.class)
				.addMember("name", "$S", model.getName())
				.build())
			.addFields(fields)
			.build();

		JavaFile javaFile = JavaFile.builder("com.exemple.demo.entities", typeSpec).build();

		String path = "target" + File.separator + "generated-mda-projects" + File.separator + "teste" + File.separator + "demo" + File.separator + "src" +
			File.separator + "main" + File.separator + "java" + File.separator + "com" + File.separator + "example" + File.separator + "demo" +
			File.separator + "entities";

		File directory = new File(path);
		if (!directory.exists()){
			directory.mkdir();
		}

		try {
			FileWriter file = new FileWriter(new File(path + File.separator + StringUtils.capitalize(entity) + ".java"));
			file.write(javaFile.toString());
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
