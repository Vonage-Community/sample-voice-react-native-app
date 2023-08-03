package com.rnapptophone;

import android.annotation.SuppressLint;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.vonage.android_core.VGClientConfig;
import com.vonage.clientcore.core.api.ClientConfigRegion;
import com.vonage.voice.api.*;
import com.vonage.clientcore.core.api.LoggingLevel;
import static com.vonage.clientcore.core.api.ConfigKt.setDefaultLoggingLevel;

import java.util.HashMap;

public class ClientManager extends ReactContextBaseJavaModule {
    VoiceClient client;
    String callID;
    EventEmitter eventEmitter;

    ClientManager(ReactApplicationContext context) {
        super(context);
        setDefaultLoggingLevel(LoggingLevel.Verbose);
        client = VoiceClientKt.VoiceClient(context);
        client.setConfig(new VGClientConfig((ClientConfigRegion.US)));
        eventEmitter = new EventEmitter(context);
    }

    public String getName() {
        return "ClientManager";
    }

    private void sendEvent(String event, String name, String text) {
        WritableMap params = Arguments.createMap();
        params.putString(name, text);
        eventEmitter.sendEvent(event, params);
    }

    @ReactMethod
    public void login(String jwt) {
        client.createSession(jwt, null, (error, sessionId) -> {
            if (error != null) {
                this.sendEvent("onStatusChange", "status", "Error");
            }
            if (sessionId != null) {
                this.sendEvent("onStatusChange", "status", "Connected");
            }
            return null;
        });
    }

    @SuppressLint("MissingPermission")
    @ReactMethod
    public void makeCall(String number) {
        HashMap<String, String> callData = new HashMap();

        callData.put("to", number);
        client.serverCall(callData, (error, outboundCallID) -> {
            if (error != null) {
                this.sendEvent("onCallStateChange", "state", "Error");
            }
            if (outboundCallID != null) {
                this.sendEvent("onCallStateChange", "state", "On Call");
                callID = outboundCallID;
            }
            return null;
        });
    }

    @ReactMethod
    public void endCall() {
        if (callID != null) {
            client.hangup(callID, (error)-> {
                if (error != null) {
                    this.sendEvent("onCallStateChange", "state", "Idle");
                }
                return null;
            });
        }
    }
}
