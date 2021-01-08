package server;

import java.io.Serializable;
import java.net.Socket;

interface IDownloadProgressObserver
{
    void updateProgress(int bytesDownloaded, int total);
}

public class RemoteFileInfo implements Serializable
{
    private static final long serialVersionUID = 1L;
    public String path;
    public Integer sizeBytes;
    
    public RemoteFileInfo(String path, Integer size) {
        this.path = path;
        this.sizeBytes = size;
    }
    
    public void get(Socket sock, IDownloadProgressObserver observer)
    {
        
    }

}
