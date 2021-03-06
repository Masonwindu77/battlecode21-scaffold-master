package testPlayerv09;

import battlecode.common.*;
import testPlayerv09.Service.Communication;
import testPlayerv09.Service.Movement;
import testPlayerv09.Service.SenseRobots;

public class Slanderer extends RobotPlayer {

    static Direction nextDirection;
    static boolean enemyMuckrakersNearby = false;
    static MapLocation closestEnemyMuckraker;

    public static void run() throws GameActionException 
    {
        senseRobotsNearby();
        checkNearbyFlagsForEnemy();

        if (robotController.getRoundNum() % 2 == 0) 
        {
            if (robotController.canSetFlag(Communication.SLANDERER_FLAG)) 
            {
                robotController.setFlag(Communication.SLANDERER_FLAG);    
            }    
        }
        else
        {
            if (haveMessageToSend) 
            {
                Communication.setFlagMessageForScout();
            }            
        }

        if(!enemyEnlightenmentCenterFound)
        {
            Communication.checkIfSpawnEnlightenmentCenterHasEnemyLocation();
        }

        if (!enemyMuckrakersNearby && !enemyEnlightenmentCenterFound) 
        {  
            stayNearHomeBase();

            if (nextDirection == null) 
            {
                Direction randomDirection = Movement.getRandomDirection();
                while (robotController.getLocation().add(randomDirection).isAdjacentTo(spawnEnlightenmentCenterHomeLocation)) 
                {
                    randomDirection = Movement.getRandomDirection();
                }

                Movement.scoutTheDirection(randomDirection);
            } else 
            {
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
    }

    private static void senseRobotsNearby() throws GameActionException {
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;
        RobotInfo[] robots = robotController.senseNearbyRobots(sensorRadiusSquared);
        enemyMuckrakersNearby = false;

        for (RobotInfo robotInfo : robots) 
        {
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
            else if (robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER) 
            {
                SenseRobots.processEnlightenmentCenterFinding(robotInfo);
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

    private static void assignRobotRole() 
    {
        robotRole = RobotRoles.PoliticianEnlightenmentCenterBomb;
    }
}
