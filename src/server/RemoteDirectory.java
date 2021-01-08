package server;

import java.io.Serializable;
import java.util.ArrayList;

public class RemoteDirectory implements Serializable {
    private static final long serialVersionUID = 1L;

    public String path;
    public ArrayList<RemoteFileInfo> files = new ArrayList<RemoteFileInfo>();
    public ArrayList<RemoteDirectory> dirs = new ArrayList<RemoteDirectory>();
    public RemoteDirectory(String path)
    {
        this.path = path;
    }
    
    public synchronized void addFile(RemoteFileInfo file)
    {
        files.add(file);
    }
    public synchronized void addDir(RemoteDirectory dir)
    {
        dirs.add(dir);
    }

}
