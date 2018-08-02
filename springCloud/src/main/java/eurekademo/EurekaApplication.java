package eurekademo;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.cloud.netflix.eureka.server.EurekaServerMarkerConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.net.URL;
import java.util.*;

/**
 *
 * @author Gunnar Hillert
 *
 */
@SpringBootApplication
@EnableEurekaServer
@EnableEurekaClient
public class EurekaApplication {
	public static final String FACTORIES_RESOURCE_LOCATION = "META-INF/spring.factories";

	static class PropertyInfo {
		String path;
		List<Name> name;

		public PropertyInfo(String path, List<Name> name) {
			this.path = path;
			this.name = name;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public List<Name> getName() {
			return name;
		}

		public void setName(List<Name> name) {
			this.name = name;
		}
	}

	static class Name {
		String name;

		public Name(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}


	public static void main(String[] args) throws Exception {

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		Enumeration<URL> urls = (classLoader != null ?
				classLoader.getResources(FACTORIES_RESOURCE_LOCATION) :
				ClassLoader.getSystemResources(FACTORIES_RESOURCE_LOCATION));
		Map<String, PropertyInfo> result = new HashMap<>();
		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();
			UrlResource resource = new UrlResource(url);
			Properties properties = PropertiesLoaderUtils.loadProperties(resource);
			for (Map.Entry<?, ?> entry : properties.entrySet()) {
				List<String> factoryClassNames = Arrays.asList(StringUtils.commaDelimitedListToStringArray((String) entry.getValue()));
				List<Name> names = Lists.newArrayList();
				for (String factoryClassName : factoryClassNames) {
					names.add(new Name(factoryClassName));
				}
				result.put((String) entry.getKey(), new PropertyInfo(url.getPath(), names));
			}
		}
		System.out.println(JSON.toJSONString(result));


		ConfigurableApplicationContext context = SpringApplication.run(EurekaApplication.class, args);
		System.out.println(context);
		// 静态读取配置信息
		System.out.println(context.getBean(MyImportSelector.class));
		System.out.println(context.getBean(Market.class));
		System.out.println(context.getBean(Sales.class));
        System.out.println(context.getBean(EurekaServerMarkerConfiguration.class));
    }

}
