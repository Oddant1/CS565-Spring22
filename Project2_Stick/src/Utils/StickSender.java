package Utils;

import Client.StickClientListener;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StickSender implements StickDirections
{
    public void send(StickMessage toSend, StickNodeInfo myInfo, StickNodeInfo predecessorInfo,
                     StickNodeInfo successorInfo)
    {
        Socket socket = null;
        ObjectOutputStream toNeighbor;

        try
        {
            // Don't send to ourselves
            if (toSend.direction == PREDECESSOR && !predecessorInfo.equals(myInfo))
            {
                socket = new Socket(predecessorInfo.syncReadIP(), predecessorInfo.syncReadPort());
            }
            else if (toSend.direction == SUCCESSOR && !successorInfo.equals(myInfo))
            {
                socket = new Socket(successorInfo.syncReadIP(), successorInfo.syncReadPort());
            }

            // If we tried to send to ourselves, we won't have a socket
            if (socket != null)
            {
                toNeighbor = new ObjectOutputStream(socket.getOutputStream());
                toNeighbor.writeObject(toSend);
            }
        }
        catch (IOException e)
        {
            Logger.getLogger(StickClientListener.class.getName()).log(Level.SEVERE, null, e);
            System.err.println("Error sending message: IOException " + e);
            System.exit(1);
        }
    }
}
