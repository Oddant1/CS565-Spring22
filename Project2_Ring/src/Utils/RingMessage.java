package Utils;

import java.io.Serializable;

public class RingMessage implements Serializable, RingMessageTypes
{
    public RingNodeInfo origin;
    public RingNodeInfo other;
    public String note;
    public int type;

    public RingMessage(RingNodeInfo initOrigin, RingNodeInfo initOther, String initNote, int initType)
    {
        origin = initOrigin;
        other = initOther;
        note = initNote;
        type = initType;
    }
}
