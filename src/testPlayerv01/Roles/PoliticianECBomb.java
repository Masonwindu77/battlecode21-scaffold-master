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
        if (goToEnemyEnlightenmentCenter && enemyEnlightenmentCenterHasBeenConverted) 
        {
            goToEnemyEnlightenmentCenter = false;
            goToNeutralEnlightenmentCenter = true;
        }
        else if (goToNeutralEnlightenmentCenter && neutralEnlightenmentCenterHasBeenConverted)
        {
            goToNeutralEnlightenmentCenter = false;
            goToEnemyEnlightenmentCenter = true;   
        }

        if (enemyEnlightenmentCenterFound && neutralEnlightenmentCenterFound) 
        {
            if (robotController.getLocation().distanceSquaredTo(currentEnemyEnlightenmentCenterGoingFor) 
            < robotController.getLocation().distanceSquaredTo(currentNeutralEnlightenmentCenterGoingFor)) 
            {
                goToEnemyEnlightenmentCenter = true;
                goToNeutralEnlightenmentCenter = false;
            }
            else
            {
                goToNeutralEnlightenmentCenter = true;
                goToEnemyEnlightenmentCenter = false;
            }
        }
        else if (enemyEnlightenmentCenterFound && !neutralEnlightenmentCenterFound) 
        {
            goToEnemyEnlightenmentCenter = true;
            goToNeutralEnlightenmentCenter = false;
        }
        else if (neutralEnlightenmentCenterFound)
        {
            goToNeutralEnlightenmentCenter = true;
            goToEnemyEnlightenmentCenter = false;
        }

        if (goToEnemyEnlightenmentCenter) 
        {
            attackEnemyEnlightenmentCenterLocation();
        }
        else if (goToNeutralEnlightenmentCenter) 
        {
            decideIfEmpowerForNeutralEnlightenmentCenter();       

            if (moveRobot) 
            {
                if (currentNeutralEnlightenmentCenterGoingFor != null
                && !robotController.getLocation().isAdjacentTo(currentNeutralEnlightenmentCenterGoingFor)
                && (!politicianECBombNearby || (politicianECBombNearby && lowestRobotIdOfFriendlies))) 
                {
                    Movement.moveToNeutralEnlightenmentCenter(currentNeutralEnlightenmentCenterGoingFor);
                }
                else if (currentNeutralEnlightenmentCenterGoingFor != null 
                && politicianECBombNearby
                && !lowestRobotIdOfFriendlies)
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
                && checkIfPoliticianShouldEmpower() && distanceToClosestRobotMapLocation != 0) 
            {
                if (robotController.canEmpower(distanceToClosestRobotMapLocation)) {
                    robotController.empower(distanceToClosestRobotMapLocation);
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
        if (robotController.canEmpower(distanceToEnemyEnlightenmentCenter) 
            && enemyEnlightenmentCenterIsAround) 
        {
            // If it has enough, convert it.
            if (hasEnoughConvictionToConvertEnlightenmentCenter()) 
            {
                robotController.empower(distanceToEnemyEnlightenmentCenter);
                return;
            }
            // If adjacent to it, empower
            else if (robotController.getLocation().isAdjacentTo(currentEnemyEnlightenmentCenterGoingFor)
            && countOfFriendliesInActionRadiusAroundEnemyEnlightenmentcenter < 1 )
            {
                robotController.empower(distanceToEnemyEnlightenmentCenter);
                return;
            }
            // If it is the first in line and it's been stuck near it, attack.
            else if (lowestRobotIdOfFriendlies 
            && (distanceToEnemyEnlightenmentCenter <= 5 && turnsNearEnemyEnlightenmentCenter >= 15))
            {
                robotController.empower(distanceToEnemyEnlightenmentCenter);
                return;
            }
            // Movement
            else if (politicianECBombNearby && !lowestRobotIdOfFriendlies) 
            {
                moveAwayFromEnemyEnlightenmentCenter();
                moveRobot = false;
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

        if (countOfAllRobotsNearby > 0) {
            remainderOfEnemyConviction = (int) (enemyEnlightenmentCenterCurrentInfluence - (getCurrentConviction() / countOfAllRobotsNearby));
        } 
        else 
        {
            remainderOfEnemyConviction = enemyEnlightenmentCenterCurrentInfluence - robotCurrentConviction;
        }

        if (remainderOfEnemyConviction < 0) {
            hasEnoughToEmpower = true;
        }

        return hasEnoughToEmpower;
    }

    static void decideIfEmpowerForNeutralEnlightenmentCenter() throws GameActionException
    {
        if (robotController.canEmpower(distanceToNeutralEnlightenmentCenter) 
            && neutralEnlightenmentCenterIsAround) 
        {
            // If it has enough, convert it.
            if (hasEnoughConvictionToConvertNeutralEnlightenmentCenter()) 
            {
                robotController.empower(distanceToNeutralEnlightenmentCenter);
                return;
            }
            // If adjacent to it, empower
            else if (currentNeutralEnlightenmentCenterGoingFor != null 
            && robotController.getLocation().isAdjacentTo(currentNeutralEnlightenmentCenterGoingFor)
            && countOfFriendliesInActionRadiusAroundNeutralEnlightenmentcenter < 1 )
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
            // Movement
            else if (politicianECBombNearby && !lowestRobotIdOfFriendlies) 
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

        if (countOfAllRobotsNearby > 0) 
        {
            remainderOfNeutralConviction = (int) (neutralEnlightenmentCenterCurrentInfluence - (getCurrentConviction() / countOfAllRobotsNearby));
        } 
        else 
        {
            remainderOfNeutralConviction = neutralEnlightenmentCenterCurrentInfluence - robotCurrentConviction;
        }

        if (remainderOfNeutralConviction < 0) {
            hasEnoughToEmpower = true;
        }

        return hasEnoughToEmpower;
    }
}