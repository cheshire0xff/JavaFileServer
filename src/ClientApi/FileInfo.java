package ClientApi;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FileInfo 
{
    public Path path;
    public String name;
    public long size;
    public FileInfo(Path path, long size)
    {
        this.path = path;
        this.name = path.getFileName().toString();
        this.size = size;
    }
    
    public FileInfo(String path, long size)
    {
        this(Paths.get(path), size);
    }
}
