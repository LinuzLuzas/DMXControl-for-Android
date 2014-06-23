package de.dmxcontrol.network.UDP;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

import de.dmxcontrol.device.Entity;

/**
 * Created by Qasi on 12.06.2014.
 */

public class ReaderKernelPing extends Thread {
    private boolean bKeepRunning = true;
    private String lastMessage = "";
    private KernelPingDeserielizer lastKernelPing;
    private Entity lastEntity;

    private DatagramSocket kernelsocket;

    public KernelPingDeserielizer GetLastKernelPing() {
        return lastKernelPing;
    }

    private ArrayList<KernelPingDeserielizer> KernelPings;

    public ArrayList<KernelPingDeserielizer> GetKernelPings() {
        return KernelPings;
    }

    public interface NewsUpdateListener {
        void onNewsUpdate();
    }

    private ArrayList<NewsUpdateListener> listeners = new ArrayList<NewsUpdateListener>();

    public void setOnNewsUpdateListener(NewsUpdateListener listener) {
        // Store the listener object
        this.listeners.add(listener);
    }

    private ArrayList<byte[]> sendData=new ArrayList<byte[]>();

    public void run() {
        this.setPriority(MIN_PRIORITY);
        String message = "";
        byte[] lmessage = new byte[0x000fff];
        DatagramPacket packet = new DatagramPacket(lmessage, lmessage.length);
        KernelPings = new ArrayList<KernelPingDeserielizer>();
        try {
            if(kernelsocket==null) {
                kernelsocket = new DatagramSocket(12352);
            }
                while (bKeepRunning) {
                    try {
                        receiveKernalPing(message, lmessage, kernelsocket, packet);
                    }
                    catch (Exception e) {
                        e.toString();
                    }
                    finally {
                    Thread.sleep(2);
                }
            }
            if (kernelsocket != null) {
                kernelsocket.close();
            }
        } catch (Throwable e) {
            Log.e("UDP Listener", e.getMessage());
            run();
        }
    }

    private void receiveKernalPing(String message,byte[] lmessage, DatagramSocket socket, DatagramPacket packet) {
        try {
            socket.receive(packet);
            message = new String(lmessage, 0, packet.getLength());
            lastMessage = message;
            if (message.length() > 0) {
                lastKernelPing = KernelPingDeserielizer.Get(message);

                Log.d("UDP", "KernelPing Received: IP "+lastKernelPing.GetIPAdresses()[0]);
                boolean add = true;
                if (KernelPings.size() > 0) {
                    for (int i = 0; i < KernelPings.size(); i++) {
                        if (KernelPings.get(i).GetHostName().equals(lastKernelPing.GetHostName())) {
                            add = false;
                            KernelPings.remove(i);
                            KernelPings.add(i, lastKernelPing);
                        }
                    }
                }
                if (add) {
                    KernelPings.add(lastKernelPing);
                }
                for (NewsUpdateListener listener : listeners) {
                    listener.onNewsUpdate();
                }
            }
            message=null;
            if(message!=null) {
                message = null;
            }
            lmessage=null;
            if(lmessage!=null) {
                lmessage = null;
            }
        } catch (Throwable e) {
            Log.e("Can't receive KernelPing", e.getMessage());
        }
    }

    public void kill() {
        bKeepRunning = false;
    }

    public String getLastMessage() {
        return lastMessage;
    }
//}

//private Runnable updateTextMessage = new Runnable() {
//    public void run() {
//        if (myDatagramReceiver == null) return;
//        textMessage.setText(myDatagramReceiver.getLastMessage());
//    }
};