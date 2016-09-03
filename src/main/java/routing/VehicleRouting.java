package routing;

import org.ejml.simple.SimpleMatrix;

import java.util.List;

/**
 * Created by sebastianuskh on 9/4/16.
 *
 * interface for usign vehicle routing
 */
public interface VehicleRouting {

    VehicleRouting setClient(SimpleMatrix distanceMatrix, List<Double> clientDemand);

    SimpleMatrix computeBestRoute();
}
