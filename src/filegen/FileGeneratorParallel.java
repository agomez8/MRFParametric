/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filegen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Andres Gomez.
 */
public class FileGeneratorParallel {

    public static void main(String[] args) throws IOException {

        String instanceTrain = "java "
                //                + "-Djava.library.path=\"C:/Program Files/IBM/ILOG/CPLEX_Studio1271/cplex/bin/x64_win64" 
                + " -cp ./dist/MRFs.jar mrfs.MRFs";
        int[] seeds = {101, 102, 103, 104, 105};
        int num = 25;
        int[] ns =new int[]{100,500,1000,1500,2000,2500,3000,3500,4000};
        int T = 10;
        double[] hs = {1};
        double[] shrinks = { 0.5};
        double[] linftys = new double[]{0.2};
        int[] threads= new int[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};

        try ( FileWriter out = new FileWriter(new File("./runParallel.bat"))) {
            for (int n : ns) {
                int m=n/2;
                for (int seed : seeds) {
                    
                    String trainData = "./data/synt_" + n + "_" + T + "_" + seed + ".csv";
                   
                    for (double h : hs) {
                        for (double shrink : shrinks) {
                            for (double linfty : linftys) {
                                for(int thread: threads)
                                {
                                out.write(instanceTrain + " " + trainData + " " + m + " "
                                        + h + " " + shrink + " " + linfty +" "+thread+"\n");
                                }
                            }
                        }
                    }

                }
            }

        }
    }
}
