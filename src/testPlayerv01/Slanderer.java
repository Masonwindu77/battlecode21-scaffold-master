package testPlayerv01;

import java.util.Random;

import battlecode.common.*;
import testPlayerv01.Service.Movement;

public class Slanderer extends RobotPlayer {

    static Direction nextDirection;
    static boolean enemyMuckrakersNearby = false;
    static MapLocation closestEnemyMuckraker;

    public static void run() throws GameActionException {
        senseRobotsNearby();
        checkNearbyFlagsForEnemy();

        if (!enemyEnlightenmentCenterFound && !enemyMuckrakersNearby) {
            checkIfEnemeyEnlightenmentCenterHasBeenFound(spawnEnlightenmentCenterRobotId);
            stayNearHomeBase();

            if (nextDirection == null) {
                println("SLANDERER HERE1:");
                Movement.scoutTheDirection(Movement.getRandomDirection());
            } else {
                println("SLANDERER HERE2:" + nextDirection);
                Movement.scoutTheDirection(nextDirection);
            }

        } else if (enemyMuckrakersNearby) {

            Movement.moveAwayFromLocation(closestEnemyMuckraker);

        } else if (enemyEnlightenmentCenterFound) {

            Movement.moveAwayFromLocation(currentEnemyEnlightenmentCenterGoingFor);

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
                        && robotController.getLocation().distanceSquaredTo(closestEnemyMuckraker) >= robotController
                                .getLocation().distanceSquaredTo(robotInfo.getLocation())) {
                    closestEnemyMuckraker = robotInfo.getLocation();
                }
            }
        }
    }

    private static void checkNearbyFlagsForEnemy() {

    }

    private static void stayNearHomeBase() {
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;
        if (robotController.getLocation().isWithinDistanceSquared(enlightenmentCenterHomeLocation,
                sensorRadiusSquared)) {
            nextDirection = null;
        } else {
            nextDirection = robotController.getLocation().directionTo(enlightenmentCenterHomeLocation);
        }
    }

    public static void setup() {
        assignHomeEnlightenmentCenterLocation();
        robotCurrentInfluence = robotController.getInfluence();
        assignRobotRole();
        enemy = robotController.getTeam().opponent();
        friendly = robotController.getTeam();
        randomInteger = new Random();
    }

    private static void assignRobotRole() {
        if (robotController.getInfluence() >= POLITICIAN_EC_BOMB) {
            robotRole = RobotRoles.PoliticianEnlightenmentCenterBomb;
        } else {
            robotRole = RobotRoles.Follower;
        }

    }
}
