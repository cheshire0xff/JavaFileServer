package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class RemoteFileInfo implements Serializable
{
    private static final long serialVersionUID = 1L;
    public String path;
    public int sizeBytes;
    byte[] md5digest;
    
    /*
     * should be constructed only by side containing actual file on disk
     */
    
    public RemoteFileInfo(String path, int size) throws IOException {
        this.path = path;
        this.sizeBytes = size;
        md5digest = calculateMD5(path);
    }
    
    public boolean get(String outputPath, Socket sock, IDownloadProgressObserver observer) throws IOException
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
            int chunkSize = TdpServer.downloadChunkSize;
            if (sizeLeft < chunkSize)
            {
                chunkSize = sizeLeft;
            }
            sizeLeft -= chunkSize;
            var buf = sock.getInputStream().readNBytes((int) chunkSize);
            fileInput.write(buf);
            observer.updateProgress(sizeBytes - sizeLeft, sizeBytes);
        }
        fileInput.flush();
        fileInput.close();
        return Arrays.equals(calculateMD5(file.getPath()), md5digest);
    }
    
    public byte[] calculateMD5(String filePathOnDisk) throws IOException
    {
        byte[] buf = new byte[TdpServer.downloadChunkSize];
        var fileInput = new FileInputStream(new File(filePathOnDisk));
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        while (md != null)
        {
            int readBytes;
            readBytes = fileInput.read(buf);
            if (readBytes == -1)
            {
                break;
            }
            md.update(buf, 0, readBytes);
        }
        return md.digest();
    }

}
