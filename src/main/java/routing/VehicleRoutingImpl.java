package routing;

import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sebastianuskh on 9/3/16.
 * Vehicle Routing Impl to count best route
 */
public class VehicleRoutingImpl implements VehicleRouting {

    /**
     * solution is stored in best solution matrix
     * sized (vehicle number) x (maximum client)
     * every vehicle get its column for the route
     */
    private SimpleMatrix bestSolution = null;

    /**
     * input from client, consist of :
     * A.   distance matrix
     *      sized (client + 1 (depot)) x (client + 1 (depot))
     * B.   client demad
     *      each client demand
     */
    private SimpleMatrix distanceMatrix;
    private List<Double> clientsDemand;

    /**
     * variable for computing the best route
     */
    private int numVehicle = 3; // there are 3 vehicle
    private int capacity = 999; // vehicle capacity
    private int tolerance = 5; // more is better, cost computing time


    public VehicleRoutingImpl() {
    }

    public VehicleRoutingImpl(int numVehicle, int capacity, int tolerance) {
        this.numVehicle = numVehicle;
        this.capacity = capacity;
        this.tolerance = tolerance;
    }

    public VehicleRouting setClient(SimpleMatrix distanceMatrix, List<Double> clientDemand) {
        this.distanceMatrix = distanceMatrix;
        this.clientsDemand = clientDemand;
        return this;
    }

    public SimpleMatrix computeBestRoute() {

        if(distanceMatrix != null && clientsDemand != null) {

            int numClients = distanceMatrix.numRows() - 1;

            List<Integer> clientsUnserved;
            List<Double> goodsTransported;

            // some variable for storing temporary solution
            SimpleMatrix neighborSolution;
            SimpleMatrix bestNeighSolution;

            // objective value = minimize total distance traveled
            Double neighborObj;
            Double bestObj = 9999.0;


            // INITIALIZATION PHASE
            // initial solution
            SimpleMatrix initialSolution = new SimpleMatrix(numVehicle, numClients + 2);
            for (int i = 0; i < numVehicle - 1; i++) {
                for (int j = 0; j < numClients + 1; j++) {
                    initialSolution.set(i, j, 0.0);
                }
            }

            // initial goodsTransported with 0
            goodsTransported = new ArrayList<Double>();
            for (int i = 0; i < numVehicle; i++) {
                goodsTransported.add(0.0);
            }

            // defining clientsUnserved
            clientsUnserved = new ArrayList<Integer>();
            for (int i = 0; i < numClients + 1; i++) {
                clientsUnserved.add(i);
            }


            // sequence is choosen depends on the least distance relative to the
            // previous sequence as long as the capacity constrained is not violated
            for (int i = 0; i < initialSolution.numRows(); i++) {
                for (int j = 1; j < initialSolution.numCols(); j++) {
                    // if capacity constraint is not violated, then place the nearest client,
                    // else, move to the next vehicle
                    if (goodsTransported.get(i) + clientsDemand.get(
                            minRelativeDistance(initialSolution.get(i, j - 1), distanceMatrix, clientsUnserved)) <= capacity) {
                        initialSolution.set(i, j,
                                minRelativeDistance(initialSolution.get(i, j - 1), distanceMatrix, clientsUnserved));
                        // update clientsunserved and the goodstransported
                        goodsTransported.set(i, goodsTransported.get(i) + clientsDemand.get(clientsUnserved.get((int) initialSolution.get(i, j))));
                        clientsUnserved.set((int) initialSolution.get(i, j), 0);
                    } else {
                        break;
                    }
                }
            }

            // IMPROVEMENT PHASE
            bestNeighSolution = new SimpleMatrix(numVehicle, numClients + 2);
            Double bestNeighObj = 9999.0;

            int counter = 0;

            while (counter <= tolerance) {
                neighborSolution = new SimpleMatrix(initialSolution);

                // SHIFT MOVE
                int neighbor = 1;

                // calculate num of vehicle used and num of client in each vehicle in the initial solution
                int inNumVeh = 0;
                List<Integer> inNumClient = new ArrayList<Integer>();
                for (int i = 0; i < numVehicle; i++) {
                    inNumClient.add(0);
                }
                for (int i = 0; i < initialSolution.numRows(); i++) {
                    if (initialSolution.get(i, 1) > 0) {
                        inNumVeh += 1;
                    }
                }

                for (int i = 0; i <= inNumVeh; i++) {
                    inNumClient.set(i, 0);
                    for (int j = 0; j < initialSolution.numCols(); j++) {
                        if (initialSolution.get(i, j) > 0) {
                            inNumClient.set(i, inNumClient.get(i) + 1);
                        }
                    }
                }

                // clients to be shifted
                for (int i = 0; i <= inNumVeh; i++) {
                    for (int j = 1; j <= inNumClient.get(i); j++) {
                        here1:
                        for (int k = 0; k <= inNumVeh; k++) {
                            int limit;
                            if (k == i) {
                                limit = inNumClient.get(k);
                            } else {
                                limit = inNumClient.get(k) + 1;
                            }
                            here2:
                            for (int l = 1; l <= limit; l++) {
                                if (i == k) {
                                    // shift within the same vehicle
                                    if (j < l) {
                                        int save1 = (int) neighborSolution.get(i, j);
                                        for (int a = j; a < l; a++) {
                                            neighborSolution.set(i, a, neighborSolution.get(i, a + 1));
                                        }
                                        neighborSolution.set(k, l, save1);
                                        neighbor += 1;
                                    } else {
                                        if (j == l) {
                                            if (l + 1 > limit) {
                                                continue here1;
                                            }
                                            continue;
                                        } else {
                                            int save2;
                                            save2 = (int) neighborSolution.get(i, j);
                                            for (int a = j; a >= l + 1; a--) {
                                                neighborSolution.set(i, a, neighborSolution.get(i, a - 1));
                                            }
                                            neighborSolution.set(k, l, save2);
                                            neighbor++;
                                        }
                                    }
                                } else {
                                    // shift on different vehicle
                                    if (goodsTransported.get(k) + clientsDemand.get((int) neighborSolution.get(i, j)) < capacity) {
                                        // shift initial solution (i, j) to initial solution (k, l)
                                        for (int a = neighborSolution.numCols() - 2; a >= l; a--) {
                                            neighborSolution.set(k, a + 1, neighborSolution.get(k, a));
                                        }
                                        neighborSolution.set(k, l, neighborSolution.get(i, j));
                                        for (int a = j; a < neighborSolution.numCols() - 1; a++) {
                                            neighborSolution.set(i, a, neighborSolution.get(i, a + 1));
                                        }
                                        neighbor++;
                                    }
                                }

                                // update goodstransported
                                for (int ii = 0; ii < neighborSolution.numRows(); ii++) {
                                    goodsTransported.set(ii, 0.0);
                                    for (int jj = 0; jj < neighborSolution.numCols(); jj++) {
                                        goodsTransported.set(ii, goodsTransported.get(ii) + clientsDemand.get((int) neighborSolution.get(ii, jj)));
                                    }
                                }

                                // calculate objective
                                neighborObj = calcObj(neighborSolution, distanceMatrix);

                                if (calcObj(neighborSolution, distanceMatrix) < bestNeighObj) {
                                    bestNeighSolution = new SimpleMatrix(neighborSolution);
                                    bestNeighObj = neighborObj;
                                }

                                neighborSolution = new SimpleMatrix(initialSolution);
                            }
                        }
                    }
                }

                // SWAP MOVE
                swap:
                for (int i = 0; i < inNumVeh; i++) {
                    for (int j = 1; j <= inNumClient.get(i); j++) {
                        for (int k = 0; k < inNumVeh; k++) {
                            for (int l = 1; l <= inNumClient.get(k); l++) {
                                if (i == k && j == l) {
                                    l += 1;
                                    if (l > inNumClient.get(i)) {
                                        k += 1;
                                        if (k > inNumVeh - 1) {
                                            break swap;
                                        }
                                    }
                                }

                                // swap
                                if (goodsTransported.get(i)
                                        - clientsDemand.get((int) neighborSolution.get(i, j))
                                        + clientsDemand.get((int) neighborSolution.get(k, l))
                                        < capacity
                                        &&
                                        goodsTransported.get(k)
                                                - clientsDemand.get((int) neighborSolution.get(k, l))
                                                + clientsDemand.get((int) neighborSolution.get(i, j))
                                                < capacity) {
                                    int save3 = (int) neighborSolution.get(i, j);
                                    neighborSolution.set(i, j, neighborSolution.get(k, l));
                                    neighborSolution.set(k, l, save3);
                                    neighbor++;

                                    // update goodstransported
                                    for (int ii = 0; ii < neighborSolution.numRows(); ii++) {
                                        goodsTransported.set(ii, 0.0);
                                        for (int jj = 0; jj < neighborSolution.numCols(); jj++) {
                                            goodsTransported.set(ii, clientsDemand.get((int) neighborSolution.get(ii, jj)));
                                        }
                                    }

                                    neighborObj = calcObj(neighborSolution, distanceMatrix);
                                    if (calcObj(neighborSolution, distanceMatrix) < bestNeighObj) {
                                        bestNeighSolution = new SimpleMatrix(neighborSolution);
                                        bestNeighObj = neighborObj;
                                    }

                                    // copy initial solution to neighborsolution
                                    neighborSolution = new SimpleMatrix(initialSolution);
                                }
                            }
                        }
                    }
                }

                // if the solution is better than bestknowsolution, replace
                if (bestNeighObj < bestObj) {
                    bestSolution = new SimpleMatrix(bestNeighSolution);
                    bestObj = bestNeighObj;
                } else {
                    counter++;
                }


                // update the initialsolution = bestneighsolution
                initialSolution = new SimpleMatrix(bestNeighSolution);

                // copy initialsolution to neighborsolution
                neighborSolution = new SimpleMatrix(initialSolution);

                inNumVeh = 0;
                // update initial solution as well as the inNumVeh
                for (int i = 0; i < initialSolution.numRows(); i++) {
                    if (initialSolution.get(i, 1) > 0) {
                        inNumVeh++;
                    }
                }

                for (int i = 0; i <= inNumVeh; i++) {
                    inNumClient.set(i, 0);
                    for (int j = 0; j < initialSolution.numCols(); j++) {
                        if (initialSolution.get(i, j) > 0) {
                            inNumClient.set(i, inNumClient.get(i) + 1);
                        }
                    }
                }
            }
        }
        return bestSolution;
    }

    /**
     * calcObj : for creating objective cost
     * @param solution : solution to be count
     * @param distanceMatrix : distance matrix that needed
     * @return result count
     */
    private Double calcObj(SimpleMatrix solution, SimpleMatrix distanceMatrix) {
        Double calcObj = 0.0;
        for (int i = 0; i < solution.numRows(); i ++){
            for (int j = 0; j < solution.numCols() - 1; j ++){
                calcObj += distanceMatrix.get((int) solution.get(i, j), (int) solution.get(i, j + 1));
            }
        }
        return calcObj;
    }

    /**
     * finding the minimum relative distance from client
     * @param client : number of client
     * @param distanceMatrix : distance matrix betweet point
     * @param clientsUpdated : client that is updated
     * @return relative distance
     */
    private int minRelativeDistance(double client, SimpleMatrix distanceMatrix, List<Integer> clientsUpdated) {
        int nearestClient = 0;
        double nearestDistance = 9999.0;
        for (int i = 0; i < distanceMatrix.numCols(); i ++){
            if(distanceMatrix.get((int) client, i) < nearestDistance && clientsUpdated.get(i) != 0){
                nearestDistance = distanceMatrix.get((int) client, i);
                nearestClient = i;
            }
        }
        return nearestClient;
    }
}
