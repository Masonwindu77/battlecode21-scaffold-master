package testPlayerv01.Roles;

import battlecode.common.*;
import testPlayerv01.PoliticianTest01;
import testPlayerv01.Service.Communication;
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
            Communication.checkIfSpawnEnlightenmentCenterHasEnemyLocation();
            setCurrentEnemyEnlightenmentCenterGoingFor();
        }

        senseAreaForRobots();
        senseActionRadiusForRobots();

        if (enemyEnlightenmentCenterIsAround) 
        {
            Communication.announceEnemyEnlightenmentCenterCurrentInfluence(ENEMY_ENLIGHTENMENT_CENTER_INFLUENCE);
        }
        else if (enemyEnlightenmentCenterHasBeenConverted)
        {
            Communication.announceEnemyEnlightenmentCenterHasBeenConverted();
            currentEnemyEnlightenmentCenterGoingFor = null;
        }

        if ((enemyEnlightenmentCenterFound || enemyEnlightenmentCenterIsAround) 
            && !homeEnlightenmentCenterSurrounded() && !empowerTheHomeBase()) 
        {
            decideIfEmpowerForEnlightenmentCenterBombs();
        }
        else if (empowerTheHomeBase())
        {
            if (robotController.canEmpower(distanceToFriendlyEnlightenmentCenter)) {
                robotController.empower(distanceToFriendlyEnlightenmentCenter);
            }
        }
        else if (homeEnlightenmentCenterSurrounded()) 
        {
            if (robotController.canEmpower(ACTION_RADIUS_POLITICIAN)) {
                robotController.empower(ACTION_RADIUS_POLITICIAN);
            }
        }
        else if (robotController.getRoundNum() >= MIDDLE_GAME_ROUND_START 
            && checkIfPoliticianShouldEmpower()) 
        {
            if (robotController.canEmpower(distanceToclosestRobotMapLocation)) {
                robotController.empower(distanceToclosestRobotMapLocation);
            }
            return;
        }        

        if (moveRobot) 
        {   // Move closer if not adjacent even if other bomb nearby as long as you lowest.
            // TODO: Add closest here too
            if ((currentEnemyEnlightenmentCenterGoingFor != null
                && !robotController.getLocation().isAdjacentTo(currentEnemyEnlightenmentCenterGoingFor))
                && (!politicianECBombNearby || (politicianECBombNearby && lowestRobotIdOfFriendlies))) 
            {
                Movement.moveToEnemyEnlightenmentCenter(currentEnemyEnlightenmentCenterGoingFor);
            }
            // Move away if not lowest ID
            else if (currentEnemyEnlightenmentCenterGoingFor != null 
            && politicianECBombNearby
            && !lowestRobotIdOfFriendlies) 
            {
                Movement.moveAwayFromLocation(currentEnemyEnlightenmentCenterGoingFor);
            }
            // Move towards if not nearby.
            else if(currentEnemyEnlightenmentCenterGoingFor != null
            && !robotController.getLocation().isAdjacentTo(currentEnemyEnlightenmentCenterGoingFor))
            {
                Movement.moveToEnemyEnlightenmentCenter(currentEnemyEnlightenmentCenterGoingFor);
            } 
            // Otherwise scout around
            else if (currentEnemyEnlightenmentCenterGoingFor == null)
            {
                Movement.scoutAction();
            }
        }
        
    }

    // TODO: Make a check for closest
    protected static void decideIfEmpowerForEnlightenmentCenterBombs() throws GameActionException {
        if (robotController.canEmpower(enemyEnlightenmentCenterDistanceSquared) 
            && enemyEnlightenmentCenterIsAround) 
        {
            // If it has enough, convert it.
            if (hasEnoughConvictionToConvertEnlightenmentCenter()) 
            {
                robotController.empower(enemyEnlightenmentCenterDistanceSquared);
                return;
            }
            // If adjacent to it, empower
            else if (robotController.getLocation().isAdjacentTo(currentEnemyEnlightenmentCenterGoingFor)
            && countOfFrienliesInActionRadiusAroundEnemyEnlightenmentcenter < 1 )
            {
                robotController.empower(enemyEnlightenmentCenterDistanceSquared);
                return;
            }
            // If it is the first in line and it's been stuck near it, attack.
            else if (lowestRobotIdOfFriendlies 
            && (enemyEnlightenmentCenterDistanceSquared <= 5 && turnsNearEnemyEnlightenmentCenter >= 10))
            {
                robotController.empower(enemyEnlightenmentCenterDistanceSquared);
                return;
            }
            // Movement
            else if (politicianECBombNearby && !lowestRobotIdOfFriendlies) 
            {
                moveAwayFromEnemyEnlightenmentCenter();
                moveRobot = false;
            } 
        } else {
            moveRobot = true;
        }
    }

    static boolean hasEnoughConvictionToConvertEnlightenmentCenter() 
    {
        getSumOfEnemyConvictionInEnemyEnlightenmentRadiusSquared();
        int countOfAllRobotsNearby = countOfEnemiesInActionRadiusAroundEnemyEnlightenmentcenter
                + countOfFrienliesInActionRadiusAroundEnemyEnlightenmentcenter;
        int remainderOfEnemyConviction = 0;
        boolean hasEnoughToEmpower = false;

        if (countOfEnemiesInActionRadiusAroundEnemyEnlightenmentcenter > 0) {
            remainderOfEnemyConviction = (int) (enemyEnlightenmentCenterConviction - (getCurrentConviction() / countOfAllRobotsNearby));
        } else {
            remainderOfEnemyConviction = enemyEnlightenmentCenterConviction - robotCurrentConviction;
        }

        if (remainderOfEnemyConviction < 0) {
            hasEnoughToEmpower = true;
        }

        return hasEnoughToEmpower;
    }
}