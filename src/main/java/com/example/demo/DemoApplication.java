package com.example.demo;

import io.dekorate.kubernetes.annotation.ImagePullPolicy;
import io.dekorate.kubernetes.annotation.KubernetesApplication;
import io.dekorate.kubernetes.annotation.Port;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@KubernetesApplication(
		name = "hello-world-fwless-k8s",
		ports = @Port(name = "web", containerPort = 8080),
		expose = true,
		host = "fw-app.127.0.0.1.nip.io",
		imagePullPolicy = ImagePullPolicy.Always
)
@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
