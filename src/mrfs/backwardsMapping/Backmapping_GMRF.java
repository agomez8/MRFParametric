/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mrfs.backwardsMapping;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 *
 * @author andre
 */
public class Backmapping_GMRF extends Backmapping {

    double shrink; //shrinkage parameter
    double T, h;
    RealMatrix[] covariances;

  
    public Backmapping_GMRF(Instance instance, int date,double[] backmap,double shrink,double h, RealMatrix[] covariances) {
        super(instance, date,backmap);
        this.shrink = shrink;
        this.covariances=covariances;
        this.T = covariances.length;
        this.h = h;
    }
    
   

    @Override
    public void run() {
        double w, num;

        RealMatrix cov = new Array2DRowRealMatrix(instance.varNames.length,
                instance.varNames.length);
        //Smoothed average
//        System.out.println("Kernel");
        double sum=0, sum2=0;
        for (int i = 0; i < T; i++) {
            num = (date - i) / (T * h);
            sum+=Math.exp(-num*num / 2.0);
        }
        for (int i = 0; i < T; i++) {
            num = (date - i) / (T * h);
//            w = 1 / ((double)(T * h)) * 1.465 * Math.exp(-num*num / 2.0);
//            w =  Math.exp(-num*num / 2.0)/sum;
            w=(num==0)?1:0;
            sum2+=w;
            cov = cov.add(covariances[i].scalarMultiply(w));
        }
//        System.out.println("Sum="+sum2);

        //Mapping
        for (int i = 0; i < cov.getRowDimension(); i++) {
            for (int j = i + 1; j < cov.getColumnDimension(); j++) {
                cov.addToEntry(i, j, -Math.signum(cov.getEntry(i, j)) * Math.min(shrink, Math.abs(cov.getEntry(i, j))));
            }
        }
//        DecompositionSolver solver = new LUDecomposition(cov).getSolver();
//        RealMatrix inv = solver.getInverse();
        RealMatrix inv=MatrixUtils.inverse(cov);
//        System.out.println("Date= "+date);
//        for (int i = 0; i < inv.getRowDimension(); i++) {
//            for (int j = 0; j < inv.getColumnDimension(); j++) {
//                System.out.print(inv.getEntry(i, j)+" ");
//            }
//            System.out.println("");
//            
//        }

//        backmap = new double[inv.getColumnDimension() * (inv.getColumnDimension() + 1) / 2];
        int counter = 0;
        for (int i = 0; i < inv.getRowDimension(); i++) {
            for (int j = i; j < inv.getColumnDimension(); j++) {
                backmap[counter] = inv.getEntry(i, j);
                counter++;
            }

        }
//        System.out.println("\t Date "+date+" complete.");

    }
}
