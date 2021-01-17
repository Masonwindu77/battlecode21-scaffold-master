package testPlayerv01;

import java.util.Random;

import battlecode.common.*;
import testPlayerv01.Service.Movement;
import testPlayerv01.Roles.PoliticianECBomb;

public class PoliticianTest01 extends RobotPlayer {
    // Who is around
    static boolean enemyPoliticianIsAround;
    protected static boolean enemyEnlightenmentCenterIsAround;
    protected static int enemyEnlightenmentCenterConviction;
    static boolean enemyEnlightenmentCenterIsLessThanTwoDistanceAway;
    protected static int enemyEnlightenmentCenterDistanceSquared;

    // Target Following
    static boolean hasTarget;
    protected static int distanceToclosestRobotMapLocation;
    static MapLocation closestRobotMapLocation;

    // Counting robots
    static int countOfEnemies;
    static int countOfFriendlies;
    static int countOfEnemiesInActionRadius;
    static int countOfFriendliesInActionRadius;
    protected static int countOfEnemiesInActionRadiusAroundEnemyEnlightenmentcenter;
    protected static int countOfFrienliesInActionRadiusAroundEnemyEnlightenmentcenter;

    protected static int turnsNearEnemyEnlightenmentCenter;

    // Sum of enemy robot conviction
    static int sumOfEnemyConvictionNearby;
    static int sumOfEnemyConviciontInActionRadiusAroundEnemyEnlightenmentcenter;

    static void run() throws GameActionException {
        if (robotRole == RobotRoles.PoliticianEnlightenmentCenterBomb) {
            PoliticianECBomb.run();
        } else {
            runThis();
        }

    }

    protected static void runThis() throws GameActionException {
        enemyEnlightenmentCenterConviction = 0;
        robotCurrentConviction = robotController.getConviction();
        empowerFactor = robotController.getEmpowerFactor(friendly, 0);
        moveRobot = true;

        senseAreaForRobots();
        senseActionRadiusForRobots();

        if (currentEnemyEnlightenmentCenterGoingFor == null) {
            MapLocation enemyCenterLocation = enemyEnlightenmentCenterMapLocation
                    .get(enemyEnlightenmentCenterMapLocation.size());
            currentEnemyEnlightenmentCenterGoingFor = enemyCenterLocation; // TODO: can make this cleaner
        }

        // TODO: Make a check for if the EC has been converted.
        // TODO: Make a check for attacking enemies even with allies around

        decideIfEmpowerForNonEnlightenmentCenterBombs();

        if (hasTarget) {
            if (countOfEnemiesInActionRadius >= 2 && countOfEnemiesInActionRadius < 4
                    && robotController.canEmpower(distanceToclosestRobotMapLocation)) {
                robotController.empower(distanceToclosestRobotMapLocation);
                return;
            } else if (robotController.getRoundNum() >= END_GAME_ROUND_STRAT
                    && robotController.canEmpower(distanceToclosestRobotMapLocation)) {
                robotController.empower(distanceToclosestRobotMapLocation);
                return;
            } else if (robotController.getRoundNum() >= END_GAME_ROUND_STRAT
                    && robotController.canEmpower(ACTION_RADIUS_POLITICIAN) && countOfEnemiesInActionRadius != 0) {
                robotController.empower(distanceToclosestRobotMapLocation);
                return;
            } else if (closestRobotMapLocation != null) {
                Movement.basicBugMovement(closestRobotMapLocation);
            } else {
                hasTarget = false;
            }
        }

        if (moveRobot || politicianECBombNearby) // Movement
        {

            if (enemyEnlightenmentCenterIsAround && politicianECBombNearby) {
                moveAwayFromEnemyEnlightenmentCenter();
            } else if (turnCount < 75) {
                tryMove(randomDirection());
            } else {
                Movement.scoutAction();
            }
        }
        // Follower Role

        // Leader Role

        // Defender Role

        // Scout Role
        if (robotRole == RobotRoles.Scout) {
            Movement.scoutAction();
        }
        // Testing
    }

    protected static void senseAreaForRobots() throws GameActionException {
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;

        enemyEnlightenmentCenterIsAround = false;
        countOfEnemies = 0;
        countOfFriendlies = 0;

        hasTarget = false;
        RobotInfo[] allRobotInfos = robotController.senseNearbyRobots(sensorRadiusSquared);

        for (RobotInfo robotInfo : allRobotInfos) {
            if (robotInfo.getType() == RobotType.POLITICIAN && robotInfo.getTeam() == enemy) {
                enemyPoliticianIsAround = true;
                countOfEnemies++;
                if (!hasTarget) {
                    distanceToclosestRobotMapLocation = 0;
                    getClosestEnemyRobot(robotInfo);
                }

            } else if (robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER && robotInfo.getTeam() == enemy) {
                enemyEnlightenmentCenterIsAround = true;
                enemyEnlightenmentCenterDistanceSquared = robotInfo.getLocation()
                        .distanceSquaredTo(robotController.getLocation());
                enemyEnlightenmentCenterConviction = robotInfo.getConviction();
                countOfEnemies++;
                turnsNearEnemyEnlightenmentCenter++;
                currentEnemyEnlightenmentCenterGoingFor = robotInfo.getLocation();
            } else if (robotInfo.getTeam() == friendly) {
                countOfFriendlies++;
            }

            // If enemy enlightenment center has been converted.
            if (currentEnemyEnlightenmentCenterGoingFor == robotInfo.getLocation() && robotInfo.getTeam() == friendly
                    && robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                enemyEnlightenmentCenterFound = false;
                currentEnemyEnlightenmentCenterGoingFor = null;
            }
        }
    }

    protected static void senseActionRadiusForRobots() {
        int actionRadiusSquared = robotController.getType().actionRadiusSquared;
        RobotInfo[] allRobotInfos = robotController.senseNearbyRobots(actionRadiusSquared);
        sumOfEnemyConvictionNearby = 0;
        countOfEnemiesInActionRadius = 0;
        countOfFriendliesInActionRadius = 0;
        politicianECBombNearby = false;

        for (RobotInfo robotInfo : allRobotInfos) {
            if (robotInfo.getTeam() == enemy) {
                sumOfEnemyConvictionNearby += robotInfo.getConviction();
                countOfEnemiesInActionRadius++;
            } else if (robotInfo.getTeam() == friendly && robotInfo.getType() == RobotType.POLITICIAN
                    && robotInfo.getConviction() >= POLITICIAN_EC_BOMB && enemyEnlightenmentCenterIsAround) {
                politicianECBombNearby = true;
                if (robotInfo.getID() > robotController.getID()
                        && robotRole == RobotRoles.PoliticianEnlightenmentCenterBomb) {
                    lowestRobotIdOfFriendlies = true;
                }
            } else if (robotInfo.getTeam() == friendly) {
                countOfFriendliesInActionRadius++;
            }
        }
    }

    private static void getClosestEnemyRobot(RobotInfo robotInfo) {
        if (robotController.getLocation()
                .distanceSquaredTo(robotInfo.getLocation()) <= distanceToclosestRobotMapLocation
                || distanceToclosestRobotMapLocation == 0) {
            closestRobotMapLocation = robotInfo.getLocation();
            distanceToclosestRobotMapLocation = robotController.getLocation()
                    .distanceSquaredTo(robotInfo.getLocation());
        }
    }

    protected static void getSumOfEnemyConvictionInEnemyEnlightenmentRadiusSquared() {
        countOfEnemiesInActionRadiusAroundEnemyEnlightenmentcenter = 0;
        sumOfEnemyConviciontInActionRadiusAroundEnemyEnlightenmentcenter = 0;
        countOfFrienliesInActionRadiusAroundEnemyEnlightenmentcenter = 0;

        RobotInfo[] allRobotInfos = robotController.senseNearbyRobots(enemyEnlightenmentCenterDistanceSquared);
        for (RobotInfo robotInfo : allRobotInfos) {
            if (robotInfo.getTeam() == enemy && robotInfo.getType() != RobotType.ENLIGHTENMENT_CENTER) {
                countOfEnemiesInActionRadiusAroundEnemyEnlightenmentcenter++;
                sumOfEnemyConviciontInActionRadiusAroundEnemyEnlightenmentcenter += robotInfo.getConviction();
            } else if (robotInfo.getTeam() == friendly) {
                countOfFrienliesInActionRadiusAroundEnemyEnlightenmentcenter++;
            }
        }
    }

    static void decideIfEmpowerForNonEnlightenmentCenterBombs() throws GameActionException {
        if (checkIfPoliticianShouldEmpower()) {
            robotController.empower(ACTION_RADIUS_POLITICIAN); // TODO: Get the actual radius not the full thing
            return;
        } else if (countOfEnemies != 0) {
            hasTarget = true;
        } else {
            moveRobot = true;
        }
    }

    protected static boolean checkIfPoliticianShouldEmpower() {
        return ((countOfEnemiesInActionRadius >= 2) || (countOfEnemiesInActionRadius != 0))
                && robotController.canEmpower(ACTION_RADIUS_POLITICIAN);
    }

    protected static void moveAwayFromEnemyEnlightenmentCenter() throws GameActionException {
        Movement.moveAwayFromLocation(currentEnemyEnlightenmentCenterGoingFor);
    }

    static boolean empowerCanConvertEnemyAtMaxRadius() {
        boolean empowerCanConvertEnemy = false;
        int countOfAllRobotsInActionRadius = countOfEnemiesInActionRadius + countOfFriendliesInActionRadius;
        int remainderOfEnemyConviction = 0;

        if (countOfAllRobotsInActionRadius > 0) {
            remainderOfEnemyConviction = (int) (sumOfEnemyConvictionNearby
                    - (getCurrentConviction() / countOfAllRobotsInActionRadius));
        }

        if (remainderOfEnemyConviction < 0) {
            empowerCanConvertEnemy = true;
        }

        return empowerCanConvertEnemy;
    }

    protected static double getCurrentConviction() {
        return (robotCurrentConviction * empowerFactor) - POLITICIAN_TAX;
    }

    static void setup() {
        assignHomeEnlightenmentCenterLocation();
        robotCurrentInfluence = robotController.getInfluence();
        assignRobotRole();
        enemy = robotController.getTeam().opponent();
        friendly = robotController.getTeam();
        randomInteger = new Random();
    }

    static void assignRobotRole() {
        if (robotCurrentInfluence >= POLITICIAN_EC_BOMB) {
            robotRole = RobotRoles.PoliticianEnlightenmentCenterBomb;
        } else if (robotCurrentInfluence == POLITICIAN_LEADER) {
            robotRole = RobotRoles.Leader;
        } else if (robotCurrentInfluence >= POLITICIAN_FOLLOWER) {
            robotRole = RobotRoles.Follower;
        }
    }
}
