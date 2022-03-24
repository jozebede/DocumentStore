package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.impl.*;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;

import javafx.concurrent.Worker;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.print.Doc;
import javax.swing.plaf.basic.BasicTreeUI;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.function.Function;


public class DocumentStoreImpl implements DocumentStore {

    private int countLimit = 0;
    private int docBytes = 0;
    private int bytesLimit = 0;
    long initialTime = System.nanoTime();

    protected File thisDir = new File(System.getProperty("user.dir"));
    ArrayList<URI> docTrack = new ArrayList<URI>();
    HashMap<URI, UriTime> uriFind = new HashMap<>();
    ArrayList<URI> uriDisk = new ArrayList<URI>();



    // constructors
    protected File baseDir;

    public DocumentStoreImpl(File baseDir) {
        this.baseDir = baseDir;
        DocumentPersistenceManager pm = new DocumentPersistenceManager(baseDir);
        tree.setPersistenceManager(pm);
    }


    public DocumentStoreImpl() {
        DocumentPersistenceManager pm2 = new DocumentPersistenceManager(thisDir);
        tree.setPersistenceManager(pm2);
    }

    //instances
    StackImpl<Undoable> stack = new StackImpl<>();
    StackImpl<Undoable> stack2 = new StackImpl<>();
    TrieImpl<URI> myTrie = new TrieImpl<>();
    MinHeapImpl<UriTime> myHeap = new MinHeapImpl<>();
    BTreeImpl<URI, DocumentImpl> tree = new BTreeImpl<>();


    enum DocumentFormat {
        TXT,
        PDF
    }


    protected Document getDocument(URI uri) {
        if (docTrack.contains(uri)) {
            Document doc = (DocumentImpl) tree.get(uri);
            return doc;
        }
        return null;
    }


    class UriTime<URI extends Comparable> implements Comparable {

        URI uri;

        protected UriTime(URI u) {
            this.uri = u;
        }


        @Override
        public int compareTo(Object o) {

            if (o == null) {
                return 1;
            }
            DocumentImpl d2 = (DocumentImpl) tree.get(uri);

            UriTime uriTime = (UriTime) o;
            DocumentImpl d1 = (DocumentImpl) tree.get(uriTime.uri);


            long docTime1 = d2.getLastUseTime();
            long docTime2 = d1.getLastUseTime();


            if (docTime1 < docTime2) {
                return -1;
            } else if (docTime1 > docTime2) {
                return 1;
            } else {
                return 0;
            }
        }
    }


    @Override
    public void setMaxDocumentCount(int limit) {
        countLimit = limit;
        while (docTrack.size() > countLimit && countLimit != 0) {


            UriTime uTime = myHeap.removeMin();
            URI docKey = (URI) uTime.uri;
            int thisByte = getDocumentAsTxt(docKey).getBytes().length + getDocumentAsPdf(docKey).length;
            docBytes = docBytes - thisByte;
            docTrack.remove(docKey);
            uriFind.remove(docKey);


            try {
                tree.moveToDisk(docKey);
                uriDisk.add(docKey);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setMaxDocumentBytes(int limit) {
        bytesLimit = limit;
        while (docBytes > bytesLimit && bytesLimit != 0) {

            UriTime uriTime = myHeap.removeMin();
            URI docKey = (URI) uriTime.uri;

            int thisByte = getDocumentAsTxt(docKey).getBytes().length + getDocumentAsPdf(docKey).length;
            docBytes = docBytes - thisByte;
            docTrack.remove(docKey);
            uriFind.remove(docKey);

            try {
                tree.moveToDisk(docKey);
                uriDisk.add(docKey);
            } catch (Exception e) {
                e.printStackTrace();
            }
           // tree.put(docKey, null);

        }
    }

    protected void memoryCheck(){
        while (countLimit != 0 && docTrack.size() > countLimit || bytesLimit != 0 && docBytes > bytesLimit) {

            UriTime uriTime = myHeap.removeMin();
            URI docKey = (URI) uriTime.uri;

            int thisByte = getDocumentAsTxt(docKey).getBytes().length + getDocumentAsPdf(docKey).length;
            docBytes = docBytes - thisByte;
            docTrack.remove(docKey);
            uriFind.remove(docKey);
            try {
                tree.moveToDisk(docKey);
                uriDisk.add(docKey);
            } catch (Exception e) {
                e.printStackTrace();
            }
           // tree.put(docKey, null);
        }
    }


    protected byte[] toByteArray(InputStream in) throws IOException {
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
    protected DocumentImpl getTree(URI u){
        // method for deserialize

        DocumentImpl doc = (DocumentImpl) tree.get(u);
        if(doc == null){
            return null;
        }

        if(uriDisk.contains(u)){
            uriDisk.remove(u);

            doc.setLastUseTime(System.nanoTime());
            UriTime ut = new UriTime(u);
            uriFind.put(u, ut);
            myHeap.insert(ut);
            docTrack.add(u);
            docBytes = docBytes + getDocumentAsTxt(u).getBytes().length + getDocumentAsPdf(u).length;
            memoryCheck();
        }

        return doc;
    }


    @Override
    public int putDocument(InputStream input, URI uri, DocumentStore.DocumentFormat format) {

        if (uri == null) {
            throw new IllegalArgumentException("URI cannot be null!");
        }
        if (format == null) {
            throw new IllegalArgumentException("Format cannot be null!");
        }



               if (input == null || getTree(uri) != null) {// input null OR hay un doc con ese uri
            //si el input tiene valor y no exite doc-> no entra en el if


            if (getTree(uri) != null && input == null) { // si el input es null y ya existe un doc -> borrar el doc return valor anterior
                DocumentImpl document = getTree(uri);
                deleteDocument(uri);
                return document.getDocumentTextHashCode();
            }

        }

        if (getTree(uri) == null && input == null) {
            // input null y no exite un doc -> no hacer nada, return 0
            return 0;
        }

        try {
            if (format == DocumentStore.DocumentFormat.TXT) {

                DocumentImpl lastDoc = getTree(uri);

                String info = new String(toByteArray(input));
                DocumentImpl doc = new DocumentImpl(uri, info, info.hashCode());
                tree.put(uri, doc);

                doc.mapPut();
                for (String key : doc.WordMap.keySet()) {
                    myTrie.put(key, uri);
                }
                doc.setLastUseTime(System.nanoTime());
                UriTime ut = new UriTime(uri);
                uriFind.put(uri, ut);
                myHeap.insert(ut);
                docTrack.add(uri);
                docBytes = docBytes + getDocumentAsTxt(uri).getBytes().length + getDocumentAsPdf(uri).length;
                memoryCheck();

                // si existia un doc -> undo al update
                if (lastDoc == null) {
                    Function<URI, Boolean> deletes = (URI) -> deleteDocument(uri);
                    GenericCommand command = new GenericCommand(uri, deletes);
                    stack.push(command);
                    return 0;
                }


                if (lastDoc != null) {

                    Function<URI, Boolean> puts = (URI) -> {
                        deleteDocument(uri); // borramos el documento anterior

                        tree.put(uri, lastDoc);

                        for (String key : lastDoc.getWordMap().keySet()) {
                            myTrie.put(key, uri);
                        }
                        lastDoc.setLastUseTime(System.nanoTime());

                        UriTime ut5 = new UriTime(uri);
                        uriFind.put(uri, ut5);
                        myHeap.insert(ut5);
                        docTrack.add(uri);
                        docBytes = docBytes + getDocumentAsTxt(uri).getBytes().length + getDocumentAsPdf(uri).length;
                        return true;
                    };

                    GenericCommand command = new GenericCommand(uri, puts);
                    stack.push(command);
                    return lastDoc.getDocumentTextHashCode();
                }
            }

            if (format == DocumentStore.DocumentFormat.PDF) {
                DocumentImpl lastDoc = getTree(uri);

                PDFTextStripper inParts = new PDFTextStripper();
                String lines = inParts.getText(PDDocument.load(input)).trim();
                DocumentImpl document = new DocumentImpl(uri, lines, lines.hashCode(), toByteArray(input));
                tree.put(uri, document);
                document.mapPut();
                for (String key : document.WordMap.keySet()) {
                    myTrie.put(key, uri);
                }
                document.setLastUseTime(System.nanoTime());
                UriTime ut = new UriTime(uri);
                uriFind.put(uri, ut);
                myHeap.insert(ut);
                docTrack.add(uri);
                docBytes = docBytes + getDocumentAsTxt(uri).getBytes().length + getDocumentAsPdf(uri).length;

                memoryCheck();

                // si existia un doc -> undo al update
                if (lastDoc == null) {
                    Function<URI, Boolean> deletes = (URI) -> deleteDocument(uri);
                    GenericCommand command = new GenericCommand(uri, deletes);
                    stack.push(command);
                    return 0;
                }


                if (lastDoc != null) {

                    Function<URI, Boolean> puts = (URI) -> {
                        deleteDocument(uri); // borramos el documento anterior

                        tree.put(uri, lastDoc);

                        for (String key : lastDoc.getWordMap().keySet()) {
                            myTrie.put(key, uri);
                        }
                        lastDoc.setLastUseTime(System.nanoTime());

                        UriTime ut5 = new UriTime(uri);
                        uriFind.put(uri, ut5);
                        myHeap.insert(ut5);
                        docTrack.add(uri);
                        docBytes = docBytes + getDocumentAsTxt(uri).getBytes().length + getDocumentAsPdf(uri).length;
                        return true;
                    };

                    GenericCommand command = new GenericCommand(uri, puts);
                    stack.push(command);
                    return lastDoc.getDocumentTextHashCode();
                }
            }
        }catch (IOException e) {
                e.printStackTrace();
            }
        return 0;
    }

    @Override
    public byte[] getDocumentAsPdf(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("URI cannot be null!");
        }

        if (getTree(uri) == null) {
            return null;
        }

        try {
            DocumentImpl document = getTree(uri);
            DocumentImpl dd = new DocumentImpl(uri, document.getDocumentAsTxt(), document.hashCode());

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

        if (getTree(uri) == null) {
            return null;
        }

        DocumentImpl document = getTree(uri);
        DocumentImpl drs = new DocumentImpl(uri, document.getDocumentAsTxt(), document.hashCode());

        return drs.getDocumentAsTxt();
    }

    @Override
    public boolean deleteDocument(URI uri) {

        if (uri == null) {
            throw new IllegalArgumentException("URI cannot be null!");
        }

        DocumentImpl document = getTree(uri);
        if (document == null){
            return false;
        }

        Function<URI, Boolean> puts = (URI) -> {
            tree.put(uri, document);

            for (String key : document.getWordMap().keySet()) {
                myTrie.put(key, uri);
            }
            document.setLastUseTime(System.nanoTime());

            UriTime ut = new UriTime(uri);
            uriFind.put(uri, ut);
            myHeap.insert(ut);
            docTrack.add(uri);
            docBytes = docBytes + getDocumentAsTxt(uri).getBytes().length + getDocumentAsPdf(uri).length;
            memoryCheck();


            return true;
        };

        GenericCommand command = new GenericCommand(uri, puts);
        stack.push(command);

        int thisByte = getDocumentAsTxt(uri).getBytes().length + getDocumentAsPdf(uri).length;

        for (String key : document.WordMap.keySet()) {
            myTrie.delete(key, uri);
        }
        docTrack.remove(uri);

        docBytes = docBytes - thisByte;


        document.setLastUseTime(initialTime);
        UriTime ut = uriFind.get(uri);
        uriFind.remove(uri);
        myHeap.reHeapify(ut);
        myHeap.removeMin();
        if (getTree(uri)!= null) {
            tree.put(uri, null); // put key with a null value

            return true;
        }
        //@return true if the document is deleted, false if no document exists with that URI
        return false;
    }


    public void undo() throws IllegalStateException {

    Undoable com = stack.peek();


    stack.pop();
    // esto tira exceprion si no hah nasa antes
    com.undo();


     }

    public void undo(URI uri) throws IllegalStateException {



       boolean ready = false;

        while (!ready) {
            Undoable top = stack.peek();

            if (top.getClass() == GenericCommand.class) {

                GenericCommand gc = (GenericCommand) top;

              if( gc.getTarget() != uri  ) {
                stack2.push(stack.peek());
                stack.pop();
                 }
                if( gc.getTarget() == uri  ) {
                    ready = true;
                }
            }
            if (top.getClass() == CommandSet.class) {
                    CommandSet cSet = (CommandSet) top;

                    if(!cSet.containsTarget(uri)){
                        stack2.push(stack.peek());
                        stack.pop();
                    }

                if(cSet.containsTarget(uri)) {
                    ready = true;
                }
            }
        }

        Undoable top = stack.peek();

        if (top.getClass() == GenericCommand.class) {
            stack.pop();
            top.undo();

        }
        if (top.getClass() == CommandSet.class) {

            ((CommandSet) top).undo(uri);

            if (((CommandSet) top).size() ==0){
                stack.pop();
            }
        }
        while (stack2.size()!= 0) {
            stack.push(stack2.peek());
            stack2.pop();
        }
    }


    @Override
    public List<String> search(String keyword) {

        if (keyword == null) {
            throw new IllegalArgumentException("Keyword cannot be null!");
        }

        List result = new ArrayList();

        if (keyword != null) {
            for (Object uri : myTrie.getAllSorted(keyword, new CompareTo(keyword,this.tree))) {
                DocumentImpl d = getTree((URI) uri);
                d.setLastUseTime(System.nanoTime());// timepo de uso  al doc
                UriTime ut = uriFind.get(uri);
                myHeap.reHeapify((ut));
                String txt = d.getDocumentAsTxt();
                result.add(txt);
            }
        }


        return result;
    }


    @Override
    public List<byte[]> searchPDFs(String keyword) {
        if (keyword == null) {
            throw new IllegalArgumentException("Keyword cannot be null!");
        }

        List result = new ArrayList();

        if (keyword != null) {
            for (Object uri : myTrie.getAllSorted(keyword, new CompareTo(keyword, this.tree))) {
                DocumentImpl d = getTree((URI) uri);
                d.setLastUseTime(System.nanoTime());
                UriTime ut = uriFind.get(uri);
                myHeap.reHeapify((ut));
                byte[] pdf = d.getDocumentAsPdf();
                result.add(pdf);

            }
        }

        return result;

    }

    @Override
    public List<String> searchByPrefix(String keywordPrefix) {
        if (keywordPrefix == null) {
            throw new IllegalArgumentException("Keyword cannot be null!");
        }
        List result = new ArrayList();

        if (keywordPrefix != null) {
            ComparePrefix comp = new ComparePrefix(keywordPrefix,tree);
            for (Object uri : myTrie.getAllWithPrefixSorted(keywordPrefix, comp)) {
                DocumentImpl document = getTree((URI) uri);
                document.setLastUseTime(System.nanoTime());
                UriTime ut = uriFind.get(uri);
                myHeap.reHeapify((ut));
                String txt = document.getDocumentAsTxt();
                result.add(txt);
            }
        }

        return result;
    }

    @Override
    public List<byte[]> searchPDFsByPrefix(String keywordPrefix) {
        if (keywordPrefix == null) {
            throw new IllegalArgumentException("Keyword cannot be null!");
        }

        List result = new ArrayList();


        for (Object uri : myTrie.getAllWithPrefixSorted(keywordPrefix, new ComparePrefix(keywordPrefix , tree))) {
            DocumentImpl document = getTree((URI) uri);
            document.setLastUseTime(System.nanoTime());
            UriTime ut = uriFind.get(uri);
            myHeap.reHeapify((ut));
            byte[] pdf = document.getDocumentAsPdf();
            result.add(pdf);
        }

        return result;
    }

    @Override
    public Set<URI> deleteAll(String keyword) {

        Set<URI> result = new HashSet<URI>();
        CommandSet commandSet = new CommandSet();//this commandSet will take many commands

        for (Object uri1 : myTrie.getAllSorted(keyword, new CompareTo(keyword, this.tree))) {

            DocumentImpl test = getTree((URI) uri1);
            URI uri = test.getKey();
            result.add(uri);
            docTrack.remove(uri);
            int thisByte = getDocumentAsTxt(uri).getBytes().length + getDocumentAsPdf(uri).length;
            docBytes = docBytes - thisByte;
            test.setLastUseTime(initialTime);
            UriTime ut = uriFind.get(uri);
            uriFind.remove(uri); // cuidado aqui
            myHeap.reHeapify(ut);
            myHeap.removeMin();

            if (getTree(uri) != null) {
                tree.put(uri, null);
            }

            Function<URI, Boolean> puts = (URI) -> {

                    for (String key :    test.WordMap.keySet()) {
                        myTrie.put(key, uri);
                    }
                    tree.put(uri,test);
                    docTrack.add(uri);
                    test.setLastUseTime(System.nanoTime());
                    UriTime ut2 = new UriTime((Comparable) uri);
                    uriFind.put(uri,ut2);
                    myHeap.insert(ut2);

                    docBytes = docBytes + getDocumentAsTxt(uri).getBytes().length + getDocumentAsPdf(uri).length;
                    memoryCheck();
                    return true;
                };

                 GenericCommand command = new GenericCommand(uri, puts); // create a command
                 commandSet.addCommand(command);
                }

        if (result.size() !=0) {
            myTrie.deleteAll(keyword.toLowerCase());
            stack.push(commandSet);
        }

        return result;
    }


    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
        Set<URI> result = new HashSet<URI>();
        CommandSet commandSet = new CommandSet();


        for (Object uri1 : myTrie.getAllWithPrefixSorted(keywordPrefix, new CompareTo(keywordPrefix,this.tree))) {

            DocumentImpl test = getTree((URI) uri1);
            URI uri = test.getKey();
            result.add(uri);

            docTrack.remove(uri);
            int thisByte = getDocumentAsTxt(uri).getBytes().length + getDocumentAsPdf(uri).length;
            docBytes = docBytes - thisByte;
            test.setLastUseTime(initialTime);
            UriTime ut = uriFind.get(uri);
            uriFind.remove(uri); // cuidado aqui
            myHeap.reHeapify(ut);
            myHeap.removeMin();

            if (getTree(uri) != null) {
                tree.put(uri, null);
            }

            Function<URI, Boolean> puts = (URI) -> {

                for (String key :    test.WordMap.keySet()) {
                    myTrie.put(key, uri);
                }
                tree.put(uri,test);
                docTrack.add(uri);
                docBytes = docBytes + getDocumentAsTxt(uri).getBytes().length + getDocumentAsPdf(uri).length;
                test.setLastUseTime(System.nanoTime());
                UriTime ut2 = new UriTime((Comparable) uri);
                uriFind.put(uri,ut2);
                myHeap.insert(ut2);
                memoryCheck();
                return true;
            };

            GenericCommand command = new GenericCommand(uri, puts);
            commandSet.addCommand(command);
           // stack.push(command);
        }

        if (result.size() !=0) {
            myTrie.deleteAllWithPrefix(keywordPrefix.toLowerCase());
            stack.push(commandSet);
        }

        return result;
    }
}



    class CompareTo implements Comparator<URI> {

        String key;
        BTreeImpl tree;

        public CompareTo(String key, BTreeImpl tree) {
            this.key = key;
            this.tree=tree;

        }


        @Override
        public int compare(URI u1, URI u2) {


            DocumentImpl o1 = (DocumentImpl) tree.get(u1);
            DocumentImpl o2 = (DocumentImpl) tree.get(u2);

            if (o1 == null || o2 == null) {
                return -1;
            }


            int result = o2.wordCount(this.key) - o1.wordCount(this.key);

            return result;
        }

    }


    class ComparePrefix implements Comparator<URI> {

        String prefix;
        BTreeImpl tree;

        public ComparePrefix(String prefix, BTreeImpl tree) {
            this.prefix = prefix;
            this.tree =tree;
        }


        @Override

        public int compare(URI u1, URI u2) {
            int total1 = 0;
            int total2 = 0;


            DocumentImpl o1 = (DocumentImpl) tree.get(u1);
            DocumentImpl o2 = (DocumentImpl) tree.get(u2);


            List<String> list_without_null = new ArrayList<String>();
            List<String> list_without_null2 = new ArrayList<String>();

            String[] keys1 = o1.getDocumentAsTxt().split("\\s+");
            String[] keys2 = o2.getDocumentAsTxt().split("\\s+");

            for (int i = 0; i < keys1.length; i++) {
                keys1[i] = keys1[i].replaceAll("[^a-zA-Z]", "");
            }
            for (int i = 0; i < keys2.length; i++) {
                keys2[i] = keys2[i].replaceAll("[^a-zA-Z]", "");
            }

            for (String new_string : keys1) {
                if (new_string != null && new_string.length() > 0) {
                    list_without_null.add(new_string);
                }
            }
            for (String string2 : keys2) {
                if (string2 != null && string2.length() > 0) {
                    list_without_null2.add(string2);
                }
            }

            for (String word : list_without_null) {
                if (word.startsWith(prefix)) {
                    total1 += o1.wordCount(word);
                }
            }

            for (String word2 : list_without_null2) {
                if (word2.startsWith(prefix)) {
                    total2 += o2.wordCount(word2);
                }
            }


            int result = total2 - total1;

             return result;

        }
    }













