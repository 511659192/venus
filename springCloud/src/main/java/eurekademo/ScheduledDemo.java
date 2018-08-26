package eurekademo;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

//@EnableScheduling
//@Component
//@EnableCaching
public class ScheduledDemo {

    @Scheduled(fixedRate = 1000)
    public void executeFileDownLoadTask() {
        Thread current = Thread.currentThread();
        System.out.println("定时任务1:"+current.getId());
    }
}
