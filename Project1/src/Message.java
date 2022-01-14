import java.io.Serializable;

public class Message implements Serializable {
    Object content;
    MessageTypes type;

    public Message(Object initContent, MessageTypes initType) {
        content = initContent;
        type = initType;
    }
}
