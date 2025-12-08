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
public class FileGeneratorCrossval {

    public static void main(String[] args) throws IOException {

        String instanceTrain = "java "
                //                + "-Djava.library.path=\"C:/Program Files/IBM/ILOG/CPLEX_Studio1271/cplex/bin/x64_win64" 
                + " -cp ./dist/MRFs.jar mrfs.MRFs";
        String instanceParse = "java "
                //                + "-Djava.library.path=\"C:/Program Files/IBM/ILOG/CPLEX_Studio1271/cplex/bin/x64_win64" 
                + " -cp ./dist/MRFs.jar parse_sol.Parser";
        int[] seeds = {101, 102, 103, 104, 105};
   
        int n = 50;
        int T = 10;
        int[] ms = new int[]{100,200,300,400,500,600,700,800,900,1000,1050,1100, 1150, 1200, 1250, 1300, 1350, 1400, 1450, 1500,
             1550, 1600, 1650, 1700, 1750, 1800, 1850, 1900, 1950, 2000};
//        int[] ms = new int[]{2000};
        double[] hs = {1};
        double[] shrinks = {0,0.2,0.5,0.8,2};
        double[] linftys = new double[]{0.2};

        try ( FileWriter out = new FileWriter(new File("./runCrossval.bat"))) {
            for (int m : ms) {
                for (int seed : seeds) {
                    String trainData = "./data/synt_" + n + "_" + m + "_" + T + "_" + seed + ".csv";
                    String valData = "./data/syntTest_" + n + "_" +m+"_"+ T + "_" + seed + ".csv";
                    String trueData = "./data/syntTrue_" + n + "_"+m+"_" + T + "_" + seed + ".csv";
                    String outputData = "./results/synt_" + n + "_"+m+"_" + T + "_" + seed + "_vals.csv";
                    for (double h : hs) {
                        for (double shrink : shrinks) {
                            for (double linfty : linftys) {
                                out.write(instanceTrain + " " + trainData + " " + m + " "
                                        + h + " " + shrink + " " + linfty +" "+"true"+ "\n");
                                out.write(instanceParse + " " + outputData + " " + valData + " "
                                        + trueData + " " + n + " " + T + " " + m*T + " "
                                        + h + " " + shrink + " " + linfty + "\n");
                            }
                        }
                    }

                }
            }

        }
    }
}
