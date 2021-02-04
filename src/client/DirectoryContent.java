package client;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

public class DirectoryContent<Object> extends SimpleListProperty {
	public DirectoryContent() {
		super(FXCollections.observableArrayList());
	}
}
