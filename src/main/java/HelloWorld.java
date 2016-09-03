import org.ejml.simple.SimpleMatrix;
import routing.VehicleRouting;
import routing.VehicleRoutingImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sebastianuskh on 9/3/16.
 *
 * Main for testing the code
 */
public class HelloWorld {
    public static void main(String[] args){
        System.out.println("Hello, World");

        int numClients = 5;

        // clientsUnserved demand
        List<Double> clientsDemand = new ArrayList<Double>();
        clientsDemand.add((double) 0);
        for (int i = 0; i < numClients; i++){
            clientsDemand.add((double) 5);
        }

        // init distance matrix
        SimpleMatrix distanceMatrix = new SimpleMatrix(numClients + 1, numClients + 1, true,

                0.0, 500.0, 3.0, 4.0, 2.0, 1.0,
                500.0, 0.0, 18.0, 16.0, 20.0, 14.0,
                3.0, 18.0, 0.0, 5.0, 2.0, 7.0,
                4.0, 16.0, 5.0, 0.0, 3.0, 5.0,
                2.0, 20.0, 2.0, 3.0, 0.0, 6.0,
                1.0, 14.0, 7.0, 5.0, 6.0, 0.0
        );

        System.out.println("======== USING OBJECT =========");
        System.out.println("===============================");

        new VehicleRoutingImpl()
                .setClient(distanceMatrix, clientsDemand)
                .computeBestRoute()
                .print();


    }
}
