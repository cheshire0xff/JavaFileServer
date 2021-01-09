package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RemoteFileInfo implements Serializable
{
    private static final long serialVersionUID = 1L;
    public String path;
    public int sizeBytes;
    public byte[] md5digest;
    
    /*
     * should be constructed only by side containing actual file on disk
     */
    
    public RemoteFileInfo(String path, int size) throws IOException {
        this.path = path;
        this.sizeBytes = size;
        md5digest = calculateMD5(path);
    }
    

    
    public byte[] calculateMD5(String filePathOnDisk) throws IOException
    {
        byte[] buf = new byte[TdpServer.downloadChunkSize];
        var fileInput = new FileInputStream(new File(filePathOnDisk));
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
        return md.digest();
    }

}
