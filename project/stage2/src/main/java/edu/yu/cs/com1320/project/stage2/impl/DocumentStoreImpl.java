package edu.yu.cs.com1320.project.stage2.impl;

import edu.yu.cs.com1320.project.Command;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;

import edu.yu.cs.com1320.project.stage2.Document;
import edu.yu.cs.com1320.project.stage2.DocumentStore;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.print.Doc;
import java.io.*;
import java.net.URI;
import java.util.function.Function;


public class DocumentStoreImpl  implements DocumentStore {

    DocumentStore.DocumentFormat format;
    public DocumentStoreImpl(){
        format = null;
    }

    enum DocumentFormat {
        TXT,
        PDF
    }

    HashTableImpl<URI, DocumentImpl> table = new HashTableImpl<>(); // Hashtable instance
    StackImpl<Command> stack = new StackImpl<>(); // Stack instance
    StackImpl<Command> stack2 = new StackImpl<>();






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

            Function<URI, Boolean> deletes =  (URI) -> deleteDocument(uri);    //write a function that deletes (i.e. undo the put) the document
            Command command = new Command(uri,deletes); // create a command
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
                    return info.hashCode();
                }

                if (format == DocumentStore.DocumentFormat.PDF) {

                    PDFTextStripper inParts = new PDFTextStripper();
                    // byte[] arrByte = toByteArray(input);

                    String lines = inParts.getText(PDDocument.load(input)).trim();
                    DocumentImpl document = new DocumentImpl(uri, lines, lines.hashCode(), toByteArray(input));// construct a DOCUMENT object
                    table.put(uri, document);
                    return lines.hashCode();


                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        public byte[] getDocumentAsPdf (URI uri){
            if (uri == null) {
                throw new IllegalArgumentException("URI cannot be null!");
            }

            if (table.get(uri) == null) { // return null if there is no document with that URI.
                return null;
            }

            try {
                DocumentImpl dd = new DocumentImpl(uri, table.get(uri).getDocumentAsTxt(), table.get(uri).hashCode()); // new a document from where to to extract the text

                File PDFFile = new File("joey"); // name on file


                PDDocument doc = new PDDocument();
                PDPage page = new PDPage();
                doc.addPage(page);

                PDPageContentStream cont = new PDPageContentStream(doc, page);


                cont.setFont(PDType1Font.COURIER, 12);
                cont.beginText();
                cont.showText(dd.getDocumentAsTxt()); // text in the document file
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
        public String getDocumentAsTxt (URI uri){
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
        public boolean deleteDocument (URI uri){

        DocumentImpl d = new DocumentImpl( uri,getDocumentAsTxt(uri),getDocumentAsTxt(uri).hashCode());
        InputStream is = new ByteArrayInputStream( d.getDocumentAsTxt().getBytes() );
        Function<URI, Boolean> puts =  (InputStream, URI, DocumentStore.DocumentFormat) -> this.putDocument(is, uri, this.format );
        Command command = new Command(uri,puts); // create a command
            stack.push(command); // this command



            if (uri == null) {
                throw new IllegalArgumentException("URI cannot be null!");
            }
            if (table.get(uri) != null) {
                table.put(uri, null); // put key with a null value

                return true;
            }
            //@return true if the document is deleted, false if no document exists with that URI
            return false;
        }


        public void undo () throws IllegalStateException {
                Command com = stack.peek();
                stack.pop();
                com.undo();  // call command undo

        }


       public void undo (URI uri) throws IllegalStateException {

        Command c = stack.peek();
          if( c.getUri() != uri ){
             stack2.push(stack.peek());
             stack.pop();
         }

         Command com = stack.peek();
         stack.pop();
         com.undo();

         while (stack2.size() != 0 ){
             stack.push(stack2.peek());
             stack2.pop();
         }


        }
    }



