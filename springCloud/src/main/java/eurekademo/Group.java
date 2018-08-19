package eurekademo;

import org.springframework.beans.factory.annotation.Value;

/**
 * Created by ym on 2018/8/15.
 */
public class Group {

    private User user;

    private String name;

    @Value("${spring.group.name}")
    private String name2;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }

    @Override
    public String toString() {
        return "Group{" +
                "user=" + user +
                ", name='" + name + '\'' +
                ", name2='" + name2 + '\'' +
                '}';
    }
}
