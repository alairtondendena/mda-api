package net.unesc.tcc.mda.resources;

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
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.ValidationException;
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
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "MDA")
@RestController
@RequestMapping("/api")
public class MdaController {

	private final StartSpringClient startSpringClient;

	public MdaController(StartSpringClient startSpringClient) {
		this.startSpringClient = startSpringClient;
	}

	@ApiOperation(value = "Create")
	@PostMapping("/create")
	public ResponseEntity<MdaProjectResponse> create(@Valid @RequestBody MdaProjectRequest request) {
		String source = "target" + File.separator + "start-spring" + File.separator + request.getName() + ".zip";
		String destination = "target" + File.separator + "generated-mda-projects" + File.separator + request.getName();

		ResponseEntity<byte[]> resource = startSpringClient.download();
		if (resource.getStatusCode().is2xxSuccessful()) {
			try {
				FileUtils.writeByteArrayToFile(new File(source), Objects.requireNonNull(resource.getBody()));
				ZipFile zipFile = new ZipFile(source);
				zipFile.extractAll(destination);
			} catch (IOException e) {
				e.printStackTrace();
				ResponseEntity.badRequest();
			}
		}

		try {
			MavenXpp3Reader reader = new MavenXpp3Reader();
			Model model = reader.read(new FileReader(destination + File.separator + "demo" + File.separator + "pom.xml"));

			Dependency dependency = new Dependency();
			dependency.setGroupId("io.springfox");
			dependency.setArtifactId("springfox-swagger-ui");
			dependency.setVersion("2.9.2");
			model.addDependency(dependency);

			MavenXpp3Writer writer = new MavenXpp3Writer();
			writer.write(new FileWriter(destination + File.separator + "demo" + File.separator + "pom.xml"), model);
			System.out.println("feito!");
		} catch (XmlPullParserException | IOException e) {
			e.printStackTrace();
			ResponseEntity.badRequest();
		}

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
						rsColumns.getString("column_def"),
						primaryKeys.contains(rsColumns.getString("column_name")),
						rsColumns.getString("is_nullable").equals(YesNo.YES.getValue()),
						DataType.of(rsColumns.getString("type_name")),
						rsColumns.getInt("column_size"),
						rsColumns.getInt("column_size"),
						rsColumns.getInt("decimal_digits"),
						rsColumns.getString("remarks")));
				}

				models.add(new MdaModel(tableName, metaModels));
			}
			Set<MdaAttributeTypes> mdaAttributeTypes = new LinkedHashSet<>();
			for (DataType type : DataType.values()) {
				mdaAttributeTypes.add(new MdaAttributeTypes(type.name(), type.getValue()));
			}
			return ResponseEntity.ok(new MdaProjectResponse(request.getName(), models, mdaAttributeTypes));
		} catch (SQLException e) {
			throw new ValidationException("Erro ao conectar com a base de dados");
		}
	}

	@ApiOperation(value = "Generate")
	@PostMapping("/generate")
	public ResponseEntity<byte[]> generate(@Valid @RequestBody MdaModelRequest request) {
		Set<MdaGenerator> generators = new LinkedHashSet<>();
		generators.add(new EntityGenerator());
		generators.add(new RepositoryGenerator());
		generators.add(new ServiceGenerator());
		generators.add(new ControllerGenerator());

		for (MdaModel model : request.getModels()) {
			for (MdaGenerator generator : generators) {
				generator.generate(model);
			}
		}

		String source = "target" + File.separator + "generated-mda-projects" + File.separator + request.getName();
		String destination = "target" + File.separator + "mda-projects" + File.separator + request.getName() + ".zip";

		byte[] content = new byte[0];
		ZipFile result = new ZipFile(destination);
		File directory = new File("target" + File.separator + "mda-projects");

		try {
			if (!directory.exists()) {
				directory.mkdir();
			}
			result.addFolder(new File(source), new ZipParameters());
			content = FileUtils.readFileToByteArray(new File(destination));
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body(content);
		}

		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + request.getName() + ".zip\"")
			.contentType(MediaType.APPLICATION_OCTET_STREAM)
			.contentLength(content.length)
			.body(content);
	}
}
