package com.ddm.ddm_backend;

import com.ddm.ddm_backend.config.RsaKeyConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableConfigurationProperties(RsaKeyConfigProperties.class)
@EnableFeignClients(basePackages = "com.ddm.ddm_backend.util")
@SpringBootApplication
public class DdmBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(DdmBackendApplication.class, args);
	}

}
