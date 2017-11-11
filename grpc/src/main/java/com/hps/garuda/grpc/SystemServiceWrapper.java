package com.hps.garuda.grpc;

import java.util.Map;
import java.util.HashMap;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

/**
 * Created by amitverma on 11/11/17.
 */

public class SystemServiceWrapper extends ISystemServiceWrapper.Stub {

  public static final String WHO_KEY = "who";
  public static final String MESSENGER_KEY = "mess";

  public enum ACTION {
    CONNECT(101),
    DISCONNECT(102);

    public final int value;
    private static Map<Integer, ACTION> map = new HashMap<Integer, ACTION>();

    static {
      for (ACTION action : ACTION.values()) {
        map.put(action.value, action);
      }
    }

    private ACTION(int _value) { value = _value; }

    public int value(){
      return value;
    }

    public static ACTION valueOf(int value) {
      return map.get(value);
    }
  };

  private HashMap<String, Messenger> whoMap = new HashMap<>();
  private HashMap<Messenger, String> messengerMap = new HashMap<>();

  final Messenger messenger = new Messenger(new Handler(){
    @Override
    public void handleMessage(Message msg) {
      if(msg.replyTo == null) return;

      String who = messengerMap.get(msg.replyTo);
      if(who == null) return;

      cb.onMessage(who, msg.getData());
    }
  });

  String serviceName;
  SystemService.Callback cb;
  SystemServiceWrapper(String serviceName, SystemService.Callback cb){
    this.cb = cb;
    this.serviceName = serviceName;
  }

  @Override
  public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
    if(data == null) return false;

    Bundle req = data.readBundle();
    if(req == null) return false;

    String who = req.getString(WHO_KEY);
    Messenger mess = req.getParcelable(MESSENGER_KEY);

    if(who == null && mess == null) return false;

    ACTION action = ACTION.valueOf(code);

    if(action == null) return false;

    if(reply != null){
      Bundle res = new Bundle();
      res.putString(WHO_KEY, serviceName);
      res.putParcelable(MESSENGER_KEY, messenger);
      reply.writeBundle(res);
    }

    switch (action){
      case CONNECT:
        whoMap.put(who, mess);
        messengerMap.put(mess, who);
        cb.onConnect(who);
        break;
      case DISCONNECT:
        whoMap.remove(who, mess);
        messengerMap.remove(mess, who);
        cb.onDisconnect(who);
        break;
      default:
        break;
    }

    return true;
  }

  public boolean send(String who, Bundle what){
    boolean rc = false;
    Messenger mess = whoMap.get(who);
    if(mess == null) return rc;

    Message msg = Message.obtain();
    msg.setData(what);
    try {
      mess.send(msg);
      rc = true;
    } catch (RemoteException e) {
      e.printStackTrace();
    }

    return rc;
  }
}
