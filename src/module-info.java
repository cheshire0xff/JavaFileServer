module fileServer {
	requires javafx.fxml;
	requires javafx.graphics;
	requires javafx.controls;
	requires javafx.base;
	requires jdk.compiler;
	exports client;
	opens client to javafx.graphics;
}