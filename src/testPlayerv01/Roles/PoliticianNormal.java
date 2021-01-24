package testPlayerv01.Roles;

import testPlayerv01.PoliticianTest01;
import battlecode.common.*;
import testPlayerv01.Service.Movement;
import testPlayerv01.Service.Sense;

public class PoliticianNormal extends PoliticianTest01
{
    static Direction nextDirection;
    static boolean currentLocationIsInFrontOfSlandererNearHomeOrEdge;
    private static Direction directionToEdgeOfMap;

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
        currentLocationIsInFrontOfSlandererNearHomeOrEdge = false;
        directionToEdgeOfMap = Sense.lookForEdgeOfMap(); 

        if (!friendlySlandererNearby && friendlySlandererSeenTurnsAgo > 3) 
        {
            friendlySlandererSeenTurnsAgo = 0;    
        }

        if (politicianECBombNearby && !slandererIsInTrouble) 
        {
            if (neutralEnlightenmentCenterIsAround && neutralCurrentEnlightenmentCenterGoingFor != null) 
            {
                Movement.moveAwayFromLocation(neutralCurrentEnlightenmentCenterGoingFor);
            }
            else if (enemyEnlightenmentCenterIsAround && enemyCurrentEnlightenmentCenterGoingFor != null) 
            {
                Movement.moveAwayFromLocation(enemyCurrentEnlightenmentCenterGoingFor);
            }
        }

        // TODO: Add in a check for the farthest muckraker and try to get 2?
        checkLocationComparedToSlanderer();
        stayNearSlanderers();

        // if (turnCount > 14) 
        // {
            
        // }
        // else if (spawnEnlightenmentCenterHomeLocation != null)
        // {
        //     nextDirection = spawnEnlightenmentCenterHomeLocation.directionTo(robotController.getLocation());
        // }        

        tryAttackEnemyMuckrakerIfNearby();
        
        if (robotController.isReady()) 
        {
            if (nextDirection != null) 
            {
                Movement.scoutTheDirection(nextDirection);
            } 
            // If there is no specific direction they need to go
            else if (nextDirection == null) 
            {
                Movement.scoutTheDirection(Movement.getRandomDirection());
            }
        }
    }

    private static void checkLocationComparedToSlanderer() 
    {
        if (closestMapLocationSlandererToDefend != null && spawnEnlightenmentCenterHomeLocation != null 
            && robotController.getLocation().distanceSquaredTo(spawnEnlightenmentCenterHomeLocation) 
            > closestMapLocationSlandererToDefend.distanceSquaredTo(spawnEnlightenmentCenterHomeLocation)) 
        {
            currentLocationIsInFrontOfSlandererNearHomeOrEdge = true;            
        }
        else if (directionToEdgeOfMap == null || (closestMapLocationSlandererToDefend != null && mapLocationOfEdge != null
            && robotController.getLocation().distanceSquaredTo(mapLocationOfEdge) 
                > closestMapLocationSlandererToDefend.distanceSquaredTo(mapLocationOfEdge)))
        {
            currentLocationIsInFrontOfSlandererNearHomeOrEdge = true;
        }
    }

    private static void tryAttackEnemyMuckrakerIfNearby() throws GameActionException 
    {
        if(enemyMuckrakersNearby && distanceToNearestMuckraker != 0)
        {
            getSumOfConvictionInEnemyMuckrakerRadiusSquared();
        }

        if (enemyMuckrakersNearby && (friendlySlandererNearby || friendlySlandererSeenTurnsAgo < 3)) 
        {
            if (empowerCanConvertEnemyAtMaxRadius() && countOfEnemiesInActionRadiusAroundEnemyMuckraker > 1)
            {
                if (distanceToNearestMuckraker != 0 && robotController.canEmpower(distanceToNearestMuckraker)) 
                {
                    robotController.empower(distanceToNearestMuckraker);
                    return;
                }
            }
            else if (canConvertEnemyMuckraker() && countOfEnemiesInActionRadiusAroundEnemyMuckraker > 1) 
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
            else if (canConvertEnemyMuckraker()) 
            {
                if (distanceToNearestMuckraker != 0 && robotController.canEmpower(distanceToNearestMuckraker)) 
                {
                    robotController.empower(distanceToNearestMuckraker);
                    return;
                }
            }
            // Move to the location that is closest to nearest Slander
            else if(closestMapLocationSlandererToDefend != null)
            {
                Movement.moveInFrontOfTarget(closestEnemyMuckrakerMapLocation, closestMapLocationSlandererToDefend);
            }                        
        }
        else if (enemyMuckrakersNearby && !friendlySlandererNearby) 
        {
            if (empowerCanConvertEnemyAtMaxRadius() && countOfEnemiesInActionRadius > 1)
            {
                if (distanceToNearestMuckraker != 0 && robotController.canEmpower(ACTION_RADIUS_POLITICIAN)) 
                {
                    robotController.empower(ACTION_RADIUS_POLITICIAN);
                    return;
                }
            }
            else if (canConvertEnemyMuckraker() && countOfEnemiesInActionRadiusAroundEnemyMuckraker > 1) 
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
    }

    // TODO: Once we know where enemy is at, we can move towards there
    private static void stayNearSlanderers() throws GameActionException 
    {
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;

        // Check that the slanderer to defend is within the radius
        // Move randomly either towards enemy or back to slanderer
        // Move to target if spot Muckraker
        if (closestMapLocationSlandererToDefend != null && !slandererIsInTrouble) 
        {
            if (robotController.getLocation().isWithinDistanceSquared(closestMapLocationSlandererToDefend, sensorRadiusSquared)
                && robotController.getLocation().distanceSquaredTo(closestMapLocationSlandererToDefend) >= ACTION_RADIUS_POLITICIAN
                && (spawnEnlightenmentCenterHomeLocation == null || robotController.getLocation().distanceSquaredTo(spawnEnlightenmentCenterHomeLocation) >= ACTION_RADIUS_POLITICIAN)
                && !enemyEnlightenmentCenterFound
                && currentLocationIsInFrontOfSlandererNearHomeOrEdge) 
            {
                nextDirection = null;
            } 
            else if (enemyEnlightenmentCenterFound && enemyCurrentEnlightenmentCenterGoingFor != null 
                && closestMapLocationSlandererToDefend.distanceSquaredTo(closestMapLocationSlandererToDefend) 
                >= robotController.getLocation().distanceSquaredTo(enemyCurrentEnlightenmentCenterGoingFor))
            {
                nextDirection = robotController.getLocation().directionTo(enemyCurrentEnlightenmentCenterGoingFor);
            }
            else if (robotController.getLocation().distanceSquaredTo(closestMapLocationSlandererToDefend) < ACTION_RADIUS_POLITICIAN)
            {
                nextDirection = Movement.getOppositeDirection(robotController.getLocation().directionTo(closestMapLocationSlandererToDefend));
            }
            else 
            {
                nextDirection = robotController.getLocation().directionTo(closestMapLocationSlandererToDefend);
            }
        }
        else if (slandererIsInTrouble && !enemyMuckrakersNearby)
        {
            nextDirection = robotController.getLocation().directionTo(slandererInTroubleMapLocation);
        }
        // else if (enemyEnlightenmentCenterFound && friendlySlandererNearby) 
        // {
        //     Movement.moveToTargetLocation(enemyCurrentEnlightenmentCenterGoingFor);    
        // }
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
        else if (enemyEnlightenmentCenterFound)
        {
            nextDirection = robotController.getLocation().directionTo(enemyCurrentEnlightenmentCenterGoingFor);
        }        
    }

    private static boolean canConvertEnemyMuckraker()
    {
        boolean empowerCanConvertEnemy = false;
        int countOfAllRobotsInActionRadius = countOfEnemiesInActionRadiusAroundEnemyMuckraker + countOfFriendliesInActionRadiusAroundEnemyMuckraker;
        int remainderOfEnemyConviction = 0;

        if (countOfAllRobotsInActionRadius > 0) 
        {
            remainderOfEnemyConviction = (int) (enemyMuckrakerConviction - (getCurrentConvictionWithEmpower() / countOfAllRobotsInActionRadius));
        }

        if (remainderOfEnemyConviction < 0) 
        {
            empowerCanConvertEnemy = true;
        }

        return empowerCanConvertEnemy;
    }
}
