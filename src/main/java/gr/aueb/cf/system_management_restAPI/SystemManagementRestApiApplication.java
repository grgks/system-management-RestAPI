package gr.aueb.cf.system_management_restAPI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class SystemManagementRestApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SystemManagementRestApiApplication.class, args);
	}

}
