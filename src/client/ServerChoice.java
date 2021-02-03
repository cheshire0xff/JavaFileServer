package client;

import java.io.Serializable;
import java.net.InetAddress;

class ServerChoice implements Serializable{
	private static final long serialVersionUID = 1L;
	public String  serverName;
	public InetAddress addres;
	
	public ServerChoice() {
		super();
		serverName = "";
		addres = null;
	}
	
	public ServerChoice(String serverName, InetAddress addres) {
		super();
		this.serverName = serverName;
		this.addres = addres;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public InetAddress getAddres() {
		return addres;
	}

	public void setAddres(InetAddress addres) {
		this.addres = addres;
	}
	
	@Override
	public String toString() {
		return serverName;
		
	}
}