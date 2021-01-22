package testPlayerv01.Roles;

import testPlayerv01.PoliticianTest01;
import battlecode.common.*;
import testPlayerv01.Service.Movement;

public class PoliticianNormal extends PoliticianTest01
{
    static Direction nextDirection;

    public static void run() throws GameActionException 
    {        
        
        // Scouting Role
        if (robotRole == RobotRoles.Scout) 
        {
            runScoutRole();
        }
        // Defender Role
        else if (robotRole == RobotRoles.DefendSlanderer)
        {
            runDefendSlandererRole();    
        }
    }

    protected static void runScoutRole() throws GameActionException
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
            } 
            else 
            {
                Movement.scoutAction();
            }
        }
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

    protected static void runDefendSlandererRole() throws GameActionException
    {
        if (politicianECBombNearby) 
        {
            if (neutralEnlightenmentCenterIsAround && currentNeutralEnlightenmentCenterGoingFor != null) 
            {
                Movement.moveAwayFromLocation(currentNeutralEnlightenmentCenterGoingFor);
            }
            else if (enemyEnlightenmentCenterIsAround && currentEnemyEnlightenmentCenterGoingFor != null) 
            {
                Movement.moveAwayFromLocation(currentEnemyEnlightenmentCenterGoingFor);
            }
        }

        // TODO: Add in a check for the farthest muckraker and try to get 2?
        stayNearSlanderers();

        if(enemyMuckrakersNearby && distanceToNearestMuckraker != 0)
        {
            getSumOfConvictionInEnemyMuckrakerRadiusSquared();
        }

        if (enemyMuckrakersNearby && friendlySlandererNearby) 
        {
            if (canConvertEnemyMuckraker() && countOfEnemiesInActionRadiusAroundEnemyMuckraker > 1) 
            {
                if (distanceToNearestMuckraker != 0 && robotController.canEmpower(distanceToNearestMuckraker)) 
                {
                    robotController.empower(distanceToNearestMuckraker);
                    return;
                }
            }
            else if(robotController.getLocation().isAdjacentTo(closestEnemyMuckrakerMapLocation))
            {
                if (distanceToNearestMuckraker != 0 && robotController.canEmpower(distanceToNearestMuckraker)) 
                {
                    robotController.empower(distanceToNearestMuckraker);
                    return;
                }
            }
            else if(closestMapLocationSlandererToDefend != null)
            {
                Movement.moveInFrontOfTarget(closestEnemyMuckrakerMapLocation, closestMapLocationSlandererToDefend);
            }
                        
            // Move to the location that is closest to nearest Slander
        }
        else if (enemyMuckrakersNearby) 
        {
            if (canConvertEnemyMuckraker() && countOfEnemiesInActionRadiusAroundEnemyMuckraker > 1)
            {
                if (distanceToNearestMuckraker != 0 && robotController.canEmpower(distanceToNearestMuckraker)) 
                {
                    robotController.empower(distanceToNearestMuckraker);
                    return;
                }
            }
            else if (nearFriendlyEnlightenmentCenter && canConvertEnemyMuckraker()) 
            {
                if (distanceToNearestMuckraker != 0 && robotController.canEmpower(distanceToNearestMuckraker)) 
                {
                    robotController.empower(distanceToNearestMuckraker);
                    return;
                }
            }
            else if(closestMapLocationSlandererToDefend != null)
            {
                Movement.moveInFrontOfTarget(closestEnemyMuckrakerMapLocation, closestMapLocationSlandererToDefend);
            }
            else if(closestMapLocationSlandererToDefend == null && spawnEnlightenmentCenterHomeLocation != null)
            {
                Movement.moveInFrontOfTarget(closestEnemyMuckrakerMapLocation, spawnEnlightenmentCenterHomeLocation);
            }
            else 
            {
                Movement.moveToTargetLocation(closestEnemyMuckrakerMapLocation);
            }
        }
        else if (nextDirection == null) 
        {
            Movement.scoutTheDirection(Movement.getRandomDirection());
        } 
        else 
        {
            Movement.scoutTheDirection(nextDirection);
        }
    }

    // TODO: Once we know where enemy is at, we can move towards there
    private static void stayNearSlanderers() throws GameActionException 
    {
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;

        // Check that the slanderer to defend is within the radius
        // Move randomly either towards enemy or back to slanderer
        // Move to target if spot Muckraker
        if (closestMapLocationSlandererToDefend != null) 
        {
            if (robotController.getLocation().isWithinDistanceSquared(closestMapLocationSlandererToDefend, sensorRadiusSquared)
                && robotController.getLocation().distanceSquaredTo(closestMapLocationSlandererToDefend) >= 20) 
            {
                nextDirection = null;
            } 
            else if (robotController.getLocation().distanceSquaredTo(closestMapLocationSlandererToDefend) <= 20)
            {
                nextDirection = Movement.getOppositeDirection(robotController.getLocation().directionTo(closestMapLocationSlandererToDefend));
            }
            else 
            {
                nextDirection = robotController.getLocation().directionTo(closestMapLocationSlandererToDefend);
            }
        }
        else if (enemyEnlightenmentCenterFound && friendlySlandererNearby) 
        {
            Movement.moveAwayFromLocation(currentEnemyEnlightenmentCenterGoingFor);    
        }
        else if (spawnEnlightenmentCenterHomeLocation != null)
        {
            if (robotController.getLocation().isWithinDistanceSquared(spawnEnlightenmentCenterHomeLocation, sensorRadiusSquared)) 
            {
                nextDirection = null;
            } 
            else 
            {
                nextDirection = robotController.getLocation().directionTo(spawnEnlightenmentCenterHomeLocation);
            }
        }        
    }

    private static boolean canConvertEnemyMuckraker()
    {
        boolean empowerCanConvertEnemy = false;
        int countOfAllRobotsInActionRadius = countOfEnemiesInActionRadiusAroundEnemyMuckraker + countOfFriendliesInActionRadiusAroundEnemyMuckraker;
        int remainderOfEnemyConviction = 0;

        if (countOfAllRobotsInActionRadius > 0) 
        {
            remainderOfEnemyConviction = (int) (enemyMuckrakerConviction - (getCurrentConviction() / countOfAllRobotsInActionRadius));
        }

        if (remainderOfEnemyConviction < 0) 
        {
            empowerCanConvertEnemy = true;
        }

        return empowerCanConvertEnemy;
    }
}
