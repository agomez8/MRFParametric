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
public class FileGeneratorInstance {

    public static void main(String[] args) throws IOException {

        String instance = "java "
                //                + "-Djava.library.path=\"C:/Program Files/IBM/ILOG/CPLEX_Studio1271/cplex/bin/x64_win64" 
                + " -cp ./dist/MRFs.jar mrfs.backwardsMapping.InstanceGenLarge";
      
        int[] seeds = {101, 102, 103, 104, 105};
        int[] ns = {50};
        int[] ms = {1050,1100,1150,1200,1250,1300,1350,1400,1450,1500,1550,1600,1650,1700,1750,1800,1850,1900,1950,2000};
        int T = 10;
        try ( FileWriter out = new FileWriter(new File("./runInstanceGen.bat"))) {
            for (int n : ns) {
               for(int m:ms){
                for (int seed : seeds) {

                    out.write(instance + " " + n + " " + m + " "
                            + T + " " + seed + "\n");

                }
            }
            }

        }
    }
}
