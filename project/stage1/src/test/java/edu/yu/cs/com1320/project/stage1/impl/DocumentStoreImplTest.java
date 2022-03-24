package edu.yu.cs.com1320.project.stage1.impl;

import edu.yu.cs.com1320.project.stage1.DocumentStore;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import org.junit.Test;

import java.io.*;
import java.net.URI;

import static org.junit.Assert.*;

public class DocumentStoreImplTest {

    @Test
  public void test1() throws FileNotFoundException {
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


      // get as pdf
     //assertEquals( "Bytes", ds.getDocumentAsPdf(uri2));
        // assertEquals( "Bytes", ds.getDocumentAsPdf(uri1));


        //delete doc
        assertEquals(true, ds.deleteDocument(uri1));
        assertEquals(true, ds.deleteDocument(uri2));
   }


}
