package testPlayerv01.Service;

import battlecode.common.*;
import testPlayerv01.RobotPlayer;

public class Movement extends RobotPlayer 
{
    static final Direction[] flipDirections = { Direction.NORTHEAST, Direction.SOUTHEAST, Direction.SOUTHWEST,
            Direction.NORTHWEST, };

    static final Direction[] mirrorDirections = { Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH, };

    private static MapLocation nextLocationToGo;

    public static void basicBugMovement(MapLocation target) throws GameActionException {
        MapLocation currentLocation = robotController.getLocation();
        Direction direction = robotController.getLocation().directionTo(target);

        if (!currentLocation.equals(target) && robotController.isReady()) {
            if (canRobotMoveThroughTile(direction, currentLocation)) {
                robotController.move(direction);
                bugDirection = null;
            } else if (!canRobotMoveThroughTile(direction, currentLocation) && rightBugMovement) {
                if (bugDirection == null) {
                    bugDirection = direction;
                }
                for (int i = 0; i < 8; ++i) {
                    if (robotController.canMove(bugDirection) && robotController.sensePassability(
                            robotController.getLocation().add(bugDirection)) >= passabilityThreshold) {
                        robotController.setIndicatorDot(robotController.getLocation().add(bugDirection), 0, 255, 255);
                        robotController.move(bugDirection);
                        bugDirection = bugDirection.rotateRight();
                        break;
                    }
                    robotController.setIndicatorDot(robotController.getLocation().add(bugDirection), 255, 0, 0);
                    bugDirection = bugDirection.rotateLeft();
                }
            } else if (!canRobotMoveThroughTile(direction, currentLocation) && leftBugMovement) {
                if (bugDirection == null) {
                    bugDirection = direction;
                }

                for (int i = 0; i < 8; ++i) {
                    if (robotController.canMove(bugDirection) && robotController.sensePassability(
                            robotController.getLocation().add(bugDirection)) >= passabilityThreshold) {
                        robotController.setIndicatorDot(robotController.getLocation().add(bugDirection), 0, 255, 255);
                        robotController.move(bugDirection);
                        bugDirection = bugDirection.rotateLeft();
                        break;
                    }
                    robotController.setIndicatorDot(robotController.getLocation().add(bugDirection), 255, 0, 0);
                    bugDirection = bugDirection.rotateRight();
                }
            } else if (!robotController.onTheMap(robotController.adjacentLocation(direction))) {
                if (bugDirection == null) {
                    bugDirection = direction;
                }

                while (bugDirection == direction || !canRobotMoveThroughTile(bugDirection, currentLocation)) {
                    bugDirection = getRandomDirection();
                    if (canRobotMoveThroughTile(bugDirection, currentLocation))
                    {
                        robotController.move(bugDirection);
                        break;
                    }
                }
            } else {
                // Location is not on map. Reached an edge.
                while (directionToScout == direction) {
                    Direction randomDirection = getRandomDirection();
                    if (robotController.onTheMap(currentLocation.add(randomDirection))) {
                        if (robotController.canMove(randomDirection)) {
                            robotController.move(randomDirection);
                            // change direction to scout
                            directionToScout = randomDirection;
                            break;
                        }
                    }
                }
            }
        } else if (robotController.isReady()) 
        {
            targetInSight = true;
        }
    }

    static boolean canRobotMoveThroughTile(Direction direction, MapLocation currentLocation)
            throws GameActionException {
        return robotController.canMove(direction);
    }

    protected static void greedyMoveToLocation(MapLocation target) throws GameActionException 
    {
        if (!robotController.getLocation().equals(target) && robotController.isReady()) 
        {
            MapLocation currentLocation = robotController.getLocation();
            int distanceSquaredToTarget = currentLocation.distanceSquaredTo(target);
            MapLocation[] adjacentLocation = getAdjacentLocations(currentLocation);
            double passabilty = 0;
            if (robotController.canMove(currentLocation.directionTo(target)) 
                && robotController.sensePassability(currentLocation.add(currentLocation.directionTo(target))) > .9)
            {
                robotController.move(currentLocation.directionTo(target));
            }
            else
            {
                // greedy will search all and take first best
                for (MapLocation mapLocation : adjacentLocation) 
                {
                    if (robotController.onTheMap(mapLocation) && robotController.canMove(currentLocation.directionTo(mapLocation))) 
                    {
                        int checkingDistanceSquared = mapLocation.distanceSquaredTo(target);
                        double sensedPassability = robotController.sensePassability(mapLocation);

                        if (checkingDistanceSquared < distanceSquaredToTarget) 
                        {
                            if (sensedPassability >= passabilty) 
                            {
                                passabilty = sensedPassability;
                                nextLocationToGo = mapLocation;
                            }
                        }
                    }                
                }

                // Move
                if (nextLocationToGo != null && robotController.canMove(currentLocation.directionTo(nextLocationToGo))) 
                {
                    robotController.move(currentLocation.directionTo(nextLocationToGo));    
                } 

                // check if it is empty
                if (nextLocationToGo == null || !robotController.canMove(currentLocation.directionTo(nextLocationToGo))) 
                {
                    basicBugMovement(target);                   
                }                    
            }                 
        }
    }     

    protected static MapLocation[] getAdjacentLocations(MapLocation sourceLocation)
    {
        MapLocation[] adjacentLocations = new MapLocation[directions.length];
        for (int i = 0; i < adjacentLocations.length; i++) 
        {
            adjacentLocations[i] = sourceLocation.add(directions[i]);
        }
        return adjacentLocations;
    }

    

    public static void scoutTheDirection(Direction direction) throws GameActionException 
    {
        if (robotController.onTheMap(robotController.getLocation().add(direction).add(direction).add(direction))) 
        {
            //basicBugMovement(robotController.getLocation().add(direction));
            greedyMoveToLocation(robotController.getLocation().add(direction).add(direction).add(direction));
        }
        // Checking if still near home base and if found a wall.
        else
        {
            directionToScout = getRandomDirection();

            if (spawnEnlightenmentCenterHomeLocation != null 
                && robotController.canSenseLocation(spawnEnlightenmentCenterHomeLocation)) 
            {
                directionToScout = getOppositeDirection(direction);
            }    
            
            //basicBugMovement(robotController.getLocation().add(directionToScout));
            greedyMoveToLocation(robotController.getLocation().add(directionToScout));
        }       
    }

    public static void scoutAction() throws GameActionException {

        if (directionToScout != null) 
        {
            scoutTheDirection(directionToScout);

        } 
        else if (locationToScout != null) 
        {
            //basicBugMovement(locationToScout);
            greedyMoveToLocation(locationToScout);
        } 
        else 
        {
            directionToScout = getRandomDirection();
            scoutTheDirection(directionToScout);
        }
    }

    public static Direction getRandomDirection() {
        int max = directions.length - 1;
        int min = 0;
        int randomNum = randomInteger.nextInt(max - min + 1) + min;

        return directions[randomNum];
    }

    public static void moveAwayFromLocation(MapLocation locationToMoveAwayFrom) throws GameActionException
    {
        Direction directionTowardsTarget = robotController.getLocation().directionTo(locationToMoveAwayFrom);
        Direction directionToMoveAway = getOppositeDirection(directionTowardsTarget);

        if (robotController.canMove(directionToMoveAway)) {
            robotController.move(directionToMoveAway);
        } 
        else 
        {
            //basicBugMovement(robotController.getLocation().add(directionToMoveAway));
            greedyMoveToLocation(robotController.getLocation().add(directionToMoveAway).add(directionToMoveAway));
        }
    }

    public static Direction getOppositeDirection(Direction directionTowardsTarget) {
        Direction oppositeDirection = Direction.CENTER;

        if (directionTowardsTarget == Direction.NORTH) {
            oppositeDirection = Direction.SOUTH;
        } else if (directionTowardsTarget == Direction.NORTHWEST) {
            oppositeDirection = Direction.SOUTHEAST;
        } else if (directionTowardsTarget == Direction.NORTHEAST) {
            oppositeDirection = Direction.SOUTHWEST;
        } else if (directionTowardsTarget == Direction.EAST) {
            oppositeDirection = Direction.WEST;
        } else if (directionTowardsTarget == Direction.WEST) {
            oppositeDirection = Direction.EAST;
        } else if (directionTowardsTarget == Direction.SOUTHWEST) {
            oppositeDirection = Direction.NORTHEAST;
        } else if (directionTowardsTarget == Direction.SOUTHEAST) {
            oppositeDirection = Direction.NORTHWEST;
        } else if (directionTowardsTarget == Direction.SOUTH) {
            oppositeDirection = Direction.NORTH;
        }

        return oppositeDirection;
    }

    public static void moveToEnemyEnlightenmentCenter(MapLocation enemyCenterLocation) throws GameActionException {
        //Movement.basicBugMovement(enemyCenterLocation);
        greedyMoveToLocation(enemyCenterLocation);
    }

    public static void moveToNeutralEnlightenmentCenter(MapLocation neutralCenterLocation) throws GameActionException {
        //Movement.basicBugMovement(neutralCenterLocation);
        greedyMoveToLocation(neutralCenterLocation);
    }

    public static void moveToTargetLocation(MapLocation targetLocation) throws GameActionException
    {
        //Movement.basicBugMovement(targetLocation);
        greedyMoveToLocation(targetLocation);
    }

    public static void moveInFrontOfTarget(MapLocation targetLocation, MapLocation defendLocation)
            throws GameActionException
    {
        MapLocation adjacentMapLocation = targetLocation.subtract(targetLocation.directionTo(defendLocation));

        if (!robotController.getLocation().equals(adjacentMapLocation)) 
        {
            //basicBugMovement(adjacentMapLocation);
            greedyMoveToLocation(adjacentMapLocation);
        }
    }


//     static Map<MapLocation, Double> dialsAlgorithmMovement(MapLocation source,  MapLocation target) throws GameActionException
//     {
//         //MapLocation[] distanceOneFromLocation = getDistanceOneFromLocation();
//         // int[] timeToTavel[u, v] = how long to travel from u to v ---Is this a function that returns the time it takes to travel from u to target v? cooldoiwn        
//         int C = 10; // max time a single step can take

//         //ArrayList<ArrayList<MapLocation> > queueOfTravelLocations = new ArrayList<ArrayList<MapLocation> >(C + 1);

//         ArrayList<MapLocation>[] queueOfTravelLocations = new ArrayList[C + 1];
//         for (int i =0; i < queueOfTravelLocations.length; i++) {
//             queueOfTravelLocations[i] = new ArrayList<MapLocation>();
//         }
//         queueOfTravelLocations[0].add(source);
//         //queueOfTravelLocations[1] = distanceOneFromLocation;

//         Set<MapLocation> expanded = new HashSet<MapLocation>();
//         Map<MapLocation, Double> distance = new HashMap<MapLocation, Double>();
//         distance.put(source, (double) 0);
//         distance = getDistance();


//         //ouble[] distance = new double[C+1]; // ---> So this has a mapLocation as a key and then a distance Integer as a 2nd key? Or is it double array?
//         //distance[source] = 0;
        

//         // double[] test = new double[C+1];
//         // for (int i = 0; i < test.length; i++) 
//         // {
//         //     test[i] = Double.POSITIVE_INFINITY;
//         // }
//         /**
//          * 
//          * So I go through all the locations 1 spot away from source then 2 spots then 3 and 
//          * I check the cooldown on them.
//          */

        

//         for (int i = 0; Clock.getBytecodesLeft() > 4000; i = (i + 1) % (C + 1)) 
//         {
//             // while array of locations at different distances away...
//             while (queueOfTravelLocations[i] != null) 
//             {
//                 // Grab one location from the array.
//                 MapLocation locationToExpand = queueOfTravelLocations[i].get(0);

//                 if (expanded.contains(locationToExpand)) 
//                 {
//                     continue;
//                 }

//                 for (MapLocation location : getAdjacentLocations(locationToExpand)) 
//                 {
//                     double passabilityTime = distance.get(locationToExpand) + getPassabilty(location);
//                     if (passabilityTime < distance.get(location)) 
//                     {
//                         distance.replace(location, passabilityTime);
//                         queueOfTravelLocations[(int) (distance.get(location) % C + 1)].add(location);
//                     }    
//                 }

//                 expanded.add(locationToExpand);
//                 queueOfTravelLocations[i].remove(locationToExpand);
//             }
//         }

//         return distance;

//         /**
//          * while Q[i] is not empty:
//         Let u = remove any element from Q[i]----> which is a list of locations i spaces away...so it grabs 1 location
//         If Expanded[u] is true: ---> if that location has already been expanded out....
//             // Don't expand a location you've already expanded before
//             continue

//         for each location v that you can reach from u: ---> For each location around 
//             // This is the expand step: we will expand from u to v
//             Let T = Distance[u] + TimeToTravel[u, v] ---> The distance (i.e. squares) PLUS the cooldown for that square....
//             if T < Distance[v]: ----> how long it would take to get to that location... so an integer... 
//                 Distance[v] = T ---> If it is less than default or the lowest, it becomes the lowest of the possibilities...
//                 Add v to Q[Distance[v] % (C+1)]  // This works because, Distance[v] <= C-----> Add this to the Queue as something to expand in the future since it is one of the lowest....

//         Expanded[u] = true ---> You have expanded that specific location... 

// Output: Distance[]
//     You can also keep track of how you got to each location when you Expand.
//          */
//     }

//     protected static double timeToTravel(MapLocation source, MapLocation target) throws GameActionException
//     {
//         double test = robotController.sensePassability(source);
//         return test;
//     }

//     protected static Map<MapLocation, Double> getDistance()
//     {
//         Map<MapLocation, Double> distance = new HashMap<MapLocation, Double>();

//         for (int i = 0; i < 4; i++) 
//         {
//             for (int ii = 0; ii < directions.length; ii++) 
//             {
//                 if (i == 0) 
//                 {
//                     distance.put(robotController.getLocation().add(directions[ii]), Double.POSITIVE_INFINITY);
//                 }
//                 else if (i == 1)
//                 {
//                     distance.put(robotController.getLocation().add(directions[ii]).add(directions[ii]), Double.POSITIVE_INFINITY);
//                 }
//                 else if (i == 2)
//                 {
//                     distance.put(robotController.getLocation().add(directions[ii]).add(directions[ii]).add(directions[ii]), Double.POSITIVE_INFINITY);
//                 }
//                 else if (i == 3)
//                 {
//                     distance.put(robotController.getLocation().add(directions[ii]).add(directions[ii]).add(directions[ii]).add(directions[ii]), Double.POSITIVE_INFINITY);
//                 }
//             }    
//         }

//         return distance;
//     }

// static double getPassabilty(MapLocation targetLocation) throws GameActionException
//     {
//         return (robotController.sensePassability(targetLocation) * 10);
//     }   

//     private static MapLocation[] getDistanceOneFromLocation()
//     {
//         MapLocation[] distanceOneFromMapLocation = new MapLocation[7];

//         for (int i = 0; i < distanceOneFromMapLocation.length; i++) 
//         {
//             distanceOneFromMapLocation[i] = robotController.getLocation().add(directions[i]);
//         }

//         return distanceOneFromMapLocation;
//     }
}