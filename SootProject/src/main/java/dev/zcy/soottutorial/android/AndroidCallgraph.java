package dev.zcy.soottutorial.android;

import dev.zcy.soottutorial.visual.AndroidCallGraphFilter;
import dev.zcy.soottutorial.visual.Visualizer;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import soot.*;
import soot.options.Options;
import soot.Body;
import soot.Local;
import soot.Scene;
import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.toolkits.scalar.LocalDefs;
import soot.jimple.JimpleBody;


import soot.jimple.JimpleBody;
import soot.jimple.InvokeStmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.InvokeExpr;
import java.lang.System;

import java.util.stream.Collectors;
import java.io.File;
import java.util.*;

public class AndroidCallgraph {
    private final static String USER_HOME = System.getProperty("user.home");
    private static String androidJar = USER_HOME + "/Library/Android/sdk/platforms";
    static String androidDemoPath = System.getProperty("user.dir") + File.separator + "demo" + File.separator + "Android";

    static String apkPath = "!!!!PATH2APK!!!";

    public static void main(String[] args){
        if(System.getenv().containsKey("ANDROID_HOME"))
            androidJar = System.getenv("ANDROID_HOME")+ File.separator+"platforms";

        // Parse arguments
        InfoflowConfiguration.CallgraphAlgorithm cgAlgorithm = InfoflowConfiguration.CallgraphAlgorithm.SPARK;
        if (args.length > 0 && args[0].equals("CHA"))
            cgAlgorithm = InfoflowConfiguration.CallgraphAlgorithm.CHA;
        boolean drawGraph = false;
        if (args.length > 1 && args[1].equals("draw"))
            drawGraph = true;
        
        // Setup FlowDroid
        final InfoflowAndroidConfiguration config = AndroidUtil.getFlowDroidConfig(apkPath, androidJar, cgAlgorithm);
        SetupApplication app = new SetupApplication(config);
        // Create the Callgraph without executing taint analysis
        app.constructCallgraph();
        CallGraph callGraph = Scene.v().getCallGraph();

        /*
        //Find Package contains onCreate()
        String packageName = getMainPackage(callGraph);
        System.out.println("MainPackageName = " + packageName);
        if(packageName.equals("Error")){
            packageName = AndroidUtil.getPackageName(apkPath);
        }
        */
        String packageName = AndroidUtil.getPackageName(apkPath);
        //packageName = getMainPackage(callGraph);
        AndroidCallGraphFilter androidCallGraphFilter = new AndroidCallGraphFilter(packageName);


        /*
        SootClass mainClass = Scene.v().getSootClass("com.connect.UpdateApp");
        ArrayType stringArrayType = ArrayType.v(RefType.v("java.lang.String"), 1);
        SootMethod sm = mainClass.getMethod("doInBackground", Arrays.asList(stringArrayType));
        JimpleBody body = (JimpleBody) sm.retrieveActiveBody();

        // Print some information about printFizzBuzz
        System.out.println("Method Signature: " + sm.getSignature());
        System.out.println("--------------");
        System.out.println("Argument(s):");
        for (Local l : body.getParameterLocals()) {
            System.out.println(l.getName() + " : " + l.getType());
        }
        System.out.println("--------------");
        System.out.println("This: " + body.getThisLocal());
        System.out.println("--------------");
        System.out.println("Units:");
        int c = 1;
        for (Unit u : body.getUnits()) {
            System.out.println("(" + c + ") " + u.toString());
            c++;
        }
        System.out.println("--------------");
        */

        System.out.println("\n-----------------------------------------------------------------------------\n");


        // My Output
        int classIndex = 0;
        List<String> Calls = new ArrayList<>();
        StringBuilder allNetworkOutput = new StringBuilder();
        for(SootClass sootClass: androidCallGraphFilter.getValidClasses()){  //User Classes Only
            System.out.println(String.format("Class %d: %s", ++classIndex, sootClass.getName()));
            for(SootMethod sootMethod : sootClass.getMethods()){

                // Identify if it is a Life Cycle Function
                if(androidCallGraphFilter.isLifecycleMethods(sootMethod)){
                    System.out.println(String.format("\t[Life Cycle Function] Class %s, Method %s", sootMethod.getDeclaringClass().getName(), sootMethod.getName()));
                    // Get all the reachable Functions
                    Map<SootMethod, SootMethod> reachableMethodMap = getAllReachableMethodsNew(sootMethod); //Including Lib Functions

                    //Travel all the reachable Functions, output vaild methods and the path
                    for (Map.Entry<SootMethod, SootMethod> entry : reachableMethodMap.entrySet()) {
                        SootMethod mapKey = entry.getKey();
                        SootMethod mapValue = entry.getValue();
                        
                        if((mapValue != null && isNotJavaLibFuns(mapValue))){
                            System.out.println("\t\t[Reachable] " + mapKey.getDeclaringClass().getName() + " " + mapKey.getName() + " | " + getPossiblePath(reachableMethodMap, mapKey));
                            //System.out.println("\t\t[Reachable] " + mapKey.getName());
                        }


                        if((androidCallGraphFilter.isNetworkCallLoose(mapKey))){

                            System.out.println("\t\t\t[Network] " + mapKey.getDeclaringClass().getName() + " " + mapKey.getName() + " | " + getPossiblePath(reachableMethodMap, mapKey));

                            String thisCall = mapKey.getDeclaringClass().getName() + " " + mapKey.getName();
                            int flag = 0;
                            Iterator<String> it = Calls.iterator();
                            while(it.hasNext()){
                                String inCall = it.next();
                                if(inCall.equals(thisCall)){
                                    flag = 1;
                                    break;
                                }
                            }
                            if(flag == 0){
                                Calls.add(thisCall);
                                allNetworkOutput.append(mapKey.getDeclaringClass().getName() + " " + mapKey.getName());
                                List<Type> paramTypes = mapKey.getParameterTypes();
                                for (int i = 0; i < paramTypes.size(); i++) {
                                    allNetworkOutput.append(" arg" + (i+1));
                                }
                                allNetworkOutput.append("\n");
                            }
                        }
                    }       
                    
                    /*
                    //DEBUG
                    for (Map.Entry<SootMethod, SootMethod> entry : reachableMethodMap.entrySet()) {
                        SootMethod mapKey = entry.getKey();
                        SootMethod mapValue = entry.getValue();                        
                        System.out.println("\t\t\t\t[ALL] " + mapKey.getDeclaringClass().getName() + " " + mapKey.getName() + " | " + getPossiblePath(reachableMethodMap, mapKey));
                    } 
                    */               
               }
            }
        }

        System.out.println("\n-----------------------------------------------------------------------------\n");
        System.out.println(allNetworkOutput);
        System.out.println("\n-----------------------------------------------------------------------------\n");

        // Draw a subset of call graph
        if (drawGraph) {
            Visualizer.v().addCallGraph(callGraph,
                    androidCallGraphFilter,
                    new Visualizer.AndroidNodeAttributeConfig(true));
            Visualizer.v().draw();
        }
    }

    public static void printInAndOutEdges(SootMethod sootMethod, CallGraph callGraph){
        //One Step Only!!!!!!!!!!
        int incomingEdge = 0;
        for(Iterator<Edge> it = callGraph.edgesInto(sootMethod); it.hasNext();incomingEdge++,it.next());
        int outgoingEdge = 0;
        for(Iterator<Edge> it = callGraph.edgesOutOf(sootMethod); it.hasNext();outgoingEdge++,it.next()){
            /*
            Edge edge = it.next();
            SootMethod src = edge.src();
            SootMethod tgt = edge.tgt();
            System.out.println("\t\t" + outgoingEdge + " " + src.getName() + " -> " + tgt.getName());
            */
        }
        //System.out.println(String.format("\tClass %s, Method %s, #IncomeEdges: %d, #OutgoingEdges: %d", sootMethod.getDeclaringClass().getName(), sootMethod.getName(), incomingEdge, outgoingEdge));
        System.out.println(String.format("\tMethod %s, #IncomeEdges: %d, #OutgoingEdges: %d", sootMethod.getName(), incomingEdge, outgoingEdge));
        
        /*
        // DEBUG
        for(Iterator<Edge> it = callGraph.edgesOutOf(sootMethod); it.hasNext();it.next()){
            Edge edge = it.next();

            SootMethod src = edge.src();
            SootMethod tgt = edge.tgt();
            System.out.println("\t\t" + src.getName() + " -> " + tgt.getName());
            if(tgt.getName().equals("aNetWorkCallFun")){
                System.out.println("true!!!");
                for(Iterator<Edge> ittgt = callGraph.edgesOutOf(tgt); ittgt.hasNext();ittgt.next()){
                    SootMethod a = ittgt.next().tgt();
                    System.out.println("\t\t\t\t" + a.getName());
                }
            }
            
        }
        */
    }

    // A Breadth-First Search algorithm to get all reachable methods from initialMethod in the callgraph
    // The output is a map from reachable methods to their parents
    public static Map<SootMethod, SootMethod> getAllReachableMethods(SootMethod initialMethod){
        CallGraph callgraph = Scene.v().getCallGraph();
        List<SootMethod> queue = new ArrayList<>();
        queue.add(initialMethod);
        Map<SootMethod, SootMethod> parentMap = new HashMap<>();
        parentMap.put(initialMethod, null);
        for(int i=0; i< queue.size(); i++){
            SootMethod method = queue.get(i);
            for (Iterator<Edge> it = callgraph.edgesOutOf(method); it.hasNext(); ) {
                Edge edge = it.next();
                SootMethod childMethod = edge.tgt();
                if(parentMap.containsKey(childMethod))
                    continue;
                parentMap.put(childMethod, method);
                queue.add(childMethod);
            }
        }
        return parentMap;
    }

    public static Map<SootMethod, SootMethod> getAllReachableMethodsNew(SootMethod initialMethod){
        /*
        JimpleBody body = (JimpleBody) initialMethod.retrieveActiveBody();
        for (Unit unit : body.getUnits()){
            if(unit instanceof InvokeStmt){
                InvokeStmt invokeStmt = (InvokeStmt) unit;
                System.out.println("\t\tInvokeExpr: " + invokeStmt.getInvokeExpr().getMethod());
            }
            if(unit instanceof JAssignStmt){
                Value rightOp = ((JAssignStmt) unit).getRightOp();
                if(rightOp instanceof InvokeExpr){
                    InvokeExpr InvokeExpr = (InvokeExpr) rightOp;
                    System.out.println("\t\tRight InvokeExpr: " + InvokeExpr.getMethod());
                }
            }
        }
        */
        CallGraph callgraph = Scene.v().getCallGraph();
        List<SootMethod> queue = new ArrayList<>();
        queue.add(initialMethod);
        Map<SootMethod, SootMethod> parentMap = new HashMap<>();
        parentMap.put(initialMethod, null);

        //现在改为先对这个用户函数做一轮这种情况的
        JimpleBody body = (JimpleBody) initialMethod.retrieveActiveBody(); 
        for (Unit unit : body.getUnits()){
            if(unit instanceof JAssignStmt){
                Value rightOp = ((JAssignStmt) unit).getRightOp();
                if(rightOp instanceof InvokeExpr){
                    InvokeExpr InvokeExpr = (InvokeExpr) rightOp;
                    SootMethod childMethod = InvokeExpr.getMethod();
                    if(parentMap.containsKey(childMethod))
                        continue;
                    parentMap.put(childMethod, initialMethod);
                    queue.add(childMethod);
                }
            }
        }

        for(int i=0; i< queue.size(); i++){
            SootMethod method = queue.get(i);
            for (Iterator<Edge> it = callgraph.edgesOutOf(method); it.hasNext(); ) {
                Edge edge = it.next();
                SootMethod childMethod = edge.tgt();
                if(parentMap.containsKey(childMethod))
                    continue;
                parentMap.put(childMethod, method);
                queue.add(childMethod);
            }
        }
        return parentMap;
    }

    public static String getPossiblePath(Map<SootMethod, SootMethod> reachableParentMap, SootMethod it) {
        String possiblePath = null;
        while(it != null){
            String itName = it.getDeclaringClass().getShortName()+"."+it.getName();
            if(possiblePath == null)
                possiblePath = itName;
            else
                possiblePath = itName + " -> " + possiblePath;
            it = reachableParentMap.get(it);
        } return possiblePath;
    }

    public static String getMainPackage(CallGraph callGraph){
        Iterator<SootClass> classes = Scene.v().getApplicationClasses().iterator();
        while (classes.hasNext()) {
            SootClass cls = classes.next();
            Iterator<SootMethod> methods = cls.getMethods().iterator();
            while (methods.hasNext()) {
                SootMethod method = methods.next();
                if(method.getName().equals("onCreate")){
                    String packageName = method.getDeclaringClass().getPackageName();
                    return packageName;
                }
            }
        }
        return "Error"; 
    }

    public static boolean isNotJavaLibFuns(SootMethod sootMethod){
        if(sootMethod.getDeclaringClass().getPackageName().startsWith("java.lang"))
            return false;
        if(sootMethod.getDeclaringClass().getPackageName().startsWith("java.util"))
            return false;
        if(sootMethod.getDeclaringClass().getPackageName().startsWith("java.io"))
            return false;
        return true;
    }
}