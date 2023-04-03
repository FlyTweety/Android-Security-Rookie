package dev.zcy.soottutorial;

import dev.zcy.soottutorial.android.AndroidCallgraph;

import java.util.Arrays;

public class Main {
    public static void main(String[] args){
        if (args.length == 0){
            System.err.println("You must provide the name of the Java class file that you want to run.");
            return;
        }
        String[] restOfTheArgs = Arrays.copyOfRange(args, 1, args.length);
        if(args[0].equals("AndroidCallGraph")) {
            AndroidCallgraph.main(restOfTheArgs);
        }
        
    }
}
