package de.dmxcontrol.network.UDP;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

import de.dmxcontrol.device.Entity;
import de.dmxcontrol.device.EntityDevice;
import de.dmxcontrol.device.EntityGroup;
import de.dmxcontrol.executor.EntityExecutor;
import de.dmxcontrol.network.ResceivdData;

/**
 * Created by Qasi on 12.06.2014.
 */

public class Reader extends Thread {
    private boolean bKeepRunning = true;
    private String lastMessage = "";
    private KernelPingDeserielizer lastKernelPing;
    private Entity lastEntity;

    public enum Type {
        DEVICE,
        DEVICECOUNT,
        GROUP,
        GROUPCOUNT,
        PRESET,
        PRESETCOUNT,
        EXECUTOR,
        EXECUTORCOUNT;
        public static Type convert(byte value) {
            return Type.values()[value];
        }
        }

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
            DatagramSocket kernelsocket = new DatagramSocket(12352         );
                DatagramSocket androidApp = new DatagramSocket(13141);
                while (bKeepRunning) {
                    if (false) {
                        receiveKernalPing(message, lmessage, kernelsocket, packet);
                    }
                    try {
                        receiveAndroidAppPluginEntity(message, lmessage, androidApp, packet);
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
    private void receiveAndroidAppPluginEntity(String message,byte[] lmessage, DatagramSocket socket, DatagramPacket packet) {
        try {
            socket.receive(packet);
            message = new String(lmessage, 0, packet.getLength());
            lastMessage = message;
            if (message.length() > 0) {
                Type t=Type.convert(lmessage[0]);
                Entity entity = null;
                switch(t){
                    case DEVICE:
                        entity= EntityDevice.Receive(lmessage);
                        ResceivdData.get().Devices.add((EntityDevice)entity);
                        break;
                    case DEVICECOUNT:

                        break;
                    case GROUP:
                        entity= EntityGroup.Receive(lmessage);
                        break;
                    case GROUPCOUNT:
                        break;
                    case PRESET:
                        //entity=  EntityPreset(lmessage);
                        break;
                    case EXECUTOR:
                        entity= EntityExecutor.Receive(lmessage);
                        ResceivdData.get().Executors.add((EntityExecutor)entity);
                        break;
                    default:
                        break;
                }

                lastEntity=entity;

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
            run();
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