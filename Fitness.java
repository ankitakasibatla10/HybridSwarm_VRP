import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Random;
import java.util.Arrays;

// Remove the import statement
// import PreprocessData.*;


public class Fitness {

    static VehicleDataTera vehicle = new VehicleDataTera();
    static List<Integer> initializeRandomIndividual(Map<Integer, int[]> customer_data) {
        List<Integer> individual = new ArrayList<Integer>(customer_data.keySet());
        Collections.shuffle(individual);

        return individual;
    }


    public static boolean checkCapacityConstraint(Map<Integer, int[]> customer_data, HashMap<Integer, Integer> vehicle_data, HashMap<Integer, List<Integer>> updatedVehicleRoutes) {

        for (Map.Entry<Integer, List<Integer>> entry : updatedVehicleRoutes.entrySet()) {
            int current_vehicle_id = entry.getKey();
            List<Integer> subroute = entry.getValue();
            int vehicle_load = 0;

            for (Integer customer_id : subroute) {
                int demand = customer_data.get(customer_id)[2];
                int vehicle_capacity = vehicle_data.getOrDefault(current_vehicle_id, 0);
                vehicle_load += demand;

                if (vehicle_load > vehicle_capacity) {
                    return false;
                }
            }
        }
        return true;
    }


    public static ArrayList<ArrayList<Integer>> possibleRoutes(List<Integer> random_individual,
                                                               Map<Integer, int[]> customer_data,
                                                               HashMap<Integer, Integer> vehicle_data,
                                                               HashMap<Integer, List<Integer>> updatedVehicleRoutes) {

        ArrayList<ArrayList<Integer>> route = new ArrayList<>();
        ArrayList<Integer> subroute = new ArrayList<>();

        int vehicle_load = 0;
        int current_vehicle_id = 0;

        for (Integer customer_id : random_individual) {
            int demand = customer_data.get(customer_id)[2]; // Assuming [2] is the demand index
            int vehicle_capacity = vehicle_data.getOrDefault(current_vehicle_id, 0);

            int updated_vehicle_load = vehicle_load + demand;

            if (updated_vehicle_load <= vehicle_capacity) {
                subroute.add(customer_id);
                vehicle_load = updated_vehicle_load;
            } else {
                // Check if all vehicles are already used
                if (current_vehicle_id >= vehicle_data.size() - 1) {
                    // Handle the case when all vehicles are used
                    break;
                }
                // Add current subroute to the route list and update the vehicle routes map
                if (!subroute.isEmpty()) {
                    route.add(new ArrayList<>(subroute));
                    updatedVehicleRoutes.put(current_vehicle_id, new ArrayList<>(subroute));
                }

                // Clear the subroute and start a new one with the current customer
                subroute.clear();
                subroute.add(customer_id);
                vehicle_load = demand; // Reset the vehicle load to the current demand

                current_vehicle_id++; // Move to the next vehicle
            }
        }

        // Add the last subroute if it's not empty
        if (!subroute.isEmpty()) {
            route.add(new ArrayList<>(subroute));
            updatedVehicleRoutes.put(current_vehicle_id, new ArrayList<>(subroute));
        }

        return route;
    }

    public static Map<Double, Double> initialFitness(List<Integer> random_individual, Map<Integer, int[]> customer_data, double[][] distanceMatrix, HashMap<Integer, Integer> vehicle_data, HashMap<Integer, List<Integer>> updatedVehicleRoutes, HashMap<Integer, Double> xiValueMap) {
        double transport_cost = 10.0;
        int vehicle_setup_cost = 50;
        ArrayList<ArrayList<Integer>> route_instance = possibleRoutes(random_individual, customer_data, vehicle_data, updatedVehicleRoutes);

        double total_cost;

        Map<Double, Double> initialFitness_map = new HashMap<Double, Double>();

        double fitness_value = 0;
        //maximum vehicle count
        int max_vehicle_count = vehicle_data.size();
        int length_of_route = route_instance.size();
        if (length_of_route <= max_vehicle_count) {
            total_cost = 0;
            //traverse the route_instance
            for (int i = 0; i < length_of_route; i++) {
                int route_length = route_instance.get(i).size();
                double sub_route_distance = 0;
                int prev_cutomer_id = 0;
                //traverse the route
                for (int j = 1; j < route_length; j++) {    //[ [c1,c2]]  //distancematrix[0][c1]
                    double distance = distanceMatrix[prev_cutomer_id][route_instance.get(i).get(j)];
                    sub_route_distance += distance;
                    prev_cutomer_id = route_instance.get(i).get(j);
                }
                //distance from last customer id to depot
                double distance_to_depot = distanceMatrix[prev_cutomer_id][0];
                sub_route_distance += distance_to_depot;

                //sub_route_transport_cost = sub_route_distance * transport_cost_per_km
                double sub_route_transport_cost = sub_route_distance * transport_cost + vehicle_setup_cost;

                xiValueMap.put(i, sub_route_transport_cost);

                //calucate velocity of each vehicle

                //sub_route_transport_cost  = Xi of the vehicle
                total_cost += sub_route_transport_cost;

            } //end of route_instance   (all set of sub_routes are traversed)

            fitness_value = 100000.0 / total_cost;
            //add the fitness value and total cost in the map

            initialFitness_map.put(fitness_value, total_cost);
        
        }
        return initialFitness_map; //{fitness_value:total_cost}

    }
    //i have initial global best (present in initialFitness_map.value) and initial personal best of each vehicle stored in a map , which is Xivaluemap

    //New phase
//==========================================================================================================

    public static HashMap<Integer, List<Double>> VelocityAndDistance(HashMap<Integer, Double> xiValueMap, Map<Double, Double> initialFitness_map) {

        // Constants for PSO - these could be tuned for better performance
        final double C1 = 2.0; // Cognitive coefficient
        final double C2 = 2.0; // Social coefficient

        // Extract the global best value
        double initialgBestValue = Collections.max(initialFitness_map.keySet());

        HashMap<Integer, List<Double>> velocityAndXiMap = new HashMap<>();

        for (Map.Entry<Integer, Double> entry : xiValueMap.entrySet()) {
            Integer particleId = entry.getKey();
            double pBest = entry.getValue(); // Personal best of each vehicle

            // Generate random coefficients for each iteration
            double r1 = Math.random();
            double r2 = Math.random();

            // Retrieve the current velocity, if not present initialize with 0.0
            List<Double> currentVelocityXi = velocityAndXiMap.getOrDefault(particleId, Arrays.asList(0.0, pBest));
            double Vcurrent = currentVelocityXi.get(0);
            double Xi = currentVelocityXi.get(1);

            // Update the velocity
            double Vnext = Vcurrent + C1 * r1 * (pBest - Xi) + C2 * r2 * (initialgBestValue - Xi);

            // Update the position (Xi)
            double XiNext = Xi + Vnext;

            // Store the updated values
            velocityAndXiMap.put(particleId, new ArrayList<>(List.of(Vnext, XiNext)));
        }

        return velocityAndXiMap;
    }


    static HashMap<Integer, List<Integer>> swapCustomers(HashMap<Integer, List<Double>> velocityAndXiMap,
                                                         HashMap<Integer, List<Integer>> updatedVehicleRoutes,
                                                         Map<Integer, int[]> customer_data,
                                                         HashMap<Integer, Integer> vehicle_data) {

        // Initialize the map once before the loop
        HashMap<Integer, List<Integer>> updatedVehicleMapAfterSwap = new HashMap<>(updatedVehicleRoutes);
        Random random = new Random();

        // Traverse the velocityAndXiMap
        for (Map.Entry<Integer, List<Double>> entry : velocityAndXiMap.entrySet()) {
            double xiValue = entry.getValue().get(1);
            int numberOfOperations = (int) Math.round(xiValue) / 1000;

            for (int i = 0; i < numberOfOperations; i++) {
                // Selecting two random routes
                List<Integer> keys = new ArrayList<>(updatedVehicleMapAfterSwap.keySet());
                if (keys.size() < 2) {
                    continue; // Not enough routes to swap
                }

                Collections.shuffle(keys);
                Integer routeId1 = keys.get(0);
                Integer routeId2 = keys.get(1);

                List<Integer> route1 = updatedVehicleMapAfterSwap.get(routeId1);
                List<Integer> route2 = updatedVehicleMapAfterSwap.get(routeId2);
                //System.out.println(route1 + "route1");
                //System.out.println(route2 + "route2");

                // Perform swap if both routes are not empty
                if (!route1.isEmpty() && !route2.isEmpty()) {
                    int index1 = random.nextInt(route1.size());
                    int index2 = random.nextInt(route2.size());

                    // Swap the customers
                    Integer customerIdToSwap1 = route1.get(index1);
                    Integer customerIdToSwap2 = route2.get(index2);

                    route1.set(index1, customerIdToSwap2);
                    route2.set(index2, customerIdToSwap1);

                    // Update the routes in the map
                    updatedVehicleMapAfterSwap.put(routeId1, route1);
                    updatedVehicleMapAfterSwap.put(routeId2, route2);
                    
                }
            }
        }

        if (checkCapacityConstraint(customer_data, vehicle_data, updatedVehicleMapAfterSwap)) {
            return updatedVehicleMapAfterSwap;
        } else {
            // If constraint is violated, consider handling it without recursion to avoid stack overflow.
            // For now, returning the original map before swap.
            return updatedVehicleRoutes;
        }
    }

    //Whenever i swap new set of customer ids, to evalute , ill call this function
    public static Map<Double, Double> newFitness(Map<Integer, int[]> customer_data, double[][] distanceMatrix, HashMap<Integer, Integer> vehicle_data, HashMap<Integer, List<Integer>> updatedVehicleRoutesAfterSwap, HashMap<Integer, Double> xiValueMap) {

        double transport_cost = 10.0;
        int vehicle_setup_cost = 50;
        double newTotal_cost = 0;

        Map<Double, Double> newFitness_map = new HashMap<Double, Double>();

        //traverse updatedVehicleRoutes
        for (Map.Entry<Integer, List<Integer>> entry : updatedVehicleRoutesAfterSwap.entrySet()) {
            int current_vehicle_id = entry.getKey();
            List<Integer> subroute = entry.getValue();
            int route_length = subroute.size();
            double sub_route_distance = 0;
            int prev_cutomer_id = 0;
            //traverse the route
            for (int j = 1; j < route_length; j++) {    //[ [c1,c2]]  //distancematrix[0][c1]
                double distance = distanceMatrix[prev_cutomer_id][subroute.get(j)];
                sub_route_distance += distance;
                prev_cutomer_id = subroute.get(j);
            }
            //distance from last customer id to depot
            double distance_to_depot = distanceMatrix[prev_cutomer_id][0];

            sub_route_distance += distance_to_depot;

            //sub_route_transport_cost = sub_route_distance * transport_cost_per_km
            double sub_route_transport_cost = sub_route_distance * transport_cost + vehicle_setup_cost;

            xiValueMap.put(current_vehicle_id, sub_route_transport_cost); //overwriting the xi value

            //sub_route_transport_cost  = Xi of the vehicle
            newTotal_cost += sub_route_transport_cost;

        } //end of route_instance   (all set of sub_routes are traversed)

        double newFitness_value = 100000.0 / newTotal_cost;

        newFitness_map.put(newFitness_value, newTotal_cost);

        return newFitness_map; //{fitness_
        // value:total_cost}
    }

    // Global variable to hold the best solution across iterations
    static Map<Double, Double> globalBestFitnessMap = new HashMap<>();

    public static void PSO(List<Integer> random_individual, HashMap<Integer, Integer> vehicle_data, Map<Integer, int[]> customer_data, double[][] distanceMatrix, HashMap<Integer, List<Integer>> updatedVehicleRoutes, HashMap<Integer, Double> xiValueMap) {

    // Calculate the initial fitness only once
    if (globalBestFitnessMap.isEmpty()) {
        globalBestFitnessMap = initialFitness(random_individual, customer_data, distanceMatrix, vehicle_data, updatedVehicleRoutes, xiValueMap);
    }

    // Compute velocity and update routes
    HashMap<Integer, List<Double>> velocityAndXiMap = VelocityAndDistance(xiValueMap, globalBestFitnessMap);
    HashMap<Integer, List<Integer>> updatedVehicleRoutesAfterSwap = swapCustomers(velocityAndXiMap, updatedVehicleRoutes, customer_data, vehicle_data);

    // Compute the new fitness
    Map<Double, Double> newFitness_map = newFitness(customer_data, distanceMatrix, vehicle_data, updatedVehicleRoutesAfterSwap, xiValueMap);

    // Compare new fitness with the global best and update if better
    if (newFitness_map.values().iterator().next() < globalBestFitnessMap.values().iterator().next()) {
        globalBestFitnessMap.clear();
        globalBestFitnessMap.putAll(newFitness_map);
        System.out.println(updatedVehicleRoutesAfterSwap + "routes");

        // Optionally, print the updated global best fitness and its cost
        System.out.println("Updated Global Best Fitness:");
        for (Map.Entry<Double, Double> entry : globalBestFitnessMap.entrySet()) {
            System.out.println("Fitness value: " + entry.getKey() + " Total cost: " + entry.getValue());
        }
    }
    }

    public static void main(String[] args) {
        // Fitness fitness = new Fitness();
        HashMap<Integer, List<Integer>> updatedVehicleRoutes = new HashMap<Integer, List<Integer>>();

        HashMap<Integer, Double> xiValueMap = new HashMap<>();

        double[][] distanceMatrix = PreprocessDataMera.getdistanceMatrix("C101.txt");

        Map<Integer, int[]> customer_data = PreprocessDataMera.getCustomerData("C101.txt");

        List<Integer> random_individual = initializeRandomIndividual(customer_data);

        HashMap<Integer, Integer> vehicle_data = vehicle.initializeVehicleData(25, 60, 180);

        for (int i = 0; i < 1000; i++) {
            PSO(random_individual, vehicle_data, customer_data, distanceMatrix, updatedVehicleRoutes, xiValueMap);
        }


    }

}