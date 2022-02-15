package Utils;

import java.io.Serializable;

public class NodeInfo implements Serializable
{
    public String name;
    public String ip;
    public int port;

    public NodeInfo(String initName, String initIp, int initPort)
    {
        name = initName;
        ip = initIp;
        port = initPort;
    }

//    public NodeInfo(NodeInfo toCopy)
//    {
//        name = toCopy.name;
//        ip = toCopy.ip;
//        port = toCopy.port;
//    }

    public NodeInfo()
    {
        name = null;
        ip = null;
        port = -1;
    }
}
