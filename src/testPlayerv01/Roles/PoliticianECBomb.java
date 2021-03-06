package testPlayerv01.Roles;

import battlecode.common.*;
import testPlayerv01.PoliticianTest01;
import testPlayerv01.Service.Movement;
import testPlayerv01.Service.Communication;

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
            if (robotController.getLocation().distanceSquaredTo(enemyCurrentEnlightenmentCenterGoingFor) 
                <= robotController.getLocation().distanceSquaredTo(neutralCurrentEnlightenmentCenterGoingFor)) 
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
            if (robotController.getRoundNum() >= END_GAME_ROUND_STRAT 
            && checkIfPoliticianShouldEmpower() && distanceToClosestRobotMapLocation != 0) 
            {
                if (robotController.canEmpower(distanceToClosestRobotMapLocation)) {
                    robotController.empower(distanceToClosestRobotMapLocation);
                }
                return;
            }
            else if (checkIfBombShouldEmpower() && empowerCanConvertEnemyAtMaxRadius())
            {
                if (robotController.canEmpower(ACTION_RADIUS_POLITICIAN)) 
                {
                    robotController.empower(ACTION_RADIUS_POLITICIAN);
                }
                return;
            }

            if (robotController.getRoundNum() <= MIDDLE_GAME_ROUND_START) 
            {
                if (closestFriendlyEnlightenmentCenter != null)
                {
                    Direction directionFromFriendlyEnlightenmentCenter = closestFriendlyEnlightenmentCenter.directionTo(robotController.getLocation());
                    MapLocation placeNearbyFriendlyEnlightenmentCenter = closestFriendlyEnlightenmentCenter.add(directionFromFriendlyEnlightenmentCenter).add(directionFromFriendlyEnlightenmentCenter);
                    int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;

                    if (robotController.getLocation().distanceSquaredTo(closestFriendlyEnlightenmentCenter) <= (sensorRadiusSquared * 1.5)) 
                    {
                        Movement.moveToTargetLocation(placeNearbyFriendlyEnlightenmentCenter);    
                    }
                }
                else if (closestRobotMapLocation != null)
                {
                    directionToScout = robotController.getLocation().directionTo(closestRobotMapLocation);
                    Movement.scoutAction();
                }                
            }
            else 
            {
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
    }

    public static void processActionRadiusSensedRobots(RobotInfo robotInfo) throws GameActionException
    {
        int robotFlag = 0; 
        if (robotController.canGetFlag(robotInfo.getID()))
        {
            robotFlag = robotController.getFlag(robotInfo.getID());
        }

        if (robotInfo.getTeam() == enemy 
                && robotInfo.getType() == RobotType.MUCKRAKER) 
            {
                sumOfEnemyConvictionNearby += robotInfo.getConviction();
                countOfEnemiesInActionRadius++;
                countOfEnemyMuckrakerInActionRadius++;
            } 
            else if (robotInfo.getTeam() == enemy)
            {
                sumOfEnemyConvictionNearby += robotInfo.getConviction();
                countOfEnemiesInActionRadius++;
            }
            else if (robotInfo.getTeam() == friendly 
                && robotInfo.getType() == RobotType.POLITICIAN
                && (robotFlag != Communication.SLANDERER_FLAG)
                && robotInfo.getInfluence() >= POLITICIAN_EC_BOMB 
                && (enemyEnlightenmentCenterIsAround || neutralEnlightenmentCenterIsAround)) 
            {
                if (robotInfo.getID() < robotController.getID())
                {
                    lowestRobotIdOfFriendlies = false;
                }

                if (neutralCurrentEnlightenmentCenterGoingFor != null
                    && robotController.getLocation().distanceSquaredTo(neutralCurrentEnlightenmentCenterGoingFor) > 
                robotInfo.getLocation().distanceSquaredTo(neutralCurrentEnlightenmentCenterGoingFor)) 
                {
                    closestRobotToNeutralEnlightenmentCenter = false;
                }

                if (enemyCurrentEnlightenmentCenterGoingFor != null
                    && robotController.getLocation().distanceSquaredTo(enemyCurrentEnlightenmentCenterGoingFor) > 
                robotInfo.getLocation().distanceSquaredTo(enemyCurrentEnlightenmentCenterGoingFor)) 
                {
                    closestRobotToEnemyEnlightenmentCenter = false;
                }
            
                politicianECBombNearby = true;    
            }
            else if (robotInfo.getTeam() == friendly && robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER) 
            {
                nearFriendlyEnlightenmentCenter = true;
                friendlyEnlightenmentCenterInfluence = robotInfo.getInfluence();
                distanceToFriendlyEnlightenmentCenter = robotController.getLocation().distanceSquaredTo(robotInfo.getLocation());
            } 
            else if (robotInfo.getTeam() == friendly) 
            {
                countOfFriendliesInActionRadius++;
            }
    }

    protected static void attackEnemyEnlightenmentCenterLocation() throws GameActionException
    {  
        decideAttackForEnemyEnlightenmentBomb();

        if (moveRobot) 
        {   
            if (enemyCurrentEnlightenmentCenterGoingFor != null
                && !robotController.getLocation().isAdjacentTo(enemyCurrentEnlightenmentCenterGoingFor))
            {
                moveCloserOrFartherFromEnemyEnlightenmentCenter();
            }
            else if (enemyCurrentEnlightenmentCenterGoingFor == null 
                && hasTarget && closestRobotMapLocation != null) 
            {
                Movement.moveToTargetLocation(closestRobotMapLocation);
            }
            // Otherwise scout around
            else if (enemyCurrentEnlightenmentCenterGoingFor == null)
            {
                Movement.scoutAction();                
            }
        }
    }

    protected static void decideAttackForEnemyEnlightenmentBomb() throws GameActionException
    {
        if (enemyEnlightenmentCenterIsAround
            && !homeEnlightenmentCenterSurrounded()) 
        {
            decideIfEmpowerForEnemyEnlightenmentCenterBombs();
        }
        else if (homeEnlightenmentCenterSurrounded() && robotController.canEmpower(ACTION_RADIUS_POLITICIAN)) 
        {
            robotController.empower(ACTION_RADIUS_POLITICIAN);
            return;
        }
    }

    protected static void decideIfEmpowerForEnemyEnlightenmentCenterBombs() throws GameActionException {
        if (robotController.canEmpower(distanceToEnemyEnlightenmentCenter)) 
        {
            // If it has enough, convert it.
            if (hasEnoughConvictionToConvertEnemyEnlightenmentCenter() || canUseFullEmpowerWithoutDilution) 
            {
                robotController.empower(distanceToEnemyEnlightenmentCenter);
                return;
            }
            // If adjacent to it, empower
            else if (enemyCurrentEnlightenmentCenterGoingFor != null
                && robotController.getLocation().isAdjacentTo(enemyCurrentEnlightenmentCenterGoingFor)
                && politicianECBombNearby)
            {
                robotController.empower(distanceToEnemyEnlightenmentCenter);
                return;
            }
            // If waiting around and close, empower
            else if (closestRobotToEnemyEnlightenmentCenter 
                && politicianECBombNearby
                && turnsNearEnemyEnlightenmentCenterForAttacking > 20
                && (robotController.getLocation().isAdjacentTo(enemyCurrentEnlightenmentCenterGoingFor) 
                    || (countOfFriendliesInActionRadiusAroundEnemyEnlightenmentcenter < 1 && countOfEnemiesInActionRadiusAroundEnemyEnlightenmentcenter < 3)
                    || robotController.getLocation().distanceSquaredTo(enemyCurrentEnlightenmentCenterGoingFor) >= 5))
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

    static boolean hasEnoughConvictionToConvertEnemyEnlightenmentCenter() 
    {
        getSumOfEnemyConvictionInEnemyEnlightenmentRadiusSquared();
        int countOfAllRobotsNearby = countOfEnemiesInActionRadiusAroundEnemyEnlightenmentcenter
                + countOfFriendliesInActionRadiusAroundEnemyEnlightenmentcenter;
        int remainderOfEnemyConviction = 0;
        boolean hasEnoughToEmpower = false;
        canUseFullEmpowerWithoutDilution = false;

        if (countOfAllRobotsNearby > 1) 
        {
            remainderOfEnemyConviction = (int) (enemyEnlightenmentCenterCurrentInfluence - (getCurrentConvictionWithEmpower() / countOfAllRobotsNearby));
        } 
        else 
        {
            remainderOfEnemyConviction = (int) ((int) enemyEnlightenmentCenterCurrentInfluence - (getCurrentConvictionWithEmpower() - POLITICIAN_TAX));
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

    protected static void moveCloserOrFartherFromEnemyEnlightenmentCenter() throws GameActionException
    {
        if(!politicianECBombNearby)
        {
            Movement.moveToEnemyEnlightenmentCenter(enemyCurrentEnlightenmentCenterGoingFor);
        }
        else if (politicianECBombNearby)
        {
            if (lowestRobotIdOfFriendlies && closestRobotToEnemyEnlightenmentCenter)
            {
                Movement.moveToEnemyEnlightenmentCenter(enemyCurrentEnlightenmentCenterGoingFor);
            }
            else if(!lowestRobotIdOfFriendlies && !closestRobotToEnemyEnlightenmentCenter)
            {
                Movement.moveAwayFromLocation(enemyCurrentEnlightenmentCenterGoingFor);
            }
            else if (lowestRobotIdOfFriendlies && !closestRobotToEnemyEnlightenmentCenter)
            {
                Movement.moveAwayFromLocation(enemyCurrentEnlightenmentCenterGoingFor);
            }
            else if (closestRobotToEnemyEnlightenmentCenter && !lowestRobotIdOfFriendlies)
            {
                Movement.moveToEnemyEnlightenmentCenter(enemyCurrentEnlightenmentCenterGoingFor);
            }
        }
    }

    protected static void attackNeutralEnlightenmentCenterLocation() throws GameActionException
    {
        decideAttackForNeutralEnlightenmentBomb();

        if (moveRobot) 
        {
            if(neutralCurrentEnlightenmentCenterGoingFor != null
                && !robotController.getLocation().isAdjacentTo(neutralCurrentEnlightenmentCenterGoingFor))
            {
                moveCloserOrFartherFromNeutralEnlightenmentCenter();
            }
            else if (hasTarget && closestRobotMapLocation != null) 
            {
                Movement.moveToTargetLocation(closestRobotMapLocation);
            }
            else if (neutralCurrentEnlightenmentCenterGoingFor == null)
            {
                Movement.scoutAction();                
            }
        }
    }

    protected static void decideAttackForNeutralEnlightenmentBomb() throws GameActionException
    {
        if (neutralEnlightenmentCenterIsAround
            && !homeEnlightenmentCenterSurrounded())
        {
            decideIfEmpowerForNeutralEnlightenmentCenter(); 
        }
        else if (homeEnlightenmentCenterSurrounded() && robotController.canEmpower(ACTION_RADIUS_POLITICIAN)) 
        {
            robotController.empower(ACTION_RADIUS_POLITICIAN);
            return;
        }
    }

    static void decideIfEmpowerForNeutralEnlightenmentCenter() throws GameActionException
    {
        if (robotController.canEmpower(distanceToNeutralEnlightenmentCenter)) 
        {
            // If it has enough, convert it.
            if (hasEnoughConvictionToConvertNeutralEnlightenmentCenter() || canUseFullEmpowerWithoutDilution) 
            {
                robotController.empower(distanceToNeutralEnlightenmentCenter);
                return;
            }
            // If adjacent to it, empower
            else if (neutralCurrentEnlightenmentCenterGoingFor != null 
            && robotController.getLocation().isAdjacentTo(neutralCurrentEnlightenmentCenterGoingFor)
            && countOfFriendliesInActionRadiusAroundNeutralEnlightenmentcenter < 1)
            {
                robotController.empower(distanceToNeutralEnlightenmentCenter);
                return;
            }
            else if (closestRobotToNeutralEnlightenmentCenter && canUseFullEmpowerWithoutDilution) 
            {
                robotController.empower(distanceToNeutralEnlightenmentCenter);
                return;
            }
            // The count goes up if they are adjacent or <= 2 away
            else if (turnsNearNeutralEnlightenmentCenterForAttacking > 10 && robotController.getLocation().isAdjacentTo(neutralCurrentEnlightenmentCenterGoingFor))
            {
                robotController.empower(distanceToNeutralEnlightenmentCenter);
                return;
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
            remainderOfNeutralConviction = (int) (neutralEnlightenmentCenterCurrentInfluence - (getCurrentConvictionWithEmpower() / countOfAllRobotsNearby));
        } 
        else 
        {
            remainderOfNeutralConviction = (int) (neutralEnlightenmentCenterCurrentInfluence - getCurrentConvictionWithEmpower());
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

    protected static void moveCloserOrFartherFromNeutralEnlightenmentCenter() throws GameActionException
    {
        if (!politicianECBombNearby) 
        {
            Movement.moveToNeutralEnlightenmentCenter(neutralCurrentEnlightenmentCenterGoingFor);
        }
        else if (politicianECBombNearby)
        {
            if (lowestRobotIdOfFriendlies && closestRobotToNeutralEnlightenmentCenter)
            {
                Movement.moveToNeutralEnlightenmentCenter(neutralCurrentEnlightenmentCenterGoingFor);
            }
            else if (!lowestRobotIdOfFriendlies && !closestRobotToNeutralEnlightenmentCenter)
            {
                Movement.moveAwayFromLocation(neutralCurrentEnlightenmentCenterGoingFor);
            }
            else if (lowestRobotIdOfFriendlies && !closestRobotToNeutralEnlightenmentCenter)
            {
                Movement.moveAwayFromLocation(neutralCurrentEnlightenmentCenterGoingFor);
            }
            else if (closestRobotToNeutralEnlightenmentCenter && !lowestRobotIdOfFriendlies)
            {
                Movement.moveToNeutralEnlightenmentCenter(neutralCurrentEnlightenmentCenterGoingFor);
            }
        }
    }

    private static boolean checkIfBombShouldEmpower() 
    {
        return ((countOfEnemiesInActionRadius >= 6) && (countOfEnemiesInActionRadius != 0))
        && robotController.canEmpower(ACTION_RADIUS_POLITICIAN)
        && empowerCanConvertEnemyAtMaxRadius();
    }
}