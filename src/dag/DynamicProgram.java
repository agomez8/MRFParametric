/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dag;

import com.gurobi.gurobi.GRB;
import com.gurobi.gurobi.GRBEnv;
import com.gurobi.gurobi.GRBException;
import com.gurobi.gurobi.GRBLinExpr;
import com.gurobi.gurobi.GRBModel;
import com.gurobi.gurobi.GRBVar;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import mrfs.MRFs;
import mrfs.backwardsMapping.Instance;

/**
 *
 * @author andre
 */
public class DynamicProgram implements Runnable {

    Instance instance;
    int index;
    String name;
    int n;
    double[] lb, ub;
    int norm;
    int initialIndex;
    double[][] costs;
    double[] valueFunct;
    double[][] sol, solCont;
    double[] heatMap;
    String[] dates;
    double timeDP;
    double timeMIP;

    /**
     * Constructor by parameters. <br>
     *
     * @param backmap Backmapping parameters. <br>
     * @param linfty Max deviation from backward mapping in L infinity norm.
     * <br>
     * @param norm Norm to be used. <br>
     * @param costs Array to store the costs. <br>
     * @param sol Array to store the solution. <br>
     */
    public DynamicProgram(Instance instance, int index,String name, double[] backmap, double linfty, int norm, double[] valueFunct, double[] heatMap, String[] dates) {
        this.instance = instance;
        this.index = index;
        int counter = 0;
        this.name=name;
//        for (int i = 0; i < instance.varNames.length; i++) {
//            for (int j = i; j < instance.varNames.length; j++) {
//                if (counter == index) {
//                    name = instance.varNames[i] + ":" + instance.varNames[j];
//                }
//                counter++;
//            }
//        }
        n = backmap.length;
        lb = new double[n];
        ub = new double[n];
        for (int i = 0; i < n; i++) {
            lb[i] = backmap[i] - linfty;
            ub[i] = backmap[i] + linfty;
        }
        this.norm = norm;
        this.costs = new double[n + 1][n + 1];
        this.valueFunct = valueFunct;
        this.heatMap = heatMap;
        this.dates=dates;
    }

    @Override
    public void run() {
        timeDP= System.currentTimeMillis();
        if (norm == 0) {
            computeL0();
        }
        solveDP(costs);
       
        
//        timeMIP=System.currentTimeMillis();
//        for (int k = 1; k < n; k++) {
//            solveGurobi(k);
//        }
//        timeMIP=(System.currentTimeMillis()-timeMIP)/1000.0;
        
   
    }

    void computeL0() {
        solCont = new double[n][n];

        double cumLB, cumUB, penalty, jump;
        for (int i = 0; i <= n; i++) {
            int lastJump = i;
            if (i == 0) {
                cumLB = Double.NEGATIVE_INFINITY;
                cumUB = Double.POSITIVE_INFINITY;
            } else {
                cumLB = 0;
                cumUB = 0;
            }
            penalty = 0;
            for (int j = 0; j < i; j++) {
                costs[i][j] = Double.NaN;
            }
//            if (index == 95 && i == 0) {
//                System.out.println(i + " " + cumLB + " " + cumUB);
//            }
            for (int j = i + 1; j <= n; j++) {
//                if (index == 95 && i == 0) {
//                    System.out.println(j + " " + lb[j - 1] + " " + ub[j - 1]);
//                    System.out.println("\t" + j + " " + cumLB + " " + cumUB);
//                }

                if (lb[j - 1] > cumUB || ub[j - 1] < cumLB) {
                    for (int k = lastJump; k < j - 1; k++) {
                        if (cumLB <= 0 && cumUB >= 0) {
                            solCont[i][k] = 0;

                        } else {
                            solCont[i][k] = (cumLB + cumUB) / 2.0;
                        }
                    }
                    penalty++;
                    lastJump = j - 1;

                    cumUB = ub[j - 1];
                    cumLB = lb[j - 1];
//                    if (index == 22762 && i == 0) {
//                        System.out.println("Penalty increased");
//                    }
                } else {
                    cumUB = Math.min(cumUB, ub[j - 1]);
                    cumLB = Math.max(cumLB, lb[j - 1]);

                }
//                if (index == 22755 && i == 0) {
//                    System.out.println(j + " " + cumLB + " " + cumUB+"\t"+lb[j-1]+" "+ub[j-1]);
//                }
                jump = (j < n && (cumLB > 0 || cumUB < 0)) ? 1 : 0;
                costs[i][j] = penalty + jump;
//                costs[i][j] = penalty + 2;
//                costs[i][j] = penalty + (j < n ? 1 : 0) + (i > 0 ? 1 : 0);
//                if (index == 95 && i == 0) {
//                    System.out.println(j + " " + costs[i][j]);
//                }
            }
            for (int k = lastJump; k < n; k++) {
                if (cumLB <= 0 && cumUB >= 0) {
                    solCont[i][k] = 0;

                } else {
                    solCont[i][k] = (cumLB + cumUB) / 2.0;
                }
            }
        }

//        if (index == 95) {
//            System.out.println("f=");
//            for (int i = 0; i < costs.length; i++) {
//                System.out.print(i + "-,");
//                for (int j = 0; j < costs[i].length; j++) {
//                    System.out.print(costs[i][j] + ",");
//                }
//                System.out.println("");
//            }
//            System.out.println("");
//            System.out.println("");
//            System.out.println("sol=");
//            for (int i = 0; i < solCont.length; i++) {
//                System.out.print(i + "-,");
//                for (int j = 0; j < solCont[i].length; j++) {
//                    System.out.print(solCont[i][j] + ",");
//                }
//                System.out.println("");
//            }
//
//        }
    }
    
    

    void solveDP(double[][] f) {
        sol = new double[n + 1][n];
        int[][] prevZero = new int[n + 1][n + 1];
        int[][] prevCard = new int[n + 1][n + 1];
        for (int b = 0; b < n + 1; b++) {
            for (int k = 0; k < n + 1; k++) {
                prevZero[b][k] = -2;
                prevCard[b][k] = -2;
            }
        }

        double[][] nu = new double[n + 1][n + 1];
        if (lb[0] > 0 || ub[0] < 0) {

            for (int k = 0; k <= n; k++) {
                nu[0][k] = Double.POSITIVE_INFINITY;
            }
        }
        for (int b = 1; b <= n; b++) {
            for (int k = 0; k <= n; k++) {
                if (b < n && (lb[b] > 0 || ub[b] < 0)) {
//                    System.out.println("Infinity ("+b+","+k+") with \t"+lb[b]+" -> "+ub[b]);
                    nu[b][k] = Double.POSITIVE_INFINITY;
//                    sou
                } else if (k >= b) {
                    nu[b][k] = f[0][b];
                    prevZero[b][k] = -1;
                    prevCard[b][k] = -1;
                } else {
                    double min = Double.POSITIVE_INFINITY;
                    for (int t = 0; t <= b - 1; t++) {
                        if (lb[t] <= 0 && ub[t] >= 0) {

                            int cap = k - (b - t - 1);
                            double val;
                            if (cap >= 0) {

                                val = nu[t][cap] + f[t + 1][b];
                                if (val < min) {
                                    min = val;
                                    prevZero[b][k] = t;
                                    prevCard[b][k] = cap;
                                }
                            }
                        }
                    }
                    nu[b][k] = min;
                }
            }
        }


        int prev, current, cardinality;
        for (int i = 0; i <= n; i++) {
            valueFunct[i] = nu[n][i];
            if (Double.isInfinite(valueFunct[i])) {
                for (int j = 0; j < sol[i].length; j++) {
                    sol[i][j] = Double.NaN;
                }
            } else {
                current = n;
                cardinality = i;
                prev = prevZero[current][cardinality];

                while (prev >= -1) {
                    for (int j = prev + 1; j < current; j++) {
//                        System.out.println(prev+" "+current);
                        sol[i][j] = solCont[prev + 1][j];
                    }

                    cardinality = prevCard[current][cardinality];
                    current = prev;
                    if (current >= 0) {
                        prev = prevZero[current][cardinality];
                    } else {
                        prev = -2;
                    }
                }

                if (norm == 0 && i == n) {
//                if (norm == 0) {
                    for (int j = 1; j < sol[i].length; j++) {
                        heatMap[j] += Math.abs(sol[i][j] - sol[i][j - 1]) > 1e-5 ? 1 : 0;
                    }
//                    for (int j = 0; j < sol[i].length; j++) {
//                        heatMap[j] += Math.abs(sol[i][j]) > 1e-5 ? 1 : 0;
//                    }
                }

            }

        }
         timeDP=(System.currentTimeMillis()-timeDP)/1000.0;
        try {
            exportSolution(sol);
        } catch (IOException ex) {
            System.out.println("Error exporting the solutions");
            System.out.println(ex);
        }
//        System.out.println("Solutions");
//
//        System.out.print(index + " ");
//        for (int j = 0; j < sol[n].length; j++) {
//            System.out.print(sol[n][j] + " ");
//        }
    }
    
    void solveGurobi(int k)
    {
        try {
           
            // ---------------------------------
            // Gurobi environment & model
            // ---------------------------------
            GRBEnv env = new GRBEnv();
            GRBModel model = new GRBModel(env);
            
            model.set(GRB.IntParam.LogToConsole, 0);


            // ---------------------------------
            // Variables
            // ---------------------------------
            // theta_t : continuous, t = 0,...,T
            GRBVar[] theta = new GRBVar[n];

            // z_t : binary indicator for theta_t != 0
            GRBVar[] z = new GRBVar[n];

            // w_t : binary indicator for theta_t != theta_{t-1}, t = 1,...,T
            GRBVar[] w = new GRBVar[n]; // w[0] unused
           

            for (int t = 0; t < n; t++) {
                theta[t] = model.addVar(
                        lb[t],           // lower bound
                        ub[t],           // upper bound
                        0.0,            // objective coefficient
                        GRB.CONTINUOUS,
                        "theta_" + t);

                z[t] = model.addVar(
                        0.0, 1.0,
                        0.0,
                        GRB.BINARY,
                        "z_" + t);
            }

            for (int t = 1; t < n; t++) {
                w[t] = model.addVar(
                        0.0, 1.0,
                        0.0,
                        GRB.BINARY,
                        "w_" + t);
            }



            for (int t = 0; t <n; t++) {
                boolean zeroInInterval = (lb[t] <= 0.0 && 0.0 <= ub[t]);

                if (zeroInInterval) {
                   
                    // theta_t <= M * z_t
                    GRBLinExpr c1 = new GRBLinExpr();
                    c1.addTerm(1.0, theta[t]);
                    c1.addTerm(-ub[t], z[t]);
                    model.addConstr(c1, GRB.LESS_EQUAL, 0.0, "link_pos_" + t);

                    // theta_t >= -M * z_t  <=> -theta_t <= M * z_t
                    GRBLinExpr c2 = new GRBLinExpr();
                    c2.addTerm(-1.0, theta[t]);
                    c2.addTerm(lb[t], z[t]);
                    model.addConstr(c2, GRB.LESS_EQUAL, 0.0, "link_neg_" + t);

                } else {
                    // 0 is not feasible for theta_t => indicator is 1
                    model.addConstr(z[t], GRB.EQUAL, 1.0, "force_nonzero_" + t);
                }
            }

            // ---------------------------------
            // Cardinality constraint: sum z_t <= k
            // ---------------------------------
            GRBLinExpr cardExpr = new GRBLinExpr();
            for (int t = 0; t < n; t++) {
                cardExpr.addTerm(1.0, z[t]);
            }
            model.addConstr(cardExpr, GRB.LESS_EQUAL, k, "cardinality");

 
            for (int t = 1; t < n; t++) {
                // Big-M for difference between theta_t and theta_{t-1}
                double Mdiff1 = Math.abs(ub[t] - lb[t - 1]);
                double Mdiff2 = Math.abs(lb[t] - ub[t - 1]);
                double M = Math.max(Mdiff1, Mdiff2);

                // theta_t - theta_{t-1} <= M * w_t
                GRBLinExpr cPos = new GRBLinExpr();
                cPos.addTerm(1.0, theta[t]);
                cPos.addTerm(-1.0, theta[t - 1]);
                cPos.addTerm(-M, w[t]);
                model.addConstr(cPos, GRB.LESS_EQUAL, 0.0, "chg_pos_" + t);

                // -(theta_t - theta_{t-1}) <= M * w_t
                // => -theta_t + theta_{t-1} <= M * w_t
                GRBLinExpr cNeg = new GRBLinExpr();
                cNeg.addTerm(-1.0, theta[t]);
                cNeg.addTerm(1.0, theta[t - 1]);
                cNeg.addTerm(-M, w[t]);
                model.addConstr(cNeg, GRB.LESS_EQUAL, 0.0, "chg_neg_" + t);
            }

            // ---------------------------------
            // Objective: min sum_{t=1}^T w_t  (q = 0)
            // ---------------------------------
            GRBLinExpr obj = new GRBLinExpr();
            for (int t = 1; t < n; t++) {
                obj.addTerm(1.0, w[t]);
            }
            model.setObjective(obj, GRB.MINIMIZE);

            // ---------------------------------
            // Optimize
            // ---------------------------------
            model.optimize();

            // ---------------------------------
            // Print solution
            // ---------------------------------
//            int status = model.get(GRB.IntAttr.Status);
//            if (status == GRB.OPTIMAL) {
//                System.out.println("Optimal objective (number of change points): " +
//                        model.get(GRB.DoubleAttr.ObjVal));
//
////                for (int t = 0; t <= T; t++) {
////                    double thetaVal = theta[t].get(GRB.DoubleAttr.X);
////                    int zVal = (int) Math.round(z[t].get(GRB.DoubleAttr.X));
////                    System.out.printf("t = %2d : theta = %8.4f, z = %d", t, thetaVal, zVal);
////                    if (t > 0) {
////                        int wVal = (int) Math.round(w[t].get(GRB.DoubleAttr.X));
////                        System.out.printf(", w = %d", wVal);
////                    }
////                    System.out.println();
////                }
//            } else {
//                System.out.println("Optimization ended with status: " + status);
//            }

            // ---------------------------------
            // Cleanup
            // ---------------------------------
            model.dispose();
            env.dispose();

        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
        }
    }

    void exportSolution(double[][] sol) throws IOException {
        try ( FileWriter out = new FileWriter(new File("./results/"+MRFs.name+"_vals.csv"), true)) {

            double prevCard = -1, prevSmooth = -1, gamma;
            for (int i = 0; i < sol.length; i++) {
                if (!Double.isNaN(sol[i][0])) {

                    if (i == 0) {
                        out.write(index + "," + name + ",0" + "," + valueFunct[i] + "," + Double.POSITIVE_INFINITY);
                        prevCard = 0;
                        prevSmooth = valueFunct[i];
                        out.write("\n");
                    } else {
                        if (Double.isNaN(sol[i - 1][0]) || (valueFunct[i] < valueFunct[i - 1])) {
                            if (prevCard < 0) {
                                gamma = Double.POSITIVE_INFINITY;
                            } else {
                                gamma = (prevSmooth - valueFunct[i]) / (double) (i - prevCard);
                            }
                            prevSmooth = valueFunct[i];
                            prevCard = i;
                            out.write(index + "," + name + "," + i + ", " + valueFunct[i] + ","+gamma+",");
                            for (int j = 0; j < sol[i].length; j++) {

                                if (sol[i][j] != 0) {
                                    out.write(j + "_" + sol[i][j] + ",");
                                }
                            }
                            out.write("\n");
                        }

                    }
                }
            }

        }
    }

}
