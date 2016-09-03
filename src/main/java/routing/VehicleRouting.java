package routing;

import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sebastianuskh on 9/3/16.
 */
public class VehicleRouting {

    int numClients; // number of nodes + 1 because depot also count as nodes
    int numVehicle; // there are 3 vehicle
    int maxNeighbor; // more is better, cost computing time
    int tolerance; // more is better, cost computing time
    int capacity; // vehicle capacity

    List<Integer> clientsUnserved;
    List<Double> clientsDemand;
    SimpleMatrix distanceMatrix;
    List<Double> goodsTransported;

    // solution is represented by a 2D matrix, 1st is the vehicle dimension, 2nd is the sequence of service
    SimpleMatrix initialSolution;
    SimpleMatrix neighborSolution;
    SimpleMatrix bestSolution;

    // objective value = minimize total distance traveled
    Double initialObj;
    Double neighborObj;
    Double bestObj = 9999.0;

    SimpleMatrix bestNeighSolution;


    public VehicleRouting(int numClients, int numVehicle, int maxNeighbor, int tolerance, int capacity){
        this.numClients = numClients;
        this.numVehicle = numVehicle;
        this.maxNeighbor = maxNeighbor;
        this.tolerance = tolerance;
        this.capacity = capacity;

        // defining clientsUnserved
        clientsUnserved = new ArrayList<Integer>();
        for (int i = 0; i < numClients + 1; i ++){
            clientsUnserved.add(i);
        }
        // clientsUnserved demand
        clientsDemand = new ArrayList<Double>();
        clientsDemand.add((double) 0);
        for (int i = 0; i < numClients; i++){
            clientsDemand.add((double) 5);
        }

        // init distance matrix
        distanceMatrix = new SimpleMatrix(numClients + 1, numClients + 1, true,

                0.0, 2.0, 3.0, 4.0, 2.0, 1.0,
                2.0, 0.0, 4.0, 6.0, 2.0, 4.0,
                3.0, 4.0, 0.0, 5.0, 2.0, 7.0,
                4.0, 6.0, 5.0, 0.0, 3.0, 5.0,
                2.0, 2.0, 2.0, 3.0, 0.0, 6.0,
                1.0, 4.0, 7.0, 5.0, 6.0, 0.0
                );

        // INITIALIZATION PHASE
        // initial solution

        initialSolution = new SimpleMatrix(numVehicle, numClients + 2);
        for (int i = 0 ; i < numVehicle - 1; i ++){
            for (int j = 0; j < numClients + 1 ; j++){
                initialSolution.set(i, j, 0.0);
            }
        }

        // initial goodsTransported with 0
        goodsTransported = new ArrayList<Double>();
        for (int i = 0; i < numVehicle; i ++){
            goodsTransported.add(0.0);
        }

        // sequence is choosen depends on the least distance relative to the
        // previous sequence as long as the capacity constrained is not violated
        for (int i = 0; i < initialSolution.numRows(); i ++){
            for (int j = 1; j < initialSolution.numCols(); j ++){
                // if capacity constraint is not violated, then place the nearest client,
                // else, move to the next vehicle
                if(goodsTransported.get(i) + clientsDemand.get(
                        minRelativeDistance(initialSolution.get(i, j - 1), distanceMatrix, clientsUnserved)) <= capacity){
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

        // calculate initial obj
        initialObj = calcObj(initialSolution, distanceMatrix);


        // IMPROVEMENT PHASE
        bestNeighSolution = new SimpleMatrix(numVehicle, numClients + 2);
        Double bestNeighObj = 9999.0;

        int counter = 0;

        while (counter <= tolerance){
            neighborSolution = new SimpleMatrix(initialSolution);

            // SHIFT MOVE
            int neighbor = 1;

            // calculate num of vehicle used and num of client in each vehicle in the initial solution
            int inNumVeh = 0;
            List<Integer> inNumClient = new ArrayList<Integer>();
            for (int i = 0; i < numVehicle; i ++){
                inNumClient.add(0);
            }
            for (int i = 0; i < initialSolution.numRows(); i ++){
                if(initialSolution.get(i, 1) > 0){
                    inNumVeh += 1;
                }
            }

            for (int i = 0; i <= inNumVeh; i ++){
                inNumClient.set(i, 0);
                for (int j = 0; j < initialSolution.numCols(); j++){
                    if (initialSolution.get(i, j) > 0){
                        inNumClient.set(i, inNumClient.get(i) + 1);
                    }
                }
            }

            // clients to be shifted
            for (int i = 0; i <= inNumVeh; i ++){
                for (int j = 1; j <= inNumClient.get(i); j++){
                    here1:
                    for (int k = 0; k <= inNumVeh; k ++){
                        int limit;
                         if (k == i){
                             limit = inNumClient.get(k);
                         } else {
                             limit = inNumClient.get(k) + 1;
                         }
                         here2:
                         for (int l = 1; l <= limit; l ++){
                             if(i == k){
                                 // shift within the same vehicle
                                 if( j < l){
                                     int save1 = (int) neighborSolution.get(i,j);
                                     for (int a = j; a < l; a ++){
                                         neighborSolution.set(i, a, neighborSolution.get(i, a + 1));
                                     }
                                     neighborSolution.set(k, l, save1);
                                     neighbor += 1;
                                 } else {
                                     if (j == l) {
                                         l += 1;
                                         if (l > limit){
                                             k += 1;
                                             continue here1;
                                         }
                                         continue here2;
                                     } else {
                                         int save2;
                                         save2 = (int) neighborSolution.get(i, j);
                                         for (int a = j; a >= l + 1; a -- ){
                                             neighborSolution.set(i, a, neighborSolution.get(i, a - 1));
                                         }
                                         neighborSolution.set(k, l, save2);
                                         neighbor++;
                                     }
                                 }
                             } else {
                                 // shift on different vehicle
                                 if (goodsTransported.get(k) + clientsDemand.get((int) neighborSolution.get(i , j)) < capacity){
                                     // shift initial solution (i, j) to initial solution (k, l)
                                     for (int a = neighborSolution.numCols() - 1; a >= l ; a--){
                                         neighborSolution.set(k, a + 1, neighborSolution.get(k, a));
                                     }
                                     neighborSolution.set(k, l, neighborSolution.get(i, j));
                                     for (int a = j; a <= neighborSolution.numCols() - 1; a ++){
                                         neighborSolution.set(i, a, neighborSolution.get(i, a + 1));
                                     }
                                     neighbor++;
                                 }
                             }

                             // update goodstransported
                             for (int ii = 0; ii < neighborSolution.numRows(); ii ++){
                                 goodsTransported.set(ii, 0.0);
                                 for (int jj = 0; jj < neighborSolution.numCols(); jj++){
                                     goodsTransported.set(ii, goodsTransported.get(ii) + clientsDemand.get((int) neighborSolution.get(ii, jj)));
                                 }
                             }

                             // calculate objective
                             neighborObj = calcObj(neighborSolution, distanceMatrix);

                             if(calcObj(neighborSolution, distanceMatrix) < bestNeighObj){
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
            for (int i = 0; i < inNumVeh; i ++){
                for (int j = 1; j <= inNumClient.get(i); j ++){
                    for(int k = 0; k < inNumVeh; k ++){
                        for (int l = 1; l <= inNumClient.get(k); l ++){
                            if(i == k && j == l){
                                l += 1;
                                if (l > inNumClient.get(i)){
                                    k += 1;
                                    if (k > inNumVeh - 1){
                                        break swap;
                                    }
                                }
                            }

                            // swap
                            if(goodsTransported.get(i)
                                    - clientsDemand.get((int) neighborSolution.get(i, j))
                                    + clientsDemand.get((int) neighborSolution.get(k, l))
                                    < capacity
                                    &&
                                    goodsTransported.get(k)
                                    - clientsDemand.get((int) neighborSolution.get(k, l))
                                    + clientsDemand.get((int) neighborSolution.get(i, j))
                                    < capacity){
                                int save3 = (int) neighborSolution.get(i, j);
                                neighborSolution.set(i, j, neighborSolution.get(k, l));
                                neighborSolution.set(k, l, save3);
                                neighbor ++;

                                // update goodstransported
                                for(int ii = 0; ii < neighborSolution.numRows(); ii ++){
                                    goodsTransported.set(ii, 0.0);
                                    for (int jj = 0; jj < neighborSolution.numCols(); jj ++){
                                        goodsTransported.set(ii, clientsDemand.get((int) neighborSolution.get(ii, jj)));
                                    }
                                }

                                neighborObj = calcObj(neighborSolution, distanceMatrix);
                                if(calcObj(neighborSolution, distanceMatrix) < bestNeighObj){
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
            if(bestNeighObj < bestObj){
                bestSolution = new SimpleMatrix(bestNeighSolution);
                bestObj = bestNeighObj;
            } else {
                counter ++;
            }

            // update the initialsolution = bestneighsolution
            initialSolution = new SimpleMatrix(bestNeighSolution);

            // copy initialsolution to neighborsolution
            neighborSolution = new SimpleMatrix(initialSolution);

            inNumVeh = 0;
            // update initial solution as well as the inNumVeh
            for(int i = 0; i < initialSolution.numRows(); i++){
                if(initialSolution.get(i, 1) > 0){
                    inNumVeh++;
                }
            }

            for(int i = 0; i <= inNumVeh; i++){
                inNumClient.set(i, 0);
                for (int j = 1; j < initialSolution.numCols(); j++){
                    if(initialSolution.get(i, j) > 0){
                        inNumClient.set(i, inNumClient.get(i) + 1);
                    }
                }

            }
        }



    }

    private Double calcObj(SimpleMatrix solution, SimpleMatrix distanceMatrix) {
        Double calcObj = 0.0;
        for (int i = 0; i < solution.numRows(); i ++){
            for (int j = 0; j < solution.numCols() - 1; j ++){
                calcObj += distanceMatrix.get((int) solution.get(i, j), (int) solution.get(i, j + 1));
            }
        }
        return calcObj;
    }

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
