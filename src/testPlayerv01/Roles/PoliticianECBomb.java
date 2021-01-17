package testPlayerv01.Roles;

import battlecode.common.*;
import testPlayerv01.PoliticianTest01;
import testPlayerv01.Service.Movement;

public class PoliticianECBomb extends PoliticianTest01 {
    // If has enemy ec location,
    /// go to it and decide to attack or not

    // If location is friendly, setMessage to that

    // get ID of friendly EC && periodically check it responds.
    /// check if spawn EC responds as well, if not, go back to it.

    // Check spawn for new coordinates && if not new coordinates,
    /// Scout around for enemies...

    // If enemies found, get close and empower if can convert
    /// or if there are more than 1 enemies nearby.

    public static void run() throws GameActionException {
        robotCurrentConviction = robotController.getConviction();
        empowerFactor = robotController.getEmpowerFactor(friendly, 0);
        moveRobot = true;

        if (!enemyEnlightenmentCenterFound) {
            checkIfSpawnEnlightenmentCenterHasEnemyLocation();
        }

        senseAreaForRobots();
        senseActionRadiusForRobots();

        decideIfEmpowerForEnlightenmentCenterBombs();

        if ((currentEnemyEnlightenmentCenterGoingFor != null
                && robotController.getLocation().distanceSquaredTo(currentEnemyEnlightenmentCenterGoingFor) > 2)
                && !politicianECBombNearby) {

            Movement.moveToEnemyEnlightenmentCenter(currentEnemyEnlightenmentCenterGoingFor);
        } else if (currentEnemyEnlightenmentCenterGoingFor != null && politicianECBombNearby
                && !lowestRobotIdOfFriendlies) {

            Movement.moveAwayFromLocation(currentEnemyEnlightenmentCenterGoingFor);
        }
    }

    protected static void decideIfEmpowerForEnlightenmentCenterBombs() throws GameActionException {
        if (robotController.canEmpower(enemyEnlightenmentCenterDistanceSquared) && enemyEnlightenmentCenterIsAround
                && enemyEnlightenmentCenterDistanceSquared <= 9) {
            if ((hasEnoughConvictionToConvertEnlightenmentCenter() || (lowestRobotIdOfFriendlies
                    && (enemyEnlightenmentCenterDistanceSquared < 2 || turnsNearEnemyEnlightenmentCenter >= 5)))) {
                robotController.empower(enemyEnlightenmentCenterDistanceSquared);
                return;
            }
            // Movement
            else if (politicianECBombNearby && !lowestRobotIdOfFriendlies) {
                moveAwayFromEnemyEnlightenmentCenter();
                println("HERE1");
                moveRobot = false;
            } else if (turnsNearEnemyEnlightenmentCenter >= 10
                    || countOfFrienliesInActionRadiusAroundEnemyEnlightenmentcenter <= 1) {
                robotController.empower(enemyEnlightenmentCenterDistanceSquared);
                println("HERE2");
                return;
            }
        } else if (robotController.getRoundNum() >= MIDDLE_GAME_ROUND_START && !enemyEnlightenmentCenterIsAround
                && checkIfPoliticianShouldEmpower()) {
            robotController.empower(distanceToclosestRobotMapLocation);
            return;
        } else {
            moveRobot = true;
        }
    }

    static boolean hasEnoughConvictionToConvertEnlightenmentCenter() {
        getSumOfEnemyConvictionInEnemyEnlightenmentRadiusSquared();
        int countOfAllRobotsNearby = countOfEnemiesInActionRadiusAroundEnemyEnlightenmentcenter
                + countOfFrienliesInActionRadiusAroundEnemyEnlightenmentcenter;
        int remainderOfEnemyConviction = 0;
        boolean hasEnoughToEmpower = false;

        if (countOfEnemiesInActionRadiusAroundEnemyEnlightenmentcenter > 0) {
            remainderOfEnemyConviction = (int) (enemyEnlightenmentCenterConviction
                    - (getCurrentConviction() / countOfAllRobotsNearby));
        } else {
            remainderOfEnemyConviction = enemyEnlightenmentCenterConviction - robotCurrentConviction;
        }

        if (remainderOfEnemyConviction < 0) {
            hasEnoughToEmpower = true;
        }

        return hasEnoughToEmpower;
    }
}