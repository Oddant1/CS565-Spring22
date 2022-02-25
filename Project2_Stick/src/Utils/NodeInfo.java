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

    public NodeInfo(NodeInfo otherInfo)
    {
        name = otherInfo.name;
        ip = otherInfo.ip;
        port = otherInfo.port;
    }

    // Don't bother checking for name, we only really care about the connection information
    public boolean equals(NodeInfo other)
    {
        return ip.equals(other.ip) && port == other.port;
    }

    /*******************************************************************************************************************
     * Methods below this point used for synchronized access to successorInfo b/c that is a shared NodeInfo handle
     ******************************************************************************************************************/
    public synchronized void syncWrite(NodeInfo other)
    {
        name = other.name;
        ip = other.ip;
        port = other.port;
    }

    public synchronized void syncWrite(String newName, String newIP, int newPort)
    {
        name = newName;
        ip = newIP;
        port = newPort;
    }

    public synchronized String syncReadIP()
    {
        return ip;
    }

    public synchronized int syncReadPort()
    {
        return port;
    }
}
