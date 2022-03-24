package edu.yu.cs.com1320.project.stage3.impl;

import edu.yu.cs.com1320.project.stage3.Document;

import java.net.URI;
import java.util.HashMap;

public class DocumentImpl implements Document{

    public URI uri;
    public String txt;
    public int txtHash;
    public byte[]pdfBytes;
    public HashMap<String, Integer> WordMap = new HashMap<String, Integer>();




    public DocumentImpl(URI uri, String txt, int txtHash){
        this.uri = uri;
        this.txt = txt;
        this.txtHash = txtHash;
    }

    public DocumentImpl(URI uri, String txt, int txtHash, byte[] pdfBytes){
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

            for(int i=0; i<splitted.length; i++ ){
                splitted[i] = splitted[i].replaceAll("[^a-zA-Z]", "");
                if( word.equals(splitted[i])){
                    count ++;
                }

            }
            return count;
    }

    public void mapPut(){
        // insert key, COunt to the WordMap
         String[] splitted = txt.split("\\s+");

        for(int i=0; i<splitted.length; i++ ){

        WordMap.put(splitted[i], wordCount(splitted[i]));
            }
    }






}
