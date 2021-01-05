package server;

import java.io.Serializable;
import java.util.ArrayList;

public class RemoteDirectory implements Serializable {
    private static final long serialVersionUID = 1L;

    ArrayList<RemoteFileInfo> files;
    ArrayList<RemoteDirectory> dirs;

}
