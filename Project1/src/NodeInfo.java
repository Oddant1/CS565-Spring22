import java.io.Serializable;

public class NodeInfo implements Serializable {
    String ip;
    int port;
    String name;

    public NodeInfo(String initIp, int initPort, String initName) {
        ip = initIp;
        port = initPort;
        name = initName;
    }
}
