package edu.yu.cs.com1320.project.stage5.impl;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.sun.jndi.toolkit.url.Uri;
import edu.yu.cs.com1320.project.stage5.Document;

import edu.yu.cs.com1320.project.stage5.PersistenceManager;
import jdk.nashorn.internal.parser.JSONParser;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Map;
import java.util.Set;


/**
 * created by the document store and given to the BTree via a call to BTree.setPersistenceManager
 */
public class DocumentPersistenceManager implements PersistenceManager<URI, Document>{

    protected File baseDir;

    public DocumentPersistenceManager(File baseDir){
        this.baseDir = baseDir;
            }


    @Override
    public void serialize(URI uri, Document val) throws IOException {


        String txt = val.getDocumentAsTxt();
        int hash = txt.hashCode();
        URI key = uri;
        Map map = val.getWordMap();

        JsonObject jObj = new JsonObject();
        GsonBuilder builder = new GsonBuilder();
        String jsonUri = builder.create().toJson(uri);
        String jsonMap = builder.create().toJson(map);
        String base64Encoded = DatatypeConverter.printBase64Binary(val.getDocumentAsPdf());

        jObj.addProperty("text", txt);
        jObj.addProperty("hashCode", hash);
        jObj.addProperty("URI", jsonUri);
        jObj.addProperty("Map", jsonMap);
        jObj.addProperty("Bintary", base64Encoded);

        Gson gson = new Gson();

        String plainUri = uri.toString();
        String separator = File.separator;

        String replaced = plainUri.replace(uri.getScheme(), "").replace("://", "").replace("/", separator);

        String filePath = baseDir + separator + replaced;
        File fInfo = new File(filePath);
        String namePath = filePath.replace(fInfo.getName(), "");

        File f = new File(namePath);
        if (!f.exists()) {
            f.mkdirs();
        }

        FileWriter myWriter = new FileWriter(filePath + ".json"); // aqui se crea el file
        gson.toJson(jObj, myWriter);
        myWriter.close();

    }


    @Override
    public Document deserialize(URI uri) throws IOException {


        String plainUri =  uri.toString();
        String separator = File.separator;

        String replaced =  plainUri.replace(uri.getScheme(),"").replace("://","").replace("/",separator);


        String filePath = baseDir +separator+ replaced + ".json";
        File fInfo = new File(filePath);
        String namePath = filePath.replace(fInfo.getName(),"");

       Gson gson = new Gson();

        BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));


        Object d =  gson.fromJson(bufferedReader.readLine(), Object.class);
//
//        GsonBuilder builder = new GsonBuilder();
//        Gson gson1 = builder.create();
        //  String dr = gson1.toJson(d);

        Gson gson4 = new GsonBuilder().create();
        JsonElement element = gson4.toJsonTree(d,Object.class);

        String text =  element.getAsJsonObject().get("text").toString().replace("\"", "");;
        JsonElement hashc =  element.getAsJsonObject().get("hashCode");
        JsonElement key =  element.getAsJsonObject().get("URI");
        JsonElement map =  element.getAsJsonObject().get("Map");

      String ur =  key.getAsJsonPrimitive().getAsString();
     ur = ur.replace("\"", "");
      URI finalUri = URI.create(ur);

//        GsonBuilder builder = new GsonBuilder();
//        String jsonUri = builder.create().fromJson(key);
       // String jsonMap = builder.create().toJson(map);



       Document doc = new DocumentImpl(finalUri,text,text.hashCode());
       bufferedReader.close();




        File theFile =  new File(filePath);
            theFile.delete();

       for (int i =0; i< 15; i++){

           File File =  new File(namePath);
           if(File.list().length >0) {
              break;
           }
               File.delete();
               namePath= namePath.replace(File.getName(),"");
               i++;
           }

        return doc;
    }

}