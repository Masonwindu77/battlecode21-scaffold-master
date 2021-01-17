package testPlayerv01;

import java.util.Random;

import battlecode.common.*;
import testPlayerv01.Service.Movement;

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

        // Scout Role
        if (robotRole == RobotRoles.Scout && !enemyEnlightenmentCenterFound) {
            Movement.scoutAction();
        } else if (enemyEnlightenmentCenterFound) {
            if (politicianECBombNearby) {
                Movement.moveAwayFromLocation(currentEnemyEnlightenmentCenterGoingFor);
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

    private static void setFlagMessage() throws GameActionException {
        if (enemyEnlightenmentCenterFound && turnsAroundEnemyEnlightenmentCenter < 2) {

            announceEnemyEnlightenmentCenterLocation();
            haveMessageToSend = false;
        } else if (enemyEnlightenmentCenterIsAround) {

            announceEnemyEnlightenmentCenterCurrentInfluence(ENEMY_ENLIGHTENMENT_CENTER_INFLUENCE);
            haveMessageToSend = false;
        } else if (enemyEnlightenmentCenterHasBeenConverted) {

            announceEnemyEnlightenmentCenterHasBeenConverted();
            enemyEnlightenmentCenterMapLocation.clear();
            enemyEnlightenmentCenterFound = false;
            enemyEnlightenmentCenterIsAround = false;
            turnsAroundEnemyEnlightenmentCenter = 0;
            haveMessageToSend = false;
        }
    }

    private static void announceEnemyEnlightenmentCenterCurrentInfluence(int extraInformation)
            throws GameActionException {
        int encodedflag = (extraInformation << (2 * NBITS)) + enemyEnlightenmentCenterCurrentInfluence;

        if (robotController.canSetFlag(encodedflag)) {
            robotController.setFlag(encodedflag);
        }
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

    private static void checkIfRobotCanSenseEnemyEnlightenmentCenter() {
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;

        RobotInfo[] robots = robotController.senseNearbyRobots(sensorRadiusSquared, enemy);

        for (RobotInfo robotInfo : robots) {
            if (robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER
                    && (!enemyEnlightenmentCenterMapLocation.containsValue(robotInfo.getLocation())
                            || enemyEnlightenmentCenterMapLocation.size() < 1)) {

                enemyEnlightenmentCenterMapLocation.put(enemyEnlightenmentCenterMapLocation.size() + 1,
                        robotInfo.getLocation());

                haveMessageToSend = true;
                enemyEnlightenmentCenterFound = true;
                currentEnemyEnlightenmentCenterGoingFor = robotInfo.getLocation();
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
                    if (currentEnemyEnlightenmentCenterGoingFor != null && robotController.getLocation()
                            .distanceSquaredTo(currentEnemyEnlightenmentCenterGoingFor) <= ACTION_RADIUS_POLITICIAN) {

                    }
                } else if (robotInfo.getTeam() == friendly
                        && robotInfo.getLocation() == currentEnemyEnlightenmentCenterGoingFor) {

                    enemyEnlightenmentCenterFound = false;
                    haveMessageToSend = true;
                } else if (robotInfo.getTeam() != enemy && robotInfo.getTeam() != friendly) {
                    // haveMessageToSend = true;
                    // neturalEnlightenmentCenter();
                }
            } else if (checkIfPoliticianBombNearby(robotInfo)) {
                politicianECBombNearby = true;
            }
        }
    }

    private static boolean checkIfPoliticianBombNearby(RobotInfo robotInfo) {
        if (robotInfo.getType() == RobotType.POLITICIAN && robotInfo.getConviction() >= POLITICIAN_EC_BOMB
                && enemyEnlightenmentCenterIsAround && (currentEnemyEnlightenmentCenterGoingFor != null
                        && currentEnemyEnlightenmentCenterGoingFor.distanceSquaredTo(robotInfo.getLocation()) <= 9)) {
            return true;
        } else {
            return false;
        }

    }

    private static void announceEnemyEnlightenmentCenterLocation() throws GameActionException {
        MapLocation enemyCenterLocation = enemyEnlightenmentCenterMapLocation
                .get(enemyEnlightenmentCenterMapLocation.size());
        currentEnemyEnlightenmentCenterGoingFor = enemyCenterLocation;
        sendLocation(enemyCenterLocation, ENEMY_ENLIGHTENMENT_CENTER_FOUND);
    }

    private static void announceEnemyEnlightenmentCenterHasBeenConverted() throws GameActionException {
        sendLocation(currentEnemyEnlightenmentCenterGoingFor, ENEMY_ENLIGHTENMENT_CENTER_CONVERTED);
    }

    public static void setup() throws GameActionException {
        // checkIfEnemeyEnlightenmentCenterHasBeenFound();
        assignHomeEnlightenmentCenterLocation();
        setRobotRole();
        enemy = robotController.getTeam().opponent();
        friendly = robotController.getTeam();
        randomInteger = new Random();
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