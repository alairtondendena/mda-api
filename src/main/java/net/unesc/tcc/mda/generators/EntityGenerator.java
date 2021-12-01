package net.unesc.tcc.mda.generators;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.FieldSpec.Builder;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import io.swagger.annotations.ApiModelProperty;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import net.unesc.tcc.mda.core.MdaMetaModel;
import net.unesc.tcc.mda.core.MdaModel;
import org.apache.commons.lang.StringUtils;

public class EntityGenerator implements MdaGenerator {

	@Override
	public JavaFile generate(String packageName, MdaModel model) {
		MdaMetaModel pkMetaModel = model.getAttributes().stream().filter(MdaMetaModel::isPrimaryKey).findFirst().orElse(null);
		if (Objects.isNull(pkMetaModel)) {
			return null;
		}
		String entity = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, model.getName());
		Set<FieldSpec> fields = new LinkedHashSet<>();
		for (MdaMetaModel metaModel : model.getAttributes()) {
			Builder fieldBuilder = FieldSpec
				.builder(metaModel.getDataType().getType(), CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, metaModel.getName()))
				.addModifiers(Modifier.PRIVATE);
			if (metaModel.isPrimaryKey()) {
				fieldBuilder.addAnnotation(AnnotationSpec.builder(Id.class).build());
				fieldBuilder.addAnnotation(AnnotationSpec.builder(GeneratedValue.class)
					.addMember("strategy", CodeBlock.builder().add("$T.IDENTITY", ClassName.bestGuess("javax.persistence.GenerationType")).build())
					.build());
			}
			AnnotationSpec.Builder apiModelProperty = AnnotationSpec.builder(ApiModelProperty.class);
			if (Objects.nonNull(metaModel.getDescription())) {
				apiModelProperty.addMember("name", "$S", metaModel.getDescription());
			}
			if (metaModel.isRequired() && !metaModel.isPrimaryKey()) {
				fieldBuilder.addAnnotation(metaModel.getDataType().getType().equals(String.class) ? NotBlank.class : NotNull.class);
				apiModelProperty.addMember("required", "$L", metaModel.isRequired());
			}
			fieldBuilder.addAnnotation(apiModelProperty.build()).build();
			AnnotationSpec.Builder column = AnnotationSpec.builder(Column.class).addMember("name", "$S", metaModel.getId());
			if (Objects.nonNull(metaModel.getLength()) && !metaModel.isPrimaryKey()) {
				if (metaModel.getDataType().getType().equals(String.class)) {
					fieldBuilder.addAnnotation(AnnotationSpec.builder(Size.class).addMember("max", "$L", metaModel.getLength()).build());
				}
				column.addMember("length", "$L", metaModel.getLength());
			}
			fieldBuilder.addAnnotation(column.build());
			fields.add(fieldBuilder.build());
		}

		TypeSpec typeSpec = TypeSpec.classBuilder(StringUtils.capitalize(entity))
			.addModifiers(Modifier.PUBLIC)
			.addAnnotation(Data.class)
			.addAnnotation(Entity.class)
			.addAnnotation(AnnotationSpec.builder(Table.class)
				.addMember("name", "$S", model.getId())
				.build())
			.addAnnotation(AnnotationSpec.builder(JsonIgnoreProperties.class)
				.addMember("value", "{$S, $S}", "hibernateLazyInitializer", "handler")
				.build())
			.addFields(fields)
			.build();

		return JavaFile.builder(packageName + ".entities", typeSpec).build();
	}
}
