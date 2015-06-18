package teamturbo.swifthockey.wificonn;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import teamturbo.swifthockey.GameActivityMP;
import teamturbo.swifthockey.P2PMessage;
import teamturbo.swifthockey.Serializer;

/**
 * Handles reading and writing of messages with socket buffers. Uses a Handler
 * to post messages to UI thread for UI updates.
 */
public class P2PManager implements Runnable {

    private Socket socket = null;
    private Handler handler;
    private InputStream iStream;
    private OutputStream oStream;
    private static final String TAG = "P2PHandler";

    public P2PManager(Socket socket, Handler handler) {
        this.socket = socket;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {

            iStream = socket.getInputStream();
            oStream = socket.getOutputStream();
            byte[] buffer = new byte[1024];
            int bytes;
            handler.obtainMessage(GameActivityMP.MY_HANDLE, this).sendToTarget();

            while (true) {
                try {
                    // Read from the InputStream
                    bytes = iStream.read(buffer);
                    if (bytes == -1) {
                        socket.close();
                        break;
                    }

                    P2PMessage message = (P2PMessage) Serializer.deserialize(buffer);

                    // Send the obtained bytes to the UI Activity
                    Log.d(TAG, "Rec:" + String.valueOf(buffer));
                    handler.obtainMessage(GameActivityMP.MESSAGE_READ, message).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "disconnected p2p Manager");
                    break;
                }
            }
        } catch (Exception e) {

            e.printStackTrace();
        } finally {
            try {
                Log.d(TAG, "Closing Socket p2p manager");
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void write(byte[] buffer) {
        try {
            oStream.write(buffer);
        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
        }
    }

    public void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}