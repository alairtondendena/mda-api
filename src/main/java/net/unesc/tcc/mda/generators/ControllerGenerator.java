package net.unesc.tcc.mda.generators;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.lang.model.element.Modifier;
import net.unesc.tcc.mda.core.MdaMetaModel;
import net.unesc.tcc.mda.core.MdaModel;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

public class ControllerGenerator implements MdaGenerator {

	@Override
	public void generate(MdaModel model) {
		String entity = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, model.getName());
		String name = StringUtils.capitalize(entity) + "Controller";
		MdaMetaModel pkMetaModel = model.getAttributes().stream().filter(MdaMetaModel::isPrimaryKey).findFirst().orElseThrow(RuntimeException::new);
		String pkAttributeName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, pkMetaModel.getName());
		TypeSpec typeSpec = TypeSpec.classBuilder(name)
			.addModifiers(Modifier.PUBLIC)
			.addAnnotation(RestController.class)
			.addAnnotation(AnnotationSpec.builder(RequestMapping.class)
				.addMember("value", "$S", "/" + entity)
				.build())
			.addField(FieldSpec
				.builder(ClassName.get("com.exemple.demo.repositories", StringUtils.capitalize(entity) + "Repository"), entity + "Repository")
				.addModifiers(Modifier.PRIVATE)
				.addAnnotation(AnnotationSpec.builder(Autowired.class).build())
				.build())
			.addMethod(MethodSpec.methodBuilder("get")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(AnnotationSpec.builder(GetMapping.class)
					.addMember("path", "$S", "/{id}")
					.build())
				.addParameter(ParameterSpec.builder(pkMetaModel.getDataType().getType(), pkAttributeName)
					.addAnnotation(
						AnnotationSpec.builder(PathVariable.class)
							.addMember("value", "$S", pkAttributeName)
							.build()
					)
					.build())
				.returns(
					ParameterizedTypeName.get(ClassName.get(ResponseEntity.class),
					ParameterizedTypeName.get(ClassName.get(Page.class), ClassName.get("com.exemple.demo.entities", StringUtils.capitalize(entity))))
				)
				.addCode("return ResponseEntity.ok(" + entity + "Repository.findById(id));")
				.build())
			.addMethod(MethodSpec.methodBuilder("create")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(PostMapping.class)
				.addParameter(ParameterSpec
					.builder(ClassName.get("com.exemple.demo.entities", StringUtils.capitalize(entity)), "entity")
					.addAnnotation(RequestBody.class)
					.build())
				.returns(
					ParameterizedTypeName.get(ClassName.get(ResponseEntity.class),
						ClassName.get("com.exemple.demo.entities", StringUtils.capitalize(entity)))
				)
				.addCode("return ResponseEntity.ok(" + entity + "Repository.save(entity));")
				.build())
			.addMethod(MethodSpec.methodBuilder("update")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(AnnotationSpec.builder(PutMapping.class)
					.addMember("path", "$S", "/{id}")
					.build())
				.addParameter(ParameterSpec
					.builder(ClassName.get("com.exemple.demo.entities", StringUtils.capitalize(entity)), "entity")
					.addAnnotation(RequestBody.class)
					.build())
				.returns(
					ParameterizedTypeName.get(ClassName.get(ResponseEntity.class),
						ClassName.get("com.exemple.demo.entities", StringUtils.capitalize(entity)))
				)
				.addCode("return ResponseEntity.ok(" + entity + "Repository.save(entity));")
				.build())
			.addMethod(MethodSpec.methodBuilder("delete")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(AnnotationSpec.builder(DeleteMapping.class)
					.addMember("path", "$S", "/{id}")
					.build())
				.addParameter(ParameterSpec.builder(pkMetaModel.getDataType().getType(), pkAttributeName)
					.addAnnotation(
						AnnotationSpec.builder(PathVariable.class)
							.addMember("value", "$S", pkAttributeName)
							.build()
					)
					.build())
				.returns(
					ParameterizedTypeName.get(ClassName.get(ResponseEntity.class),
						ClassName.get("com.exemple.demo.entities", StringUtils.capitalize(entity)))
				)
				.addCode(entity + "Repository.deleteById(id);")
				.addCode("return ResponseEntity.noContent().build();")
				.build())
			.build();
		JavaFile javaFile = JavaFile.builder("com.exemple.demo.controllers", typeSpec).build();
		String path = "target" + File.separator + "generated-mda-projects" + File.separator + "teste" + File.separator + "demo" + File.separator + "src" +
			File.separator + "main" + File.separator + "java" + File.separator + "com" + File.separator + "example" + File.separator + "demo" +
			File.separator + "controllers";

		File directory = new File(path);
		if (!directory.exists()) {
			directory.mkdir();
		}

		try {
			FileWriter file = new FileWriter(new File(path + File.separator + name + ".java"));
			file.write(javaFile.toString());
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
