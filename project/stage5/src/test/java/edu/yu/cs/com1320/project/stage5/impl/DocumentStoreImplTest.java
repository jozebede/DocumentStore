package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;;

import org.junit.Test;

import javax.print.Doc;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DocumentStoreImplTest {

  @Test
  public void test1() throws IOException {


      File baseDir;
     baseDir = Files.createTempDirectory("stage5").toFile();

        DocumentStoreImpl ds = new DocumentStoreImpl(baseDir);

        ByteArrayInputStream in1 = new ByteArrayInputStream("How are  you ? i need to know this please, yourself?".getBytes());
        ByteArrayInputStream in2 = new ByteArrayInputStream("now you you  what to go there".getBytes());
        ByteArrayInputStream in3 = new ByteArrayInputStream("now what you you you  guys stwatt street".getBytes());
        ByteArrayInputStream in4 = new ByteArrayInputStream("ready for going there  street  stre street".getBytes());
        ByteArrayInputStream in5 = new ByteArrayInputStream("knoW this is only a stuot test".getBytes());

        File PDFF = new File ("C:/Users/jozeb/OneDrive/Documents/HelloWorld.pdf");
        InputStream pdIn = new FileInputStream(PDFF);

        URI uri1 = URI.create("http://www.yu.edu/Documents/directory/try/doc1");
        URI uri2 = URI.create("http://www.yu.edu/doc2");
        URI uri3 = URI.create("http://TRY3");
        URI uri4 = URI.create("http://TRY4");
        URI uri5 = URI.create("http://TRY5");
        URI uri6 = URI.create("http://src/Directory1/TRY6");

      ds.setMaxDocumentCount(2);

    ds.putDocument(in1, uri1, DocumentStore.DocumentFormat.TXT);
    ds.putDocument(in2, uri2, DocumentStore.DocumentFormat.TXT);


   assertNotNull(ds.getDocument(uri1));
   assertNotNull(ds.getDocument(uri2));

   ds.putDocument(in3, uri3, DocumentStore.DocumentFormat.TXT);

   // 1 disk . 2,3 memory

      assertNull(ds.getDocument(uri1));
      assertNotNull(ds.getDocument(uri2));
      assertNotNull(ds.getDocument(uri3));

      ds.deleteDocument(uri3);

      ds.search("yourself");


      assertNotNull(ds.getDocument(uri1));



  }

}
