package edu.yu.cs.com1320.project.stage1.impl;

import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage1.DocumentStore;

import org.apache.pdfbox.contentstream.PDContentStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.net.URI;
//import java.io.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;



public class DocumentStoreImpl  implements DocumentStore {

    enum DocumentFormat{
        TXT,
        PDF
    }

    HashTableImpl<URI, DocumentImpl> table = new HashTableImpl<>();


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


        if (uri == null) {
            throw new IllegalArgumentException("URI cannot be null!");
        }
        if (input == null) {
            if (table.get(uri) != null){
                table.put(uri, null);
            }
            throw new IllegalArgumentException("Input cannot be null!");
            // need to delete the URI for this value
        }

        try{
            if (format == DocumentStore.DocumentFormat.TXT) {
                String info = new String(toByteArray(input));
                DocumentImpl doc = new DocumentImpl(uri, info, info.hashCode());// construct a DOCUMENT object
                table.put(uri, doc);
                return info.hashCode();
            }

            if (format == DocumentStore.DocumentFormat.PDF) {

                PDFTextStripper inParts = new PDFTextStripper();
               // byte[] arrByte = toByteArray(input);
                // todo
                String lines = inParts.getText(PDDocument.load(input)).trim();
                DocumentImpl document = new DocumentImpl(uri, lines, lines.hashCode(), toByteArray(input));// construct a DOCUMENT object
                table.put(uri, document);
                return lines.hashCode();


            }
        } catch (IOException e ){
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public byte[] getDocumentAsPdf(URI uri) {
        if (uri == null){
            throw new IllegalArgumentException("URI cannot be null!");
        }


        if (table.get(uri) == null){
            // return null if there is no document with that URI.
            return null;
        }


        DocumentImpl dd = new DocumentImpl(uri, table.get(uri).getDocumentAsTxt(), table.get(uri).hashCode()); // new a document from where to to extract the text

      File PDFFile = new File("joey"); // name on file


        PDDocument doc = new PDDocument();
            PDPage page = new PDPage();
            doc.addPage(page);


            try {
                PDPageContentStream cont = new PDPageContentStream(doc, page);


                cont.setFont(PDType1Font.COURIER, 12);
                cont.beginText();
                cont.showText(dd.getDocumentAsTxt()); // text in the document file
                cont.endText();
                cont.close();

                doc.save(PDFFile); // name on pdf?
                doc.close();


                PDFTextStripper stripper = new PDFTextStripper();
                InputStream pdIn = new FileInputStream(PDFFile);
                String lines = stripper.getText(PDDocument.load(pdIn)).trim();
                DocumentImpl document3 = new DocumentImpl(uri, lines, lines.hashCode(), toByteArray(pdIn));



        return document3.getDocumentAsPdf();

            } catch ( IOException e){

                return null;
            }
    }

    @Override
    public String getDocumentAsTxt(URI uri) {
        if (uri == null){
            throw new IllegalArgumentException("URI cannot be null!");
        }
        if (table.get(uri) == null) { // return null if no document with that URI
            return" null";
        }

        DocumentImpl drs = new DocumentImpl(uri, table.get(uri).getDocumentAsTxt(), table.get(uri).hashCode());


        return drs.getDocumentAsTxt();
    }

    @Override
    public boolean deleteDocument(URI uri) {
        if (uri == null){
            throw new IllegalArgumentException("URI cannot be null!");
        }
           if ( table.get(uri) !=null){
               table.put(uri, null); // put key with a null value


            return true;
           }
           //@return true if the document is deleted, false if no document exists with that URI
             return false;
    }

}

