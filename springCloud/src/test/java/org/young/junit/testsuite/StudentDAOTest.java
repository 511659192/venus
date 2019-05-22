package org.young.junit.testsuite;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class StudentDAOTest extends TestCase {


    public StudentDAOTest() {
        super();
    }

    public StudentDAOTest(String name) {
        super(name);
    }

    private StudentDAO dao;

    /**
     * 创建 DAO 实例
     */
    public void setUp() {
        dao = new StudentDAOImpl();
    }

    public void testAdd() {
        Student stu = new Student();

        dao.add(stu);
    }

    public void testDelete() {

        dao.delete("id");
    }

    public void testUpdate() {
        Student stu = new Student();

        dao.update(stu);
    }

    public void testLoadWithId() {

        Student stu = dao.load("xyz");

    }

    public void testLoadWithNullOrEmptyStr() {

        Student stu = dao.load("");
        assertNull(stu);

        stu = dao.load(null);
        assertNull(stu);
    }


}

class Suit1 {

    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite("All Test");

        /*
         * StudentDAOTest 类的全部测试方法
         */
        suite.addTest(new StudentDAOTest("testDelete"));
        /*
         * CourseDAOTest 类的部分方法
         */
        suite.addTest(new CourseDAOTest("testAdd"));

        return suite;
    }

    public static void main(String[] args) {
        TestRunner.run(Suit1.suite());
    }
}
