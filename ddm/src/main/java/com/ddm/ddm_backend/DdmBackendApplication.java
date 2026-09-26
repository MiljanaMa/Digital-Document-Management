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

	// no-op change: Argo CD Image Updater digest-detection demo (master's thesis, §4.5)
	public static void main(String[] args) {
		SpringApplication.run(DdmBackendApplication.class, args);
	}

}
