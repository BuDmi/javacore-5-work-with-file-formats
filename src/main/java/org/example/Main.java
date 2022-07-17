package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        String projectFolderPath = "C://Work//Netology//javacore-5-work-with-file-formats//";

        // task 1
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        List<Employee> listFromCsv = parseCSV(columnMapping, projectFolderPath + "data.csv");
        writeToJsonFile(projectFolderPath, "dataCsv.json", listFromCsv);
        // task 2
        List<Employee> listFromXml = parseXML(projectFolderPath + "data.xml");
        writeToJsonFile(projectFolderPath, "dataXml.json", listFromXml);
        // task 3
        String json = readString(projectFolderPath + "dataXml.json");
        List<Employee> employees = jsonToList(json);
        for (Employee employee: employees) {
            System.out.println(employee);
        }
    }

    private static List<Employee> jsonToList(String json) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONArray array = (JSONArray) parser.parse(json);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        List<Employee> employees = new ArrayList<>();
        for (Object obj: array) {
            employees.add(gson.fromJson(obj.toString(), Employee.class));
        }
        return employees;
    }

    private static String readString(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            return br.readLine();
        } catch (IOException exception) {
            System.out.println(Arrays.toString(exception.getStackTrace()));
            return "";
        }
    }

    private static void writeToJsonFile(String projectFolderPath, String fileName, List<Employee> list) throws IOException {
        String json = listToJson(list);
        if (createNewFile(projectFolderPath, fileName)) {
            writeTextToFile(projectFolderPath + fileName, json);
        } else {
            System.out.println("Couldn't create file: " + fileName);
        }
    }

    private static List<Employee> parseXML(String filePath) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(filePath));

        Node root = doc.getDocumentElement();
        NodeList nodeList = root.getChildNodes();

        List<Employee> employees = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (Node.ELEMENT_NODE == node.getNodeType()) {
                Element element = (Element) node;
                long id = Long.parseLong(element.getElementsByTagName("id").item(0).getTextContent());
                String firstName = element.getElementsByTagName("firstName").item(0).getTextContent();
                String lastName = element.getElementsByTagName("lastName").item(0).getTextContent();
                String country = element.getElementsByTagName("country").item(0).getTextContent();
                int age = Integer.parseInt(element.getElementsByTagName("age").item(0).getTextContent());
                employees.add(new Employee(id, firstName, lastName, country, age));
            }
        }

        return employees;
    }

    private static Boolean createNewFile(String projectFolderPath, String fileName) {
        File file = new File(projectFolderPath, fileName);

        try {
            if (file.exists()) {
                file.delete();
            }
            return file.createNewFile();
        } catch (IOException ex) {
            return false;
        }
    }

    private static void writeTextToFile(String s, String text) throws IOException {
        try (FileWriter writer = new FileWriter(s, false)) {
            writer.write(text);
            writer.flush();
        } catch (IOException ex) {
            System.out.println(Arrays.toString(ex.getStackTrace()));
            throw new IOException(ex);
        }
    }

    private static String listToJson(List<Employee> list) {
        Type listType = new TypeToken<List<Employee>>() {}.getType();
        Gson gson = new Gson();
        return gson.toJson(list, listType);
    }

    private static List<Employee> parseCSV(String[] columnMapping, String fileName) throws Exception {
        try (CSVReader csvReader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);

            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(strategy)
                    .build();

            return csv.parse();
        } catch (Exception ex) {
            System.out.println(Arrays.toString(ex.getStackTrace()));
            throw new Exception(ex);
        }
    }
}