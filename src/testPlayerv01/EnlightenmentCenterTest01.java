package testPlayerv01;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.RootPaneContainer;

import battlecode.common.*;

@SuppressWarnings("unused")
public class EnlightenmentCenterTest01 extends RobotPlayer {

    private static class RobotBuilder
    {
        static RobotType robotTypeToBuild;
        static Direction directionToSpawn;
        static int influenceToUse;
    }

    private static MapLocation[] squaresAroundEnlightenmentCenter = new MapLocation[8];

    private static Map<Integer, RobotType> robotIds = new HashMap<Integer, RobotType>();

    private static int numberOfEnlightenmentCenters = 0;
    private static final int INFLUENCE_FOR_MUCKRAKER = 1;
    private static final int NUMBER_OF_MUCKRAKERS_IN_BEGINNING = 30;
    private static int countOfMuckrakers = 0;
    private static int numberOfMuckrakersToCreateInBeginning = 0;
    
    //This keeps looping
    @SuppressWarnings("unused")
    public static void run() throws GameActionException
    {        
        if (robotController.getRoundNum() < 3) {
            RobotType firstBuild = RobotType.SLANDERER;
            int firstInfluence = robotController.getConviction();

            RobotBuilder.robotTypeToBuild = firstBuild;
            RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
            RobotBuilder.influenceToUse = firstInfluence;
        }
        else if (countOfMuckrakers < numberOfMuckrakersToCreateInBeginning && robotController.getRoundNum() < MIDDLE_GAME_ROUND_START)
        {            
            RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
            RobotBuilder.influenceToUse = INFLUENCE_FOR_MUCKRAKER;
            RobotBuilder.robotTypeToBuild = RobotType.MUCKRAKER;;
        }
        else if (robotController.getEmpowerFactor(robotController.getTeam(), 0) > 1) {
            RobotType politician = RobotType.POLITICIAN;
            int influenceForPolitician = robotController.getConviction() / 2;

            RobotBuilder.influenceToUse = influenceForPolitician;
            RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
            RobotBuilder.robotTypeToBuild = politician;
        }
        else
        {
            RobotType toBuild = RobotType.POLITICIAN;
            Random random = new Random();
            int low = 12;
            int high = 20;            
            int influence = random.nextInt(high - low) + low;

            RobotBuilder.influenceToUse = influence;
            RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
            RobotBuilder.robotTypeToBuild = toBuild;
        }        

        buildRobot();
        checkIfRobotSignallingTheyFoundEnemyEnlightenmentCenter();

        if (enemyEnlightenmentCenterMapLocation.size() > 0) {
            System.out.println("we got location: " + enemyEnlightenmentCenterMapLocation.get(enemyEnlightenmentCenterMapLocation.size()) );
        }

        // figure out high priority locations
        // decide the priority and who is going to attack there
        // there will be people not assigned, they have a certain flag, when it changes
        // ---> add it to a list of things going that way
        // ----> when that number of things going drops, build more..

        // --> For EC we want to figure out 1. the amount of conviction we are getting each turn
        // 2. the amount of conviction on the enemy EC
        // 3. how big the "Bomb" polis should be...
        // 4. how many are going to the enemy EC to harry until there is a big enough bomb?
    }

    private static Direction getAvailableDirectionToSpawn() throws GameActionException
    {
        Direction directionToSpawn = Direction.CENTER;

        for (MapLocation possibleLocation : squaresAroundEnlightenmentCenter)
        {
            if (!robotController.isLocationOccupied(possibleLocation)) 
            {
                directionToSpawn = robotController.getLocation().directionTo(possibleLocation);
                break;
            }
        }

        return directionToSpawn;
    }

    private static void checkIfRobotSignallingTheyFoundEnemyEnlightenmentCenter() throws GameActionException
    {
        for (Map.Entry<Integer, RobotType> myRobotsId : robotIds.entrySet()) 
        {
            int robotId = myRobotsId.getKey();
            
            checkRobotFlagsForEnemyLocation(robotId);
        }
    }

    private static void checkRobotFlagsForEnemyLocation(int robotId) throws GameActionException
    {
        if (robotController.canGetFlag(robotId)) 
        {
            int flag = robotController.getFlag(robotId);

            if (flag != 0 && checkIfEnemeyEnlightenmentCenterHasBeenFound(flag)) 
            {                
                setEnemyEnlightenmentCenter(flag);
            }
        }
    }

    private static void setEnemyEnlightenmentCenter(int flag) throws GameActionException
    {
        MapLocation enemyCenterLocation = getLocationFromFlag(flag);

        if (!enemyEnlightenmentCenterMapLocation.containsValue(enemyCenterLocation) || enemyEnlightenmentCenterMapLocation.size() == 0) 
        {
            enemyEnlightenmentCenterMapLocation.put(enemyEnlightenmentCenterMapLocation.size() + 1, enemyCenterLocation);
        }
    }

    private static void buildRobot() throws GameActionException
    {
        for (Direction directionToSpawn : directions) {
            if (robotController.canBuildRobot(RobotBuilder.robotTypeToBuild, directionToSpawn, RobotBuilder.influenceToUse)) 
            {
                robotController.buildRobot(RobotBuilder.robotTypeToBuild, directionToSpawn, RobotBuilder.influenceToUse);

                MapLocation currentLocation = robotController.getLocation();
                RobotInfo[] newRobot = robotController.senseNearbyRobots(currentLocation, 5, robotController.getTeam());
                
                for (RobotInfo robotInfo : newRobot) 
                {
                    if (!robotIds.containsKey(robotInfo.getID())) {
                        robotIds.put(robotInfo.getID(), robotInfo.getType());
                    }
                }

                System.out.println("MASON!! I'm a" + RobotBuilder.robotTypeToBuild + "I spawned direction: " + directionToSpawn + "with influence: " + RobotBuilder.influenceToUse );
            } else {
                break;
            }
        }
    }

    public static void setup() throws GameActionException
    {
        if (robotController.getRoundNum() == 1) {
            numberOfEnlightenmentCenters = robotController.getRobotCount();
            if (debug) {
                System.out.println(numberOfEnlightenmentCenters);
            }            
        } 
        if (numberOfEnlightenmentCenters > 0) 
        {
            numberOfMuckrakersToCreateInBeginning = NUMBER_OF_MUCKRAKERS_IN_BEGINNING/numberOfEnlightenmentCenters;
        }
        else
        {
            numberOfMuckrakersToCreateInBeginning = NUMBER_OF_MUCKRAKERS_IN_BEGINNING;
        }
        setSquaresAroundEnlightenmentCenter();

        // TODO: What is "announceSelfLocation?"

        //storeHQLocationAndGetConstants();
        //announceSelfLocation(1);
        // if (rc.getTeamSoup() >= RobotType.DELIVERY_DRONE.cost + RobotType.MINER.cost * 2) {
        //     boolean builtUnit = false;
        //     for (int i = 9; --i >= 1; ) {
        //         if (tryBuild(RobotType.DELIVERY_DRONE, buildDir)) {
        //             builtUnit = true;
        //             break;
        //         } else {
        //             buildDir = buildDir.rotateRight();
        //         }
        //     }
        //     if (builtUnit) {
        //         dronesBuilt++;
        //     }
        //     buildDir = buildDir.rotateRight();
        // }
    }

    private static void setSquaresAroundEnlightenmentCenter()
    {
        int iterator = 0;
        for (Direction direction : directions) {
            squaresAroundEnlightenmentCenter[iterator] = robotController.adjacentLocation(direction);
            ++iterator;
        }
        
    }
    
}