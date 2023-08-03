package com.rnapptophone;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class EventEmitter extends ReactContextBaseJavaModule {
    ReactApplicationContext context;

    EventEmitter(ReactApplicationContext context) {
        super(context);
        this.context = context;
    }

    public String getName() {
        return "EventEmitter";
    }

    public void sendEvent(String eventName, @Nullable WritableMap params) {
        this.context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }
}