/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package parse_sol;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.CholeskyDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

/**
 *
 * @author andre
 */
public class ParserReal {

    int n;
    int T;
    int m;
    TreeSet<Double> breakpoints;
    List<Solution>[] solutions;
    List<double[]>[] validationData;
    double[][][] trueValues;

    /**
     * Creates an instances from a CVS file. <br>
     *
     */
    public ParserReal(int n, int T, int m, String path) {
        this.n = n;
        this.T = T;
        this.m = m;
        breakpoints = new TreeSet<>();
        solutions = readSolution(path, breakpoints);
    }

    

    /**
     * Reads the data from file. <br>
     *
     * @param path Path to file
     */
    private List<Solution>[] readSolution(String path, TreeSet<Double> breakpoints) {
        List<Solution>[] sols = new List[n * (n + 1) / 2];
        for (int i = 0; i < sols.length; i++) {
            sols[i] = new ArrayList<>();
        }
        BufferedReader br = null;
        String cvsSplitBy = ",";
        String line;
        String[] row, value;

        try {
            br = new BufferedReader(new FileReader(path));
            while ((line = br.readLine()) != null) {
                row = line.split(cvsSplitBy);
                int index = Integer.parseInt(row[0]);
                String name = row[1];
                double gamma = Double.parseDouble(row[4]);
                breakpoints.add(gamma);
                double[] vals = new double[T];
                for (int i = 5; i < row.length; i++) {
//                    System.out.println(row[i]);
                    value = row[i].split("_");
                    vals[Integer.parseInt(value[0])] = Double.parseDouble(value[1]);
                }
                Solution sol = new Solution(name, index, gamma, vals);
                sols[index].add(sol);
            }

        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }
        return sols;
    }

    public static void main(String[] args) throws IOException {
//        String data = "./results/vals.csv";
        String data = args[0];
//        String validation = "./data/syntTest.csv";
//        int n = 25;
//        int T = 4;
//        int m = 2000;
        int n = Integer.parseInt(args[3]),
                T = Integer.parseInt(args[4]), m = Integer.parseInt(args[5]);
//        System.out.println(n+" "+T+" "+m);
        ParserReal parser = new ParserReal(n, T, m, data);
        for (Double breakpoint : parser.breakpoints) {
            System.out.println(breakpoint);
        }
        System.out.println("");
        System.out.println("");
//        for (List<Solution> solution : parser.solutions) {
//            for (Solution sol : solution) {
//                System.out.print(sol.name + " " + sol.gamma + "\t");
//                for (double val : sol.vals) {
//                    System.out.print(val + " ");
//                }
//                System.out.println("");
//            }
//        }

        double[][][][] precisions = new double[parser.breakpoints.size()][T][n][n];

        int counterBreak = 0;
        int counterIndex;
        for (Double breakpoint : parser.breakpoints) {
            counterIndex = 0;
            for (int i = 0; i < n; i++) {
                for (int j = i; j < n; j++) {

                    List<Solution> list = parser.solutions[counterIndex];
                    for (int k = 0; k < list.size() - 1; k++) {
//                        if (i == 4 && j == 4) {
//                            System.out.println(i+" "+j+" "+counterBreak + " " + k + " "+breakpoint+" " + list.get(k).gamma+" "+list.get(k + 1).gamma);
//                        }
                        if (list.get(k).gamma >= breakpoint && list.get(k + 1).gamma < breakpoint) {
                            for (int t = 0; t < T; t++) {
                                precisions[counterBreak][t][i][j] = list.get(k).vals[t];
                                precisions[counterBreak][t][j][i] = list.get(k).vals[t];
                            }
                        }

                    }
                    if (list.get(list.size() - 1).gamma >= breakpoint) {
                        for (int t = 0; t < T; t++) {
                            precisions[counterBreak][t][i][j] = list.get(list.size() - 1).vals[t];
                            precisions[counterBreak][t][j][i] = list.get(list.size() - 1).vals[t];

                        }
                    }

                    counterIndex++;
                }
            }

//            System.out.println("Estimates for breakpoint "+breakpoint);
//            for (int t = 0; t < T; t++) {
//                System.out.println("Time period "+t);
//                for (int i = 0; i < n; i++) {
//                    for (int j = 0; j < n; j++) {
//                        System.out.print(precisions[counterBreak][t][i][j]+" ");
//                    }
//                    System.out.println("");
//                }
//                System.out.println("");
//                System.out.println("");
//            }
            counterBreak++;
        }
        int best = doCrossvalidation(precisions, parser.validationData);
        System.out.println("Optimal index " + best);

        double[] metrics = computeMetrics(parser.trueValues, precisions[best]);
        System.out.println("Performance");
        for (double metric : metrics) {
            System.out.print(metric + "\t");
        }
        System.out.println("");
        exportSolution(args, metrics);
//        exportSolution(precisions[best]);

    }

    static double[] computeMetrics(double[][][] trueVals, double[][][] estimatedVals) {
        double epsilon = 1e-7;
        double TP = 0, PP = 0, P = 0,
                TPDiff = 0, PPDiff = 0, PDiff = 0,
                norm = 0, L2 = 0;
//        double L0 = 0, L2 = 0, LDiff = 0, norm = 0;
        for (int t = 0; t < trueVals.length; t++) {
            for (int i = 0; i < trueVals[t].length; i++) {
                for (int j = i; j < trueVals[t][i].length; j++) {
                    norm += trueVals[t][i][j] * trueVals[t][i][j];
                    L2 += (trueVals[t][i][j] - estimatedVals[t][i][j]) * (trueVals[t][i][j] - estimatedVals[t][i][j]);
                    if (Math.abs(estimatedVals[t][i][j]) >= epsilon) {
                        PP++;
                    }
                    if (Math.abs(trueVals[t][i][j]) >= epsilon) {
                        P++;
                        if (Math.abs(estimatedVals[t][i][j]) >= epsilon) {
                            TP++;
                        }
                    }

//                    if (Math.abs(trueVals[t][i][j]) < epsilon && Math.abs(estimatedVals[t][i][j]) >= epsilon) {
//                        L0++;
//                    }
//                    if (Math.abs(trueVals[t][i][j]) >= epsilon && Math.abs(estimatedVals[t][i][j]) < epsilon) {
//                        L0++;
//                    }
                    if (t >= 1) {
                        if (Math.abs(estimatedVals[t][i][j] - estimatedVals[t - 1][i][j]) >= epsilon) {
                            PPDiff++;
                        }
                        if (Math.abs(trueVals[t][i][j] - trueVals[t - 1][i][j]) >= epsilon) {
                            PDiff++;
                            if (Math.abs(estimatedVals[t][i][j] - estimatedVals[t - 1][i][j]) >= epsilon) {
                                TPDiff++;
                            }
                        }
//                        if (Math.abs(trueVals[t][i][j] - trueVals[t - 1][i][j]) < epsilon
//                                && Math.abs(estimatedVals[t][i][j] - estimatedVals[t - 1][i][j]) >= epsilon) {
//                            LDiff++;
//                        }
//                        if (Math.abs(trueVals[t][i][j] - trueVals[t - 1][i][j]) >= epsilon
//                                && Math.abs(estimatedVals[t][i][j] - estimatedVals[t - 1][i][j]) < epsilon) {
//                            LDiff++;
//                        }
                    }
                }

            }
        }
        double precision=TP/PP, recall=TP/P,
                precisionDiff=TPDiff/PPDiff, recallDiff=TPDiff/PDiff;
        
        double fscore=2*precision*recall/(precision+recall),
                fscoreDiff=2*precisionDiff*recallDiff/(precisionDiff+recallDiff);
        
        return new double[]{precision, recall,fscore, precisionDiff,recallDiff,fscoreDiff, L2 / norm};
    }

    static int doCrossvalidation(double[][][][] precisions, List<double[]>[] validationData) {
        int best = -1;
        double bestObj = Double.POSITIVE_INFINITY;
        for (int i = 0; i < precisions.length; i++) {
            double obj = 0;

            for (int t = 0; t < precisions[i].length; t++) {
                RealMatrix precision = new Array2DRowRealMatrix(precisions[i][t]);
//                System.out.println("Precision "+i+"\t"+t);
//                for (int j = 0; j < precisions[i][t].length; j++) {
//                    for (int k = 0; k < precisions[i][t][j].length; k++) {
//                        System.out.print(precisions[i][t][j][k]+" ");
//                    }
//                    System.out.println("");
//                    
//                }
//                System.out.println("4,4"+precisions[i][t][4][4]);
//                System.out.println("");
//                System.out.println("");
                double det = new CholeskyDecomposition(precision).getDeterminant();
                for (int l = 0; l < validationData[t].size(); l++) {
                    double[] point = validationData[t].get(l);
                    for (int j = 0; j < precisions[i][t].length; j++) {
                        for (int k = 0; k < precisions[i][t][j].length; k++) {
                            obj += point[j] * point[k] * precisions[i][t][j][k] + Math.log(Math.sqrt(det));
                        }
                    }
                }
            }
            System.out.println("Breakpoint " + i + " fitness=\t" + obj);
            if (obj < bestObj) {
                best = i;
                bestObj = obj;
            }
        }
        return best;
    }

    static void exportSolution(double[][][] sol) throws IOException {

        try ( FileWriter out = new FileWriter(new File("./results/postCrossEstimates.csv"), false)) {
            int counter = 0;
            for (int i = 0; i < sol[0].length; i++) {
                for (int j = i; j < sol[0][i].length; j++) {
                    out.write(counter + ", " + i + "," + j + ",");
                    for (int t = 0; t < sol.length; t++) {
                        out.write(sol[t][i][j] + ", ");
                    }
                    out.write("\n");

                }
            }

        }
    }

    static void exportSolution(String[] args, double[] vals) throws IOException {

        try ( FileWriter out = new FileWriter(new File("./results/resultsCrossval.csv"), true)) {
            for (int i = 0; i < args.length; i++) {
                out.write(args[i] + ", ");
            }

            for (int i = 0; i < vals.length; i++) {
                out.write(vals[i] + ", ");
            }
            out.write("\n");

        }
    }

}
