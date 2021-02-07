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
import ClientApi.DirectoryInfo;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ClientApi.FileInfo;
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
	private Button newFolderButton;
	private Button uploadFileButton;
	private Button refreshButton;
	private Button openButton;
	private Button deleteButton;
	private Button homeButton;
	private ServerChoice currentServer;
	
	private ObservableList<ServerChoice> serversList;
	private ObservableList<Object> filesList;
	
	private Image folderImg;
    private Image fileImg;
    
    private ClientApi controller;
    private Stage primaryStage;
	
	private void errorDisplay(Exception e) {
		e.printStackTrace();
		
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
		
		File savedServers = new File("savedServers.bin");
		if (savedServers.exists()) {
			try (ObjectInputStream inputStream = 
					new ObjectInputStream(
						new FileInputStream("savedServers.bin"))) {			
					servers = (ArrayList<ServerChoice>) inputStream.readObject();
			} catch (Exception e) {
				errorDisplay(e);
			}
		}
		else
		
		if(servers == null 
			|| servers.size() == 0 
			|| servers.isEmpty() 
			|| !savedServers.exists()) {
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
	
	private Boolean connect(ServerChoice server) {
		filesList = new DirectoryContent<Object>();
		try {
			if (controller != null)
		    {
		        try {
                    controller.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
		    }
			
			controller = new ClientApi(server.getAddres());
			getFilesOrdered(controller.getFiles());
			serverStatusLabel.setText("Connected");
			currentServer = server;
			return true;
		} catch (ClassNotFoundException | IOException e) {
			errorDisplay(e);
			return false;
		}
	}	
	
	private void getFilesOrdered (DirectoryInfo pwd) {
        for (var f : pwd.dirs)
        {
            filesList.add(f);
        }
        
        for (var f : pwd.files)
        {
            filesList.add(f);
            
        }
	}
	
	private void getFiles (DirectoryInfo pwd) {
        for (var f : pwd.files)
        {
            filesList.add(f);
            
        }
        for (var f : pwd.dirs)
        {
            filesList.add(f);
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
			        	if(item instanceof FileInfo) {
			        		FileInfo tmp = (FileInfo) item;
			        		setText(tmp.name);
			        		imageView.setImage(fileImg);
			        		
			        	}
			        	else if (item instanceof DirectoryInfo) {
			        		DirectoryInfo tmp = (DirectoryInfo) item;
			        		setText(tmp.name);
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
		newFolderButton = (Button) scene.lookup("#newFolderButton");
		uploadFileButton = (Button) scene.lookup("#uploadFileButton");
		refreshButton = (Button) scene.lookup("#refreshButton");
		openButton = (Button) scene.lookup("#openButton");
		deleteButton = (Button) scene.lookup("#deleteButton");
		homeButton = (Button) scene.lookup("#homeButton");
		
	    Image refreshImg;
	    Image backImg;
		
		try {
			folderImg = new Image(
					new FileInputStream(
							new File(System.getProperty("user.dir")
									+ "/src/client/ui/folder.png")));
			fileImg = new Image(
					new FileInputStream(
							new File(System.getProperty("user.dir")
									+ "/src/client/ui/file.png")));
			refreshImg = new Image(
					new FileInputStream(
							new File(System.getProperty("user.dir")
									+ "/src/client/ui/refresh.png")));
			
			ImageView imageViewRefresh = new ImageView();
			imageViewRefresh.setFitWidth(16);
			imageViewRefresh.setFitHeight(16);
			imageViewRefresh.setImage(refreshImg);
			refreshButton.setGraphic(imageViewRefresh);
			
			backImg = new Image(
					new FileInputStream(
							new File(System.getProperty("user.dir")
									+ "/src/client/ui/home.png")));
			
			ImageView imageViewBack = new ImageView();
			imageViewBack.setFitWidth(16);
			imageViewBack.setFitHeight(16);
			imageViewBack.setImage(backImg);
			homeButton.setGraphic(imageViewBack);
			
		} catch (FileNotFoundException e) {
			errorDisplay(e);
		}
	}
	
	private void setEventHandlers() {
		newServerButton.setOnMouseClicked(event ->{
			if(newServerName.getText().length() != 0 
					&& newServerAddress.getText().length() != 0){
				try {
					serversList.add(new ServerChoice(
							newServerName.getText(),
							InetAddress.getByName(newServerAddress.getText())));
					saveServersList(new ArrayList<ServerChoice>(serversList));
				} catch (UnknownHostException e) {
					errorDisplay(e);
				}
			}
			if (newServerName.getText().length() == 0 )
				System.out.println("newServerName == null");
			if (newServerAddress.getText().length() == 0)
				System.out.println("newServerAddress == null");
		});
		
		deleteServerButton.setOnMouseClicked(event ->{
			ServerChoice server = listViewServers.getSelectionModel().getSelectedItem();
			
			if(server != null) {
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
			}
			else
				System.out.println("server == null");
		});	
		
		newFolderButton.setOnMouseClicked(event ->{
			if(controller != null) {
				TextInputDialog dialog = new TextInputDialog("New folder");
				dialog.setTitle("Create folder");
				dialog.setHeaderText("Please enter new folder name");
				dialog.setContentText("New folder name:");

				Optional<String> result = dialog.showAndWait();
				if (result.isPresent()){
					try {
						controller.uploadDirectory(result.get());
						filesList.clear();
						getFilesOrdered(controller.getFiles());
					} catch (ClassNotFoundException | IOException e) {
						errorDisplay(e);
					}
				}
			}
			else 
				System.out.println("controller == null");
		});
		
		uploadFileButton.setOnMouseClicked(event ->{
			if(controller != null) {				
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Open file to upload");
				
				File file = fileChooser.showOpenDialog(primaryStage);
				if (file != null){
					try {
						String resultString = file.getPath();
						String fileName = resultString.substring(resultString.lastIndexOf("\\") + 1, resultString.length());
						if(controller.uploadFile(resultString
								, fileName
								, new Observer("Uploading"))) {
							filesList.clear();
							getFilesOrdered(controller.getFiles());
						}
					} catch (ClassNotFoundException | IOException e) {
						errorDisplay(e);
					}
				}
			}
			else 
				System.out.println("controller == null");
		});
		
		openButton.setOnMouseClicked(event ->{
			if(controller != null){
				Object remoteObject = listViewFiles.getSelectionModel().getSelectedItem();
				if(remoteObject instanceof FileInfo) {
					FileInfo remoteFile = (FileInfo) remoteObject;
					File folder = new File(System.getProperty("user.dir")+ "/dowload/"); 
					folder.mkdirs(); //if dir exists no exepton is created
					String path = System.getProperty("user.dir")
							+ "/dowload/"
							+ remoteFile.name;
					String remotePath = remoteFile.path.toString();

					try {
						controller.downloadFile(path, remotePath, new Observer("Downlading"));
						String os = System.getProperty("os.name");
						if(os.contains("Windows")) {
							Runtime.getRuntime().exec("explorer.exe " + new File(path).getAbsolutePath());
						}
						else {
							System.out.print("No codepath for opening files for linux");
						}
					} catch (ClassNotFoundException | IOException e) {
						errorDisplay(e);
					}
				}
				else if(remoteObject instanceof DirectoryInfo) {
					DirectoryInfo remoteDirectory = (DirectoryInfo) remoteObject;

					filesList.clear();
					try {
						controller.refresh();
					} catch (ClassNotFoundException | IOException e) {
						errorDisplay(e);
					}
					getFilesOrdered(remoteDirectory);
					listViewFiles.setItems(filesList);
				}
			}
			else 
				System.out.println("controller == null");
		});
		
		deleteButton.setOnMouseClicked(event ->{
		    if (controller == null)
		    {
				System.out.println("controller == null");
		    }
            Object remoteObject = listViewFiles.getSelectionModel().getSelectedItem();
            if(remoteObject instanceof FileInfo) {
                FileInfo remoteFile = (FileInfo) remoteObject;
                String remotePath = remoteFile.path.toString();
                try {
                    controller.deleteFile(remotePath);
                } catch (ClassNotFoundException | IOException e) {
                    errorDisplay(e);
                }
            }
            else if(remoteObject instanceof DirectoryInfo) {
                DirectoryInfo remoteDirectory = (DirectoryInfo) remoteObject;
                try {
                    controller.deleteDir(remoteDirectory.path.toString());
                } catch (ClassNotFoundException | IOException e) {
                    errorDisplay(e);
                }
            }
            
            filesList.clear();
            try {
                controller.refresh();
            } catch (ClassNotFoundException | IOException e) {
                errorDisplay(e);
            }
            getFilesOrdered(controller.getFiles());
            displayFiles();
            listViewFiles.setItems(filesList);
		});
		
		connectButton.setOnMouseClicked(event ->{
			ServerChoice server = listViewServers.getSelectionModel().getSelectedItem();
			if(server != null && connect(server))
				displayFiles();
			else if(server == null)
				System.out.println("Selected server == null");
		});
		
		refreshButton.setOnMouseClicked(event ->{
			if(controller != null){
				filesList.clear();
				try {
					controller.refresh();
				} catch (ClassNotFoundException | IOException e) {
					errorDisplay(e);
				}
				getFilesOrdered(controller.getFiles());
			}
			else 
				System.out.println("controller == null");
		});
		
		homeButton.setOnMouseClicked(event ->{
		    if (currentServer != null)
		    {
		        filesList.clear();
		        connect(currentServer);
		        displayFiles();
		    }
		});
		
		listViewFiles.setOnMouseClicked(event -> {
			//TODO sprawdzi� jak zrobi� obs�ug� rich text albo html
			
			Object remoteObject = listViewFiles.getSelectionModel().getSelectedItem();
			String text;
			if(remoteObject instanceof FileInfo) {
				FileInfo remoteFile = (FileInfo) remoteObject;
				remoteObjectStatusLabel.setText(remoteFile.name);
				
				text = "File name: " + remoteFile.name + "\n"
						+ "File size: " + remoteFile.size + " B \n"
						+ "File path: " + remoteFile.path + " \n";
				textAreaFileDetails.setText(text);
			}
			else if(remoteObject instanceof DirectoryInfo) {
				DirectoryInfo remoteDirectory = (DirectoryInfo) remoteObject;
				remoteObjectStatusLabel.setText(remoteDirectory.name);
				
				text = "Directory name: " + remoteDirectory.name + "\n"
						+ "Directory path: " + remoteDirectory.path + " \n\n"
						+ "Directories insied:  \n";
						
				for (DirectoryInfo f : remoteDirectory.dirs) {
					text = text + f.name + "\n";
				}
				
				text = text + "\n"
						+ "Files insiede: \n";
				
				for (FileInfo f : remoteDirectory.files) {
					text = text + f.name + "\n";
				}
				textAreaFileDetails.setText(text);
			}
			else {
				if(remoteObject == null) {
					System.out.println("remoteObject == null");
				}
				else {
					text = "No path for class: " + remoteObject.getClass();
					textAreaFileDetails.setText(text);
				}

			}
		});
	}
	
	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader
					.load(getClass()
					.getResource("ui/MainWindow.fxml"));
			Scene scene = new Scene(root, 900, 600);
			root.getStylesheets().add(getClass()
					.getResource("ui/MainWindow.css")
					.toString());
			
			primaryStage.setTitle("FileServer App");		
			primaryStage.setScene(scene);
			primaryStage.show();
			this.primaryStage = primaryStage;
			
			initialize(scene);
			setEventHandlers();
			loadServersList();
			
		} catch(Exception e) {
			errorDisplay(e);
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}