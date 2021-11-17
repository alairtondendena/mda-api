package net.unesc.tcc.mda.services;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "start-spring", url = "https://start.spring.io")
public interface StartSpringClient {

	@RequestMapping(method = RequestMethod.GET, value = "starter.zip?type=maven-project&language=java&bootVersion=2.5.6&baseDir={baseDir}&groupId={groupId}&artifactId={artifactId}&name={name}&description=MDA%20project%20for%20Spring%20Boot&packageName={packageName}&packaging=jar&javaVersion=1.8&dependencies=lombok,data-jpa,validation,web,postgresql")
	ResponseEntity<byte[]> download(@PathVariable("baseDir") String baseDir, @PathVariable("groupId") String groupId,
		@PathVariable("artifactId") String artifactId, @PathVariable("name") String name, @PathVariable("packageName") String packageName);

}
