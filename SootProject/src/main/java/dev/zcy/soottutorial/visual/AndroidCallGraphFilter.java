package dev.zcy.soottutorial.visual;

import dev.zcy.soottutorial.android.AndroidUtil;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.*;

public class AndroidCallGraphFilter implements CallGraphFilter {
    public List<SootClass> getValidClasses() {
        return validClasses;
    }

    private List<SootClass> validClasses = new ArrayList<>();
    public AndroidCallGraphFilter(String appPackageName) {
        for (SootClass sootClass : Scene.v().getApplicationClasses()) {

            //System.out.println(sootClass.getName() + " ??????????? " + appPackageName);
            

            if (!sootClass.getName().contains(appPackageName)){
                //System.out.println("continue1");
                continue;
            }
                 
            if (sootClass.getName().contains(appPackageName + ".R") || sootClass.getName().contains(appPackageName + ".BuildConfig")){
                //System.out.println("continue2");
                continue;
            }
            //System.out.println("OKKKKKKKKKKKKKKK");
            validClasses.add(sootClass);
        }
    }



    public boolean isValidMethod(SootMethod sootMethod){
        if(AndroidUtil.isAndroidMethod(sootMethod))
            return false;
        if(sootMethod.getDeclaringClass().getPackageName().startsWith("java"))
            return false;
        if(sootMethod.toString().contains("<init>") || sootMethod.toString().contains("<clinit>"))
            return false;
        if(sootMethod.getName().equals("dummyMainMethod"))
            return false;
        return true;
    }

    @Override
    public boolean isValidEdge(soot.jimple.toolkits.callgraph.Edge sEdge) {
        if(!sEdge.src().getDeclaringClass().isApplicationClass())// || sEdge.tgt().getDeclaringClass().isApplicationClass())
            return false;
        if(!isValidMethod(sEdge.src()) || !isValidMethod(sEdge.tgt()))
            return false;
        boolean flag = validClasses.contains(sEdge.src().getDeclaringClass());
        flag |= validClasses.contains(sEdge.tgt().getDeclaringClass());
        return flag;
    }



    //My Work

    //Use Map, the key is the class, the value is the method
    Map<String, String> networkCallRecords = new TreeMap<String, String>(){
        {
            // java.net
            put("java.net.URL", "openConnection");
            put("java.net.URL", "<init>");
            put("java.net.URLConnection", "connect");
            put("java.net.HttpURLConnection", "setRequestMethod");
            put("java.net.HttpURLConnection", "getResponseCode");
            put("java.net.HttpURLConnection", "getInputStream");
            put("java.net.Proxy", "proxySelector");
            put("java.net.InetSocketAddress", "createUnresolved");
            // javax.net.ssl
            put("javax.net.ssl.HttpsURLConnection", "setSSLSocketFactory");
            put("javax.net.ssl.HttpsURLConnection", "setHostnameVerifier");
            put("javax.net.ssl.SSLContext", "getInstance");
            put("javax.net.ssl.SSLContext", "init");
            put("javax.net.SocketFactory", "getDefault");
            put("javax.net.ssl.X509TrustManager", "checkClientTrusted");
            put("javax.net.ssl.X509TrustManager", "checkServerTrusted");
            put("javax.net.ssl.X509TrustManager", "getAcceptedIssuers");
            put("javax.net.ssl.TrustManagerFactory", "getInstance");
            put("javax.net.ssl.TrustManagerFactory", "init");
            put("javax.net.ssl.SSLSession", "getPeerCertificates");
            put("javax.net.ssl.SSLSession", "getCipherSuite");
            put("javax.net.ssl.SSLSocket", "startHandshake");
            put("javax.net.ssl.SSLSocket", "getSession");
            put("javax.net.ssl.SSLSocketFactory", "getDefault");
            put("javax.net.ssl.X509ExtendedTrustManager", "checkClientTrusted");
            put("javax.net.ssl.X509ExtendedTrustManager", "checkServerTrusted");
            put("javax.net.ssl.X509ExtendedTrustManager", "getAcceptedIssuers");
            put("javax.net.ssl.X509KeyManager", "chooseClientAlias");
            put("javax.net.ssl.X509KeyManager", "getClientAliases");
            // android.net
            put("android.net.ConnectivityManager", "getActiveNetworkInfo");
            put("android.net.ConnectivityManager", "getAllNetworkInfo");
            put("android.net.ConnectivityManager", "requestNetwork");
            put("android.net.Network", "getSocketFactory");
            put("android.net.NetworkInfo", "getType");
            put("android.net.NetworkInfo", "isConnected");
            put("android.net.NetworkInfo", "isAvailable");
            put("android.net.Uri", "parse");
            put("android.net.wifi.WifiManager", "getConnectionInfo");
            put("android.net.wifi.WifiManager", "getScanResults");
            put("android.net.Proxy", "getDefaultProxy");
            put("android.net.ProxyInfo", "getPacFileUrl");
            put("android.net.ProxyInfo", "getHost");
            put("android.net.Uri", "fromFile");
            put("android.net.Uri", "fromParts");
            put("android.net.ConnectivityDiagnosticsManager", "startNattSocketTest");
            put("android.net.wifi.WifiConfiguration", "allowedAuthAlgorithms");
            put("android.net.wifi.WifiConfiguration", "allowedGroupCiphers");
            put("android.net.Uri", "parse");
            put("android.webkit.URLUtil", "isValidUrl");
        }
    };
    //Inherited superclasses count?

    public boolean isNetworkCallStrict(SootMethod sootMethod){
        //System.out.println(sootMethod.getDeclaringClass().getName());
        if(AndroidUtil.isAndroidMethod(sootMethod))
            return false;
        if(sootMethod.toString().contains("<init>") || sootMethod.toString().contains("<clinit>"))
            return false;
        if(sootMethod.getName().equals("dummyMainMethod"))
            return false;
        //Travel the map, to see if match
        for (Map.Entry<String, String> entry : networkCallRecords.entrySet()) {
            String className = entry.getKey();
            String methodName = entry.getValue();
            if(sootMethod.getDeclaringClass().getName().equals(className)){
                if(sootMethod.getName().equals(methodName)){
                    return true;
                }
            }
        }
        return false;
    }

    Set<String> networkClassRecords = new HashSet<String>(){
        {
            // java.net 包
            add("java.net.URL");
            add("java.net.URLConnection");
            add("java.net.HttpURLConnection");
            add("java.net.Proxy");
            add("java.net.InetSocketAddress");
            add("java.net.InetAddress");
            add("java.net.Socket");
            add("java.net.URLEncoder");

            // javax.net 包
            add("javax.net.ssl.SSLContext");
            add("javax.net.ssl.SSLParameters");
            add("javax.net.ssl.SSLSocketFactory");
            add("javax.net.ssl.HttpsURLConnection");

            // android.net 包
            add("android.net.ConnectivityManager");
            add("android.net.NetworkInfo");
            add("android.net.NetworkRequest");
            add("android.net.Uri");
            add("android.net.wifi.WifiInfo");
            add("android.net.wifi.WifiManager");
            
            // org.apache.http
            add("org.apache.http.HttpResponse");
            add("org.apache.http.client.HttpClient");
            add("org.apache.http.client.methods.HttpGet");
            add("org.apache.http.impl.client.DefaultHttpClient");
            add("org.apache.http.HttpEntity");

        }
    };


    public boolean isNetworkCallLoose(SootMethod sootMethod){
        if(AndroidUtil.isAndroidMethod(sootMethod))
            return false;
        if(sootMethod.getName().equals("dummyMainMethod"))
            return false;
        if(sootMethod.toString().contains("<init>") || sootMethod.toString().contains("<clinit>"))
            return false;
        if(!isNotJavaLibFuns(sootMethod))
            return false;
        //Travel the set, to see if its class matchs
        if(networkClassRecords.contains(sootMethod.getDeclaringClass().getName())){
            return true;
        } else {
            return false;
        }
    }

    Set<String> lifecycleMethods = new HashSet<>(Arrays.asList(
        "onCreate",
        "onStart",
        "onResume",
        "onPause",
        "onStop",
        "onDestroy",
        "onRestart"
    ));

    //
    public boolean isLifecycleMethods(SootMethod sootMethod){
        if(lifecycleMethods.contains(sootMethod.getName())){
            return true;
        } else {
            return false;
        }
    }

    public boolean isNotJavaLibFuns(SootMethod sootMethod){
        if(sootMethod.getDeclaringClass().getPackageName().startsWith("java.lang"))
            return false;
        if(sootMethod.getDeclaringClass().getPackageName().startsWith("java.util"))
            return false;
        if(sootMethod.getDeclaringClass().getPackageName().startsWith("java.io"))
            return false;
        return true;
    }
}