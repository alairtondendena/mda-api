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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.lang.model.element.Modifier;
import javax.validation.Valid;
import net.unesc.tcc.mda.core.MdaMetaModel;
import net.unesc.tcc.mda.core.MdaModel;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
	public JavaFile generate(String packageName, MdaModel model) {
		String entity = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, model.getName());
		String name = StringUtils.capitalize(entity) + "Controller";
		MdaMetaModel pkMetaModel = model.getAttributes().stream().filter(MdaMetaModel::isPrimaryKey).findFirst().orElseThrow(RuntimeException::new);
		String pkAttributeName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, pkMetaModel.getName());
		TypeSpec typeSpec = TypeSpec.classBuilder(name)
			.addModifiers(Modifier.PUBLIC)
			.addAnnotation(AnnotationSpec.builder(Api.class)
				.addMember("value", "$S", StringUtils.capitalize(entity))
				.build())
			.addAnnotation(RestController.class)
			.addAnnotation(AnnotationSpec.builder(RequestMapping.class)
				.addMember("value", "$S", "/api/" + entity)
				.build())
			.addField(FieldSpec
				.builder(ClassName.get(packageName + ".repositories", StringUtils.capitalize(entity) + "Repository"), entity + "Repository")
				.addModifiers(Modifier.PRIVATE)
				.addAnnotation(AnnotationSpec.builder(Autowired.class).build())
				.build())
			.addMethod(MethodSpec.methodBuilder("get")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(AnnotationSpec.builder(ApiOperation.class)
					.addMember("value", "$S", "Buscar " + entity)
					.build())
				.addAnnotation(AnnotationSpec.builder(GetMapping.class).build())
				.addParameter(ParameterSpec.builder(Pageable.class, "pageable").build())
				.returns(
					ParameterizedTypeName.get(ClassName.get(ResponseEntity.class),
						ParameterizedTypeName.get(ClassName.get(Page.class), ClassName.get(packageName + ".entities", StringUtils.capitalize(entity))))
				)
				.addCode("return ResponseEntity.ok(" + entity + "Repository.findAll(pageable));")
				.build())
			.addMethod(MethodSpec.methodBuilder("get")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(AnnotationSpec.builder(ApiOperation.class)
					.addMember("value", "$S", "Buscar " + entity + " por ID")
					.build())
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
						ClassName.get(packageName + ".entities", StringUtils.capitalize(entity)))
				)
				.addCode("return ResponseEntity.ok(" + entity + "Repository.getById(id));")
				.build())
			.addMethod(MethodSpec.methodBuilder("create")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(PostMapping.class)
				.addAnnotation(AnnotationSpec.builder(ApiOperation.class)
					.addMember("value", "$S", "Cadastrar " + entity)
					.build())
				.addParameter(ParameterSpec
					.builder(ClassName.get(packageName + ".entities", StringUtils.capitalize(entity)), "entity")
					.addAnnotation(Valid.class)
					.addAnnotation(RequestBody.class)
					.build())
				.returns(
					ParameterizedTypeName.get(ClassName.get(ResponseEntity.class),
						ClassName.get(packageName + ".entities", StringUtils.capitalize(entity)))
				)
				.addCode("return ResponseEntity.ok(" + entity + "Repository.save(entity));")
				.build())
			.addMethod(MethodSpec.methodBuilder("update")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(AnnotationSpec.builder(ApiOperation.class)
					.addMember("value", "$S", "Atualizar " + entity)
					.build())
				.addAnnotation(AnnotationSpec.builder(PutMapping.class)
					.addMember("path", "$S", "/{id}")
					.build())
				.addParameter(ParameterSpec
					.builder(ClassName.get(packageName + ".entities", StringUtils.capitalize(entity)), "entity")
					.addAnnotation(Valid.class)
					.addAnnotation(RequestBody.class)
					.build())
				.returns(
					ParameterizedTypeName.get(ClassName.get(ResponseEntity.class),
						ClassName.get(packageName + ".entities", StringUtils.capitalize(entity)))
				)
				.addCode("return ResponseEntity.ok(" + entity + "Repository.save(entity));")
				.build())
			.addMethod(MethodSpec.methodBuilder("delete")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(AnnotationSpec.builder(ApiOperation.class)
					.addMember("value", "$S", "Remover " + entity)
					.build())
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
						ClassName.get(packageName + ".entities", StringUtils.capitalize(entity)))
				)
				.addCode(entity + "Repository.deleteById(id);")
				.addCode("return ResponseEntity.noContent().build();")
				.build())
			.build();
		return JavaFile.builder(packageName + ".controllers", typeSpec).build();
	}
}
