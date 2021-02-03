package client;

import java.io.Console;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class Main extends Application {
	private ListView<ServerChoice> listViewServers;
	
	private void saveServersList(ArrayList<ServerChoice> servers) {
		try (ObjectOutputStream outputStream = 
				new ObjectOutputStream(
						new FileOutputStream("savedServers.bin"))) {
		    outputStream.writeObject(new ArrayList<ServerChoice>(servers));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Boolean loadServersList() throws UnknownHostException {
		ArrayList<ServerChoice> servers = new ArrayList();
//		ArrayList<ServerChoice> tmp = new ArrayList<ServerChoice>();
		
		try (ObjectInputStream inputStream = 
				new ObjectInputStream(
					new FileInputStream("savedServers.bin"))) {
//				System.out.println((ServerChoiceList<ServerChoice>) inputStream.readObject());
			
//				tmp = (ArrayList<ServerChoice>) inputStream.readObject();
			
//				List<Object> a =  Arrays.asList(inputStream.readObject());
//				servers = (ServerChoiceList<ServerChoice>) a.get(0);
			
				servers = (ArrayList<ServerChoice>) inputStream.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		servers.add(new ServerChoice("localhost2", InetAddress.getByName("Kuba")));
		
		if(servers == null || servers.size() == 0 || servers.isEmpty()) {
			System.out.println("ListViewServers == null");
			servers = new ArrayList<ServerChoice>();
			servers.add(new ServerChoice("localhost", InetAddress.getLocalHost()));
		}			
	
		if (listViewServers == null) {
			System.out.println("ListViewServers == null");
			return false;
		}
		else {
			saveServersList(servers);
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
			ObservableList<ServerChoice> servers1 = new ServerChoiceList<ServerChoice>();
			servers1.addAll(servers);
			listViewServers.setItems(servers1);
			return true;
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
			
			listViewServers = (ListView) scene.lookup("#ListViewServers");
			
			loadServersList();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
