import java.util.Arrays;

public class HybridPSOGWO {

    private final double w;
    private final double c1;
    private final double c2;
    private final int numParticles;
    private final int numDimensions;
    private final int numWolves;
    private final int maxIterations;
    private final ObjectiveFunction objectiveFunction;


    public HybridPSO_GWO hybridPSO_GWO(double w, double c1, double c2, int numParticles, int numDimensions, int numWolves, int maxIterations, ObjectiveFunction objectiveFunction) {
        this.w = w;
        this.c1 = c1;
        this.c2 = c2;
        this.numParticles = numParticles;
        this.numDimensions = numDimensions;
        this.numWolves = numWolves;
        this.maxIterations = maxIterations;
        this.objectiveFunction = objectiveFunction;

        return gwo;
    }

    public double[] hybridPSOGWO() {
        // Initialize particles and wolves
        double[][] particlesPosition = new double[numParticles][numDimensions];
        double[][] particlesVelocity = new double[numParticles][numDimensions];
        double[][] pBest = new double[numParticles][numDimensions];
        double[][] wolvesPosition = new double[numWolves][numDimensions];

        // Initialize best solution
        double[] bestSolution = new double[numDimensions];
        double bestFitness = Double.POSITIVE_INFINITY;

        // Main loop
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            // PSO update
            psoUpdate(particlesPosition, particlesVelocity, pBest);

            // GWO update
            gwoUpdate(wolvesPosition);

            // Hybridization step
            for (int i = 0; i < numParticles; i++) {
                int selectedWolf = (int) (Math.random() * numWolves);

                double[] updatedPosition = new double[numDimensions];
                for (int j = 0; j < numDimensions; j++) {
                    updatedPosition[j] = w * particlesPosition[i][j] +
                            c1 * Math.random() * (pBest[i][j] - particlesPosition[i][j]) +
                            c2 * Math.random() * (wolvesPosition[selectedWolf][j] - particlesPosition[i][j]);
                }

                double currentFitness = objectiveFunction.evaluate(updatedPosition);

                // Update p_best
                if (currentFitness < objectiveFunction.evaluate(pBest[i])) {
                    pBest[i] = updatedPosition.clone();
                }

                // Update best_solution
                if (currentFitness < bestFitness) {
                    bestSolution = updatedPosition.clone();
                    bestFitness = currentFitness;
                }
            }
        }

        return bestSolution;
    }

    private void psoUpdate(double[][] particlesPosition, double[][] particlesVelocity, double[][] pBest) {
        for (int i = 0; i < numParticles; i++) {
            for (int j = 0; j < numDimensions; j++) {
                double r1 = Math.random();
                double r2 = Math.random();

                // Update velocity
                particlesVelocity[i][j] = w * particlesVelocity[i][j] +
                        c1 * r1 * (pBest[i][j] - particlesPosition[i][j]) +
                        c2 * r2 * (wolvesPosition[selectedWolf][j] - particlesPosition[i][j]);

                // Update position
                particlesPosition[i][j] += particlesVelocity[i][j];
            }
        }
    }

    private void gwoUpdate(double[][] wolvesPosition) {
        // Alpha, beta, and delta wolves
        double[] alphaPosition = findBestWolfPosition(wolvesPosition);
        double[] betaPosition = findSecondBestWolfPosition(wolvesPosition);
        double[] deltaPosition = findThirdBestWolfPosition(wolvesPosition);
    
        // Update remaining wolves
        for (int i = 3; i < numWolves; i++) {
            for (int j = 0; j < numDimensions; j++) {
                double a1 = 2 * Math.random() - 1;
                double a2 = 2 * Math.random() - 1;
                double a3 = 2 * Math.random() - 1;
    
                double c1 = 2 * Math.random();
                double c2 = 2 * Math.random();
                double c3 = 2 * Math.random();
    
                double dAlpha = Math.abs(c1 * alphaPosition[j] - wolvesPosition[i][j]);
                double dBeta = Math.abs(c2 * betaPosition[j] - wolvesPosition[i][j]);
                double dDelta = Math.abs(c3 * deltaPosition[j] - wolvesPosition[i][j]);
    
                double A1 = Math.cos(2 * Math.PI * a1);
                double A2 = Math.cos(2 * Math.PI * a2);
                double A3 = Math.cos(2 * Math.PI * a3);
    
                wolvesPosition[i][j] = wolvesPosition[i][j] + (A1 * dAlpha + A2 * dBeta + A3 * dDelta) / 3;
            }
        }
    }
    
    private double[] findBestWolfPosition(double[][] wolvesPosition) {
        double minFitness = Double.POSITIVE_INFINITY;
        int bestIndex = -1;
    
        for (int i = 0; i < numWolves; i++) {
            double fitness = objectiveFunction.evaluate(wolvesPosition[i]);
            if (fitness < minFitness) {
                minFitness = fitness;
                bestIndex = i;
            }
        }
    
        return wolvesPosition[bestIndex].clone();
    }
    
    private double[] findSecondBestWolfPosition(double[][] wolvesPosition) {
        // Find best wolf
        double[] bestPosition = findBestWolfPosition(wolvesPosition);
    
        // Find second best wolf
        double minFitness = Double.POSITIVE_INFINITY;
        int secondBestIndex = -1;
    
        for (int i = 0; i < numWolves; i++) {
            if (i != bestIndex) {
                double fitness = objectiveFunction.evaluate(wolvesPosition[i]);
                if (fitness < minFitness) {
                    minFitness = fitness;
                    secondBestIndex = i;
                }
            }
        }
    
        return wolvesPosition[secondBestIndex].clone();
    }
    
    private double[] findThirdBestWolfPosition(double[][] wolvesPosition) {
        // Find best and second best wolf
        double[] bestPosition = findBestWolfPosition(wolvesPosition);
        double[] secondBestPosition = findSecondBestWolfPosition(wolvesPosition);
    
        // Find third best wolf
        double minFitness = Double.POSITIVE_INFINITY;
        int thirdBestIndex = -1;
    
        for (int i = 0; i < numWolves; i++) {
            if (i != bestIndex && i != secondBestIndex) {
                double fitness = objectiveFunction.evaluate(wolvesPosition[i]);
                if (fitness < minFitness) {
                    minFitness = fitness;
                    thirdBestIndex = i;
                }
            }
        }
    
        return wolvesPosition[thirdBestIndex].clone();
    }

    public static void main(String[] args) {
    // Define parameters
    double w = 0.5;
    double c1 = 1.5;
    double c2 = 1.5;
    int numParticles = 50;
    int numDimensions = 10;
    int numWolves = 10;
    int maxIterations = 100;

    // Create an instance of your VRP objective function
    ObjectiveFunction objectiveFunction = new MyVRPOjectiveFunction();

    // Instantiate the HybridPSO_GWO class
    HybridPSOGWO gwo = new HybridPSOGWO(w, c1, c2, numParticles, numDimensions, numWolves, maxIterations, objectiveFunction);
    
    // Run the hybrid algorithm and obtain the best solution
    double[] bestSolution = gwo.hybridPSOGWO();

    // Print the best solution
    System.out.println("Best solution:");
    for (double value : bestSolution) {
        System.out.print(value + " ");
    }
    System.out.println();
}
    
}

// Example usage
// public class Main {

//     public static void main(String[] args) {
//         // Parameters
//         double w = 0.5;
//         double c1 = 0.8;
//         double c2 = 0.9;
//         int numParticles = 50;
//         int numDimensions = 30;
//         int numWolves = 50;
//         int maxIterations = 1000;

//         // Objective function
//         ObjectiveFunction objectiveFunction = new ObjectiveFunction() {
//             @Override
//             public double evaluate(double[] solution) {
//                 double sum = 0;
//                 for (double x : solution) {
//                     sum += x * x;
//                 }
//                 return sum;
//             }
//         };

//         // Run algorithm
//         HybridPSOGWO hybridPSOGWO = new HybridPSOGWO(w, c1, c2, numParticles, numDimensions, numWolves, maxIterations, objectiveFunction);
//         double[] bestSolution = hybridPSOGWO.hybridPSOGWO();

//         // Print results
//         System.out.println("Best solution (vector): " + Arrays.toString(bestSolution));
//         System.out.println("Best solution (fitness): " + objectiveFunction.evaluate(bestSolution));
//     }

// }
