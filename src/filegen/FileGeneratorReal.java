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
public class FileGeneratorReal {

    public static void main(String[] args) throws IOException {

        String instanceReal = "java "
                //                + "-Djava.library.path=\"C:/Program Files/IBM/ILOG/CPLEX_Studio1271/cplex/bin/x64_win64" 
                + " -cp ./dist/MRFs.jar mrfs.MRFs ./data/daily_data_1990.csv";

        int[] ms = new int[]{60, 50, 40, 30, 20};
//        int[] ms = new int[]{2000};
        double[] hs = {0.02};
        double[] shrinks = {0};
        double[] linftys = new double[]{0.005, 0.016, 0.05, 0.1, 0.2, 0.3, 0.4, 0.5};

        try (FileWriter out = new FileWriter(new File("./runReal.bat"))) {
            for (int m : ms) {

                for (double h : hs) {
                    for (double shrink : shrinks) {
                        for (double linfty : linftys) {
                            out.write(instanceReal + " " + m + " "
                                    + h + " " + shrink + " " + linfty + " " + "false" + "\n");
                            
                        }
                    }

                }
            }

        }
    }
}
