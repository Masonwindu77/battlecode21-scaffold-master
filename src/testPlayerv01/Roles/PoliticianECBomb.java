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

    static boolean goToNeutralEnlightenmentCenter;
    static boolean goToEnemyEnlightenmentCenter;

    public static void run() throws GameActionException 
    {
        goToNeutralEnlightenmentCenter = false;
        goToEnemyEnlightenmentCenter = false;

        if (enemyEnlightenmentCenterFound && neutralEnlightenmentCenterFound) 
        {
            if (robotController.getLocation().distanceSquaredTo(currentEnemyEnlightenmentCenterGoingFor) 
                <= robotController.getLocation().distanceSquaredTo(currentNeutralEnlightenmentCenterGoingFor)) 
            {
                goToEnemyEnlightenmentCenter = true;
            }
            else
            {
                goToNeutralEnlightenmentCenter = true;
            }
        }
        else if (enemyEnlightenmentCenterFound) 
        {
            goToEnemyEnlightenmentCenter = true;
        }
        else if (neutralEnlightenmentCenterFound)
        {
            goToNeutralEnlightenmentCenter = true;
        }


        if (goToEnemyEnlightenmentCenter) 
        {
            attackEnemyEnlightenmentCenterLocation();
        }
        else if (goToNeutralEnlightenmentCenter) 
        {
            attackNeutralEnlightenmentCenterLocation();            
        }        
        else 
        {
            if (robotController.getRoundNum() >= MIDDLE_GAME_ROUND_START 
            && checkIfPoliticianShouldEmpower() && distanceToClosestRobotMapLocation != 0) 
            {
                if (robotController.canEmpower(distanceToClosestRobotMapLocation)) {
                    robotController.empower(distanceToClosestRobotMapLocation);
                }
                return;
            }

            if (hasTarget && closestRobotMapLocation != null) 
            {
                Movement.moveToTargetLocation(closestRobotMapLocation);
            }
            else 
            {
                Movement.scoutAction();
            }
        }
    }

    protected static void attackEnemyEnlightenmentCenterLocation() throws GameActionException
    {
        if (enemyEnlightenmentCenterIsAround
            && !homeEnlightenmentCenterSurrounded() && !empowerTheHomeBase()) 
        {
            decideIfEmpowerForEnlightenmentCenterBombs();
        }
        else if (empowerTheHomeBase() && robotController.canEmpower(distanceToFriendlyEnlightenmentCenter))
        {
            robotController.empower(distanceToFriendlyEnlightenmentCenter);
            return;
        }
        else if (homeEnlightenmentCenterSurrounded() && robotController.canEmpower(ACTION_RADIUS_POLITICIAN)) 
        {
            robotController.empower(ACTION_RADIUS_POLITICIAN);
            return;
        }
        else if (robotController.getRoundNum() >= MIDDLE_GAME_ROUND_START 
            && checkIfPoliticianShouldEmpower() 
            && distanceToClosestRobotMapLocation != 0
            && robotController.canEmpower(distanceToClosestRobotMapLocation)) 
        {
            robotController.empower(distanceToClosestRobotMapLocation);
            return;
        }

        if (moveRobot) 
        {   // Move closer if not adjacent even if other bomb nearby as long as you lowest.
            // TODO: Add closest here too
            if ((currentEnemyEnlightenmentCenterGoingFor != null
                && !robotController.getLocation().isAdjacentTo(currentEnemyEnlightenmentCenterGoingFor))) 
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
            else if (hasTarget && closestRobotMapLocation != null) 
            {
                Movement.moveToTargetLocation(closestRobotMapLocation);
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
        if (robotController.canEmpower(distanceToEnemyEnlightenmentCenter)) 
        {
            // If it has enough, convert it.
            if (hasEnoughConvictionToConvertEnlightenmentCenter() || canUseFullEmpowerWithoutDilution) 
            {
                robotController.empower(distanceToEnemyEnlightenmentCenter);
                return;
            }
            // Movement
            else if (politicianECBombNearby 
                && !lowestRobotIdOfFriendlies 
                && !robotController.getLocation().isAdjacentTo(currentEnemyEnlightenmentCenterGoingFor)) 
            {
                moveAwayFromEnemyEnlightenmentCenter();
                moveRobot = false;
            }
            // If adjacent to it, empower
            else if (robotController.getLocation().isAdjacentTo(currentEnemyEnlightenmentCenterGoingFor))
            {
                robotController.empower(distanceToEnemyEnlightenmentCenter);
                return;
            }
            // If it is the first in line and it's been stuck near it, attack.
            else if (lowestRobotIdOfFriendlies 
            && (distanceToEnemyEnlightenmentCenter <= 5 && turnsNearEnemyEnlightenmentCenterForAttacking >= 10))
            {
                robotController.empower(distanceToEnemyEnlightenmentCenter);
                return;
            }             
        } 
        else 
        {
            moveRobot = true;
        }
    }

    static boolean hasEnoughConvictionToConvertEnlightenmentCenter() 
    {
        getSumOfEnemyConvictionInEnemyEnlightenmentRadiusSquared();
        int countOfAllRobotsNearby = countOfEnemiesInActionRadiusAroundEnemyEnlightenmentcenter
                + countOfFriendliesInActionRadiusAroundEnemyEnlightenmentcenter;
        int remainderOfEnemyConviction = 0;
        boolean hasEnoughToEmpower = false;
        canUseFullEmpowerWithoutDilution = false;

        if (countOfAllRobotsNearby > 1) 
        {
            remainderOfEnemyConviction = (int) (enemyEnlightenmentCenterCurrentInfluence - ((robotCurrentConviction - POLITICIAN_TAX) / countOfAllRobotsNearby));
        } 
        else 
        {
            remainderOfEnemyConviction = enemyEnlightenmentCenterCurrentInfluence - (robotCurrentConviction - POLITICIAN_TAX);
        }

        if (remainderOfEnemyConviction < 0) {
            hasEnoughToEmpower = true;
        }

        if (countOfAllRobotsNearby == 1) 
        {
            canUseFullEmpowerWithoutDilution = true;
        }

        return hasEnoughToEmpower;
    }

    protected static void attackNeutralEnlightenmentCenterLocation() throws GameActionException
    {
        decideIfEmpowerForNeutralEnlightenmentCenter();       

        if (moveRobot) 
        {
            if (currentNeutralEnlightenmentCenterGoingFor != null
            && !robotController.getLocation().isAdjacentTo(currentNeutralEnlightenmentCenterGoingFor)
            && (!politicianECBombNearby 
                || (politicianECBombNearby 
                    && (lowestRobotIdOfFriendlies || closestRobotToNeutralEnlightenmentCenter)))) 
            {
                Movement.moveToNeutralEnlightenmentCenter(currentNeutralEnlightenmentCenterGoingFor);
            }
            else if (currentNeutralEnlightenmentCenterGoingFor != null 
            && politicianECBombNearby
            && !lowestRobotIdOfFriendlies
            && !closestRobotToNeutralEnlightenmentCenter)
            {
                Movement.moveAwayFromLocation(currentNeutralEnlightenmentCenterGoingFor);
            }
            else if(currentNeutralEnlightenmentCenterGoingFor != null
            && !robotController.getLocation().isAdjacentTo(currentNeutralEnlightenmentCenterGoingFor))
            {
                Movement.moveToNeutralEnlightenmentCenter(currentNeutralEnlightenmentCenterGoingFor);
            }
            else if (hasTarget && closestRobotMapLocation != null) 
            {
                Movement.moveToTargetLocation(closestRobotMapLocation);
            }
            else if (currentNeutralEnlightenmentCenterGoingFor == null)
            {
                Movement.scoutAction();                
            }
        }
    }

    static void decideIfEmpowerForNeutralEnlightenmentCenter() throws GameActionException
    {
        if (robotController.canEmpower(distanceToNeutralEnlightenmentCenter) 
            && neutralEnlightenmentCenterIsAround) 
        {
            // If it has enough, convert it.
            if (hasEnoughConvictionToConvertNeutralEnlightenmentCenter() || canUseFullEmpowerWithoutDilution) 
            {
                robotController.empower(distanceToNeutralEnlightenmentCenter);
                return;
            }
            // If adjacent to it, empower
            else if (currentNeutralEnlightenmentCenterGoingFor != null 
            && robotController.getLocation().isAdjacentTo(currentNeutralEnlightenmentCenterGoingFor)
            && countOfFriendliesInActionRadiusAroundNeutralEnlightenmentcenter < 1)
            {
                robotController.empower(distanceToNeutralEnlightenmentCenter);
                return;
            }
            // If it is the first in line and it's been stuck near it, attack.
            else if (lowestRobotIdOfFriendlies 
            && (distanceToNeutralEnlightenmentCenter <= 5 && turnsNearNeutralEnlightenmentCenter >= 15))
            {
                robotController.empower(distanceToNeutralEnlightenmentCenter);
                return;
            }
            else if (closestRobotToNeutralEnlightenmentCenter && canUseFullEmpowerWithoutDilution) 
            {
                robotController.empower(distanceToNeutralEnlightenmentCenter);
                return;
            }
            // Movement
            else if (politicianECBombNearby 
                && !lowestRobotIdOfFriendlies 
                && !closestRobotToNeutralEnlightenmentCenter) 
            {
                Movement.moveAwayFromLocation(currentNeutralEnlightenmentCenterGoingFor);
                moveRobot = false;
            } 
        } 
        else 
        {
            moveRobot = true;
        }
    }

    protected static boolean hasEnoughConvictionToConvertNeutralEnlightenmentCenter()
    {
        getSumOfConvictionInNeutralEnlightenmentRadiusSquared();
        int countOfAllRobotsNearby = countOfEnemiesInActionRadiusAroundNeutralEnlightenmentcenter
                + countOfFriendliesInActionRadiusAroundNeutralEnlightenmentcenter;
        int remainderOfNeutralConviction = 0;
        boolean hasEnoughToEmpower = false;
        canUseFullEmpowerWithoutDilution = false;

        if (countOfAllRobotsNearby > 1) 
        {
            remainderOfNeutralConviction = (int) (neutralEnlightenmentCenterCurrentInfluence - (robotCurrentConviction - POLITICIAN_TAX) / countOfAllRobotsNearby);
        } 
        else 
        {
            remainderOfNeutralConviction = neutralEnlightenmentCenterCurrentInfluence - (robotCurrentConviction - POLITICIAN_TAX);
        }

        if (remainderOfNeutralConviction < 0) 
        {
            hasEnoughToEmpower = true;
        }

        if (countOfAllRobotsNearby == 1) 
        {
            canUseFullEmpowerWithoutDilution = true;
        }

        return hasEnoughToEmpower;
    }
}