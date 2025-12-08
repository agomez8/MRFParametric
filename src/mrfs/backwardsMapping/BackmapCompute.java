/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mrfs.backwardsMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import mrfs.MRFs;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 *
 * @author andre
 */
public class BackmapCompute {

    double prop0 = 0.5;
    Instance instance;
    int number;
    double shrink;
    double h;
    boolean isGaussian = true;
    boolean isBinary = false;
    public String[] names;

    /**
     * Constructor by parameters.<br>
     *
     * @param instance Instance.
     * @param shrink Shrinkage parameter (nu in the paper).
     * @param h Kernel averaging parameter (h in the paper).
     * @param number Sample size parameter (N_t in the paper).
     * @param isGaussian Whether the problem is a GMRF (=true) or a DMRF(=false).
     */
    public BackmapCompute(Instance instance, double shrink, double h, int number, boolean isGaussian) {
        this.instance = instance;
        this.shrink = shrink;
        this.number = number;
        this.h = h;
        this.isGaussian = isGaussian;
        this.isBinary = !isGaussian;
    }

    public double[][] run() throws InterruptedException {
        int n = this.instance.varNames.length;
        int threads=MRFs.threads;
//        System.out.println("NUmber"+number);
        if (isGaussian) {
            RealMatrix[] covariances = computeCovariances(number);
            int n0 = instance.dates.length / number;
//        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            ExecutorService pool = Executors.newFixedThreadPool(threads);

//        ExecutorService pool = Executors.newFixedThreadPool(1);
            double[][] backmap = new double[n0][n * (n + 1) / 2];
            names = new String[n * (n + 1) / 2];
            int counter = 0;
            for (int i = 0; i < n; i++) {
                for (int j = i; j < n; j++) {
                    names[counter] = instance.varNames[i] + "_" + instance.varNames[j];
                    counter++;
                }
            }
            System.out.println("Computing backwards mapping using "
                    + threads + " threads.");
            long time = System.currentTimeMillis();

            for (int i = 0; i < covariances.length; i++) {
                Backmapping backmapping = new Backmapping_GMRF(instance, i, backmap[i], shrink, h, covariances);
                pool.execute(backmapping);
            }
            pool.shutdown();
            pool.awaitTermination(1, TimeUnit.DAYS);
            System.out.println("Backwards mapping for GMRF computed in " + (System.currentTimeMillis() - time) / 1000 + " seconds.");
            return backmap;
        } else if (isBinary) {
            double[][][][] probabilities = computeProbabilities(number);
            int n0 = instance.dates.length / number;
            List<Double>[] backmapList = new List[n0];
            List<String> nameList = new ArrayList<>();
            System.out.println("Computing backwards mapping using "
                    + threads + " threads.");
            long time = System.currentTimeMillis();
            for (int t = 0; t < n0; t++) {
//                System.out.println("Backmapping for time "+t+"/"+n0);
                backmapList[t] = new ArrayList<>();
                double[][] marginalProbabilities = new double[probabilities[t].length][2];
                for (int i = 0; i < probabilities[t].length; i++) {
                    double[] sum2 = new double[2];
                    double sum = 0, num, w;
                    for (int l = 0; l < n0; l++) {
                        num = (t - l) / (n0 * h);
                        sum += 1 / ((double) (n0 * h)) * 1.465 * Math.exp(-num * num / 2.0);
                    }
                    for (int l = 0; l < n0; l++) {
                        num = (t - l) / (n0 * h);
                        w = 1 / ((double) (n0 * h)) * 1.465 * Math.exp(-num * num / 2.0);
                        w /= sum;
//                            w = (num == 0) ? 1 : 0;
                        for (int k = 0; k < 2; k++) {
                            sum2[k] += w * probabilities[l][i][i][k];
                        }
                    }
                    marginalProbabilities[i][0] = sum2[0];
                    marginalProbabilities[i][1] = sum2[1];
//                    System.out.println(instance.varNames[i]+" "+t+"\t"+sum2[0]+" "+sum2[1]);
                    if (marginalProbabilities[i][0] == 0) {
                        System.out.println(instance.varNames[i] + t + " " + " 0 is zero");

                        marginalProbabilities[i][0] += 1e-3;
                        sum2[0] += 1e-3;
                    }

                    if (marginalProbabilities[i][1] == 0) {
                        System.out.println(instance.varNames[i] + t + " " + " 1 is zero");
                        marginalProbabilities[i][1] += 1e-3;
                        sum2[1] += 1e-3;

                    }

                    backmapList[t].add(Math.log(sum2[0]));
                    backmapList[t].add(Math.log(sum2[1]));
                    if (t == 0) {
                        nameList.add(instance.varNames[i] + "->0");
                        nameList.add(instance.varNames[i] + "->1");
                    }
                }
//                System.out.println("Computed marginals");
                for (int i = 0; i < probabilities[t].length; i++) {
                    for (int j = i + 1; j < probabilities[t].length; j++) {
                        double[] sum2 = new double[4];
                        double sum = 0, num, w;
                        for (int l = 0; l < n0; l++) {
                            num = (t - l) / (n0 * h);
                            sum += 1 / ((double) (n0 * h)) * 1.465 * Math.exp(-num * num / 2.0);
                        }
                        for (int l = 0; l < n0; l++) {
                            num = (t - l) / (n0 * h);
                            w = 1 / ((double) (n0 * h)) * 1.465 * Math.exp(-num * num / 2.0);
                            w /= sum;
//                            w = (num == 0) ? 1 : 0;
                            for (int k = 0; k < 4; k++) {
                                sum2[k] += w * probabilities[l][i][j][k];
                            }
                        }
                        if (sum2[0] == 0) {
                            System.out.println(instance.varNames[i] + "_" + instance.varNames[j] + " " + t + " " + " 00 is zero");
                        }
                        if (sum2[1] == 0) {
                            System.out.println(instance.varNames[i] + "_" + instance.varNames[j] + " " + t + " " + " 11 is zero");
                        }
                        if (sum2[2] == 0) {
                            System.out.println(instance.varNames[i] + "_" + instance.varNames[j] + " " + t + " " + " 10 is zero");
                        }
                        if (sum2[3] == 0) {
                            System.out.println(instance.varNames[i] + "_" + instance.varNames[j] + " " + t + " " + " 01 is zero");
                        }
                        backmapList[t].add(Math.log(sum2[0] / (marginalProbabilities[i][0] * marginalProbabilities[j][0])));
                        if (t == 0) {
                            nameList.add(instance.varNames[i] + "_" + instance.varNames[j] + "->00");
                        }
                        backmapList[t].add(Math.log(sum2[1] / (marginalProbabilities[i][1] * marginalProbabilities[j][1])));
                        if (t == 0) {
                            nameList.add(instance.varNames[i] + "_" + instance.varNames[j] + "->11");
                        }
                        backmapList[t].add(Math.log(sum2[2] / (marginalProbabilities[i][1] * marginalProbabilities[j][0])));
                        if (t == 0) {
                            nameList.add(instance.varNames[i] + "_" + instance.varNames[j] + "->10");
                        }
                        backmapList[t].add(Math.log(sum2[3] / (marginalProbabilities[i][0] * marginalProbabilities[j][1])));
                        if (t == 0) {
                            nameList.add(instance.varNames[i] + "_" + instance.varNames[j] + "->01");
                        }

                    }
                }

            }
            double[][] backmap = new double[n0][];

            for (int t = 0; t < backmap.length; t++) {
                backmap[t] = new double[backmapList[t].size()];
                for (int i = 0; i < backmapList[t].size(); i++) {
                    backmap[t][i] = backmapList[t].get(i);

                }
            }

            names = new String[nameList.size()];
            for (int i = 0; i < nameList.size(); i++) {
                names[i] = nameList.get(i);

            }
            System.out.println("Backwards mapping for DMRF computed in " + (System.currentTimeMillis() - time) / 1000 + " seconds.");
            return backmap;
        }

        return null;
    }

    public RealMatrix[] computeCovariances(int number) {
        int n0 = instance.dates.length / number;
        RealMatrix[] covariances = new RealMatrix[n0];
        for (int i = 0; i < n0; i++) {
            covariances[i] = new Array2DRowRealMatrix(instance.data[i * number].length, instance.data[i * number].length);
            for (int j = 0; j < number; j++) {
                RealVector x = new ArrayRealVector(instance.data[i * number + j]);
                covariances[i] = covariances[i].add(x.outerProduct(x));
            }
            covariances[i] = covariances[i].scalarMultiply(1 / (double) number);
        }

        return covariances;
    }

    /**
     * Compute marginal probabilities. Coordinate 1=time period; coordinate 2=
     * index1; coordinate 3=index2; coordinate 4= 0<= 00, 1<= 11, 2<= 10, 3<=
     * 01. <br>
     *
     * @param number Number per time period. <br>
     * @return
     */
    public double[][][][] computeProbabilities(int number) {
        int n0 = instance.dates.length / number;
        List<Double> values = new ArrayList<>();
        for (double[] ds : instance.data) {
            for (double d : ds) {
                values.add(Math.abs(d));
            }
        }
        Collections.sort(values);
        double threshold = values.get((int) (values.size() * prop0));
        double[][][][] probabilities = new double[n0][instance.data[0].length][instance.data[0].length][4];
        for (int i = 0; i < n0; i++) {
            for (int j = 0; j < number; j++) {
                for (int k1 = 0; k1 < instance.data[i * number + j].length; k1++) {
                    int val1 = Math.abs(instance.data[i * number + j][k1]) >= threshold ? 1 : 0;
                    probabilities[i][k1][k1][0] += (1 - val1) / (double) number;
                    probabilities[i][k1][k1][1] += val1 / (double) number;
                    for (int k2 = k1 + 1; k2 < instance.data[i * number + j].length; k2++) {
                        int val2 = Math.abs(instance.data[i * number + j][k2]) >= threshold ? 1 : 0;
                        probabilities[i][k1][k2][0] += (1 - val1) * (1 - val2) / (double) number;
                        probabilities[i][k2][k1][0] = probabilities[i][k1][k2][0];

                        probabilities[i][k1][k2][1] += val1 * val2 / (double) number;
                        probabilities[i][k2][k1][1] = probabilities[i][k1][k2][1];

                        probabilities[i][k1][k2][2] += val1 * (1 - val2) / (double) number;
                        probabilities[i][k2][k1][2] = probabilities[i][k1][k2][2];

                        probabilities[i][k1][k2][3] += (1 - val1) * val2 / (double) number;
                        probabilities[i][k2][k1][3] = probabilities[i][k1][k2][3];
                    }
                }

            }
        }
        return probabilities;
    }

}
