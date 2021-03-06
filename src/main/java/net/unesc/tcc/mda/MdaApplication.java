package net.unesc.tcc.mda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableFeignClients
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class MdaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MdaApplication.class, args);
	}

}
