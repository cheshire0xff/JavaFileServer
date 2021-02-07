package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RemoteFileInfo implements Serializable
{
    private static final long serialVersionUID = 1L;
    public String filename;
    public long sizeBytes;
    public byte[] md5digest;
    
    /*
     * should be constructed only by side containing actual file on disk
     */
    
    public RemoteFileInfo(Path pathOnDisk) throws IOException {
        this.filename = pathOnDisk.getFileName().toString();
        md5digest = calculateMD5(pathOnDisk);
        sizeBytes = Files.size(pathOnDisk);
    }
    
    
    public byte[] calculateMD5(Path filePathOnDisk) throws IOException
    {
        byte[] buf = new byte[TdpServer.downloadChunkSize];
        var fileInput = new FileInputStream(filePathOnDisk.toFile());
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
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
        fileInput.close();
        return md.digest();
    }

}
