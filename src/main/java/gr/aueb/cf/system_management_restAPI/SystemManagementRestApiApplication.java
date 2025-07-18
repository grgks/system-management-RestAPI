package gr.aueb.cf.system_management_restAPI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SystemManagementRestApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SystemManagementRestApiApplication.class, args);
	}

}
