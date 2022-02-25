package Utils;

import java.io.Serializable;

public class Message implements Serializable, MessageTypes
{
    public NodeInfo origin;
    public NodeInfo other;
    public String note;
    public int type;
    public int direction;

    public Message(NodeInfo initOrigin, NodeInfo initOther, String initNote, int initType, int initDirection)
    {
        origin = initOrigin;
        other = initOther;
        note = initNote;
        type = initType;
        direction = initDirection;
    }
}
