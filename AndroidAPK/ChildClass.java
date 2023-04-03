package com.example.myapplication3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ChildClass {

    private String resStr;

    public String aNetWorkCallFun(String nothing){

        Integer state = 0;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://www.baidu.com");

                    // 创建 HttpsURLConnection 对象
                    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

                    conn.setRequestMethod("GET");

                    Integer res = conn.getResponseCode();

                    System.out.println("Response Code : " + conn.getResponseCode());

                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    resStr = response.toString().substring(0, 4);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();

        return nothing + " ";
    }
}
