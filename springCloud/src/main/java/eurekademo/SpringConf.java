package eurekademo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by ym on 2018/8/15.
 */
@Configuration
@PropertySource(value = "classpath:aa.properties")
public class SpringConf {

    @Bean
    @ConfigurationProperties(prefix = "spring.user")
    public RunnableFactoryBean createRunnableFactoryBean() {
        RunnableFactoryBean runnableFactoryBean = new RunnableFactoryBean();
        return runnableFactoryBean;
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.group")
    public Group getGroup(User user) {
        Group group = new Group();
        group.setUser(user);
        return group;
    }

}
