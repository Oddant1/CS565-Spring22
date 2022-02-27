package Utils;

import java.io.Serializable;

public class StickNodeInfo implements Serializable
{
    public String name;
    public String ip;
    public int port;

    public StickNodeInfo(String initName, String initIp, int initPort)
    {
        name = initName;
        ip = initIp;
        port = initPort;
    }

    public StickNodeInfo(StickNodeInfo otherInfo)
    {
        name = otherInfo.name;
        ip = otherInfo.ip;
        port = otherInfo.port;
    }

    // Don't bother checking for name, we only really care about the connection information
    public boolean equals(StickNodeInfo other)
    {
        // We always call .equals on the predecessor or successor info, so we only need to syncread from ourselves
        return syncReadIP().equals(other.ip) && syncReadPort() == other.port;
    }

    /*******************************************************************************************************************
     * Methods below this point used for synchronized access to successorInfo b/c that is a shared NodeInfo handle
     ******************************************************************************************************************/
    public synchronized void syncWrite(StickNodeInfo other)
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
