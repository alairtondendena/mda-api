package net.unesc.tcc.mda.resources;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.lang.model.element.Modifier;
import javax.validation.Valid;
import javax.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.unesc.tcc.mda.core.MdaAttributeTypes;
import net.unesc.tcc.mda.core.MdaMetaModel;
import net.unesc.tcc.mda.core.MdaModel;
import net.unesc.tcc.mda.core.enums.DataType;
import net.unesc.tcc.mda.core.enums.YesNo;
import net.unesc.tcc.mda.generators.ControllerGenerator;
import net.unesc.tcc.mda.generators.EntityGenerator;
import net.unesc.tcc.mda.generators.MdaGenerator;
import net.unesc.tcc.mda.generators.RepositoryGenerator;
import net.unesc.tcc.mda.generators.ServiceGenerator;
import net.unesc.tcc.mda.services.StartSpringClient;
import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Slf4j
@Api(value = "MDA")
@RestController
@RequestMapping("/api")
public class MdaController {

	private final StartSpringClient startSpringClient;

	public MdaController(StartSpringClient startSpringClient) {
		this.startSpringClient = startSpringClient;
	}

	@ApiOperation(value = "Extrair metadados")
	@PostMapping("/extract-metadata")
	public ResponseEntity<MdaModelResponse> create(@Valid @RequestBody MdaDatabaseCredentialsRequest request) {
		try (Connection conn = DriverManager.getConnection(request.getUrl(), request.getUsername(), request.getPassword())) {
			DatabaseMetaData meta = conn.getMetaData();
			String catalog = null, schemaPattern = null, tableNamePattern = null;
			String[] types = {"TABLE"};
			ResultSet rsTables = meta.getTables(catalog, schemaPattern, tableNamePattern, types);
			Set<MdaModel> models = new LinkedHashSet<>();
			while (rsTables.next()) {
				String tableName = rsTables.getString(3);
				String columnNamePattern = null;
				ResultSet rsPK = meta.getPrimaryKeys(catalog, schemaPattern, tableName);
				Set<String> primaryKeys = new LinkedHashSet<>();
				while (rsPK.next()) {
					primaryKeys.add(rsPK.getString("column_name"));
				}
				ResultSet rsColumns = meta.getColumns(catalog, schemaPattern, tableName, columnNamePattern);
				Set<MdaMetaModel> metaModels = new LinkedHashSet<>();
				while (rsColumns.next()) {
					metaModels.add(new MdaMetaModel(
						rsColumns.getString("column_name"),
						rsColumns.getString("column_name"),
						rsColumns.getString("column_def"),
						primaryKeys.contains(rsColumns.getString("column_name")),
						rsColumns.getString("is_nullable").equals(YesNo.YES.getValue()),
						DataType.of(rsColumns.getString("type_name")),
						rsColumns.getInt("column_size"),
						rsColumns.getInt("decimal_digits"),
						rsColumns.getString("remarks")));
				}
				models.add(new MdaModel(tableName, tableName, metaModels));
			}
			Set<MdaAttributeTypes> mdaAttributeTypes = new LinkedHashSet<>();
			for (DataType type : DataType.values()) {
				mdaAttributeTypes.add(new MdaAttributeTypes(type.name(), type.getValue()));
			}
			return ResponseEntity.ok(new MdaModelResponse(models, mdaAttributeTypes));
		} catch (SQLException e) {
			throw new ValidationException("Erro ao conectar com a base de dados");
		}
	}

	@ApiOperation(value = "Generate")
	@PostMapping("/generate")
	public ResponseEntity<byte[]> generate(@Valid @RequestBody MdaProjectRequest request) {
		String uuid = Instant.now().plusSeconds(30).toEpochMilli() + "-" + UUID.randomUUID().toString();
		log.info(uuid);
		String start = "target" + File.separator + "start-spring" + File.separator + uuid + ".zip";
		String generated = "target" + File.separator + "generated-mda-projects" + File.separator + uuid;
		String generatedProject = generated + File.separator + request.getName();
		String projects = "target" + File.separator + "mda-projects";
		String project = "target" + File.separator + "mda-projects" + File.separator + uuid + ".zip";

		byte[] content = new byte[0];
		ResponseEntity<byte[]> resource = startSpringClient
			.download(request.getName(), request.getGroup(), request.getArtifact(), request.getName(), request.getPackageName());
		if (resource.getStatusCode().is2xxSuccessful()) {
			try {
				FileUtils.writeByteArrayToFile(new File(start), Objects.requireNonNull(resource.getBody()));
				ZipFile zipFile = new ZipFile(start);
				zipFile.extractAll(generated);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
				return ResponseEntity.badRequest().body(content);
			}
		}

		try {
			MavenXpp3Reader reader = new MavenXpp3Reader();
			FileReader fileReader = new FileReader(generatedProject + File.separator + "pom.xml");
			Model model = reader.read(fileReader);

			Dependency dependency = new Dependency();
			dependency.setGroupId("io.springfox");
			dependency.setArtifactId("springfox-swagger2");
			dependency.setVersion("2.9.2");
			model.addDependency(dependency);

			Dependency dependency2 = new Dependency();
			dependency2.setGroupId("io.springfox");
			dependency2.setArtifactId("springfox-swagger-ui");
			dependency2.setVersion("2.9.2");
			model.addDependency(dependency2);

			MavenXpp3Writer writer = new MavenXpp3Writer();
			FileWriter fileWriter = new FileWriter(generatedProject + File.separator + "pom.xml");
			writer.write(fileWriter, model);
			fileReader.close();
			fileWriter.close();
		} catch (XmlPullParserException | IOException e) {
			log.error(e.getMessage(), e);
			return ResponseEntity.badRequest().body(content);
		}

		try {
			String applicationProperties =
				generatedProject + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "application.properties";
			MdaDatabaseCredentialsRequest databaseCredentials = request.getDatabaseCredentials();
			FileWriter file = new FileWriter(new File(applicationProperties));
			String applicationPropertiesContent = "spring.datasource.url=" + databaseCredentials.getUrl() + System.lineSeparator()
				+ "spring.datasource.username=" + databaseCredentials.getUsername() + System.lineSeparator()
				+ "spring.datasource.password=" + databaseCredentials.getPassword();
			file.write(applicationPropertiesContent);
			file.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return ResponseEntity.badRequest().body(content);
		}

		TypeSpec typeSpec = TypeSpec.classBuilder("SwaggerConfig")
			.addAnnotation(Configuration.class)
			.addAnnotation(EnableSwagger2.class)
			.addModifiers(Modifier.PUBLIC)
			.addMethod(MethodSpec.methodBuilder("api")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(AnnotationSpec.builder(Bean.class).build())
				.returns(ClassName.get(Docket.class))
				.addCode("return new $T($T.SWAGGER_2).select().apis($T.basePackage($S)).paths($T.ant(\"/api/**\")).build();",
					Docket.class,
					DocumentationType.class,
					RequestHandlerSelectors.class,
					request.getPackageName() + ".controllers",
					PathSelectors.class)
				.build())
			.build();
		String pathConfig = generatedProject + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator +
			request.getPackageName().replace(".", File.separator) + File.separator + "config";
		File directoryConfig = new File(pathConfig);
		if (!directoryConfig.exists()) {
			directoryConfig.mkdir();
		}
		JavaFile javaFileSwaggerConfig = JavaFile.builder(request.getPackageName() + ".config", typeSpec).build();
		try {
			FileWriter file = new FileWriter(new File(pathConfig + File.separator + javaFileSwaggerConfig.typeSpec.name + ".java"));
			file.write(javaFileSwaggerConfig.toString());
			file.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return ResponseEntity.badRequest().body(content);
		}

		Set<MdaGenerator> generators = new LinkedHashSet<>();
		generators.add(new EntityGenerator());
		generators.add(new RepositoryGenerator());
		generators.add(new ServiceGenerator());
		generators.add(new ControllerGenerator());

		for (MdaModel model : request.getModels()) {
			long eStartTime = System.currentTimeMillis();
			log.info("------------------------------");
			log.info("| Entidade: " + model.getId());
			log.info("| Atributos (un):" + model.getAttributes().size());
			for (MdaGenerator generator : generators) {
				long gStartTime = System.currentTimeMillis();
				JavaFile javaFile = generator.generate(request.getPackageName(), model);
				String path = generatedProject + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator +
					javaFile.packageName.replace(".", File.separator);

				File directory = new File(path);
				if (!directory.exists()) {
					directory.mkdir();
				}

				try {
					FileWriter file = new FileWriter(new File(path + File.separator + javaFile.typeSpec.name + ".java"));
					file.write(javaFile.toString());
					file.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
					return ResponseEntity.badRequest().body(content);
				}
				long gEndTime = System.currentTimeMillis();
				log.info("| " + javaFile.typeSpec.name + " (ms): " + (gEndTime - gStartTime) + " ms");
			}
			long eEndTime = System.currentTimeMillis();
			log.info("| Total (ms): " + (eEndTime - eStartTime) + " ms");
			log.info("------------------------------");
		}

		ZipFile result = new ZipFile(project);
		File directory = new File(projects);

		try {
			if (!directory.exists()) {
				directory.mkdir();
			}
			result.addFolder(new File(generated + File.separator + request.getName()), new ZipParameters());
			content = FileUtils.readFileToByteArray(new File(project));
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return ResponseEntity.badRequest().body(content);
		}

		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + request.getName() + ".zip\"")
			.contentType(MediaType.APPLICATION_OCTET_STREAM)
			.contentLength(content.length)
			.body(content);
	}
}
