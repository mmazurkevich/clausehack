package com.example.myapplication.stomped.client;

import android.util.Log;

import com.example.myapplication.stomped.component.StompedCommand;
import com.example.myapplication.stomped.component.StompedFrame;
import com.example.myapplication.stomped.component.StompedHeaders;
import com.example.myapplication.stomped.listener.StompedListener;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.WebSocket;

public class StompedClient {

    private final static String TAG = StompedClient.class.toString();

    private ExecutorService threadPool;
    private WebSocket connector;
    private int uniqueDestinationID = 0;


    public StompedClient(WebSocket connector){
        this.connector = connector;
        this.threadPool = Executors.newSingleThreadExecutor();
        Log.d(TAG, "StompedClient has been built.");
    }

    public void disconnect(){
        this.connector.close(1000, null);
        Log.d(TAG, "Client disconnecting");
    }

    public void send(String destination){

        //Using SEND command.
        final StompedFrame frame = StompedFrame.construct(StompedCommand.STOMP_COMMAND_SEND);
        frame.addHeader(StompedHeaders.STOMP_HEADER_DESTINATION, destination);

        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                connector.send(frame.build());
            }
        });
        Log.d(TAG, "Client sending to " + destination);
    }

    public void send(String destination, String body){

        final StompedFrame frame = StompedFrame.construct(StompedCommand.STOMP_COMMAND_SEND, null, body);
        frame.addHeader(StompedHeaders.STOMP_HEADER_DESTINATION, destination);
        frame.addHeader(StompedHeaders.STOMP_HEADER_CONTENT_TYPE, "application/json");

        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                connector.send(frame.build());
            }
        });
        Log.d(TAG, "Client sending to " + destination);
    }

    public void subscribeAndSend(String destination, String subscription, @NotNull StompedListener listener){
        subscribe(subscription, listener);
        send(destination);
    }

    public void subscribe(String destination, @NotNull StompedListener listener){

        String destinationID = String.valueOf(incrementDestinationID());

        listener.setDestinationID(destinationID);
        listener.setDestination(destination);
        StompedListenerRouter.getInstance().addListener(listener);

        final StompedFrame frame = StompedFrame.construct(StompedCommand.STOMP_COMMAND_SUBSCRIBE);
        frame.addHeader(StompedHeaders.STOMP_HEADER_SUB_ID, destinationID);
        frame.addHeader(StompedHeaders.STOMP_HEADER_DESTINATION, destination);
//        frame.addHeader(StompedHeaders.STOMP_HEADER_ACK, "auto");

        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                connector.send(frame.build());
            }
        });
        Log.d(TAG, "Client subscribe to " + destination + " with destinationID " + destinationID);
    }

    public void unsubscribe(String destination){

        StompedListener currentListener = StompedListenerRouter.getInstance().removeListener(destination);

        final StompedFrame frame = StompedFrame.construct(StompedCommand.STOMP_COMMAND_UNSUBSCRIBE);
        frame.addHeader(StompedHeaders.STOMP_HEADER_SUB_ID, currentListener.getDestinationID());

        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                connector.send(frame.build());
            }
        });
        Log.d(TAG, "Client unsubscribed from " + currentListener.getDestination() + " with destinationID " + currentListener.getDestinationID());
    }

    private int incrementDestinationID(){
        return uniqueDestinationID++;
    }


}
