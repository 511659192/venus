package eurekademo;

import org.springframework.beans.factory.annotation.Value;

/**
 * Created by ym on 2018/8/15.
 */
public class User {

    @Value("${spring.user.name}")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User() {

        System.out.println(this.name);
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                '}';
    }
}
