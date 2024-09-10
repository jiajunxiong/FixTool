package org.FixTool;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class FixTagLoader {
    public Map<Integer, String> loadFixTag(String filePath) {
        Map<Integer, String> fieldMap = new HashMap<>();

        try {
            File xmlFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            // Load fields
            NodeList fieldList = doc.getElementsByTagName("field");
            for (int i = 0; i < fieldList.getLength(); i++) {
                Element field = (Element) fieldList.item(i);
                Integer name = Integer.parseInt(field.getAttribute("name"));
                String description = field.getAttribute("description");
                fieldMap.put(name, description);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fieldMap;
    }

    public Map<Integer, List<Integer>> loadRepeatingGroup(String filePath) {
        Map<Integer, List<Integer>> repeatingGroup = new HashMap<>();
        
        try {
            File xmlFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            // Load repeating groups
            NodeList groupList = doc.getElementsByTagName("repeatingGroup");
            for (int i = 0; i < groupList.getLength(); i++) {
                Element group = (Element) groupList.item(i);
                Integer groupName = Integer.parseInt(group.getAttribute("name"));

                NodeList orderList = group.getElementsByTagName("group");
                List<Integer> orders = new ArrayList<>();

                for (int j = 0; j < orderList.getLength(); j++) {
                    Element order = (Element) orderList.item(j);
                                        
                    // Assuming each order has fields
                    NodeList orderFields = order.getElementsByTagName("field");
                    for (int k = 0; k < orderFields.getLength(); k++) {
                        Element orderField = (Element) orderFields.item(k);
                        Integer name = Integer.parseInt(orderField.getAttribute("name"));
                        orders.add(name);
                    }
                }
                repeatingGroup.put(groupName, orders);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return repeatingGroup;
    }
}
