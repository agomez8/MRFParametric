[![INFORMS Journal on Computing Logo](https://INFORMSJoC.github.io/logos/INFORMS_Journal_on_Computing_Header.jpg)](https://pubsonline.informs.org/journal/ijoc)

# MRFparametric


This archive is distributed in association with the [INFORMS Journal on
Computing](https://pubsonline.informs.org/journal/ijoc) under the [MIT License](LICENSE).

The software and data in this repository are a snapshot of the software and data
that were used in the research reported on in the paper 
[Solution Path of Time-varying Markov Random
Fields with Discrete Regularization](https://doi.org/10.1287/ijoc.2024.0850) by Salar Fattahi and Andres Gomez. 


**Important: This code is being developed on an on-going basis at 
https://github.com/agomez8/MRFParametric. Please go there if you would like to
get a more recent version or would like support**

## Cite

To cite the contents of this repository, please cite both the paper and this repo, using their respective DOIs.

https://doi.org/10.1287/ijoc.2024.0850

https://doi.org/10.1287/ijoc.2024.0850.cd

Below is the BibTex for citing this snapshot of the repository.

```
@misc{MRFParametric,
  author =        {Salar Fattahi and Andres Gomez},
  publisher =     {INFORMS Journal on Computing},
  title =         {{Solution Path of Time-varying Markov Random
Fields with Discrete Regularization}},
  year =          {2026},
  doi =           {10.1287/ijoc.2024.0850.cd},
  url =           {https://github.com/INFORMSJoC/2024.0850},
  note =          {Available for download at https://github.com/INFORMSJoC/2024.0850},
}  
```

## Description

The goal of this software is to demonstrate the use of dynamic programming coupled with backwards mappings to infer sparse time-varying Markov random fields (MRFs).

The methods are implemented in Java.

## Executing the code

As a java code, the source code is precompiled and can be executed directly via file ./dist/MRFs.jar. 

The code can be executed from the console. Files "runCrossval.bat" and "runReal.bat" contain example of how to execute the code to solve synthetic and real instances. 

To run crossvalidation, two calls to the software. The first one runs the proposed algorithm itself sing backwards mapping and dynamic programming to compute the entire solution path and prints the solutions to file. The second call simple reconstructs the solutions, uses cross-validation to find the best hyperparameter and determine the statistical properties. An example command to execute the code to tackle a synthetic instance is
```
java  -cp ./dist/MRFs.jar mrfs.MRFs ./data/synt_50_100_10_101.csv 100 1.0 0.0 0.2 true

java  -cp ./dist/MRFs.jar parse_sol.Parser ./results/synt_50_100_10_101_vals.csv ./data/syntTest_50_100_10_101.csv ./data/syntTrue_50_100_10_101.csv 50 10 1000 1.0 0.0 0.2
```
where: "java  -cp ./dist/MRFs.jar" points to the direction of the executable jar file, "mrfs.MRFs" is the class used to run the algorithm, and "parse_sol.Parser" reconstructs the solution and performs cross-validation. 

The rest of parameters in the first call to the code (mrfs.MRFs) are as follows:
* First parameter (/data/synt_50_100_10_101.csv) is the path to the file containing the training data.
* Second parameter (100) is the number of datapoints per time period.
* Third parameter (1.0) is the Kernel averaging parameter (h_0 in the paper). It is not used for GMRFs.
* Fourth parameter (0.0) is the parameter for shrinkage (nu in the paper). 
* Fifth parameter (0.2) the the parameter controlling the radius of the l_\infty ball (lambda_0 in the paper).
* Sixth parameter controls whether to use GMRF (true) or a DMRF (false). Set of true by default.

The rest of parameters in the second call to the code (parse_sol.Parser) are as follows:
* First parameter (/data/synt_50_100_10_101.csv) is the path to the file containing the training data.
* Second parameter (/data/syntTest_50_100_10_101.csv) is the path to the file containing the validation data.
* Third parameter (/data/syntTrue_50_100_10_101.csv) is the path to the file containing the ground truth.
* Fourth parameter (50) is the number of random variables.
* Fifth parameter (10) is the number of time periods.
* Sixth parameter (1000) is the total number of datapoints.
* Seventh parameter (1.0) is the Kernel averaging parameter. 
* Eigth parameter (0.0) is the shrinkage parameter.
* Ninth parameter (0.2) is the radius of the l_infty ball.

An example command to execute the code to tackle a real instance is
```
java  -cp ./dist/MRFs.jar mrfs.MRFs ./data/daily_data_1990.csv 60 0.02 0.0 0.005 false
```
where: "java  -cp ./dist/MRFs.jar" points to the direction of the executable jar file, and "mrfs.MRFs" is the class used to run real instances. Note that in this setting we do not perform cross-validation, hence no second call to the code is needed.
The rest of parameters are as follows:
* First parameter (./data/daily_data_1990.csv) is the path to the dataset in csv format.
* Second parameter(60) is the number of datapoints per time period (N_t in the paper).
* Third parameter (0.02) is the Kernel averaging parameter (h_0 in the paper).
* Fourth parameter (0.0) is the parameter for shrinkage (nu in the paper). It is not used for DMRFs.
* Fifth parameter (0.005) the the parameter controlling the radius of the l_\infty ball (lambda_0 in the paper).
* Sixth parameter controls whether to use GMRF (true) or a DMRF (false). Set to false by default.

## Output

After solving an instance, the results are recorded in folder "./results". Several files are created at each run, which we now describe.

File "time.csv" records the computational times. Each time a new instance is solved, a new row is added at the bottom of this file. The file is organized as follows:
* Columns 1-6:  are the parameters used to generate the instance.
* Column 7: time to compute the backwards mapping in seconds.
* Column 8: time to run the dynamic program in seconds.
Note that the total execution time of the software is longer, since it also prints the solutions to a CSV file. This printing time, which accounts for the larger portion of the total execution time, is not included in the times reported above. 

Each time an instance with name "instName.csv" is run, a file named "instName_vals.csv" is created storing the actual solutions of the entire solution path. Each row corresponds to a different (theta,gamma) combination, where theta is the parameter estimated and gamma is the hyperparameter controlling sparsity. Each row is organized as follows:
* Columns 1:  unique ID of theta.
* Column 2: a string description of theta.
* Column 3: number of nonzeros for a given value of sparsity parameter gamma.
* Column 4: number of jumps in consecutive time periods for a given value of sparsity parameter gamma.
* Column 5: maximum value of gamma for which the solution is optimal.
* Column 6+: the actual solution, stored in sparse format. Each column contains an element in format "t_val" where t is the time period and theta_t=val in the optimal solution. If a time period t is missing, then theta_t=0 in the associated optimal solution.

Each time an instance with name "instName.csv" is run, a file named "instName_path.csv" is created storing the overall solution path (i.e., objective function of the complete problem as a function of gamma). Each row is organized as follows:
* Column 1: a value of gamma corresponding to a breakpoint in the solution path.
* Column 2: total number of nonzero parameters for this value of gamma.
* Column 3: total number of jumps for this value of gamma.
* Column 4: a parameter ID t corresponding to the breakpoint, i.e., the optimal solutions associated with this parameter are different for regularization values gamma+epsilon and gamma-epsilon. Note that several parameters can correspond to a single breakpoint, and this value just points to one of them. 

Each time an instance with name "instName.csv" is run, a file named "instName_results.csv" is created storing, for each parameter, the minimum number of jumps as a function of the sparsity. Each row is organized as follows:
* Column 1: unique ID of theta.
* Column i>2: minimum number of jumps in the subproblem associated with theta_ID when the maximum sparsity allowed is i-1. Value can be infinity if the problem is infeasible.

Each time an instance with name "instName.csv" is run, a file named "instName_path.csv" is created storing the overall solution path (i.e., objective function of the complete problem as a function of gamma). Each row is organized as follows:
* Column 1: a value of gamma corresponding to a breakpoint in the solution path.
* Column 2: total number of nonzero parameters for this value of gamma.
* Column 3: total number of jumps for this value of gamma.
* Column 4: a parameter ID t corresponding to the breakpoint, i.e., the optimal solutions associated with this parameter are different for regularization values gamma+epsilon and gamma-epsilon. Note that several parameters can correspond to a single breakpoint, and this value just points to one of them. 

Finally, if crossvalidation is done by calling "parse_sol.Parser", a row is added to file "resultsCrossval.csv". Each row of the file is organized as follows:
* Columns 1-8: parameters used to generate the instance.
* Column 9: sparsity precision of the result with the best parameter identified by cross-validation.
* Column 10: sparsity recall of the result with the best parameter identified by cross-validation.
* Column 11: sparsity F1-sccore of the result with the best parameter identified by cross-validation.
* Column 12: jumps precision of the result with the best parameter identified by cross-validation.
* Column 13: jumps recall of the result with the best parameter identified by cross-validation.
* Column 14: jumps F1-sccore of the result with the best parameter identified by cross-validation.
* Column 15: relative L2 difference between the best solution identified by cross-validation and the ground truth.







## Replicating

To replicate the results with synthetic data, use file runCrossval.bat (on a Windows machine).

To replicate the results with real data, use file runReal.bat (on a Windows machine).

## Source code
The source code can be found in the src folder.


