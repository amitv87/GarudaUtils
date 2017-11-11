package com.hps.garuda.utilstest;

import java.util.Timer;
import java.util.Arrays;
import java.util.TimerTask;

import android.os.Bundle;
import android.os.Looper;

import com.hps.garuda.grpc.SystemService;
import com.hps.garuda.grpc.SystemSerivceClient;

/**
 * Created by amitverma on 12/11/17.
 */

public class TestGrpc {

  public static void main(String[] args) {
    System.out.println("args: " + (args != null ? Arrays.toString(args) : args));
    try {
      Looper.prepare();

      final SystemService service = new SystemService("helloService", new SystemService.Callback() {
        @Override
        public void onConnect(String who) {
          System.out.println("server=> onConnect " + who);
        }

        @Override
        public void onDisconnect(String who) {
          System.out.println("server=> onDisconnect " + who);
        }

        @Override
        public void onMessage(String who, Bundle what) {
          System.out.println("server=> onMessage " + who);
        }
      });

      final SystemSerivceClient client = new SystemSerivceClient("helloService", "TestGrpc3", new SystemSerivceClient.Callback() {
        @Override
        public void onConnect() {
          System.out.println("client=> onConnect");
        }

        @Override
        public void onDisconnect() {
          System.out.println("client=> onDisconnect");
        }

        @Override
        public void onMessage(Bundle what) {
          System.out.println("client=> onMessage");
        }
      });


      new Timer().scheduleAtFixedRate(new TimerTask() {
        @Override
        public void run() {
          try {
            Bundle b = new Bundle();
            b.putString("wow", "wow2");

            client.send(b);
            service.send("TestGrpc3",b);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }, 1000, 1000);

      Looper.loop();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
