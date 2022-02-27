package Utils;

import java.io.Serializable;

public class RingNodeInfo implements Serializable
{
    public String name;
    public String ip;
    public int port;

    public RingNodeInfo(String initName, String initIp, int initPort)
    {
        name = initName;
        ip = initIp;
        port = initPort;
    }

    public RingNodeInfo(RingNodeInfo otherInfo)
    {
        name = otherInfo.name;
        ip = otherInfo.ip;
        port = otherInfo.port;
    }

    // Don't bother checking for name, we only really care about the connection information
    public boolean equals(RingNodeInfo other)
    {
        return syncReadIP().equals(other.ip) && syncReadPort() == other.port;
    }

    /*******************************************************************************************************************
     * Methods below this point used for synchronized access to successorInfo b/c that is a shared NodeInfo handle
     ******************************************************************************************************************/
    public synchronized void syncWrite(RingNodeInfo other)
    {
        name = other.name;
        ip = other.ip;
        port = other.port;
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
