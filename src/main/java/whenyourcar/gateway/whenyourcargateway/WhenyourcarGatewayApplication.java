package whenyourcar.gateway.whenyourcargateway;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class WhenyourcarGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(WhenyourcarGatewayApplication.class, args);
    }

}
