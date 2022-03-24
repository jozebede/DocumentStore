package edu.yu.cs.com1320.project.impl;

import static org.junit.Assert.*;
import edu.yu.cs.com1320.project.HashTable;
import org.junit.Test;

public class HashTableImplTest {

    @Test
    public void test1(){
//
       HashTableImpl g1 = new HashTableImpl();
        g1.put(12,"joey");
        g1.put(2,"mike");
//        g1.put(7,"juan");
       g1.put(7,"jose");
     //   g1.put(7,"mario");

        g1.put(1,"juanito");
        g1.put(6,"rico");
        g1.put(8,"pedro");

       assertEquals("jose",g1.put(7, "Juan") );
        assertEquals("juanito", g1.get(1));
        assertEquals("pedro", g1.get(8));

//        /



    }




    @org.junit.Test
    public void get() {

    }

    @org.junit.Test
    public void put() {

    }
}