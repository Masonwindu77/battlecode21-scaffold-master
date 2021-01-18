package testPlayerv01;

import battlecode.common.*;
import testPlayerv01.Service.Communication;
import testPlayerv01.Service.Movement;

public class Slanderer extends RobotPlayer {

    static Direction nextDirection;
    static boolean enemyMuckrakersNearby = false;
    static MapLocation closestEnemyMuckraker;

    public static void run() throws GameActionException {
        senseRobotsNearby();
        checkNearbyFlagsForEnemy();

        if(!enemyEnlightenmentCenterFound)
        {
            Communication.checkIfSpawnEnlightenmentCenterHasEnemyLocation();
        }

        if (!enemyMuckrakersNearby && !enemyEnlightenmentCenterFound) 
        {  
            stayNearHomeBase();

            if (nextDirection == null) {
                Movement.scoutTheDirection(Movement.getRandomDirection());
            } else {
                Movement.scoutTheDirection(nextDirection);
            }

        } 
        else if (enemyMuckrakersNearby) 
        {
            Movement.moveAwayFromLocation(closestEnemyMuckraker);
        } 
        else if (enemyEnlightenmentCenterFound) 
        {
            // if the next move places it on the adjacent square around the EC. Don't move there.
            if (!robotController.getLocation().add(Movement.getOppositeDirection(robotController.getLocation().directionTo(currentEnemyEnlightenmentCenterGoingFor))).isAdjacentTo(spawnEnlightenmentCenterHomeLocation))
            {
                Movement.moveAwayFromLocation(currentEnemyEnlightenmentCenterGoingFor);
            }
            else
            {
                Movement.scoutTheDirection(Movement.getRandomDirection());
            }
        }

        // TODO: Make Slanderer stuff
        /*
         * Make a check for a flag nearby of the muckraker. If the muckraker shows a
         * position, it means an enemy is coming
         * 
         * Also, need to figure out where we are so we can go to a corner....?
         */
    }

    private static void senseRobotsNearby() {
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;
        RobotInfo[] robots = robotController.senseNearbyRobots(sensorRadiusSquared);
        enemyMuckrakersNearby = false;

        for (RobotInfo robotInfo : robots) {
            if (robotInfo.getTeam() == enemy && robotInfo.getType() == RobotType.MUCKRAKER) {
                enemyMuckrakersNearby = true;

                if (closestEnemyMuckraker != null 
                && robotController.getLocation().distanceSquaredTo(closestEnemyMuckraker) >= robotController.getLocation().distanceSquaredTo(robotInfo.getLocation())) {
                    closestEnemyMuckraker = robotInfo.getLocation();
                }
                else if(closestEnemyMuckraker == null)
                {
                    closestEnemyMuckraker = robotInfo.getLocation();
                }
            }
        }
    }

    private static void checkNearbyFlagsForEnemy() {

    }

    private static void stayNearHomeBase() {
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;
        if (robotController.getLocation().isWithinDistanceSquared(spawnEnlightenmentCenterHomeLocation, sensorRadiusSquared)) {
            nextDirection = null;
        } 
        else 
        {
            nextDirection = robotController.getLocation().directionTo(spawnEnlightenmentCenterHomeLocation);
        }
    }

    public static void setup() 
    {
        setConstants();
        assignHomeEnlightenmentCenterLocation();
        setSquaresAroundEnlightenmentCenter();   
        robotCurrentInfluence = robotController.getInfluence();
        assignRobotRole();
        
    }

    private static void assignRobotRole() {
        if (robotController.getInfluence() >= POLITICIAN_EC_BOMB) {
            robotRole = RobotRoles.PoliticianEnlightenmentCenterBomb;
        } else {
            robotRole = RobotRoles.Follower;
        }

    }
}
