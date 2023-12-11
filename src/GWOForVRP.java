package src;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

class GWOForVRP {

    private static final String CSV_FILE_PATH = "C101.txt";

    private static class Point {
        double x;
        double y;

        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private static Point[] readPointsFromCSV(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        int lineNumber = 0;
        int dimensions = 2; // Assuming X and Y coordinates in the CSV

        // Count the number of lines in the file
        long lineCount = reader.lines().count();

        // Reset the reader to read from the beginning
        reader = new BufferedReader(new FileReader(filePath));

        Point[] points = new Point[(int) lineCount];

        while ((line = reader.readLine()) != null) {
            String[] values = line.split(",");
            if (values.length == dimensions) {
                double x = Double.parseDouble(values[1]);
                double y = Double.parseDouble(values[2]);
                points[lineNumber++] = new Point(x, y);
            }
        }
        System.out.println();
        reader.close();

        return points;
    }
    // will return an array which is locally named points

    private static double calculateDistance(Point point1, Point point2) {
        // Simple Euclidean distance formula
        return Math.sqrt(Math.pow(point1.x - point2.x, 2) + Math.pow(point1.y - point2.y, 2));
    }

    private static double calculateTotalDistance(Point[] solution) {
        double totalDistance = 0;
        for (int i = 0; i < solution.length - 1; i++) {
            totalDistance += calculateDistance(solution[i], solution[i + 1]);
        }
        return totalDistance;
    }

    private static double[] greyWolfOptimizer(Point[] points, int populationSize, int maxIterations) {
        

        // Placeholder for demonstration
        double[] bestSolution = new double[points.length];
        Random random = new Random();

        for (int i = 0; i < points.length; i++) {
            bestSolution[i] = random.nextDouble();
        }

        return bestSolution;
    }

    public static void main(String[] args) {
        try {
            Point[] points = readPointsFromCSV(CSV_FILE_PATH);

            int populationSize = 50;
            int maxIterations = 100;

            double[] solution = greyWolfOptimizer(points, populationSize, maxIterations);

            System.out.println("Best solution (route): " + Arrays.toString(solution));
            System.out.println("Total distance: " + calculateTotalDistance(points));
        } catch (IOException e) {
            System.out.println("caught");;
        }
    }
}