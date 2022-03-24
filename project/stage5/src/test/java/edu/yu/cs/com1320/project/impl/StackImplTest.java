package test.edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.impl.StackImpl;
import org.junit.Test;

import static org.junit.Assert.*;

public class StackImplTest {

    @Test
    public void test1() {

        StackImpl s1 = new StackImpl();

        s1.push("joe");
        s1.push("joe1");
        s1.push("joe2");
        s1.push("joe3");
        s1.push("joe4");
        s1.push("joe5");
        s1.push("joe6");

        s1.peek();
        s1.pop();
        s1.push("try7");
        s1.peek();
        s1.pop();
        s1.peek();
        s1.size();













    }


}