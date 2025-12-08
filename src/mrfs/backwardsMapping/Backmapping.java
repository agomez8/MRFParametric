/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mrfs.backwardsMapping;

/**
 *
 * @author andre
 */
public abstract class Backmapping implements Runnable{
    Instance instance;
    int date;
    double[] backmap; // Array to store the solution

    /**
     * Constructor by parameters. <br>
     * @param instance Instance. <br>
     * @param date Time period for backmap.<br>
     * @param backmap Array to store the backmap
     */
    public Backmapping(Instance instance, int date, double[] backmap) {
        this.instance = instance;
        this.date = date;
        this.backmap=backmap;
    }
    
    
}
