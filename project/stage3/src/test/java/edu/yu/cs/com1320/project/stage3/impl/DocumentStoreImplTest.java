package edu.yu.cs.com1320.project.stage3.impl;

import edu.yu.cs.com1320.project.stage3.DocumentStore;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import org.junit.Test;

import java.io.*;
import java.net.URI;

import static org.junit.Assert.*;

public class DocumentStoreImplTest {

    @Test
  public void test1() throws IOException {
        DocumentStoreImpl ds = new DocumentStoreImpl();

        ByteArrayInputStream in1 = new ByteArrayInputStream(" How are you?".getBytes());
        File PDFF = new File ("C:/Users/jozeb/OneDrive/Documents/HelloWorld.pdf");
        InputStream pdIn = new FileInputStream(PDFF);

        URI uri1 = URI.create("http://TRY:");
        URI uri2 = URI.create("http://TRY2:");


        ds.putDocument(in1, uri1, DocumentStore.DocumentFormat.TXT);
        ds.putDocument(pdIn, uri2, DocumentStore.DocumentFormat.PDF);


        // get as text
       assertEquals(" How are you?" , ds.getDocumentAsTxt(uri1));
      assertEquals("Hello World, How are you?" , ds.getDocumentAsTxt(uri2));

        //delete doc
//        ds.deleteDocument(uri1);
//        ds.deleteDocument(uri2);
     //   assertNull(ds.getDocument(uri1));

       //then call ds.undo();
       // ds.undo(uri1);
      //  assertNull(ds.getDocument(uri2));
       // assertNotNull(ds.getDocument(uri1));


         ds.deleteAll("are");
         assertNull(ds.getDocumentAsTxt(uri1));

       //     ds.search("world");



   }


}
