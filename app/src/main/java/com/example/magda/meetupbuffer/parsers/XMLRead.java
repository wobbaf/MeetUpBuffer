package com.example.magda.meetupbuffer.parsers;

import java.io.File;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class XMLRead {
    private XMLRead instance;
    public static String[] list;
    public static String id;
    public static String state;
    public static String location;
    public static String time;
    public static String type;
    public static String result;
    public static String xmlString = "<?xml version=\"1.0\" encoding=\"utf-8\"?><a><b></b><c></c></a>";
    public XMLRead(){
        instance = this;
    }
    public static void Read(String xml){
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));
            Element rootElement = document.getDocumentElement();
            //rootElement.getAttribute(id);
            type = getString("type",rootElement);
            id = getString("id",rootElement);
            switch(type){
                case "0":
                    location = getString("location",rootElement);
                    break;
                case "1":
                    state = getString("state",rootElement);
                    break;
                case "2":
                    location = getString("location",rootElement);
                    time = getString("time",rootElement);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    protected static String getString(String tagName, Element element) {
        NodeList list = element.getElementsByTagName(tagName);
        if (list != null && list.getLength() > 0) {
            NodeList subList = list.item(0).getChildNodes();
            if (subList != null && subList.getLength() > 0) {
                return subList.item(0).getNodeValue();
            }
        }
        return null;
    }
}