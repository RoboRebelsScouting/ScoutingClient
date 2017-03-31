package com.example.administrator.scoutingclient;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Created by Administrator on 3/29/2017
 */

public class BluetoothClient extends Thread {
    private BluetoothSocket mmSocket;
    private BluetoothAdapter mmAdapter;
    private UUID MY_UUID = UUID.fromString("35c2ad3a-14dc-11e7-93ae-92361f002671");
    private String TAG = "BluetoothClient";
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private Handler mHandler;

    BluetoothDevice mmDevice;

    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
    }
    private byte[] mmBuffer;

    public BluetoothClient(BluetoothAdapter adapter, BluetoothDevice device) {
        this.mmDevice = device;
        this.mmAdapter = adapter;

        BluetoothSocket tmp = null;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;

        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            tmpIn = mmSocket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = mmSocket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;

    }

    // convert little endian to big endian
    int littleToBig(int i)
    {
        int b0,b1,b2,b3;

        b0 = (i&0x000000ff)>>0;
        b1 = (i&0x0000ff00)>>8;
        b2 = (i&0x00ff0000)>>16;
        b3 = (i&0xff000000)>>24;

        return ((b0<<24)|(b1<<16)|(b2<<8)|(b3<<0));
    }

    // convert an integer to a byte array
    public byte[] intToBytes( final int i ) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(i);
        return bb.array();
    }

    public void btSend(String message) {
        short msgType = 1;
        byte[] header = new byte[2];
        //header[0] = (byte) (msgType & 0xff);
        //header[1] = (byte) ((msgType >> 8) & 0xff);
        header[0] = 0;
        header[1] = 1;

        String fname = String.format("%50s","UNH-1-1153-Jon");
        byte[] fileName = new byte[50];
        fileName = fname.getBytes();



        byte[] messagebytes = message.getBytes();

        Integer fLength = messagebytes.length;
        byte[] fileLength = new byte[4];
        fileLength = intToBytes(fLength);

        byte[] checksumbytes = new byte[20];
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            md.update(messagebytes);
            checksumbytes = md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        /*byte[] bytes = new byte[header.length+checksumbytes.length+messagebytes.length];

        for(int i = 0; i < messagebytes.length; i++) {
            bytes[i] = messagebytes[i];
        }
        for (int i = 0; i < checksumbytes.length; i++) {
            bytes[i+messagebytes.length] = checksumbytes[i];
        }
        for(int i = 0; i < header.length; i++) {
            bytes[i+checksumbytes.length+messagebytes.length] = header[i];
        }*/
        try {
            mmOutStream.write(header);
            mmOutStream.write(checksumbytes);
            mmOutStream.write(fileName);
            mmOutStream.write(fileLength);
            mmOutStream.write(messagebytes);

            // Share the sent message with the UI activity.
            //Message writtenMsg = mHandler.obtainMessage(
            //        MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
            //writtenMsg.sendToTarget();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);

            // Send a failure message back to the activity.
            //Message writeErrorMsg =
            //        mHandler.obtainMessage(MessageConstants.MESSAGE_TOAST);
           // Bundle bundle = new Bundle();
           // bundle.putString("toast",
           //         "Couldn't send data to the other device");
            //writeErrorMsg.setData(bundle);
            //mHandler.sendMessage(writeErrorMsg);
        }
    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        mmAdapter.cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
            btSend(MainActivity.messageString);
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        // manageMyConnectedSocket(mmSocket);



    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }
}
