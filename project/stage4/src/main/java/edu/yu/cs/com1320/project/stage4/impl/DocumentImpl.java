package edu.yu.cs.com1320.project.stage4.impl;

import edu.yu.cs.com1320.project.stage4.Document;

import java.net.URI;
import java.util.HashMap;

public class DocumentImpl implements Document {

    public URI uri;
    public String txt;
    public int txtHash;
    public byte[] pdfBytes;

    protected HashMap<String, Integer> WordMap = new HashMap<String, Integer>();
    private long time;


    public DocumentImpl(URI uri, String txt, int txtHash) {
        this.uri = uri;
        this.txt = txt;
        this.txtHash = txtHash;
    }

    public DocumentImpl(URI uri, String txt, int txtHash, byte[] pdfBytes) {
        this.uri = uri;
        this.txt = txt;
        this.txtHash = txtHash;
        this.pdfBytes = pdfBytes;
    }

    @Override
    public byte[] getDocumentAsPdf() {
        return txt.getBytes();
    }

    @Override
    public String getDocumentAsTxt() {
        return txt;
    }

    @Override
    public int getDocumentTextHashCode() {
        return txtHash;
    }

    @Override
    public URI getKey() {
        return uri;
    }

    @Override
    public int wordCount(String word) {
        int count = 0;
        String[] splitted = txt.split("\\s+");

        for (int i = 0; i < splitted.length; i++) {
            splitted[i] = splitted[i].replaceAll("[^a-zA-Z]", ""); // podri revisar aqui
            if (word.equals(splitted[i])) {
                count++;
            }
        }
        return count;
    }

    public void mapPut() {
        String[] splitted = txt.split("\\s+");

        for (int i = 0; i < splitted.length; i++) {
            splitted[i] = splitted[i].replaceAll("[^a-zA-Z]", "");
            WordMap.put(splitted[i], wordCount(splitted[i]));
        }
    }


    //return the last time this document was used, via put/get or via a search result
    @Override
    public long getLastUseTime() {
        return time;
    }

    @Override
    public void setLastUseTime(long timeInNanoseconds) {
        this.time = timeInNanoseconds;
    }


    @Override
    public int compareTo(Document o) {
// check
        if(o == null){
         return 1;
        }

        if (this.time < o.getLastUseTime()) {
            return 1;
        }
        else if (this.time > o.getLastUseTime()) {
            return -1;
        }
        else {
            return 0;
        }
    }
}


