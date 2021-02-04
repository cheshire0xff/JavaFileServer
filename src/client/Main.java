package client;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import javafx.scene.image.ImageView;
import Controller.Controller;
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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class Main extends Application {
	private ListView<ServerChoice> listViewServers;
	private Button newServerButton;
	private TextField newServerName;
	private TextField newServerAddress;
	private Button connectButton;
	private ListView<Object> listViewFiles;
	
	private ObservableList<ServerChoice> serversList;
	private ObservableList<Object> filesList;
	
	private final Image folderImg  = new Image("https://upload.wikimedia.org/wikipedia/commons/f/f1/Ruby_logo_64x64.png");
    private final Image fileImg  = new Image("http://findicons.com/files/icons/832/social_and_web/64/apple.png");
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
	
	private void connect() {
		filesList = new DirectoryContent<Object>();
		ServerChoice server = listViewServers.getSelectionModel().getSelectedItem();
		try {
			Controller controller = new Controller(server.getAddres());
			getFiles(controller.rootDir, "", controller);
		} catch (ClassNotFoundException | IOException e) {
			errorDisplay(e);
		}
	}
	
	private void getFiles (RemoteDirectory pwd, String tabs,Controller controller) {
		if(controller != null) {
	        for (var f : pwd.files)
	        {
	        	filesList.add(f);
	        	
	        }
	        for (var f : pwd.dirs)
	        {
	            System.out.println(tabs + f.directoryName);
	            filesList.add(f);
	            getFiles(f, tabs + "\t", controller);
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
			        	setGraphic(imageView);
			        	//TODO Dokoñczyæ
			        }
			    }
			});
			listViewFiles.setItems(filesList);
//			VBox box = new VBox(listViewFiles);
//	        box.setAlignment(Pos.CENTER);
			
//	        Scene scene = new Scene(box, 200, 200);
//	        primaryStage.setScene(scene);
//	        primaryStage.show();
		}
	}
	
	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader
					.load(getClass()
					.getResource("ui/MainWindow.fxml"));
			Scene scene = new Scene(root, 800, 600);
			
			primaryStage.setTitle("FileServer App");
			primaryStage.setScene(scene);
			primaryStage.show();
			
			listViewServers = (ListView<ServerChoice>) scene.lookup("#ListViewServers");
			newServerButton = (Button) scene.lookup("#newServerButton");
			newServerName = (TextField) scene.lookup("#newServerName");
			newServerAddress = (TextField) scene.lookup("#newServerAddress");
			connectButton = (Button) scene.lookup("#connectButton");
			listViewFiles = (ListView<Object>) scene.lookup("#fileListView");
			
			loadServersList();
			
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
			
			connectButton.setOnMouseClicked(event ->{
				connect();
				displayFiles();
			});
			
			
		} catch(Exception e) {
			errorDisplay(e);
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
