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
            if (robotController.getRoundNum() >= MIDDLE_GAME_ROUND_START 
            && checkIfPoliticianShouldEmpower() && distanceToClosestRobotMapLocation != 0) 
            {
                if (robotController.canEmpower(distanceToClosestRobotMapLocation)) {
                    robotController.empower(distanceToClosestRobotMapLocation);
                }
                return;
            }
            else if (checkIfBombShouldEmpower() && empowerCanConvertEnemyAtMaxRadius())
            {

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
            && !homeEnlightenmentCenterSurrounded() && !empowerTheHomeBase()) 
        {
            decideIfEmpowerForEnemyEnlightenmentCenterBombs();
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
                && countOfFriendliesInActionRadiusAroundEnemyEnlightenmentcenter < 1 )
            {
                robotController.empower(distanceToEnemyEnlightenmentCenter);
                return;
            }
            else if (closestRobotToEnemyEnlightenmentCenter && canUseFullEmpowerWithoutDilution) 
            {
                robotController.empower(distanceToNeutralEnlightenmentCenter);
                return;
            }           
            // If it is the first in line and it's been stuck near it, attack.
            else if (lowestRobotIdOfFriendlies 
            && (distanceToEnemyEnlightenmentCenter <= 5 
                && turnsNearEnemyEnlightenmentCenterForAttacking >= 15))
            {
                println("HERE POLI EC ENEMY 1");
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
            && !homeEnlightenmentCenterSurrounded() && !empowerTheHomeBase())
        {
            decideIfEmpowerForNeutralEnlightenmentCenter(); 
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
    }

    static void decideIfEmpowerForNeutralEnlightenmentCenter() throws GameActionException
    {
        if (robotController.canEmpower(distanceToNeutralEnlightenmentCenter)) 
        {
            // If it has enough, convert it.
            if (hasEnoughConvictionToConvertNeutralEnlightenmentCenter() || canUseFullEmpowerWithoutDilution) 
            {
                println("POLI NEUTRAL EC 1");
                robotController.empower(distanceToNeutralEnlightenmentCenter);
                return;
            }
            // If adjacent to it, empower
            else if (neutralCurrentEnlightenmentCenterGoingFor != null 
            && robotController.getLocation().isAdjacentTo(neutralCurrentEnlightenmentCenterGoingFor)
            && countOfFriendliesInActionRadiusAroundNeutralEnlightenmentcenter < 1)
            {
                println("POLI NEUTRAL EC 2");
                robotController.empower(distanceToNeutralEnlightenmentCenter);
                return;
            }
            // If it is the first in line and it's been stuck near it, attack.
            else if (lowestRobotIdOfFriendlies 
            && distanceToNeutralEnlightenmentCenter <= 5 
            && (turnsNearNeutralEnlightenmentCenterForAttacking >= 15 
                && (robotController.sensePassability(robotController.getLocation()) > 0.3) 
                    || turnsNearNeutralEnlightenmentCenterForAttacking > 15 * (1 + robotController.sensePassability(robotController.getLocation())))
            && robotController.isLocationOccupied(robotController.getLocation().add(robotController.getLocation().directionTo(neutralCurrentEnlightenmentCenterGoingFor))))
            {
                println("POLI NEUTRAL EC 3");
                robotController.empower(distanceToNeutralEnlightenmentCenter);
                return;
            }
            else if (closestRobotToNeutralEnlightenmentCenter && canUseFullEmpowerWithoutDilution) 
            {
                println("POLI NEUTRAL EC 4");
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
            remainderOfNeutralConviction = (int) (neutralEnlightenmentCenterCurrentInfluence - ((robotCurrentConviction - POLITICIAN_TAX) / countOfAllRobotsNearby));
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