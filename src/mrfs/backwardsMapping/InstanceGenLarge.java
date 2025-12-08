/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mrfs.backwardsMapping;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

/**
 *
 * @author adminagomez
 */
public class InstanceGenLarge {

    static int T;
    static int n;
    static int k;
    static int seed;
    static double[][][] covInv;
    
    public static void main(String[] args) throws IOException {
        n=Integer.parseInt(args[0]);
        k=Integer.parseInt(args[1]);
        T=Integer.parseInt(args[2]);
        seed=Integer.parseInt(args[3]);
        double[][] train = new double[T * k][n];
        double[][] test = new double[T * k][n];
        double[][][] precs = generateInstance(train, test);
        exportInstance(precs, train, test);
    }

    static double[][][] generateInstance(double[][] train, double[][] test) {
        RandomGenerator generator = new JDKRandomGenerator(seed);
        covInv = new double[T][n][n];
        int elements = n * (n - 1) / 2;
//        double[][] samples = new double[T * k][n];
//        double[][] test = new double[T * k][n];

        double[] means = new double[n];

        List<Integer>[] numbers = new List[T];
        int sparseInit=3*n;
        double propChange=0.04;
        numbers[0] = new ArrayList<>();
        for (int i = 0; i < sparseInit; i++) {
            numbers[0].add(generator.nextInt(elements));
        }

//        for (int t = 0; t < T; t++) {
//            numbers[t] = new ArrayList<>();
//            for (int i = 5*t; i < 30+5*t; i++) {
////            numbers[0].add(generator.nextInt(elements));
//                numbers[t].add(i);
//            }
//        }
        for (int t = 1; t < T; t++) {
            numbers[t] = new ArrayList<>();
            for (int number : numbers[t - 1]) {
                if (generator.nextDouble() >= propChange) {
                    numbers[t].add(number);
                }
            }
            for (int i = 0; i < propChange*sparseInit; i++) {
                numbers[t].add(generator.nextInt(elements));
            }
        }

        int counter;
        for (int t = 0; t < T; t++) {
            for (int i = 0; i < n; i++) {
                covInv[t][i][i] = 1;
            }
            counter = 0;
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (numbers[t].contains(counter)) {
                        covInv[t][i][j] = -0.4;
                        covInv[t][j][i] = -0.4;
                        covInv[t][i][i]+=0.4;
                        covInv[t][j][j]+=0.4;
                    }
                    counter++;
                }
            }

            RealMatrix cov = MatrixUtils.inverse(new Array2DRowRealMatrix(covInv[t]));
            MultivariateNormalDistribution dist = new MultivariateNormalDistribution(generator, means, cov.getData());
            for (int i = k * t; i < k * (t + 1); i++) {
                train[i] = dist.sample();
                test[i] = dist.sample();
            }
        }
        return covInv;
    }

    static void exportInstance(double[][][] precs, double[][] train, double[][] test) throws IOException {

        try ( FileWriter out = new FileWriter(new File("./data/synt_"+n+"_"+k+"_"+T+"_"+seed+".csv"), false)) {
            out.write(",");
            out.write(",");
            for (int i = 0; i < T * k - 1; i++) {
                out.write(i + ",");
            }
            out.write(T * k - 1 + "\n");

            for (int i = 0; i < n; i++) {
                out.write(i + ",");
                out.write(i + ",");
                for (int j = 0; j < T * k - 1; j++) {
                    out.write(train[j][i] + ",");
                }
                out.write(train[T * k - 1][i] + "\n");
            }
        }

        try ( FileWriter out = new FileWriter(new File("./data/syntTest_"+n+"_"+k+"_"+T+"_"+seed+".csv"), false)) {
            out.write(",");
            out.write(",");
            for (int i = 0; i < T * k - 1; i++) {
                out.write(i + ",");
            }
            out.write(T * k - 1 + "\n");

            for (int i = 0; i < n; i++) {
                out.write(i + ",");
                out.write(i + ",");
                for (int j = 0; j < T * k - 1; j++) {
                    out.write(test[j][i] + ",");
                }
                out.write(test[T * k - 1][i] + "\n");
            }
        }

        try ( FileWriter out = new FileWriter(new File("./data/syntTrue_"+n+"_"+k+"_"+T+"_"+seed+".csv"), false)) {
            out.write(",");
            out.write(",");
            out.write(",");
            for (int i = 0; i < T - 1; i++) {
                out.write(i + ",");
            }
            out.write(T - 1 + "\n");

            int index = 0;
            for (int i = 0; i < precs[0].length; i++) {
                for (int j = i; j < precs[0][0].length; j++) {
                    out.write(index + ",");
                    out.write(i + ",");
                    out.write(j + ",");
                    for (int t = 0; t < T - 1; t++) {
                        out.write(precs[t][i][j] + ",");
                    }
                    out.write(precs[T - 1][i][j] + "\n");
                    index++;
                }

            }
        }

    }

}
