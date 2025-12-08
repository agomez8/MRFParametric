/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mrfs;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author andre
 */
public class SolutionPath {

    double[][] sol;
    Point[] current, next;
    int totalSparse;
    double totalLq;
    List<Point> breakpoints;

    /**
     * Constructor by parameters. <br>
     *
     * @param sol Current solution.
     */
    public SolutionPath(double[][] sol) {
        this.sol = sol;
        init();
        buildPath();
    }

    void init() {
        current = new Point[sol.length];
        next = new Point[sol.length];
        totalSparse = 0;
        totalLq = 0;
        for (int i = 0; i < sol.length; i++) {
            int j = 0;
            while (Double.isInfinite(sol[i][j])) {
                j++;
            }
            current[i] = new Point(0, j, sol[i][j]);
            totalSparse += j;
            totalLq += sol[i][j];
            next[i] = findNext(i);
        }
        breakpoints=new ArrayList<>();
        breakpoints.add(new Point(Double.POSITIVE_INFINITY, totalSparse, totalLq, -1));
    }

    boolean addBreakpoint() {
        int pos = -1;
        boolean find = false;
        double val= Double.NEGATIVE_INFINITY;
        for (int i = 0; i < next.length; i++) {
            if (next[i] != null) {
                find = true;
                if(next[i].reg>val)
                {
                    val=next[i].reg;
                    pos=i;
                }
            }
        }
        
        if(find)
        {
            totalLq+=next[pos].lq-current[pos].lq;
            totalSparse+=next[pos].sparse-current[pos].sparse;
            breakpoints.add(new Point(next[pos].reg, totalSparse, totalLq, pos));
            current[pos]=new Point(next[pos].reg, next[pos].sparse, next[pos].lq);
            next[pos]=findNext(pos);
        }

        return find;
    }

    Point findNext(int i) {
        Point next = null;
        for (int k = current[i].sparse + 1; k < sol[i].length; k++) {
            if (sol[i][k] < current[i].lq) {
                double newReg = (current[i].lq - sol[i][k]) / (double) (k - current[i].sparse);
                if (next == null) {
                    next = new Point(newReg, k, sol[i][k]);
                } else if (newReg >= next.reg) {
                    next = new Point(newReg, k, sol[i][k]);
                }
            }
        }
        return next;
    }

    private void buildPath() {
        boolean found=true;
        while(found)
        {
            found=addBreakpoint();
        }
        
    }
    
//    private void postProcessing()
//    {
//        int iter=0;
//        while(iter<breakpoints.size()-1)
//        {
//            if(breakpoints.get(iter).reg==breakpoints.get(iter).reg)
//        }
//    }

    class Point implements Comparable<Point> {

        double reg; // Value of reg parameter associated with breakpoint
        double lq; // Lq penalty
        int sparse; //Sparsity
        int param=-1;

        /**
         * Constructor by parameters. <br>
         *
         * @param reg Value of reg parameter associated with breakpoint. <br>
         * @param sparse Sparsity. <br>
         * @param lq Lq penalty. <br>
         */
        public Point(double reg, int sparse, double lq) {
            this.reg = reg;
            this.lq = lq;
            this.sparse = sparse;
        }
        
        /**
         * Constructor by parameters. <br>
         *
         * @param reg Value of reg parameter associated with breakpoint. <br>
         * @param sparse Sparsity. <br>
         * @param lq Lq penalty. <br>
         * @param param Position corresponding to this breakpoint. 
         */
        public Point(double reg, int sparse, double lq, int param) {
            this.reg = reg;
            this.lq = lq;
            this.sparse = sparse;
            this.param=param;
        }

        @Override
        public int compareTo(Point o) {
            return Double.compare(reg, o.reg);
        }

    }
}
