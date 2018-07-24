package eurekademo;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(MyImportSelector.class)
/**
 *自定义Enable*
 *1.
 */
public @interface EnableLogInfo {

    String[] name();


}
