package net.unesc.tcc.mda.services;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "start-spring", url = "https://start.spring.io")
public interface StartSpringClient {

	//https://start.spring.io/starter.zip?type=maven-project&language=java&bootVersion=2.5.5&baseDir=demo&groupId=com.example&artifactId=demo&name=demo&description=Demo%20project%20for%20Spring%20Boot&packageName=com.example.demo&packaging=jar&javaVersion=1.8&dependencies=lombok,data-jpa,postgresql
	@RequestMapping(method= RequestMethod.GET, value="/starter.zip?type=maven-project&language=java&bootVersion=2.5.4&baseDir=demo&groupId=com.example&artifactId=demo&name=demo&description=Demo%20project%20for%20Spring%20Boot&packageName=com.example.demo&packaging=jar&javaVersion=11")
	ResponseEntity<byte[]> download();

}
