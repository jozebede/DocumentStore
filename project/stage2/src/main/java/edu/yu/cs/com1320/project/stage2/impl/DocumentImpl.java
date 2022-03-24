package edu.yu.cs.com1320.project.stage2.impl;

import edu.yu.cs.com1320.project.stage2.Document;

import java.net.URI;

public class DocumentImpl implements Document{

    public URI uri;
    public String txt;
    public int txtHash;
    public byte[]pdfBytes;


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
        // return document as a simple string
        return txt;
    }

    @Override
    public int getDocumentTextHashCode() {
        //* @return hash code of the plain text version of the document
        return txtHash;
    }

    @Override
    public URI getKey() {
        // return URI which uniquely identifies this document
        return uri;
    }
}
