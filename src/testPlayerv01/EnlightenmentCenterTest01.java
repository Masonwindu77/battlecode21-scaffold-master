package testPlayerv01;
import java.util.HashMap;
import java.util.Map;

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

    private static Map<Integer, RobotType> robotIds = new HashMap<Integer, RobotType>();
    
    // Integer: XCoordinate, RobotId
    private static Map<Integer, Integer> robotWithEnemyEnlightenmentCenterLocation = new HashMap<Integer, Integer>();

    //This keeps looping
    @SuppressWarnings("unused")
    public static void run() throws GameActionException
    {
        if (robotController.getRoundNum() < 3) {
            RobotType firstBuild = RobotType.SLANDERER;
            int firstInfluence = robotController.getConviction();

            RobotBuilder.robotTypeToBuild = firstBuild;
            RobotBuilder.directionToSpawn = Direction.NORTH;
            RobotBuilder.influenceToUse = firstInfluence;
        }
        else if (robotIds.size() < 25)
        {
            RobotType muckRacker = RobotType.MUCKRAKER;
            int influenceForMuckRacker = 1;

            RobotBuilder.influenceToUse = influenceForMuckRacker;
            RobotBuilder.robotTypeToBuild = muckRacker;
        }



        else if (robotController.getEmpowerFactor(robotController.getTeam(), 0) > 1) {
            RobotType politician = RobotType.POLITICIAN;
            int influenceForPolitician = robotController.getConviction() / 2;

            RobotBuilder.influenceToUse = influenceForPolitician;
            RobotBuilder.robotTypeToBuild = politician;
        }
        else
        {
            RobotType toBuild = randomSpawnableRobotType();
            int influence = 50;

            RobotBuilder.influenceToUse = influence;
            RobotBuilder.robotTypeToBuild = toBuild;
        }        

        buildRobot();
        checkIfRobotSignallingTheyFoundEnemyEnlightenmentCenter();

        int amountNeededForAMapLocation = 2;

        if (robotWithEnemyEnlightenmentCenterLocation.size() >= amountNeededForAMapLocation) 
        {
            int[] location = new int[2];
            int iterator = 0;
            for (Map.Entry<Integer, Integer> enlightenmentCenterLocation : robotWithEnemyEnlightenmentCenterLocation.entrySet()) 
            {
                int keyForXorYCoordinate = enlightenmentCenterLocation.getKey();
                int coordinate = enlightenmentCenterLocation.getValue();
                location[iterator++] = coordinate;
            } 

            MapLocation enlightenmentCenterOpponent = new MapLocation(location[0], location[1]);
            enemyEnlightenmentCenterMapLocation.put(enemyEnlightenmentCenterMapLocation.size() + 1, enlightenmentCenterOpponent);
            robotController.setFlag(RECEIVED_MESSAGE);
        }
    }

    private static void checkIfRobotSignallingTheyFoundEnemyEnlightenmentCenter() throws GameActionException
    {
        //int[] builtRobots = new int[];

        for (Map.Entry<Integer, RobotType> myRobotsId : robotIds.entrySet()) {
            int robotId = myRobotsId.getKey();
            
            checkForRobotIdFlags(robotId);
        }
    }

    private static void checkForRobotIdFlags(int robotId) throws GameActionException
    {
        if (robotController.canGetFlag(robotId)) 
        {
            int flag = robotController.getFlag(robotId);
            if (flag != 0) 
            {
                char[] splitFlag = String.valueOf(flag).toCharArray();

                if (checkIfEnemeyEnlightenmentCenterHasBeenFound(splitFlag)) 
                {
                    int xOrYCoordinate = (int)splitFlag[2] - (int)'0' + (ENEMY_ENLIGHTENMENT_CENTER_FOUND * 10);
                    // TODO: Make a check for if we already discovered it. 
                    // TODO: Make a "received" flag. 

                    if (xOrYCoordinate == ENEMY_ENLIGHTENMENT_CENTER_FOUND_X_COORDINATE) 
                    {
                        int enemyEnlightenmentCenterXCoordinate = getLastFiveIntegersFromFlag(splitFlag);
                        robotWithEnemyEnlightenmentCenterLocation.put(0, enemyEnlightenmentCenterXCoordinate);
                    }
                    else if (xOrYCoordinate == ENEMY_ENLIGHTENMENT_CENTER_FOUND_Y_COORDINATE) 
                    {
                        int enemyEnlightenmentCenterYCoordinate = getLastFiveIntegersFromFlag(splitFlag);
                        robotWithEnemyEnlightenmentCenterLocation.put(1, enemyEnlightenmentCenterYCoordinate);
                    }
                }
            }
        }
        else
        {
            robotIds.remove(robotId);
        }
    }

    private static boolean checkIfEnemeyEnlightenmentCenterHasBeenFound(char[] splitFlag) throws GameActionException
    {
        boolean foundTheCenter = false;
        int firstTwoIntegers = getFirstTwoIntegersFromFlag(splitFlag);
        
        if (firstTwoIntegers == ENEMY_ENLIGHTENMENT_CENTER_FOUND) {
            foundTheCenter = true;
        }

        return foundTheCenter;
    }

    private static void buildRobot() throws GameActionException
    {
        for (Direction directionToSpawn : directions) {
            if (robotController.canBuildRobot(RobotBuilder.robotTypeToBuild, directionToSpawn, RobotBuilder.influenceToUse)) 
            {
                robotController.buildRobot(RobotBuilder.robotTypeToBuild, directionToSpawn, RobotBuilder.influenceToUse);
                //TODO: DEBUG

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
    // private checkWhichRoundItIs()
    // {


    // }

    public static void setup() throws GameActionException
    {
        assignHomeEnlightenmentCenterLocation();
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
    
}