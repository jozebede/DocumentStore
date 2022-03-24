package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.stage4.Document;
import edu.yu.cs.com1320.project.stage4.impl.DocumentImpl;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Comparator;

import static org.junit.Assert.*;

public class TrieImplTest {


    @Test
    public  void test1() {

        TrieImpl t1 = new TrieImpl();

        URI uri1 = URI.create("http://TRY:");
        String str1 = "joey was here and JOEY wrote this!";
        String str2 = "people are awesome !";
        String str3 = "oh wow you are awesome also";
        DocumentImpl doc = new DocumentImpl(uri1, str1, str1.hashCode() );
        DocumentImpl doc2 = new DocumentImpl(uri1, str2, str2.hashCode() );

        String key1 = " joey";
        String key2 = " jos";
        String key3 = " arie";
        String key4 = " ariel";



        t1.put(key1,str1);
        t1.put(key1,str3);
        t1.put(key1,str2);
        t1.put(key2,str1);
        t1.put(key3,str2);
        t1.put(key4,str1);



//t1.getAllSorted(key1, new CompareTo(key1));

    //   t1.deleteAll(key1);

    //    t1.delete(key1,str1);
       // t1.delete(key3,str1);






    }

}
class CompareTo implements Comparator<Document> {
    // compares document from a specific word
    String key;

    public CompareTo(String key) {
        this.key = key;
    }


    @Override
    public int compare(Document o1, Document o2) {
        if  ( o1 == null || o2 == null ){
            return -1;
        }


        int result = o1.wordCount(this.key) - o2.wordCount(this.key);


        return result;
    }
}