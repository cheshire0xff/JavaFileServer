module fileServer {
	requires javafx.fxml;
	requires javafx.graphics;
	exports client;
	opens client to javafx.graphics;
}