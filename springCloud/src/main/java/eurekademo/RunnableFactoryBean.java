package eurekademo;


import org.springframework.beans.factory.FactoryBean;

/**
 * Created by liuya
 *
 * @author User: liuya
 *         Date: 2018/5/3
 *         Time:  23:31
 *         projectName:spring4
 */
public class RunnableFactoryBean implements FactoryBean<User> {


    /**
     * 获取FactoryBean获取的实例对象
     *
     * @return Runnable
     * @throws Exception
     */
    @Override
    public User getObject() throws Exception {
        return new User();
    }

    /**
     * 创建什么类型的对象
     *
     * @return Class<?>
     */
    @Override
    public Class<?> getObjectType() {
        return User.class;
    }


    /**
     * 是不是单例的
     *
     * @return
     */
    @Override
    public boolean isSingleton() {
        return true;
    }
}
