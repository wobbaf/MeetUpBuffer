package com.example.magda.meetupbuffer.parsers;
import org.w3c.dom.*;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.parsers.*;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.*;
import java.util.ArrayList;

public class XMLParse {
    private static XMLParse instance;
    public static String id;
    public static String state;
    public static String location;
    public static String time;
    public static String type;
    public static String res;
    public static ArrayList<String> friends = new ArrayList<String>();
    public static XMLParse instance(){
        return instance;
    }
    public XMLParse(){
        instance = this;
    }
    public static String Parse(){
        try {
            DocumentBuilderFactory dbFactory =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();
            Element rootElement = doc.createElement("content");
            doc.appendChild(rootElement);
            if (type != null){
                Element subElement0 = doc.createElement("type");
                subElement0.appendChild(doc.createTextNode(type));
                rootElement.appendChild(subElement0);
            }
            if (id != null){
                Element subElement = doc.createElement("id");
                subElement.appendChild(doc.createTextNode(id));
                rootElement.appendChild(subElement);
            }
            if (state != null){
                Element subElement1 = doc.createElement("state");
                subElement1.appendChild(doc.createTextNode(state));
                rootElement.appendChild(subElement1);
            }
            if (location != null){
                Element subElement2 = doc.createElement("location");
                subElement2.appendChild(doc.createTextNode(location));
                rootElement.appendChild(subElement2);
            }
            if (time != null){
                Element subElement3 = doc.createElement("time");
                subElement3.appendChild(doc.createTextNode(time));
                rootElement.appendChild(subElement3);
            }
            if (friends != null){
                Element subElement3 = doc.createElement("friends");
                for(int i = 0; i < friends.size(); i++){
                    Element friend = doc.createElement("friend");
                    friend.appendChild(doc.createTextNode(friends.get(i)));
                    friend.setAttribute("id", friends.get(i));
                    subElement3.appendChild(friend);
                }
                rootElement.appendChild(subElement3);
            }
            //DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
            //LSSerializer lsSerializer = domImplementation.createLSSerializer();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter stringWriter = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(stringWriter));
            res = stringWriter.toString();
            //res = lsSerializer.writeToString(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
}