package com.auto.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Date 01.10.2018
 *
 * @author Hursanov Sulaymon
 * @version v1.0
 **/
public class ShellScriptUtil {

    public static void execute(String path){

        File file = new File(path);

        if (!file.exists() || !file.canExecute()){
            System.err.println("Unable to find/read target shell script at -"+ path);
            return;
        }

        ProcessBuilder pb = new ProcessBuilder("cmd.exe",path);
        pb.redirectErrorStream(true);

        Process p;

        try {
            p = pb.start();
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

            System.out.println("Script starts:\n");

            String s = null;
            while ((s=stdInput.readLine())!=null){
                System.out.println(s);
            }

            System.out.println("\nScript executing finished! ");
        } catch (IOException e) {
            System.err.println("Error while executing script");
            e.printStackTrace();
        }
    }
}
