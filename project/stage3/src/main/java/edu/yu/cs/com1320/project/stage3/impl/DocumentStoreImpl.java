package edu.yu.cs.com1320.project.stage3.impl;

import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;

import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage3.Document;
import edu.yu.cs.com1320.project.stage3.DocumentStore;

import javafx.concurrent.Worker;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.print.Doc;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.function.Function;


public class DocumentStoreImpl  implements DocumentStore {

    DocumentStore.DocumentFormat format;

    public DocumentStoreImpl() {
        format = null;
    }

    enum DocumentFormat {
        TXT,
        PDF
    }

    HashTableImpl<URI, DocumentImpl> table = new HashTableImpl<>(); // Hashtable instance
    StackImpl<Undoable> stack = new StackImpl<>(); // Stack instance
    StackImpl<Undoable> stack2 = new StackImpl<>();
    TrieImpl<Document> myTrie = new TrieImpl<>();


    byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        // read bytes from the input stream and store them in buffer
        while ((len = in.read(buffer)) != -1) {
            // write bytes from the buffer into output stream
            os.write(buffer, 0, len);
        }
        return os.toByteArray();
    }


    protected Document getDocument(URI uri) {

        return table.get(uri);

    }

    @Override
    public int putDocument(InputStream input, URI uri, DocumentStore.DocumentFormat format) {

        Function<URI, Boolean> deletes = (URI) -> deleteDocument(uri);      //write a function that deletes (i.e. undo the put) the document

        GenericCommand command = new GenericCommand(uri, deletes); // create a command
        stack.push(command); // this command

        this.format = format;

        if (uri == null) {
            throw new IllegalArgumentException("URI cannot be null!");
        }
        if (input == null) {
            if (table.get(uri) != null) {
                table.put(uri, null);
            }
            throw new IllegalArgumentException("Input cannot be null!");
            // need to delete the URI for this value
        }

        try {
            if (format == DocumentStore.DocumentFormat.TXT) {
                String info = new String(toByteArray(input));
                DocumentImpl doc = new DocumentImpl(uri, info, info.hashCode());// construct a DOCUMENT object
                table.put(uri, doc);

                for (String key : doc.WordMap.keySet()) {
                    myTrie.put(key, doc);
                }
                return info.hashCode();
            }

            if (format == DocumentStore.DocumentFormat.PDF) {

                PDFTextStripper inParts = new PDFTextStripper();

                String lines = inParts.getText(PDDocument.load(input)).trim();
                DocumentImpl document = new DocumentImpl(uri, lines, lines.hashCode(), toByteArray(input));// construct a DOCUMENT object
                table.put(uri, document);

                for (String key : document.WordMap.keySet()) {
                    myTrie.put(key, document);
                }
                return lines.hashCode();


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public byte[] getDocumentAsPdf(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("URI cannot be null!");
        }

        if (table.get(uri) == null) { // return null if there is no document with that URI.
            return null;
        }

        try {
            DocumentImpl dd = new DocumentImpl(uri, table.get(uri).getDocumentAsTxt(), table.get(uri).hashCode()); // new a document from where to to extract the text

            File PDFFile = new File("myFile");

            PDDocument doc = new PDDocument();
            PDPage page = new PDPage();
            doc.addPage(page);
            PDPageContentStream cont = new PDPageContentStream(doc, page);

            cont.setFont(PDType1Font.COURIER, 12);
            cont.beginText();
            cont.showText(dd.getDocumentAsTxt());
            cont.endText();
            cont.close();

            doc.save(PDFFile);
            doc.close();

            PDFTextStripper stripper = new PDFTextStripper();
            InputStream pdIn = new FileInputStream(PDFFile);
            String lines = stripper.getText(PDDocument.load(pdIn)).trim();
            DocumentImpl document3 = new DocumentImpl(uri, lines, lines.hashCode(), toByteArray(pdIn));


            return document3.getDocumentAsPdf();

        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }
    }

    @Override
    public String getDocumentAsTxt(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("URI cannot be null!");
        }
        if (table.get(uri) == null) { // return null if no document with that URI
            return "null";
        }

        DocumentImpl drs = new DocumentImpl(uri, table.get(uri).getDocumentAsTxt(), table.get(uri).hashCode());
        return drs.getDocumentAsTxt();
    }

    @Override
    public boolean deleteDocument(URI uri) {

        if (uri == null) {
            throw new IllegalArgumentException("URI cannot be null!");
        }

        DocumentImpl document = new DocumentImpl(uri, getDocumentAsTxt(uri), getDocumentAsTxt(uri).hashCode());
        InputStream is = new ByteArrayInputStream(document.getDocumentAsTxt().getBytes());


      //  Function<URI, Boolean> puts = (URI) -> (table.put(uri,document) myTrie.put("joe", document));    // take care of trie and keep track of deleted docs


      //  GenericCommand command = new GenericCommand(uri, puts); // create a command

       // stack.push(command); // this command

        if (table.get(uri) != null) {
            table.put(uri, null); // put key with a null value

            for (String key : document.WordMap.keySet()) {
                myTrie.delete(key, document);
            }


            return true;
        }
        //@return true if the document is deleted, false if no document exists with that URI
        return false;
    }


    public void undo() throws IllegalStateException {
        Undoable com = stack.peek();
        stack.pop();
        com.undo();

    }


    public void undo(URI uri) throws IllegalStateException {

//        Undoable c = stack.peek();
//        //contains
//      Boolean is =  CommandSet.containsTarget(uri);
//        if (c.getUri() != uri) {
//            stack2.push(stack.peek());
//            stack.pop();
//        }
//
//        Command com = stack.peek();
//        stack.pop();
//        com.undo();
//
//        while (stack2.size() != 0) {
//            stack.push(stack2.peek());
//            stack2.pop();
//        }
//

    }


    @Override
    public List<String> search(String keyword) {

        List result = new ArrayList();
        if (keyword != null){
        for (Object doc : myTrie.getAllSorted(keyword, new CompareTo(keyword))) {
                DocumentImpl test = (DocumentImpl) doc;
                String txt = test.getDocumentAsTxt();
                result.add(txt);
            }
            return result;
        }
        else {
            return null;
        }
    }

    @Override
    public List<byte[]> searchPDFs(String keyword) {

        List result = new ArrayList();
        for (Object doc : myTrie.getAllSorted(keyword, new CompareTo(keyword))) {
            DocumentImpl test = (DocumentImpl) doc;
            byte[] pdf = test.getDocumentAsPdf();
            result.add(pdf);

        }
        return result;
    }

    @Override
  public List<String> searchByPrefix(String keywordPrefix) {
//        List result = new ArrayList();
//        for (Object doc : myTrie.getAllSorted(keywordPrefix, new ComparePrefix(keywordPrefix))) {
//            DocumentImpl document = (DocumentImpl) doc;
//            String txt = document.getDocumentAsTxt();
//            result.add(txt);
//        }


            return null;
        }

        @Override
        public List<byte[]> searchPDFsByPrefix (String keywordPrefix){
            return null;
        }

        @Override
        public Set<URI> deleteAll (String keyword){

            Set<URI> result = new HashSet<URI>();
            CommandSet commandSet = new CommandSet();//this commandSet will take many commands

            for (Object doc : myTrie.getAllSorted(keyword, new CompareTo(keyword))) {


                DocumentImpl test = (DocumentImpl) doc;
                URI uri = test.getKey();
                result.add(uri);

                if (table.get(uri) != null) {
                    table.put(uri, null);
                }

                DocumentImpl d = new DocumentImpl(uri, getDocumentAsTxt(uri), getDocumentAsTxt(uri).hashCode());
                InputStream is = new ByteArrayInputStream(d.getDocumentAsTxt().getBytes());
                // TODO - here


            }
            myTrie.deleteAll(keyword.toLowerCase());
            stack.push(commandSet);

            return result;


        }


        @Override
        public Set<URI> deleteAllWithPrefix (String keywordPrefix){
            Set<URI> result = new HashSet<URI>();
            CommandSet commandSet = new CommandSet();


            myTrie.deleteAllWithPrefix(keywordPrefix.toLowerCase());


            return result;
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

//class ComparePrefix implements Comparator<Document> {
//
//    String prefix;
//    public ComparePrefix(String prefix) {
//        this.prefix = prefix;
//
//    }

//
//    @Override
//    public int compare(Document o1, Document o2) {
//
//       // int result = o1.wordCount() - o2.wordCount(); // como conseguir las palabras en donde hacer el search
//
//      //  return result;
//    }
//}







