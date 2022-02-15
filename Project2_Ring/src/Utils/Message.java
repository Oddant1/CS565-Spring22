package Utils;

import java.io.Serializable;

public class Message implements Serializable, MessageTypes
{
    public NodeInfo origin;
    public NodeInfo successor;
    public String note;
    public int type;

    public Message(NodeInfo initOrigin, NodeInfo initSuccessor, String initNote, int initType)
    {
        origin = initOrigin;
        successor = initSuccessor;
        note = initNote;
        type = initType;
    }
}
