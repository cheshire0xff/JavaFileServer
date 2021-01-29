package server;

import java.io.Serializable;
import java.nio.file.Paths;
import java.util.ArrayList;

public class RemoteDirectory implements Serializable {
    private static final long serialVersionUID = 1L;

    public String directoryName;
    public RemoteDirectory parentDir;
    public ArrayList<RemoteFileInfo> files = new ArrayList<RemoteFileInfo>();
    public ArrayList<RemoteDirectory> dirs = new ArrayList<RemoteDirectory>();
    public RemoteDirectory(String name, RemoteDirectory parent)
    {
        this.directoryName = name;
        this.parentDir = parent;
    }
    public RemoteDirectory(String name)
    {
        this(name, null);
    }
    RemoteFileInfo tryGetFile(String filename)
    {
        for (var f : files)
        {
            if (f.filename.equals(filename))
            {
                return f;
            }
        }
        return null;
    }
    RemoteDirectory tryGetDir(String dirname)
    {
        for (var f : dirs)
        {
            if (f.directoryName.equals(dirname))
            {
                return f;
            }
        }
        return null;
    }
    public RemoteFileInfo tryFindFile(String path)
    {
        var p = Paths.get(path);
        if (p.equals(""))
        {
            return null;
        }
        if (p.getNameCount() == 1)
        {
            return tryGetFile(path);
        }
        var curdir = this;
        for (var i = 0; i < p.getNameCount() - 1; ++i)
        {
            curdir = curdir.tryGetDir(p.getName(i).toString());
            if (curdir == null)
            {
                return null;
            }
        }
        return curdir.tryGetFile(p.getFileName().toString());
    }

    public RemoteDirectory tryFindDir(String path)
    {
        var p = Paths.get(path);
        if (p.equals(""))
        {
            return null;
        }
        if (p.getNameCount() == 1)
        {
            return tryGetDir(path);
        }
        var curdir = this;
        for (var i = 0; i < p.getNameCount() - 1; ++i)
        {
            curdir = curdir.tryGetDir(p.getName(i).toString());
            if (curdir == null)
            {
                return null;
            }
        }
        return curdir.tryGetDir(p.getFileName().toString());
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
