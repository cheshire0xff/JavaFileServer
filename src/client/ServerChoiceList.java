package client;

import java.io.Serializable;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

public class ServerChoiceList<ServerChoice> extends SimpleListProperty implements Serializable {
	private static final long serialVersionUID = 1l;
	
	public ServerChoiceList() {
		super(FXCollections.observableArrayList());
	}
	
	
}