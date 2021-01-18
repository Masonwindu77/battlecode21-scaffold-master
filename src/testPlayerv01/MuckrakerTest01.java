package testPlayerv01;

import java.util.Random;

import battlecode.common.*;
import testPlayerv01.Service.Movement;
import testPlayerv01.Service.Communication;

public class MuckrakerTest01 extends RobotPlayer {
    static boolean enemyEnlightenmentCenterIsAround;

    static boolean enemyEnlightenmentCenterHasBeenConverted;

    static int turnsAroundEnemyEnlightenmentCenter = 0;

    @SuppressWarnings("unused")
    public static void run() throws GameActionException {
        tryExpose();
        senseNearbyRobots();

        if (!enemyEnlightenmentCenterFound) {
            checkIfRobotCanSenseEnemyEnlightenmentCenter();
        }

        if (haveMessageToSend) {
            setFlagMessage();
        }
        else
        {
            robotController.setFlag(0);
        }

        // Scout Role
        if (robotRole == RobotRoles.Scout && !enemyEnlightenmentCenterFound) {

            // This is so that the robot will go the direction it spawns. Hopefully this will make it go all over.
            if (robotController.getRoundNum() < 15) 
            {
                directionToScout = spawnEnlightenmentCenterHomeLocation.directionTo(robotController.getLocation());
                // directionTo can be from a different location. Like the enemyEC to where you are.
            }
            
            Movement.scoutAction();
        } else if (enemyEnlightenmentCenterFound) {
            if (politicianECBombNearby) {
                // TODO: Try out moving away from the ECBomb too.
                Movement.moveAwayFromLocation(currentEnemyEnlightenmentCenterGoingFor);

                println("Here Muckraker");
            } else if (!robotController.getLocation().isAdjacentTo(currentEnemyEnlightenmentCenterGoingFor)) {

                Movement.moveToEnemyEnlightenmentCenter(currentEnemyEnlightenmentCenterGoingFor);
            }
        } else {
            tryMove(randomDirection());
        }

        // TODO:
        /*
         * So these guys are mostly the scouts since they can sense. what should they
         * scout for? 1. enemy EC 2. enemy slanderer... though they will probably run
         * away 3. big Polis? 4. large groups of enemies. 5. Map edge & corners.
         */
    }

    private static void tryExpose() throws GameActionException {
        Team enemy = robotController.getTeam().opponent();
        int actionRadius = robotController.getType().actionRadiusSquared;
        RobotInfo[] enemyRobots = robotController.senseNearbyRobots(actionRadius, enemy);

        for (RobotInfo robotInfo : enemyRobots) {
            if (robotInfo.type.canBeExposed()) {
                if (robotController.canExpose(robotInfo.location)) {
                    println("MASON I EXPOSED A SLANDERER");
                    robotController.expose(robotInfo.location);
                    return;
                }
            }
        }
    }

    private static void senseNearbyRobots() throws GameActionException {
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;
        RobotInfo[] robots = robotController.senseNearbyRobots(sensorRadiusSquared);
        politicianECBombNearby = false;
        enemyEnlightenmentCenterIsAround = false;

        for (RobotInfo robotInfo : robots) {
            if (robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                if (robotInfo.getTeam() == enemy) {
                    enemyEnlightenmentCenterCurrentInfluence = robotInfo.getInfluence();
                    enemyEnlightenmentCenterIsAround = true;
                    turnsAroundEnemyEnlightenmentCenter++;
                    haveMessageToSend = true;

                    // TODO: Finish this
                    // if (currentEnemyEnlightenmentCenterGoingFor != null && robotController.getLocation()
                    //         .distanceSquaredTo(currentEnemyEnlightenmentCenterGoingFor) <= ACTION_RADIUS_POLITICIAN) {
                            
                    // }
                    // haveMessageToSend = true;
                    // neturalEnlightenmentCenter();
                }
            } else if (checkIfPoliticianBombNearby(robotInfo)) {
                politicianECBombNearby = true;
            }
        }
    }

    private static boolean checkIfPoliticianBombNearby(RobotInfo robotInfo) {
        if (robotInfo.getType() == RobotType.POLITICIAN && robotInfo.getTeam() == friendly
        && robotInfo.getConviction() >= POLITICIAN_EC_BOMB
        && (currentEnemyEnlightenmentCenterGoingFor != null && currentEnemyEnlightenmentCenterGoingFor.distanceSquaredTo(robotInfo.getLocation()) <= 10)) {
            return true;
        } else {
            return false;
        }
    }

    private static void setFlagMessage() throws GameActionException {
        if (enemyEnlightenmentCenterFound && turnsAroundEnemyEnlightenmentCenter < 2) {

            announceEnemyEnlightenmentCenterLocation();
            haveMessageToSend = false;
        } else if (enemyEnlightenmentCenterIsAround) {

            Communication.announceEnemyEnlightenmentCenterCurrentInfluence(ENEMY_ENLIGHTENMENT_CENTER_INFLUENCE);
            haveMessageToSend = false;
        } else if (enemyEnlightenmentCenterHasBeenConverted) {

            Communication.announceEnemyEnlightenmentCenterHasBeenConverted();
            enemyEnlightenmentCenterMapLocation.remove(0);
            enemyEnlightenmentCenterFound = false;
            enemyEnlightenmentCenterIsAround = false;
            turnsAroundEnemyEnlightenmentCenter = 0;
            haveMessageToSend = false;
        }
    }

    private static void checkIfRobotCanSenseEnemyEnlightenmentCenter() {
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;

        RobotInfo[] robots = robotController.senseNearbyRobots(sensorRadiusSquared, enemy);

        for (RobotInfo robotInfo : robots) {
            if (robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER
                    && (!enemyEnlightenmentCenterMapLocation.contains(robotInfo.getLocation())
                    || enemyEnlightenmentCenterMapLocation.size() < 1)) 
            {

                enemyEnlightenmentCenterMapLocation.add(robotInfo.getLocation());
                haveMessageToSend = true;
                enemyEnlightenmentCenterFound = true;
                currentEnemyEnlightenmentCenterGoingFor = robotInfo.getLocation();
            }
        }
    }

    private static void announceEnemyEnlightenmentCenterLocation() throws GameActionException 
    {
        MapLocation enemyCenterLocation = enemyEnlightenmentCenterMapLocation.get(0);
        currentEnemyEnlightenmentCenterGoingFor = enemyCenterLocation;
        Communication.sendLocation(enemyCenterLocation, ENEMY_ENLIGHTENMENT_CENTER_FOUND);
    }

    public static void setup() throws GameActionException 
    {
        setConstants();
        assignHomeEnlightenmentCenterLocation();
        setSquaresAroundEnlightenmentCenter();
        setRobotRole();        
    }

    private static void setRobotRole() {
        if (robotController.getInfluence() == 1) {
            robotRole = RobotRoles.Scout;
        } else if (robotController.getInfluence() == 2) {
            robotRole = RobotRoles.DefendHomeEnlightenmentCenter;
        } else {
            robotRole = RobotRoles.Follower;
        }
    }
}