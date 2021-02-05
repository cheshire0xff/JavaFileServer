package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Optional;
import javafx.scene.image.ImageView;
import ClientApi.ClientApi;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import server.RemoteDirectory;
import server.RemoteFileInfo;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;

public class Main extends Application {
	private ListView<ServerChoice> listViewServers;
	private Button newServerButton;
	private Button deleteServerButton;
	private TextField newServerName;
	private TextField newServerAddress;
	private Button connectButton;
	private ListView<Object> listViewFiles;
	private TextArea textAreaFileDetails;
	private Label serverStatusLabel;
	private Label remoteObjectStatusLabel;
	private MenuItem aboutMenuItem;
	private MenuBar menuBar;
	
	private ObservableList<ServerChoice> serversList;
	private ObservableList<Object> filesList;
	
	private Image folderImg;
    private Image fileImg;
//    private final Image[] listOfImages = {folderImg, fileImg};
	
	private void errorDisplay(Exception e) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error Dialog");
		alert.setHeaderText("Execption occured in FileServer App");
		alert.setContentText(e.toString());

		alert.showAndWait();
	}
	
	private void saveServersList(ArrayList<ServerChoice> servers) {
		try (ObjectOutputStream outputStream = 
				new ObjectOutputStream(
						new FileOutputStream("savedServers.bin"))) {
		    outputStream.writeObject(new ArrayList<ServerChoice>(servers));
		} catch (Exception e) {
			errorDisplay(e);
		}
	}
	
	private Boolean loadServersList() throws UnknownHostException {
		ArrayList<ServerChoice> servers = new ArrayList<ServerChoice>();
		
		try (ObjectInputStream inputStream = 
				new ObjectInputStream(
					new FileInputStream("savedServers.bin"))) {			
				servers = (ArrayList<ServerChoice>) inputStream.readObject();
		} catch (Exception e) {
			errorDisplay(e);
		}
		
		if(servers == null || servers.size() == 0 || servers.isEmpty()) {
			System.out.println("ListViewServers == null");
			servers = new ArrayList<ServerChoice>();
			servers.add(new ServerChoice("localhost", InetAddress.getLocalHost()));
			saveServersList(servers);
		}			
	
		if (listViewServers == null) {
			System.out.println("ListViewServers == null");
			return false;
		}
		else {
			listViewServers.setCellFactory(param -> new ListCell<ServerChoice>() {
			    @Override
			    protected void updateItem(ServerChoice item, boolean empty) {
			        super.updateItem(item, empty);

			        if (empty || item == null || item.getServerName() == null) {
			            setText(null);
			        } else {
			            setText(item.getServerName());
			        }
			    }
			});
			serversList = new ServerChoiceList<ServerChoice>();
			serversList.addAll(servers);
			listViewServers.setItems(serversList);
			return true;
		}
			
	}
	
	private Boolean connect() {
		filesList = new DirectoryContent<Object>();
		ServerChoice server = listViewServers.getSelectionModel().getSelectedItem();
		try {
			ClientApi controller = new ClientApi(server.getAddres());
			getFiles(controller.rootDir, controller);
			serverStatusLabel.setText("Connected");
//			TODO jak zmieni� klas� elementu z kodu
//			serverStatus.clas
			return true;
		} catch (ClassNotFoundException | IOException e) {
			errorDisplay(e);
			return false;
		}
	}
	
	private void getFiles (RemoteDirectory pwd,ClientApi controller) {
		if(controller != null) {
	        for (var f : pwd.files)
	        {
	        	filesList.add(f);
	        	
	        }
	        for (var f : pwd.dirs)
	        {
	            filesList.add(f);
//	            getFiles(f, controller); //TODO Pobiera� wszytkie pliki i je wy�wietla� na raz czy tylko p�asko i owieranie plik�
	        }
			    
		}
	}
	
	private void displayFiles () {
 		if (filesList == null || filesList.size() == 0) {
			System.out.println("filesList == null or empty");
			return;
		}
		else {
			listViewFiles.setCellFactory(param -> new ListCell<Object>() {
				private ImageView imageView = new ImageView();
				
			    @Override
			    protected void updateItem(Object item, boolean empty) {
			        super.updateItem(item, empty);

			        if (empty || item == null) {
			            setText(null);
			            setGraphic(null);
			        } else {
			        	if(item instanceof RemoteFileInfo) {
			        		RemoteFileInfo tmp = (RemoteFileInfo) item;
			        		setText(tmp.filename);
			        		imageView.setImage(fileImg);
			        		
			        	}
			        	else if (item instanceof RemoteDirectory) {
			        		RemoteDirectory tmp = (RemoteDirectory) item;
			        		setText(tmp.directoryName);
			        		imageView.setImage(folderImg);
			        	}
			        	else {
			        		setText("No path for class: " + item.getClass());
			        	}
			        	imageView.setFitWidth(16);
		        		imageView.setFitHeight(16);
			        	setGraphic(imageView);
			        }
			    }
			});
			listViewFiles.setItems(filesList);
		}
	}
	
	private void initialize(Scene scene) {
		listViewServers = (ListView<ServerChoice>) scene.lookup("#ListViewServers");
		newServerButton = (Button) scene.lookup("#newServerButton");
		newServerName = (TextField) scene.lookup("#newServerName");
		newServerAddress = (TextField) scene.lookup("#newServerAddress");
		deleteServerButton = (Button) scene.lookup("#deleteServerButton");
		connectButton = (Button) scene.lookup("#connectButton");
		listViewFiles = (ListView<Object>) scene.lookup("#fileListView");
		textAreaFileDetails = (TextArea) scene.lookup("#textAreaFileDetails");
		serverStatusLabel = (Label) scene.lookup("#serverStatus");
		remoteObjectStatusLabel = (Label) scene.lookup("#remoteObjectStatusLabel");
		
		try {
			folderImg = new Image(
					new FileInputStream(
							new File(System.getProperty("user.dir")
									+"/src/client/ui/folder.png")));
			fileImg = new Image(
					new FileInputStream(
							new File(System.getProperty("user.dir")
									+"/src/client/ui/file.png")));
		} catch (FileNotFoundException e) {
			errorDisplay(e);
		}
		
		newServerButton.setOnMouseClicked(event ->{
			try {
				serversList.add(new ServerChoice(
						newServerName.getText(),
						InetAddress.getByName(newServerAddress.getText())));
				saveServersList(new ArrayList<ServerChoice>(serversList));
			} catch (UnknownHostException e) {
				errorDisplay(e);
			}
		});
		
		deleteServerButton.setOnMouseClicked(event ->{
			ServerChoice server = listViewServers.getSelectionModel().getSelectedItem();
			
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Confirmation Dialog");
			alert.setHeaderText("Delete server");
			alert.setContentText("Are you sure you want to remove this server: "
					+ server.getServerName());

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK){
				
				serversList.remove(server);
				saveServersList(new ArrayList<ServerChoice>(serversList));
			}
			
		});
		
		connectButton.setOnMouseClicked(event ->{
			if(connect())
				displayFiles();
		});
		
		listViewFiles.setOnMouseClicked(event -> {
			//TODO sprawdzi� jak zrobi� obs�ug� rich text albo html
			
			//TODO zrobi� otwieranie folder�w
			//TODO zrobi� pobieranie plik�w
			Object remoteObject = listViewFiles.getSelectionModel().getSelectedItem();
			String text;
			if(remoteObject instanceof RemoteFileInfo) {
				RemoteFileInfo remoteFile = (RemoteFileInfo) remoteObject;
				remoteObjectStatusLabel.setText(remoteFile.filename);
				
				text = "File name: " + remoteFile.filename;
				textAreaFileDetails.setText(text);
			}
			else if(remoteObject instanceof RemoteDirectory) {
				RemoteDirectory remoteDirectory = (RemoteDirectory) remoteObject;
				remoteObjectStatusLabel.setText(remoteDirectory.directoryName);
				
				text = "Directory name: " + remoteDirectory.directoryName;
				textAreaFileDetails.setText(text);
			}
			else {
				text = "No path for class: " + remoteObject.getClass();
				textAreaFileDetails.setText(text);
			}
		});
		
		//TODO doda� wi�cej funkcionalno�ci dla folder�w/plik�w
	}
	
	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader
					.load(getClass()
					.getResource("ui/MainWindow.fxml"));
			Scene scene = new Scene(root, 800, 600);
			root.getStylesheets().add(getClass().getResource("ui/MainWindow.css").toString());
			
			primaryStage.setTitle("FileServer App");		
			primaryStage.setScene(scene);
			primaryStage.show();
			
			initialize(scene);			
			loadServersList();
			
		} catch(Exception e) {
			errorDisplay(e);
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
