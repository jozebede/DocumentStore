package edu.yu.cs.com1320.project.stage4.impl;

import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;

import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage4.Document;
import edu.yu.cs.com1320.project.stage4.DocumentStore;

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
    MinHeapImpl<Document> myHeap = new MinHeapImpl<>();

    private int docCount = 0;
    private int countLimit = 0 ;
    private int docBytes = 0 ;
    private int bytesLimit= 0;
    long initialTime = System.nanoTime();

    protected Document getDocument(URI uri) {
        return table.get(uri);
    }

    @Override
    public void setMaxDocumentCount(int limit) {
        countLimit = limit;
        while ( docCount > countLimit && countLimit != 0){

            DocumentImpl d = (DocumentImpl) myHeap.removeMin();
            int thisByte =  getDocumentAsTxt(d.getKey()).getBytes().length + getDocumentAsPdf(d.getKey()).length;
            docBytes = docBytes - thisByte;

            for (String key : d.WordMap.keySet()) {

                myTrie.delete(key,d);
            }
            if (table.get(d.getKey()) != null) {
                table.put(d.getKey(), null);
            }
            docCount--;


            // remove from  stack, command
        }




    }
    @Override
    public void setMaxDocumentBytes(int limit) {
        bytesLimit = limit;
        while (docBytes > bytesLimit && bytesLimit != 0) {

            DocumentImpl d = (DocumentImpl) myHeap.removeMin();
            int thisByte = getDocumentAsTxt(d.getKey()).getBytes().length + getDocumentAsPdf(d.getKey()).length;
            docBytes = docBytes - thisByte;

            for (String key : d.WordMap.keySet()) {

                myTrie.delete(key, d);
            }
            if (table.get(d.getKey()) != null) {
                table.put(d.getKey(), null);
            }
            docCount--;


        }
    }


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


    @Override
    public int putDocument(InputStream input, URI uri, DocumentStore.DocumentFormat format) {

// try catch
           while (countLimit != 0 && docCount > countLimit || bytesLimit != 0 &&docBytes > bytesLimit ){

            DocumentImpl d = (DocumentImpl) myHeap.removeMin();
               int thisByte =  getDocumentAsTxt(d.getKey()).getBytes().length + getDocumentAsPdf(d.getKey()).length;
               docBytes = docBytes - thisByte;


               for (String key : d.WordMap.keySet()) {
                myTrie.delete(key, d);
        }
            if (table.get(d.getKey()) != null) {
                table.put(d.getKey(), null);
            }
                docCount--;

           // remove from  stack, command
        }

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

                doc.mapPut();
                for (String key : doc.WordMap.keySet()) {

                    myTrie.put(key, doc);
                }
                myHeap.insert(doc);
                docCount ++;
                docBytes = docBytes+ getDocumentAsTxt(uri).getBytes().length + getDocumentAsPdf(uri).length;

                doc.setLastUseTime(System.nanoTime());
                myHeap.reHeapify(doc);

                return info.hashCode();
            }

            if (format == DocumentStore.DocumentFormat.PDF) {

                PDFTextStripper inParts = new PDFTextStripper();

                String lines = inParts.getText(PDDocument.load(input)).trim();
                DocumentImpl document = new DocumentImpl(uri, lines, lines.hashCode(), toByteArray(input));// construct a DOCUMENT object
                table.put(uri, document);
                document.mapPut();
                for (String key : document.WordMap.keySet()) {
                    myTrie.put(key, document);
                }
                myHeap.insert(document); // insert heap
                docCount ++;
                docBytes = docBytes+ getDocumentAsTxt(uri).getBytes().length + getDocumentAsPdf(uri).length;
                document.setLastUseTime(System.nanoTime());
                myHeap.reHeapify(document);

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
//        InputStream is = new ByteArrayInputStream(document.getDocumentAsTxt().getBytes());

        Function<URI, Boolean> puts = (URI) -> {
            if(table.put(uri, document) != null) {
                for (String key : document.getDocumentAsTxt().split("\\s+")) {
                    myTrie.put(key, document);
                }
                myHeap.insert(document);
                docCount ++;
                docBytes = docBytes+ getDocumentAsTxt(uri).getBytes().length + getDocumentAsPdf(uri).length;
                document.setLastUseTime(System.nanoTime());
                myHeap.reHeapify(document);
                return true; //
            }
            else {return false;}
        };
         // take care of trie and keep track of deleted docs

       GenericCommand command = new GenericCommand(uri, puts); // create a command
    //   stack.push(command); // this command

        if (table.get(uri) != null) {
            table.put(uri, null); // put key with a null value

            for (String key : document.WordMap.keySet()) {
                myTrie.delete(key, document);
            }
            docCount--;
            int thisByte =  getDocumentAsTxt(uri).getBytes().length + getDocumentAsPdf(uri).length;
            docBytes = docBytes - thisByte;

            document.setLastUseTime(initialTime);
            myHeap.reHeapify(document);
            myHeap.removeMin();


            return true;
        }
        //@return true if the document is deleted, false if no document exists with that URI
        return false;
    }


    public void undo() throws IllegalStateException {
        Undoable com = stack.peek();

        stack.pop();
        ((GenericCommand)com).undo();

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
                DocumentImpl d = (DocumentImpl) doc;
                d.setLastUseTime(System.nanoTime()); // timepo de uso  al doc
                myHeap.reHeapify(d);
                String txt = d.getDocumentAsTxt();
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
        if (keyword != null) {
            for (Object doc : myTrie.getAllSorted(keyword, new CompareTo(keyword))) {
                DocumentImpl d = (DocumentImpl) doc;
                d.setLastUseTime(System.nanoTime());
                myHeap.reHeapify(d);
                byte[] pdf = d.getDocumentAsPdf();
                result.add(pdf);

            }
            return result;
        }
        else {
            return null;
        }
    }

    @Override
  public List<String> searchByPrefix(String keywordPrefix) {
        List result = new ArrayList();
        if (keywordPrefix != null) {
            for (Object doc : myTrie.getAllWithPrefixSorted(keywordPrefix, new ComparePrefix(keywordPrefix))) {
                DocumentImpl document = (DocumentImpl) doc;
                document.setLastUseTime(System.nanoTime());
                myHeap.reHeapify(document);
                String txt = document.getDocumentAsTxt();
                result.add(txt);

            }
            return result;
        } else {return null;
    }
        }

        @Override
        public List<byte[]> searchPDFsByPrefix (String keywordPrefix){

            List result = new ArrayList();
            for (Object doc : myTrie.getAllWithPrefixSorted(keywordPrefix, new ComparePrefix(keywordPrefix))) {
                DocumentImpl document = (DocumentImpl) doc;
                document.setLastUseTime(System.nanoTime());
                myHeap.reHeapify(document);
                byte[] pdf  = document.getDocumentAsPdf();
                result.add(pdf);
            }
                return result;
        }

        @Override
        public Set<URI> deleteAll (String keyword){

            Set<URI> result = new HashSet<URI>();
            CommandSet commandSet = new CommandSet();//this commandSet will take many commands

            for (Object doc : myTrie.getAllSorted(keyword, new CompareTo(keyword))) {

                DocumentImpl test = (DocumentImpl) doc;
                URI uri = test.getKey();
                result.add(uri);
                docCount--;
                int thisByte =  getDocumentAsTxt(uri).getBytes().length + getDocumentAsPdf(uri).length;
                docBytes = docBytes - thisByte;
                test.setLastUseTime(initialTime);
                myHeap.reHeapify(test);
                myHeap.removeMin();

                if (table.get(uri) != null) {
                    table.put(uri, null);
                }

                DocumentImpl document = new DocumentImpl(uri, getDocumentAsTxt(uri), getDocumentAsTxt(uri).hashCode());

                Function<URI, Boolean> puts = (URI) -> {
                    if(table.put(uri, document) != null) {
                        for (String key : document.getDocumentAsTxt().split("\\s+")) {
                            myTrie.put(key, document);
                        }
                        myHeap.insert(document);
                        docCount ++;
                        docBytes = docBytes+ getDocumentAsTxt(uri).getBytes().length + getDocumentAsPdf(uri).length;
                        document.setLastUseTime(System.nanoTime());
                        myHeap.reHeapify(document);
                        return true;
                    }
                    else {return false;}
                };

                GenericCommand command = new GenericCommand(uri, puts); // create a command
                stack.push(command); // this command

            }
            myTrie.deleteAll(keyword.toLowerCase());
            stack.push(commandSet);

            return result;
        }


        @Override
        public Set<URI> deleteAllWithPrefix (String keywordPrefix){
            Set<URI> result = new HashSet<URI>();
            CommandSet commandSet = new CommandSet();


            for (Object doc : myTrie.getAllWithPrefixSorted(keywordPrefix, new CompareTo(keywordPrefix))) {

                DocumentImpl test = (DocumentImpl) doc;
                URI uri = test.getKey();
                result.add(uri);
                docCount--;
                int thisByte = getDocumentAsTxt(uri).getBytes().length + getDocumentAsPdf(uri).length;
                docBytes = docBytes - thisByte;
                test.setLastUseTime(initialTime);
                myHeap.reHeapify(test);
                myHeap.removeMin();


                if (table.get(uri) != null) {
                    table.put(uri, null);
                }

                DocumentImpl document = new DocumentImpl(uri, getDocumentAsTxt(uri), getDocumentAsTxt(uri).hashCode());

                Function<URI, Boolean> puts = (URI) -> {
                    if (table.put(uri, document) != null) {
                        for (String key : document.getDocumentAsTxt().split("\\s+")) {
                            myTrie.put(key, document);
                        }
                        myHeap.insert(document);
                        docCount++;
                        docBytes = docBytes + getDocumentAsTxt(uri).getBytes().length + getDocumentAsPdf(uri).length;
                        document.setLastUseTime(System.nanoTime());
                        myHeap.reHeapify(document);
                        return true;
                    } else {
                        return false;
                    }
                };

                GenericCommand command = new GenericCommand(uri, puts); // create a command
               // stack.push(command); // this command
            }
                myTrie.deleteAllWithPrefix(keywordPrefix.toLowerCase());
                // stack.push(commandSet);

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

class ComparePrefix implements Comparator<Document> {

    String prefix;
    public ComparePrefix(String prefix) {
        this.prefix = prefix;
    }



    @Override
    public int compare(Document o1, Document o2) {
        int total1 = 0;
        int total2 = 0;

        List<String> list_without_null = new ArrayList<String>();
        List<String> list_without_null2 = new ArrayList<String>();

        String[] keys1 = o1.getDocumentAsTxt().split("\\s+");
        String[] keys2 = o2.getDocumentAsTxt().split("\\s+");

        for(int i = 0; i < keys1.length;i++) {
            keys1[i] = keys1[i].replaceAll("[^a-zA-Z]", "");
        }
        for(int i = 0; i < keys2.length;i++) {
            keys2[i] = keys2[i].replaceAll("[^a-zA-Z]", "");
        }

        for(String new_string : keys1) {
            if (new_string != null && new_string.length() > 0) {
                list_without_null.add(new_string);
            }
        }
            for(String string2 : keys2) {
                if (string2 != null && string2.length() > 0) {
                    list_without_null2.add(string2);
                }
            }



        for (String word: list_without_null) {
            if(word.startsWith(prefix)){
                total1 += o1.wordCount(word);
            }
        }

        for(String word2 : list_without_null2){
            if(word2.startsWith(prefix)){
                total2 += o2.wordCount(word2);
            }
        }


        int result = total1 - total2 ;

    return result;
    }
}







