package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import org.junit.Test;
import static org.junit.Assert.*;

public class MinHeapImplTest {

    @Test
    public void Test1() {

        MinHeapImpl mp = new MinHeapImpl();

            int x = 8;
            int y = 15;

        mp.insert(1);
        mp.insert(9);
        mp.insert(8);
       mp.insert(3);
        mp.insert(y);
       mp.insert(10);
        mp.insert(7);
        mp.insert(16);
        mp.insert(22);



            mp.removeMin();
         mp.removeMin();

         mp.removeMin();

         mp.removeMin();









    }


}
