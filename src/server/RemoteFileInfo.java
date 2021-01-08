package server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.nio.file.Paths;

public class RemoteFileInfo implements Serializable
{
    private static final long serialVersionUID = 1L;
    public String path;
    public int sizeBytes;
    
    public RemoteFileInfo(String path, int size) {
        this.path = path;
        this.sizeBytes = size;
    }
    
    public void get(String outputPath, Socket sock, IDownloadProgressObserver observer) throws IOException
    {
        var writer = new PrintWriter(sock.getOutputStream());
        writer.println("getfile " + path);
        writer.flush();
        var sizeLeft = sizeBytes;
        var file = new File(outputPath + Paths.get(path).getFileName());
        file.createNewFile();
        var fileInput = new FileOutputStream(file);
        while (sizeLeft > 0)
        {
            int chunkSize = 10000;
            if (sizeLeft < 10000)
            {
                chunkSize = sizeLeft;
            }
            sizeLeft -= chunkSize;
            var buf = sock.getInputStream().readNBytes((int) chunkSize);
            fileInput.write(buf);
            observer.updateProgress(sizeBytes - sizeLeft, sizeBytes);
        }
        fileInput.close();
    }

}
