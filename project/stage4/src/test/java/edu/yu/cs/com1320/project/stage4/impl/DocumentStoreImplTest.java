package edu.yu.cs.com1320.project.stage4.impl;

import edu.yu.cs.com1320.project.stage4.DocumentStore;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import org.junit.Test;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DocumentStoreImplTest {

    @Test
  public void test1() throws IOException {
        DocumentStoreImpl ds = new DocumentStoreImpl();

        ByteArrayInputStream in1 = new ByteArrayInputStream("How are you ? i need to know this please, yourself?".getBytes());
        ByteArrayInputStream in2 = new ByteArrayInputStream("now you what to go there".getBytes());
        ByteArrayInputStream in3 = new ByteArrayInputStream("now what guys".getBytes());
        ByteArrayInputStream in4 = new ByteArrayInputStream("ready for going there".getBytes());
        ByteArrayInputStream in5 = new ByteArrayInputStream("this is only a test".getBytes());

        File PDFF = new File ("C:/Users/jozeb/OneDrive/Documents/HelloWorld.pdf");
        InputStream pdIn = new FileInputStream(PDFF);

        URI uri1 = URI.create("http://TRY:");
        URI uri2 = URI.create("http://TRY2:");
        URI uri3 = URI.create("http://TRY3:");
        URI uri4 = URI.create("http://TRY4:");
        URI uri5 = URI.create("http://TRY5:");
        URI uri6 = URI.create("http://TRY6:");




        //ds.putDocument(pdIn, uri2, DocumentStore.DocumentFormat.PDF);
        ds.putDocument(in1, uri3, DocumentStore.DocumentFormat.TXT);
         ds.putDocument(in2, uri1, DocumentStore.DocumentFormat.TXT);
       ds.putDocument(in3, uri4, DocumentStore.DocumentFormat.TXT);
        ds.putDocument(in4, uri5, DocumentStore.DocumentFormat.TXT);
       ds.putDocument(in5, uri6, DocumentStore.DocumentFormat.TXT);

     //     ds.setMaxDocumentCount(3);

        assertEquals("How are you ? i need to know this please, yourself?" , ds.getDocumentAsTxt(uri3));
      assertEquals("now you what to go there" , ds.getDocumentAsTxt(uri1));
      //  assertEquals("now what guys" , ds.getDocumentAsTxt(uri4));


     //  ds.setMaxDocumentBytes(200);

        // get as text
//       assertEquals(" How are you ? i need to know this please, yourself?" , ds.getDocumentAsTxt(uri1));
//      assertEquals("Hello World, How are you?" , ds.getDocumentAsTxt(uri2));

        //delete doc
    //  ds.deleteDocument(uri3);

//assertEquals(" How are you ? i need to know this please, yourself?" , ds.getDocumentAsTxt(uri1));


//        ds.deleteDocument(uri2);
     //   assertNull(ds.getDocument(uri1));

       //then call ds.undo();
       // ds.undo(uri1);
      //  assertNull(ds.getDocument(uri2));
       // assertNotNull(ds.getDocument(uri1));

//        List list = new ArrayList( ds.search("know"));
//            ds.deleteAll("know");
//        List list2 = new ArrayList( ds.search("know"));
//            assertNull(ds.getDocumentAsTxt(uri1));

        // Search    / search things that doesnt exist
   //  List list2 = new ArrayList( ds.search("you"));
//        List list = new ArrayList( ds.searchPDFs("you"));

// revisar por q salen todos dublicados

       List listPref = new ArrayList(ds.searchByPrefix("yo"));
       ds.deleteAllWithPrefix("yo");


        assertEquals("How are you ? i need to know this please, yourself?" , ds.getDocumentAsTxt(uri3));
        assertEquals("now you what to go there" , ds.getDocumentAsTxt(uri1));

//        int total = ds.getByteCount();
//        int totalDoc = ds.getDocCount();


   }
}
