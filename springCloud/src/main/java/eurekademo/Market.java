package eurekademo;

/**
 * Created by ym on 2018/7/21.
 */
public class Market {

    public static void main(String[] args) {
        System.out.println(Market.class.getClassLoader().getResource("").getPath());
    }

}