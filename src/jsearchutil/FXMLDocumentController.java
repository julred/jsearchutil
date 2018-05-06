/*
 * Copyright (C) 2018 Julian Reddemann @raspitux.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package jsearchutil;


import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;




public class FXMLDocumentController implements Initializable {
    
    @FXML
    public Label copy_by;
    
    @FXML
    public Label about_sw_name;
    
    @FXML
    public Label sumfolder;
    
    @FXML
    public TextField tf_input_search;
    
    @FXML
    public Button btn_add_element;
    
    @FXML
    public Button btn_del_element;
    
    @FXML
    public TextField tf_element_path;
    
    @FXML
    public ListView<String> listBoxMain;
    
    @FXML
    public TableView<DataModel> tv_search_result = new TableView<DataModel>();


    
    @FXML
    public TableColumn<DataModel, String> col_path;
    
    @FXML
    public TableColumn<DataModel, String> col_folder;
    
    public boolean bInit = false;
    public static ObservableList<String> listItems = FXCollections.observableArrayList();   
    
    public static List<String[]> DirectoriesList = new ArrayList<String[]>();
    

    ThreadReadDir readdir = new ThreadReadDir("ReadDir");
    
    
    final ObservableList<DataModel> TableData = FXCollections.observableArrayList();
    
    
    @FXML
    public void tv_mouse_clicked(MouseEvent event){
        
            if (event.getClickCount() == 2) //Checking double click
            {
            printLog(tv_search_result.getSelectionModel().getSelectedItem().getFolder());
            printLog(tv_search_result.getSelectionModel().getSelectedItem().getPath());
            
            // String -> File
            

            File openfile = new File(tv_search_result.getSelectionModel().getSelectedItem().getPath());
            
                try {
                    //Öffne path
                    java.awt.Desktop.getDesktop().open(openfile);
                } catch (IOException ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            
            }
    }
    
    
    @FXML
    public void openhomepage(){
        
         if(Desktop.isDesktopSupported()){
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(settings.StrOpenHomepage));
            } catch (IOException | URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }else{
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("xdg-open " + settings.StrOpenHomepage);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void add_element(ActionEvent action){
       String StrTF = tf_element_path.getText();
       
       
       // Überprüfen ob String leer ist
       
       if(StrTF.equals("")){
           // Nichts machen -> Dialog ausgeben -> Keine Eingabe getätigt
           Alert alert = new Alert(AlertType.WARNING, "No input for path!", ButtonType.OK);
            alert.showAndWait();
       }else{
       
       // Überprüfen ob Element bereits vorhanden ist
       
       
       if(listItems.contains(StrTF)){
           //Element bereits vorhanden
          Alert alert = new Alert(AlertType.WARNING, "Path already included.\nNot possible to add again.", ButtonType.OK);
            alert.showAndWait();

       }else{
           listItems.add(StrTF);
       }
       
       
       tf_element_path.setText("");
       
       // Elemente wurden verändert -> Verzeichnis aktualisieren
       readDirectories();
       initTable();
       }
     }
    
    @FXML
    public void clickInTable(MouseEvent event){
        
        if (event.getClickCount() == 2) //Checking double click
        {
        System.out.println(tv_search_result.getSelectionModel().getSelectedItem().getFolder());
        System.out.println(tv_search_result.getSelectionModel().getSelectedItem().getPath());
        }
        
    }
    
    @FXML
    public void del_element(ActionEvent action){
        int iMarkedElement = listBoxMain.getSelectionModel().getSelectedIndex();
        listItems.remove(iMarkedElement);
        
        // Gelöschte Datensätze -> Verzeichnis muss aktualisiert werden
        readDirectories();
        initTable();
        
        
     }
    
    @FXML
    public void tf_search_input_key(){
        String StrInputName = tf_input_search.getText();
        
       
        
        printLog(StrInputName);
       
        searchInput(StrInputName);
    }
    
    public void initTable(){
        
        
        printLog("InitTable wird aufgerufen");
        
        clearTable();
            String StrActualPath[] = null;

            
        for(int i=0; i<DirectoriesList.size(); i++){
            StrActualPath = DirectoriesList.get(i);
            addToTable(StrActualPath[0], StrActualPath[1]);
            }
            
        // Anzahl der gefilterten Datensätze auswerten
        sumfolder.setText("Datacount: "+TableData.size());
        
    }
    
    public void searchInput(String StrInputName){
        
         // Alte Einträge löschen
        clearTable();
            String StrActualPath[] = null;
            boolean bResult = false;
            
        for(int i=0; i<DirectoriesList.size(); i++){
            StrActualPath = DirectoriesList.get(i);
            
            
            // Unterscheidung der Groß- und Kleinschreibung
            
            if(StrActualPath[0].toUpperCase().contains(StrInputName.toUpperCase())==true){
                // Zeichenkette wurde gefunden -> Printout
                printLog("Zeichenkette: "+StrInputName+" wurde hier gefunden: " + StrActualPath[0]);
                
                // Unterscheidung Ordner und Path
                
                // Path:
                printLog("Folder: "+StrActualPath[0]);
                printLog("Path: "+StrActualPath[1]);
                
                addToTable(StrActualPath[0], StrActualPath[1]);
                
                // 
                
                
            }
            
            
            
        }
        
        // Anzahl der gefilterten Datensätze auswerten
        sumfolder.setText("Datacount: "+TableData.size());
        
    }
    
    public void addToTable(String StrFolder, String StrPath){
        
       
        
        // Hinzufügen zur Tabelle
        
        
        DataModel dm = new DataModel();
        dm.setPath(StrPath);
        dm.setFolder(StrFolder);
        TableData.add(dm);
        tv_search_result.refresh();
       
        
        
        
    }
    
    public void clearTable(){
        
        // Löscht die Eingaben
        
           TableData.clear();
           tv_search_result.refresh();
        
    }
    
    
    public void readDirectories(){
        
        
        
        
        
        // Initialisieren der DirectoriesList
        
        
        
        readdir.start();
        
        
        
        tv_search_result.refresh();
        
        
        /*
        // ListElemente laden und in eine Liste speichern mit Unterordnern
        if (listItems.size() != 0) {
            // Elemente vorhanden -> diese nun auslesen
  

            for(int i =0; i<listItems.size(); i++){
                File folder = new File(listItems.get(i));
                File[] listOfFiles = folder.listFiles();

                for (int j = 0; j < listOfFiles.length; j++) {
                    if (listOfFiles[j].isDirectory()) {
                        String[] StrElement = new String[2];
                        StrElement[0] = listOfFiles[j].getName();
                        StrElement[1] = folder.toString()+File.separator+listOfFiles[j].getName();
                        DirectoriesList.add(StrElement);
                        //printLog(folder.toString()+File.separator+listOfFiles[j].getName());
                    }
                }
            }           
          
           
        }
        */
    }
    
    
    public void printLog(String StrToPrint){
        if(settings.activatelog==true){
        System.out.println(StrToPrint);
        }
    }
    
    public void initAbout(){
        // Initialisiert das About fenster
        about_sw_name.setText(settings.StrWindowTitle + " V."+settings.StrAppVersion+"\nBuild: "+settings.StrBuildNo);
        copy_by.setText("\u00a9 by "+settings.StrSWCreator+" ("+settings.StrCreateDate+")");
        
        
        
    }

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        
        // Initialisierungsmethode
        
        col_path.setCellValueFactory(new PropertyValueFactory<>("path"));
        col_folder.setCellValueFactory(new PropertyValueFactory<>("folder"));


        tv_search_result.setItems(TableData);
        tv_search_result.refresh();

        tv_search_result.setEditable(false);
        
        

        
        
        tf_input_search.setText("");
        tf_element_path.setText("");
        
        
        
        // Datensätze einlesen
        try {
            listItems = handleFile.loadIniFile();
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Datensätze anzeigen
        listBoxMain.setItems(listItems);
        
        // Eingelesene Datensätze -> Verzeichnisliste erstellen
        readdir.start();
        

        

         
        initAbout();

        
    }    
    
    
    
    /*
    Threads
    */
   class ThreadReadDir extends Thread {
    private Thread t;
    private final String threadName;
   
   ThreadReadDir( String name) {
      threadName = name;
      printLog("Creating " +  threadName );
   }
   
    @Override
   public void run() {
      printLog("Running " +  threadName );
      DirectoriesList.clear();
      if (!listItems.isEmpty()) {
          // Elemente vorhanden -> diese nun auslesen
          
          
          for(int i =0; i<listItems.size(); i++){
              File folder = new File(listItems.get(i));
              File[] listOfFiles = folder.listFiles();
              
              for (File listOfFile : listOfFiles) {
                  if (listOfFile.isDirectory()) {
                      String[] StrElement = new String[2];
                      StrElement[0] = listOfFile.getName();
                      StrElement[1] = folder.toString()+File.separator + listOfFile.getName();
                      DirectoriesList.add(StrElement);
                      //printLog(folder.toString()+File.separator + listOfFile.getName());
                  }
              }
          }           
          
          
      }

      printLog("Thread " +  threadName + " exiting.");
              tv_search_result.refresh();
        initTable();
   }
   
    @Override
   public void start () {
      printLog("Starting " +  threadName );
      if (t == null) {
         t = new Thread (this, threadName);
         t.start ();
      }
   }
}

}


