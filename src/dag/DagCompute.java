/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dag;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import mrfs.MRFs;
import mrfs.backwardsMapping.Backmapping;
import mrfs.backwardsMapping.Backmapping_GMRF;
import mrfs.backwardsMapping.Instance;

/**
 *
 * @author andre
 */
public class DagCompute {
    
    
    Instance instance;
    String[] dates;
    double[][] backmaps;
    String[] names;
    double lambda0;
    public double[] heatMap;
    public double timeDP;

    public DagCompute(Instance instance,double[][] backmaps,String[] names,String[] dates , double lambda0) {
        this.instance=instance;
        this.backmaps = backmaps;
        this.dates=dates;
        this.names=names;
        this.lambda0 = lambda0;
        File file= new File("./results/"+MRFs.name+"_vals.csv");
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException ex) {
            Logger.getLogger(DagCompute.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

    public double[][] run() throws InterruptedException {
        int m = backmaps[0].length;
        int threads=MRFs.threads;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
//        ExecutorService pool = Executors.newFixedThreadPool(1);

        System.out.println("Computing arc costs using "
                + threads + " threads.");
        long time = System.currentTimeMillis();
//        double[][][] costs = new double[m][backmaps.length + 1][backmaps.length + 1];
//        double[][] costs = new double[backmaps.length + 1][backmaps.length + 1];
        double sol[][] = new double[m][backmaps.length + 1];
        heatMap= new double[backmaps.length];
        timeDP=0;
        for (int i = 0; i < m; i++) {

            double[] series = new double[backmaps.length];
            for (int j = 0; j < series.length; j++) {
                series[j] = backmaps[j][i];
            }


            DynamicProgram forwardArcs = new DynamicProgram(instance,i,names[i], series, lambda0, 0, sol[i],heatMap,dates);
            forwardArcs.run();
            timeDP+=forwardArcs.timeDP;
//            timeMIP+=forwardArcs.timeMIP;
//            pool.execute(forwardArcs);
        }
        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.DAYS);

//        System.out.println("Costs");
//        for (int i = 0; i < costs[1].length; i++) {
//            System.out.print(i+":\t");
//            for (int j = 0; j < costs[1].length; j++) {
//                System.out.print(costs[1][i][j]+" ");
//            }
//            System.out.println("");
//        }
        System.out.println("Arc costs computed in " + (System.currentTimeMillis() - time) / 1000 + " seconds.");
        System.out.println("Time DP"+timeDP);
        return sol;
    }
}
