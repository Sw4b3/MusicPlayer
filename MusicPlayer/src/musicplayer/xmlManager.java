/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template xmlLibrary, choose Tools | Templates
 * and open the template in the editor.
 */
package musicplayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Andrew
 */
public class xmlManager {

    static String columnNames[] = {"No.", "Song", "Artist", "Album"};
    static String data = new String();
    static String currentUsersHomeDir = System.getProperty("user.home");
    static String newDirectory;
    static File xmlLibrary;
    static String location = currentUsersHomeDir + File.separator + "Google Drive\\Programs\\MusicPlayer\\src\\library\\Settings.xml";
    static File xmlSettings = new File(location);
    static ArrayList numberList = new ArrayList();
    static ArrayList songList = new ArrayList();
    static ArrayList artistList = new ArrayList();
    static ArrayList albumList = new ArrayList();
    static ArrayList audioFilePath = new ArrayList();
    static ArrayList<String> imageFilePath = new ArrayList();
    static public JTable playlist = new JTable();
    static String imagePath = "";

    public xmlManager(JTable playlist, ArrayList songList, ArrayList artistList, ArrayList audioFilePath) {
        this.playlist = playlist;
        this.songList = songList;
        this.artistList = artistList;
        this.audioFilePath = audioFilePath;
    }

    public static void getSettings() {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(xmlSettings);
            NodeList nList = document.getElementsByTagName("generalsettings");
            newDirectory = document.getElementsByTagName("librarydic").item(0).getTextContent();
            xmlLibrary = new File(newDirectory);
            System.out.println(newDirectory);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(xmlManager.class
                    .getName()).log(Level.SEVERE, null, ex);

        }
    }

    public static void updateSettings() {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(xmlSettings);

            Element rootElement = document.getDocumentElement();
            Node director = document.getElementsByTagName("generalsettings").item(0);

            rootElement.appendChild(director);
            NodeList list = director.getChildNodes();
            Node node = list.item(1);

            if ("librarydic".equals(node.getNodeName())) {
                node.setTextContent(SettingsForm.libDic.getText());
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(xmlSettings);
            transformer.transform(source, result);
            populateTable();
            refreshDetails();
            xmlValidition();

        } catch (ParserConfigurationException | SAXException | IOException ex) {
        } catch (TransformerException ex) {
            Logger.getLogger(xmlManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void xmlValidition() {
        if (xmlSettings.exists()) {
            getSettings();
        } else {
            System.out.println("File not found");
            try {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document document = docBuilder.newDocument();
                Element rootElement = document.createElement("settings");
                document.appendChild(rootElement);

                Element generalSettings = document.createElement("generalsettings");
                rootElement.appendChild(generalSettings);

                Element directory = document.createElement("librarydic");
                directory.appendChild(document.createTextNode(currentUsersHomeDir
                        + File.separator + "Google Drive\\Programs\\MusicPlayer\\src\\library\\Musiclibrary.xml"));
                generalSettings.appendChild(directory);

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(document);
                StreamResult result = new StreamResult(xmlSettings);
                transformer.transform(source, result);

                System.out.println("File create");

            } catch (ParserConfigurationException | TransformerException pce) {
            }
        }

        if (xmlLibrary.exists()) {
            populateTable();
            getSongDetails();
            getFilePath();
        } else {
            System.out.println("File not found");
            try {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document document = docBuilder.newDocument();
                Element rootElement = document.createElement("library");
                document.appendChild(rootElement);
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(document);
                StreamResult result = new StreamResult(xmlLibrary);
                transformer.transform(source, result);

                System.out.println("File create");

            } catch (ParserConfigurationException | TransformerException pce) {
            }
        }

    }

    public static void populateTable() {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(xmlLibrary);
            NodeList nList = document.getElementsByTagName("musicpropities");
            Object rowData[][] = new String[nList.getLength()][4];

            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                rowData[i][0] = document.getElementsByTagName("number").item(i)
                        .getTextContent();
                rowData[i][1] = document
                        .getElementsByTagName("song").item(i)
                        .getTextContent();
                rowData[i][2] = document
                        .getElementsByTagName("artist").item(i)
                        .getTextContent();
                rowData[i][3] = document
                        .getElementsByTagName("album").item(i)
                        .getTextContent();
            }

            DefaultTableModel tableModel = new DefaultTableModel(rowData, columnNames);
            playlist.setModel(tableModel);
        } catch (IOException | ParserConfigurationException | DOMException | SAXException e) {
        }
    }

    public static void getFilePath() {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(xmlLibrary);
            NodeList nList = document.getElementsByTagName("musicpropities");

            for (int i = 0; i < nList.getLength(); i++) {
                audioFilePath.add(document.getElementsByTagName("directory").item(i).getTextContent());
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(xmlManager.class
                    .getName()).log(Level.SEVERE, null, ex);

        }

    }

    public static void refreshDetails() {
        numberList.clear();
        songList.clear();
        artistList.clear();
        albumList.clear();
        audioFilePath.clear();
        imageFilePath.clear();
        getSongDetails();
        getFilePath();
    }

    public static void getSongDetails() {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;

        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(xmlLibrary);
            NodeList nList = document.getElementsByTagName("musicpropities");

            for (int i = 0; i < nList.getLength(); i++) {
                numberList.add(document.getElementsByTagName("number").item(i).getTextContent());
                songList.add(document.getElementsByTagName("song").item(i).getTextContent());
                artistList.add(document.getElementsByTagName("artist").item(i).getTextContent());
                albumList.add(document.getElementsByTagName("album").item(i).getTextContent());
                imageFilePath.add(document.getElementsByTagName("cover").item(i).getTextContent());
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(xmlManager.class
                    .getName()).log(Level.SEVERE, null, ex);

        }
    }

    public static void insertSong() {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(xmlLibrary);

            Element rootElement = document.getDocumentElement();

            Element properties = document.createElement("musicpropities");
            rootElement.appendChild(properties);

            Element number = document.createElement("number");
            number.appendChild(document.createTextNode(AddForm.numberField.getText()));
            properties.appendChild(number);

            Element song = document.createElement("song");
            song.appendChild(document.createTextNode(AddForm.songField.getText()));
            properties.appendChild(song);

            Element artist = document.createElement("artist");
            artist.appendChild(document.createTextNode(AddForm.artistField.getText()));
            properties.appendChild(artist);

            Element album = document.createElement("album");
            album.appendChild(document.createTextNode(AddForm.albumField.getText()));
            properties.appendChild(album);

            Element directory = document.createElement("directory");
            directory.appendChild(document.createTextNode(AddForm.directoryField.getText()));
            properties.appendChild(directory);

            Element coverPath = document.createElement("cover");
            coverPath.appendChild(document.createTextNode(""));
            properties.appendChild(coverPath);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(xmlLibrary);
            transformer.transform(source, result);
            populateTable();
            refreshDetails();

        } catch (ParserConfigurationException | SAXException | IOException ex) {
        } catch (TransformerException ex) {
            Logger.getLogger(xmlManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void updateSongDetails(int index, String coverPath) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(xmlLibrary);

            Element rootElement = document.getDocumentElement();
            Node property = document.getElementsByTagName("musicpropities").item(index);

            rootElement.appendChild(property);
            NodeList list = property.getChildNodes();

            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                if ("number".equals(node.getNodeName())) {
                    node.setTextContent(SongProperties.songNumber.getText());
                }
                if ("song".equals(node.getNodeName())) {
                    node.setTextContent(SongProperties.songName.getText());
                }
                if ("artist".equals(node.getNodeName())) {
                    node.setTextContent(SongProperties.artistName.getText());
                }
                if ("album".equals(node.getNodeName())) {
                    node.setTextContent(SongProperties.albumName.getText());
                }

                if ("cover".equals(node.getNodeName())) {
                    node.setTextContent(coverPath);
                    System.out.println(node);
                }

            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(xmlLibrary);
            transformer.transform(source, result);
            populateTable();
            refreshDetails();

        } catch (ParserConfigurationException | SAXException | IOException ex) {
        } catch (TransformerException ex) {
            Logger.getLogger(xmlManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void updateDirectoy(int index, String audioPath) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(xmlLibrary);

            Element rootElement = document.getDocumentElement();
            Node property = document.getElementsByTagName("musicpropities").item(index);

            rootElement.appendChild(property);
            NodeList list = property.getChildNodes();

            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                if ("directory".equals(node.getNodeName())) {
                    node.setTextContent(audioPath);

                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(xmlLibrary);
            transformer.transform(source, result);
            populateTable();
            refreshDetails();

        } catch (ParserConfigurationException | SAXException | IOException ex) {
        } catch (TransformerException ex) {
            Logger.getLogger(xmlManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void clearLibrary() {
        try {
            if (xmlLibrary.delete()) {
                System.out.println(xmlLibrary.getName() + " is deleted");
                try {
                    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                    Document doc = docBuilder.newDocument();
                    Element rootElement = doc.createElement("library");
                    doc.appendChild(rootElement);
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    DOMSource source = new DOMSource(doc);
                    StreamResult result = new StreamResult(xmlLibrary);
                    transformer.transform(source, result);
                    System.out.println("File create");
                    populateTable();
                    refreshDetails();

                } catch (ParserConfigurationException | TransformerException pce) {
                }
            } else {
                System.out.println("Delete operation is failed");
            }

        } catch (DOMException e) {
        }
    }
}
