package eurekademo;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by ym on 2018/8/21.
 */
//@Component
@EnableScheduling
@EnableCaching
public class ScheduledTasks{

    @Scheduled(fixedRate = 1000)
    public void reportCurrentTime(){
        System.out.println ("Scheduling Tasks Examples: The time is now " + dateFormat ().format (new Date()) + " thread " + Thread.currentThread().getId());
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //每1分钟执行一次
//    @Scheduled(cron = "*/1 * *  * * * ")
    public void reportCurrentByCron(){
        System.out.println ("reportCurrentByCron1: The time is now " + dateFormat ().format (new Date ()) + " thread " + Thread.currentThread().getId());
    }

    private SimpleDateFormat dateFormat(){
        return new SimpleDateFormat ("HH:mm:ss");
    }

}