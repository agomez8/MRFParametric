/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package mrfs.backwardsMapping;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 *
 * @author andre
 */
public class Instance {

    public double[][] data; //rows=timePeriods, columns=variables
    public String[] dates, varNames;

    /**
     * Creates an instances from a CVS file. <br>
     * @param path Path to file.
     */
    public Instance(String path) {
        readData(path);
    }
    
    

    /**
     * Reads the data from file. <br>
     *
     * @param path Path to file
     */
    private void readData(String path) {
        BufferedReader br = null;
        String cvsSplitBy = ",";
        String line;
        String[] row;

        try {
            br = new BufferedReader(new FileReader(path));
            row = br.readLine().split(cvsSplitBy); // First line contains headers.
            dates = new String[row.length - 2];
            for (int i = 2; i < row.length; i++) {
                dates[i - 2] = row[i];
            }
            List<String> varsList = new ArrayList<>();
            List<double[]> series = new ArrayList<>();
            int counter=0;
//            while ((line = br.readLine()) != null && counter<2) {
            while ((line = br.readLine()) != null ) {
                row = line.split(cvsSplitBy);
                String var = row[1];
                double[] serie = new double[row.length - 2];
                for (int i = 2; i < row.length; i++) {
                    serie[i - 2] = Double.parseDouble(row[i]);
                }
                varsList.add(var);
                series.add(serie);
                counter++;
            }
            
            data= new double[dates.length][series.size()];
           
            for (int i = 0; i < dates.length; i++) {
                for (int j = 0; j < series.size(); j++) {
                    data[i][j]=series.get(j)[i];                    
                }  
            }
            
             varNames= new String[varsList.size()];
             for (int i = 0; i < varNames.length; i++) {
                varNames[i]=varsList.get(i);
                
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
    }
    
    
   
    

}
