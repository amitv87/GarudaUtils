package com.hps.garuda.grpc;

import android.os.Bundle;
import android.os.ServiceManager;

/**
 * Created by amitverma on 11/11/17.
 */

public class SystemService {
  final String serviceName;
  final SystemServiceWrapper sw;

  public interface Callback{
    void onConnect(String who);
    void onDisconnect(String who);
    void onMessage(String who, Bundle what);
  }

  public SystemService(String serviceName, Callback cb){
    this.serviceName = serviceName;
    this.sw = new SystemServiceWrapper(serviceName ,cb);
    ServiceManager.addService(serviceName, sw);
  }

  public boolean send(String who, Bundle what){
    return sw.send(who, what);
  }
}
