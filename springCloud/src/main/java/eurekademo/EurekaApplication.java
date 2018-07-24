package eurekademo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.cloud.netflix.eureka.server.EurekaServerMarkerConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

/**
 *
 * @author Gunnar Hillert
 *
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaApplication {

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext context = SpringApplication.run(EurekaApplication.class, args);
		System.out.println(context);
		// 静态读取配置信息
		System.out.println(context.getBean(MyImportSelector.class));
		System.out.println(context.getBean(Market.class));
		System.out.println(context.getBean(Sales.class));
        System.out.println(context.getBean(EurekaServerMarkerConfiguration.class));
    }

}
