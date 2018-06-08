package com.example.demo.spring;

import com.alibaba.fastjson.JSON;
import javafx.scene.media.SubtitleTrack;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;
import org.springframework.expression.*;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.*;

public class SpelDemo {

    public static void main(String[] args) {
        SpelExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression("T(java.lang.Math).random() * 100.0");
        System.out.println(expression.getValue());

        TestBean testBean = new TestBean();
        testBean.setName("aaa");
        EvaluationContext context = new StandardEvaluationContext(testBean); // 可以缓存
        expression = parser.parseExpression("#root.name");
        System.out.println(expression.getValue(context));
    }

    @Test
    public void testText() {
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression("'Hello World'");
        String message = (String) exp.getValue();
        System.out.println("simple:" + message);

        exp = parser.parseExpression("'Hello World'.concat('!')");
        message = (String) exp.getValue();
        System.out.println("concat:" + message);

        exp = parser.parseExpression("'Hello World'.bytes");
        byte[] bytes = (byte[]) exp.getValue();
        System.out.println("bytes:" + new String(bytes));

        exp = parser.parseExpression("'Hello World'.bytes.length");
        int length = (Integer) exp.getValue();
        System.out.println("length:" + length);

        exp = parser.parseExpression("new String('hello world').toUpperCase()");
        message = exp.getValue(String.class);
        System.out.println("toUpperCase:" + message);

        Inventor tesla = new Inventor("Nikola Tesla", new Date(), "Serbian");
        exp = parser.parseExpression("name");
        EvaluationContext context = new StandardEvaluationContext(tesla); // 可以缓存
        String name = (String) exp.getValue(context);
        System.out.println("context:" + name);

        name = (String) exp.getValue(tesla); // 作用域是每个解析
        System.out.println("getValueFromContext:" + name);

        exp = parser.parseExpression("name == 'Nikola Tesla'");
        boolean result = exp.getValue(context, Boolean.class);
        System.out.println("booleanValue" + result);
    }

    @Test
    public void testThis() {
        List<Integer> primes = new ArrayList<Integer>();
        primes.addAll(Arrays.asList(2,3,5,7,11,13,17));

        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("primes",primes);

        List<Integer> primesGreaterThanTen = (List<Integer>) parser.parseExpression("#primes.?[#this>10]").getValue(context);
        System.out.println(JSON.toJSONString(primesGreaterThanTen));
    }

    @Test
    public void testSetValue() {
        Simple simple = new Simple();
        simple.booleanList.add(true);
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext simpleContext = new StandardEvaluationContext(simple);
        parser.parseExpression("booleanList[0]").setValue(simpleContext, "false");
        Boolean b = simple.booleanList.get(0);
        System.out.println(b);
    }


    @Test
    public void testType() {
        ExpressionParser parser = new SpelExpressionParser();
        Class dateClass = parser.parseExpression("T(java.util.Date)").getValue(Class.class);
        Class stringClass = parser.parseExpression("T(String)").getValue(Class.class);
        boolean trueValue = parser.parseExpression("T(java.math.RoundingMode).CEILING < T(java.math.RoundingMode).FLOOR").getValue(Boolean.class);
    }

    @Test
    public void testConstructor() {
        ExpressionParser parser = new SpelExpressionParser();
        Inventor einstein = parser.parseExpression("new org.spring.samples.spel.inventor.Inventor('Albert Einstein', 'German')")
                .getValue(Inventor.class);
        System.out.println(JSON.toJSONString(einstein));
    }

    @Test
    public void testVar() {
        ExpressionParser parser = new SpelExpressionParser();
        Inventor tesla = new Inventor("Nikola Tesla", new Date(), "Serbian");
        StandardEvaluationContext context = new StandardEvaluationContext(tesla);
        context.setVariable("newName", "Mike Tesla");
        parser.parseExpression("Name = #newName").getValue(context);
        System.out.println(tesla.getName()); // "Mike Tesla"
    }

    @Test
    public void testBeanRef() {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setBeanResolver(new BeanResolver() {
            @Override
            public Object resolve(EvaluationContext context, String beanName) throws AccessException {
                return new TestBean();
            }
        });

        Object bean = parser.parseExpression("@name").getValue(context);
        System.out.println(JSON.toJSONString(bean));
    }

    @Test
    public void testInstanceOf() {
        ExpressionParser parser = new SpelExpressionParser();
        boolean falseValue = parser.parseExpression("'xyz' instanceof T(int)").getValue(Boolean.class);
        System.out.println(falseValue);
    }

    @Test
    public void testMatch() {
        ExpressionParser parser = new SpelExpressionParser();
        boolean trueValue = parser.parseExpression("'5.00' matches '^-?\\d+(\\.\\d{2})?$'").getValue(Boolean.class);
        System.out.println(trueValue);
        boolean falseValue = parser.parseExpression("'5.0067' matches '\\^-?\\d+(\\.\\d{2})?$'").getValue(Boolean.class);
        System.out.println(falseValue);
    }

    private class Simple {
        public List<Boolean> booleanList = new ArrayList<Boolean>();
    }

    private class Inventor {

        private String name;
        private Date birthDay;
        private String address;

        public Inventor(String name, Date birthDay, String address) {
            this.name = name;
            this.birthDay = birthDay;
            this.address = address;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Date getBirthDay() {
            return birthDay;
        }

        public void setBirthDay(Date birthDay) {
            this.birthDay = birthDay;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }
}
