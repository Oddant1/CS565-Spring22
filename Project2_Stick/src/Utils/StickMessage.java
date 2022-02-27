package Utils;

import java.io.Serializable;

public class StickMessage implements Serializable, StickMessageTypes
{
    public StickNodeInfo origin;
    public StickNodeInfo other;
    public String note;
    public int type;
    public int direction;

    public StickMessage(StickNodeInfo initOrigin, StickNodeInfo initOther, String initNote, int initType, int initDirection)
    {
        origin = initOrigin;
        other = initOther;
        note = initNote;
        type = initType;
        direction = initDirection;
    }
}
