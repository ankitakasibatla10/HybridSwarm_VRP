
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class PreprocessDataMera{


    public static double[][] getdistanceMatrix(String filePath){
        Map<Integer, int[]> customerData = parseCustomerData(filePath);
        int msize = customerData.size();
        double[][] distanceMatrix = new double[msize][msize];
        distanceMatrix = createDistanceMatrix(customerData);
        return distanceMatrix;
    }


    public static Map<Integer, int[]> getCustomerData(String filePath){
        Map<Integer, int[]> customerData = parseCustomerData(filePath);

        //traverse through this customerData and print
        return customerData;
    }

    public static double[][] createDistanceMatrix(Map<Integer, int[]> customerData) {
        int size = customerData.size();
        double[][] distanceMatrix = new double[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i != j) {
                    int[] coord1 = customerData.get(i);
                    int[] coord2 = customerData.get(j);
                    distanceMatrix[i][j] = computeDistance(coord1, coord2);
                }
            }
        }

        return distanceMatrix;
    }
    
    public static double computeDistance(int[] coord1, int[] coord2) {
        double dx = coord2[0] - coord1[0];
        double dy = coord2[1] - coord1[1];
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static Map<Integer, int[]> parseCustomerData(String filePath) { //create a map of customer data
        Map<Integer, int[]> customerData = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isCustomerSection = false;

            while ((line = reader.readLine()) != null) {
                if (line.trim().equals("CUSTOMER")) {
                    isCustomerSection = true;
                    reader.readLine();  // Skipping the next line with column headers
                    continue;
                }

                if (isCustomerSection && !line.trim().isEmpty()) {
                    String[] parts = line.trim().split("\\s+");
                    int custNo = Integer.parseInt(parts[0]);
                    int xCoord = Integer.parseInt(parts[1]);
                    int yCoord = Integer.parseInt(parts[2]);
                    int demand = Integer.parseInt(parts[3]);
                    customerData.put(custNo, new int[]{xCoord, yCoord, demand});
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return customerData; //has customer id as key and x,y coordinates as value
                            //need to add demand(weight of each customer) to this map

    }

    
    //need to add demand(weight of each customer) to this map

    // 0 -> {x,y} -> {x,y,demand}
    
}