package com.ninjacoders.show_me;

import android.util.Log;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by jheru on 13-Dec-17.
 */

public class Singleton {

    private static Singleton instance = null;
    private Socket socket;
    private static final String TAG = "Singleton";

    // Private constructor so no instances can be made outside this class.
    private Singleton() {
        connectSocket();
    }

    public static synchronized Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }

        return instance;
    }

    public Socket getSocket() {
        return socket;
    }

    /**
     * Create a SocketIO object and make connection to the phone facade.
     */
    private void connectSocket() {
        try {
            socket = IO.socket("http://40.68.124.79:1903/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        socket.on(socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i(TAG,  "Connected to phone facade");
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i(TAG, "Disconnected from phone facade");
            }
        }).on(Socket.EVENT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i(TAG, "Failed to emit data to phone facade");
            }
        });
        socket.connect();
    }
}
