/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package mrfs;

import dag.DagCompute;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import mrfs.SolutionPath.Point;
import mrfs.backwardsMapping.BackmapCompute;
import mrfs.backwardsMapping.Instance;

/**
 *
 * @author andre
 */
public class MRFs {
    
    public static String name;
    public static int threads=1;

    /**
     * @param args the command line arguments
     * args[0]: path to instances.
     * args[1]: number of datapoints per time period.
     * args[2]: parameter h0 for kernel averaging.
     * args[3]: parameter nu_0 for shrinkage.
     * args[4]: linfty norm radius.
     * args[5]: true for synthetic instances, false for real instances.
     */
    public static void main(String[] args) throws InterruptedException, IOException {
//        Instance instance= new Instance("./data/daily_data_1990.csv");
//        String path=args[0];
//        Instance instance= new Instance("./data/synt.csv");
        String path=args[0];
        name=path.split("/")[2].split("\\.")[0];
        System.out.println(name);
        Instance instance= new Instance(path);
        int number=Integer.parseInt(args[1]);
        double h0= Double.parseDouble(args[2]);
        double shrink0=Double.parseDouble(args[3]);
        double linfty0=Double.parseDouble(args[4]);
        boolean synthetic=Boolean.parseBoolean(args[5]);
//        if(args.length>5)
//        {
//            threads=Integer.parseInt(args[5]);
//        }
        double T = instance.data.length/number;
        BackmapCompute back;
        long timeBackwards=System.currentTimeMillis();
        if(synthetic)
        {
            double h = 1;
            double shrink =shrink0* Math.sqrt(Math.log(instance.varNames.length) / (T*number * h));
            back= new BackmapCompute(instance,shrink,h,number, true);
        }
        
        else
        {
            double h = h0 * Math.pow(T, -(1/3));
            double shrink =shrink0* Math.sqrt(Math.log(instance.varNames.length) / (T*number * h));
            linfty0=linfty0*Math.sqrt(Math.log(instance.varNames.length)/(T*number*h));
            back= new BackmapCompute(instance,shrink,h,number, false);
        }
      
        

        double[][] array=back.run();
          timeBackwards=System.currentTimeMillis()-timeBackwards;
        String[] names=back.names;
        String[] dates= new String[(int)T];
        for (int i = 0; i < T; i++) {
            dates[i]=instance.dates[i*number+number/2];
        }
        // TODO code application logic here
        
//        for (int i = 0; i < array.length; i++) {
//            System.out.print("Time period " + i+":\t");
//            for (int j = 0; j < array[i].length; j++) {
//                System.out.print(array[i][j]+" ");
//            }
//            System.out.println("");
//        }
        double timeDag;
//        DagCompute dag= new DagCompute(instance,array,names,dates, linfty0*Math.sqrt(Math.log(instance.varNames.length)/(T*number*h)));
        DagCompute dag= new DagCompute(instance,array,names,dates, linfty0);
        
//        DagCompute dag= new DagCompute(instance,array, 0.16*Math.sqrt(Math.log(instance.varNames.length)/(T*number*h)));
//        System.out.println("Infty=" +0.16*Math.sqrt(Math.log(instance.varNames.length)/(T*number*h)));
        double[][] sol= dag.run();
        timeDag=dag.timeDP;
        
        for (int i = 0; i < dag.heatMap.length; i++) {
            System.out.println(instance.dates[i*number+number/2]+"\t"+dag.heatMap[i]);
        }
        
        
        
        SolutionPath solPath=new SolutionPath(sol);
//        for (SolutionPath.Point breakpoint : path.breakpoints) {
//            System.out.println(breakpoint.reg+"\t"+breakpoint.sparse+"\t"+breakpoint.lq);
//        }
//        for (int i = 0; i < sol.length; i++) {
//            System.out.print("Parameter " + i + ":\t");
//            for (int j = 0; j < sol[i].length; j++) {
//                System.out.print(sol[i][j] + " ");
//            }
//            System.out.println("");
//        }
        exportSolution(sol);
        exportPath(solPath.breakpoints);
        exportTime(args, new double[]{timeBackwards/1000.0,timeDag});
        
    }
    
    static void exportSolution(double[][] sol) throws IOException {

        try (FileWriter out = new FileWriter(new File("./results/"+name+"_results.csv"), false)) {
            for (int i = 0; i < sol.length; i++) {
                out.write(i + ", ");
                for (int j = 0; j < sol[i].length; j++) {
                    out.write(sol[i][j]+",");
                }
                out.write("\n");
            }
            

        }
    }
    
    static void exportPath(List<Point> path) throws IOException {

        try (FileWriter out = new FileWriter(new File("./results/"+name+"_path.csv"), false)) {
            out.write("Regularization, Sparsity, Smoothness, Parameter\n");
            for (SolutionPath.Point breakpoint : path) {
            out.write(breakpoint.reg+","+breakpoint.sparse+","+breakpoint.lq+","+breakpoint.param+"\n");
        }
            

        }
    }

    private static void exportTime(String[] args, double[] results) throws IOException {
       try (FileWriter out = new FileWriter(new File("./results/time.csv"), true)) {
           for (String arg : args) {
               out.write(arg + ", ");
           }
            for (double res : results) {
               out.write(res + ", ");
               
           }
            out.write("\n");
            

        }
    }
    
    
}
