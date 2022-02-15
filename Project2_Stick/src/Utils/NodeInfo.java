package Utils;

import java.io.Serializable;

public class NodeInfo implements Serializable {
    public String ip;
    public int port;
    public String name;

    public NodeInfo(String initIp, int initPort, String initName) {
        ip = initIp;
        port = initPort;
        name = initName;
    }
}
