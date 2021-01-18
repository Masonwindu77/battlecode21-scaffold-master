package testPlayerv01.Roles;

import testPlayerv01.PoliticianTest01;
import battlecode.common.*;
import testPlayerv01.Service.Movement;

public class PoliticianNormal extends PoliticianTest01
{
    public static void run() throws GameActionException 
    {        
        decideIfEmpowerForNonEnlightenmentCenterBombs();

        if (hasTarget) 
        {
            if (countOfEnemiesInActionRadius >= 2 && robotController.canEmpower(distanceToClosestRobotMapLocation)) 
            {
                robotController.empower(distanceToClosestRobotMapLocation);
                return;
            } 
            else if (robotController.getRoundNum() >= END_GAME_ROUND_STRAT
                && robotController.canEmpower(distanceToClosestRobotMapLocation)) 
            {
                robotController.empower(distanceToClosestRobotMapLocation);
                return;
            } 
            else if (robotController.getRoundNum() >= END_GAME_ROUND_STRAT
                && robotController.canEmpower(ACTION_RADIUS_POLITICIAN) && countOfEnemiesInActionRadius != 0) 
            {
                robotController.empower(ACTION_RADIUS_POLITICIAN);
                return;
            } 
            else if (closestRobotMapLocation != null 
                && !robotController.getLocation().isAdjacentTo(closestRobotMapLocation))
            {
                Movement.basicBugMovement(closestRobotMapLocation);
            } 
            else 
            {
                hasTarget = false;
            }
        }
        else if (empowerFactor > 3 && nearFriendlyEnlightenmentCenter)
        {
            if (robotController.canEmpower(distanceToFriendlyEnlightenmentCenter)) {
                robotController.empower(distanceToFriendlyEnlightenmentCenter);
            }
        }

        // Movement
        if (moveRobot || (politicianECBombNearby && enemyEnlightenmentCenterIsAround))
        {
            if (enemyEnlightenmentCenterIsAround && politicianECBombNearby) 
            {
                moveAwayFromEnemyEnlightenmentCenter();
            } else 
            {
                Movement.scoutAction();
            }
        }
        // Follower Role

        // Leader Role

        // Defender Role

        // Scout Role
        // if (robotRole == RobotRoles.Scout) {
        //     Movement.scoutAction();
        // }
        // Testing
    }

    protected static void decideIfEmpowerForNonEnlightenmentCenterBombs() throws GameActionException {
        if (checkIfPoliticianShouldEmpower()) 
        {
            robotController.empower(ACTION_RADIUS_POLITICIAN); // TODO: Get the actual radius not the full thing
            return;
        } else if (countOfEnemies != 0) {
            hasTarget = true;
        } else {
            moveRobot = true;
        }
    }
}
