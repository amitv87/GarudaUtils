package com.hps.garuda.grpc;

import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ServiceManager;


import static com.hps.garuda.grpc.SystemServiceWrapper.MESSENGER_KEY;
import static com.hps.garuda.grpc.SystemServiceWrapper.WHO_KEY;

/**
 * Created by amitverma on 11/11/17.
 */

public class SystemSerivceClient extends TimerTask {
  private static final String TAG = SystemSerivceClient.class.getCanonicalName();

  private final String who;
  private final Callback cb;
  private final String serviceName;

  public interface Callback{
    void onConnect();
    void onDisconnect();
    void onMessage(Bundle what);
  }

  Messenger serviceMessenger = null;

  final Messenger messenger = new Messenger(new Handler(){
    @Override
    public void handleMessage(Message msg) {
      cb.onMessage(msg.getData());
    }
  });

  public SystemSerivceClient(String serviceName, String who, Callback cb){
    this.cb = cb;
    this.who = who;
    this.serviceName = serviceName;
    connect();
    new Timer().scheduleAtFixedRate(this, 5000, 5000);
  }

  void connect() {
    IBinder serviceConnection = ServiceManager.checkService(serviceName);
    if(serviceConnection != null){
      Parcel preq = Parcel.obtain();
      Parcel pres = Parcel.obtain();

      Bundle req = new Bundle();
      req.putString(WHO_KEY, who);
      req.putParcelable(MESSENGER_KEY, messenger);
      preq.writeBundle(req);

      try {
        serviceConnection.transact(SystemServiceWrapper.ACTION.CONNECT.value(), preq, pres, 0);
      } catch (RemoteException e) {
        e.printStackTrace();
      }

      if(pres == null) return;

      Bundle res = pres.readBundle();


      if(res == null) return;

      String who = res.getString(WHO_KEY);

      Messenger mess = res.getParcelable(MESSENGER_KEY);

      if(mess != null && mess.getBinder().pingBinder()){
        serviceMessenger = mess;
        cb.onConnect();
      }
    }
  }

  @Override
  public void run() {
    if(serviceMessenger == null || !serviceMessenger.getBinder().pingBinder()){
      serviceMessenger = null;
      Log.d(TAG,"reconnecting to " + serviceName);
      cb.onDisconnect();
      connect();
    }
  }

  public boolean send(Bundle what){
    boolean rc = false;
    if(serviceMessenger == null) return rc;

    Message msg = Message.obtain();
    msg.setData(what);
    msg.replyTo = messenger;
    try {
      serviceMessenger.send(msg);
      rc = true;
    } catch (RemoteException e) {
      e.printStackTrace();
    }
    return rc;
  }
}
