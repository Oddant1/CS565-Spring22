package Utils;

import java.io.Serializable;

public class Message implements Serializable {
    public Object content;
    public MessageTypes type;

    public Message(Object initContent, MessageTypes initType) {
        content = initContent;
        type = initType;
    }
}
