package ClientApi;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DirectoryInfo
{
    public ArrayList<DirectoryInfo> dirs;
    public ArrayList<FileInfo> files;
    public String name;
    public Path path;
    public DirectoryInfo(Path path)
    {
        this.path = path;
        this.name = path.getFileName().toString();
        this.dirs = new ArrayList<>();
        this.files = new ArrayList<>();
    }
    
    public DirectoryInfo(String path)
    {
        this(Paths.get(path));
    }
}
