package testPlayerv01;

import battlecode.common.*;
import testPlayerv01.Service.Communication;
import testPlayerv01.Service.Sense;
import testPlayerv01.Service.Movement;
import testPlayerv01.Roles.PoliticianECBomb;
import testPlayerv01.Roles.PoliticianNormal;

public class PoliticianTest01 extends RobotPlayer 
{
    // Who is around
    // Friendly
    protected static boolean nearFriendlyEnlightenmentCenter;
    protected static int distanceToFriendlyEnlightenmentCenter;
    protected static MapLocation closestFriendlyEnlightenmentCenter;
    protected static int friendlyEnlightenmentCenterInfluence;
    protected static boolean tooCloseToFriendlyDefender;
    protected static MapLocation closestFriendlyDefenderMapLocation;
    
    // Enemy
    protected static int distanceToEnemyEnlightenmentCenter;
    protected static boolean closestRobotToEnemyEnlightenmentCenter;
    //// Muckraker
    protected static boolean enemyMuckrakersNearby;
    protected static int distanceToNearestMuckraker;
    protected static MapLocation closestEnemyMuckrakerMapLocation;
    protected static int enemyMuckrakerConviction;

    // Nuetral
    protected static int distanceToNeutralEnlightenmentCenter;
    protected static int turnsNearNeutralEnlightenmentCenterForAttacking; 
    protected static boolean closestRobotToNeutralEnlightenmentCenter;

    protected static boolean canUseFullEmpowerWithoutDilution;

    // Target Following
    protected static boolean hasTarget;
    protected static int distanceToClosestRobotMapLocation;
    protected static MapLocation closestRobotMapLocation;

    // Defend Slanderer
    protected static MapLocation closestMapLocationSlandererToDefend;
    protected static boolean friendlySlandererNearby;
    protected static int friendlySlandererSeenTurnsAgo;
    protected static boolean slandererIsInTrouble;
    protected static MapLocation slandererInTroubleMapLocation;

    // Counting robots
    protected static int countOfEnemies;
    protected static int countOfFriendlies;
    protected static int countOfEnemiesInActionRadius;
    protected static int countOfFriendliesInActionRadius;
    protected static int countOfEnemyMuckrakerInActionRadius;
    protected static int muckrakerAdjacentToSpawn;
    protected static boolean enemyPoliticianNearby;
    protected static int enemyPoliticianAdjacentToSpawn;
    protected static int countOfEnemyPoliticiansInActionRadius;
    // ENEMY MUCKRAKER
    protected static int countOfEnemiesInActionRadiusAroundEnemyMuckraker;
    protected static int sumOfEnemyConviciontInActionRadiusAroundEnemyMuckraker;
    protected static int countOfFriendliesInActionRadiusAroundEnemyMuckraker;
    // ENEMY EC
    protected static int countOfEnemiesInActionRadiusAroundEnemyEnlightenmentcenter;
    protected static int countOfFriendliesInActionRadiusAroundEnemyEnlightenmentcenter;
    // NEUTRAL EC
    protected static int countOfEnemiesInActionRadiusAroundNeutralEnlightenmentcenter;
    protected static int sumOfEnemyConviciontInActionRadiusAroundNeutralEnlightenmentcenter;
    protected static int countOfFriendliesInActionRadiusAroundNeutralEnlightenmentcenter;

    protected static int turnsNearEnemyEnlightenmentCenterForAttacking = 0;

    // Sum of enemy robot conviction
    protected static int sumOfEnemyConvictionNearby;
    static int sumOfEnemyConviciontInActionRadiusAroundEnemyEnlightenmentcenter;

    static void run() throws GameActionException 
    {
        resetVariablesForSensing();
        
        Sense.checkForCommunications();        
        senseAreaForRobots();
        senseActionRadiusForRobots(); 

        if (countOfEnemies == 0) 
        {
            distanceToClosestRobotMapLocation = 0;           
            distanceToNearestMuckraker = 0;  
            hasTarget = false;  
        }

        // Have to check this after senseing.
        if (!neutralEnlightenmentCenterIsAround) 
        {
            turnsNearNeutralEnlightenmentCenterForAttacking = 0;    
        }
        else if (!enemyEnlightenmentCenterIsAround)
        {
            turnsNearEnemyEnlightenmentCenterForAttacking = 0;
        }
                
        if (haveMessageToSend) 
        {
            Communication.setFlagMessageForScout();
        }
        else
        {
            robotController.setFlag(0);
        }

        if (robotController.getInfluence() <= 10 && (robotController.getInfluence() != 1 || robotRole == RobotRoles.Converted)) 
        {
            if (robotController.canEmpower(ACTION_RADIUS_POLITICIAN)) 
            {
                robotController.empower(ACTION_RADIUS_POLITICIAN);    
            }    
        }

        if (robotRole == RobotRoles.PoliticianEnlightenmentCenterBomb) 
        {
            PoliticianECBomb.run();
        } 
        else if (robotRole != RobotRoles.Converted)
        {
            PoliticianNormal.run();
        }
        else if (robotRole == RobotRoles.Converted) 
        {           
            attackOrScoutAround();
        }
    }

    protected static void resetVariablesForSensing()
    {
        robotCurrentConviction = robotController.getConviction();
        empowerFactor = robotController.getEmpowerFactor(friendly, 0);
        moveRobot = true;

        if (spawnEnlightenmentCenterHomeLocation != null 
            && closestFriendlyEnlightenmentCenter == spawnEnlightenmentCenterHomeLocation) 
        {
            closestFriendlyEnlightenmentCenter = null;
        }

        enemyEnlightenmentCenterIsAround = false;
        neutralEnlightenmentCenterIsAround = false;

        countOfEnemies = 0;
        countOfFriendlies = 0;
        hasTarget = false; //TODO: This should be handeled better. The target stuff.
        sumOfEnemyConvictionNearby = 0;
        countOfEnemiesInActionRadius = 0;
        countOfFriendliesInActionRadius = 0;
        countOfEnemyMuckrakerInActionRadius = 0;
        countOfEnemyPoliticiansInActionRadius = 0;
        muckrakerAdjacentToSpawn = 0;        
        enemyPoliticianAdjacentToSpawn = 0;
        enemyPoliticianNearby = false;
        nearFriendlyEnlightenmentCenter = false;
        closestRobotMapLocation = null;

        // Deciding who empowers first
        closestRobotToNeutralEnlightenmentCenter = true;
        closestRobotToEnemyEnlightenmentCenter = true;
        lowestRobotIdOfFriendlies = true;
        politicianECBombNearby = false;

        // Defend Slanderer From Muckraker
        enemyMuckrakersNearby = false;
        friendlySlandererNearby = false;
        friendlySlandererSeenTurnsAgo++;
        closestEnemyMuckrakerMapLocation = null;
        closestMapLocationSlandererToDefend = null;
        enemyMuckrakerConviction = 0;
        slandererIsInTrouble = false;
        slandererInTroubleMapLocation = null;
        tooCloseToFriendlyDefender = false;
        closestFriendlyDefenderMapLocation = null;

        if (messageLastTwoTurnsForConverted == 0) 
        {
            enemyEnlightenmentCenterHasBeenConverted = false;
        }

        if (messageLastTwoTurnsForConverted == 0) 
        {
            neutralEnlightenmentCenterHasBeenConverted = false;    
        }
        
    }

    protected static void senseAreaForRobots() throws GameActionException {
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;        
        RobotInfo[] allRobotInfos = robotController.senseNearbyRobots(sensorRadiusSquared);

        for (RobotInfo robotInfo : allRobotInfos) 
        {
            if (robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER) 
            {
                Sense.processEnlightenmentCenterFinding(robotInfo);

                if (robotInfo.getTeam() == enemy) 
                {
                    distanceToEnemyEnlightenmentCenter = robotInfo.getLocation().distanceSquaredTo(robotController.getLocation());
                    countOfEnemies++;
                    if (robotInfo.getLocation().isAdjacentTo(enemyCurrentEnlightenmentCenterGoingFor) || distanceToEnemyEnlightenmentCenter <= 2) 
                    {
                        turnsNearEnemyEnlightenmentCenterForAttacking++;
                    } 
                }
                else if (robotInfo.getTeam() == Team.NEUTRAL) 
                {
                    distanceToNeutralEnlightenmentCenter = robotInfo.getLocation().distanceSquaredTo(robotController.getLocation());
                    if (robotInfo.getLocation().isAdjacentTo(neutralCurrentEnlightenmentCenterGoingFor) || distanceToNeutralEnlightenmentCenter <= 2) 
                    {
                        turnsNearNeutralEnlightenmentCenterForAttacking++;
                    } 
                }
            }                     
            else if (robotInfo.getTeam() == enemy) 
            {
                countOfEnemies++;
                hasTarget = true; // for scout role
                getClosestEnemyRobot(robotInfo);

                if (robotInfo.getType() == RobotType.MUCKRAKER) 
                {
                    enemyMuckrakersNearby = true;
                    getClosestEnemyMuckraker(robotInfo);  
                    
                    if (spawnEnlightenmentCenterHomeLocation != null 
                        && robotInfo.getLocation().isAdjacentTo(spawnEnlightenmentCenterHomeLocation)) 
                    {
                        muckrakerAdjacentToSpawn++;    
                    }
                }
                else if (robotInfo.getType() == RobotType.POLITICIAN)
                {
                    enemyPoliticianNearby = true;

                    if (spawnEnlightenmentCenterHomeLocation != null
                        && robotInfo.getLocation().isAdjacentTo(spawnEnlightenmentCenterHomeLocation)) 
                    {
                        enemyPoliticianAdjacentToSpawn++;    
                    }
                }
            }     
            else if (robotInfo.getTeam() == friendly) 
            {
                countOfFriendlies++;  

                if (robotController.canGetFlag(robotInfo.getID()) 
                    && robotController.getFlag(robotInfo.ID) == Communication.SLANDERER_FLAG) 
                {
                    friendlySlandererNearby = true;
                    friendlySlandererSeenTurnsAgo = 0;
                    if (closestMapLocationSlandererToDefend == null || robotController.getLocation().distanceSquaredTo(robotInfo.getLocation()) <=
                        robotController.getLocation().distanceSquaredTo(closestMapLocationSlandererToDefend)) 
                    {
                        closestMapLocationSlandererToDefend = robotInfo.getLocation();
                    }                    
                }
                else if (robotController.canGetFlag(robotInfo.getID()) 
                && robotController.getFlag(robotInfo.ID) == Communication.SLANDERER_IN_TROUBLE_FLAG)
                {
                    slandererIsInTrouble = true;
                    slandererInTroubleMapLocation = robotInfo.getLocation();
                }
                else if (robotInfo.getInfluence() > POLITICIAN_SCOUT && robotInfo.getInfluence() <= POLITICIAN_DEFEND_SLANDERER
                    && robotInfo.getType() == RobotType.POLITICIAN)
                {
                    if (closestFriendlyDefenderMapLocation == null || robotController.getLocation().distanceSquaredTo(robotInfo.getLocation()) <=
                    robotController.getLocation().distanceSquaredTo(closestFriendlyDefenderMapLocation)) 
                    {
                        closestFriendlyDefenderMapLocation = robotInfo.getLocation();
                        if (robotController.getLocation().isAdjacentTo(closestFriendlyDefenderMapLocation)
                        && (spawnEnlightenmentCenterHomeLocation != null 
                            && !robotController.getLocation().isAdjacentTo(spawnEnlightenmentCenterHomeLocation))) 
                        {
                            tooCloseToFriendlyDefender = true;
                        }
                    }
                }
            }
        }
    }        

    protected static void senseActionRadiusForRobots() throws GameActionException {
        int actionRadiusSquared = robotController.getType().actionRadiusSquared;
        RobotInfo[] allRobotInfos = robotController.senseNearbyRobots(actionRadiusSquared);        

        for (RobotInfo robotInfo : allRobotInfos) 
        {
            if (robotRole == RobotRoles.PoliticianEnlightenmentCenterBomb) 
            {
                PoliticianECBomb.processActionRadiusSensedRobots(robotInfo);    
            }
            else
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
                else if (robotInfo.getTeam() == enemy
                    && robotInfo.getType() == RobotType.POLITICIAN)
                {
                    sumOfEnemyConvictionNearby += robotInfo.getConviction();
                    countOfEnemiesInActionRadius++;
                    countOfEnemyPoliticiansInActionRadius++;
                }
                else if (robotInfo.getTeam() == enemy)
                {
                    sumOfEnemyConvictionNearby += robotInfo.getConviction();
                    countOfEnemiesInActionRadius++;
                }
                else if (robotInfo.getTeam() == friendly 
                    && robotInfo.getType() == RobotType.POLITICIAN
                    && robotFlag != Communication.SLANDERER_FLAG
                    && robotInfo.getInfluence() >= POLITICIAN_EC_BOMB 
                    && (enemyEnlightenmentCenterIsAround || neutralEnlightenmentCenterIsAround)) 
                {            
                    politicianECBombNearby = true;    
                }
                else if (robotInfo.getTeam() == friendly && robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER) 
                {
                    nearFriendlyEnlightenmentCenter = true;
                    friendlyEnlightenmentCenterInfluence = robotInfo.getInfluence();
                    distanceToFriendlyEnlightenmentCenter = robotController.getLocation().distanceSquaredTo(robotInfo.getLocation());
                    closestFriendlyEnlightenmentCenter = robotInfo.getLocation();
                } 
                else if (robotInfo.getTeam() == friendly) 
                {
                    countOfFriendliesInActionRadius++;
                }
            }
            
        }
    }

    private static void getClosestEnemyRobot(RobotInfo robotInfo) 
    {
        if (closestRobotMapLocation == null ||
        robotController.getLocation().distanceSquaredTo(robotInfo.getLocation()) <= robotController.getLocation().distanceSquaredTo(closestRobotMapLocation)) 
        {
            closestRobotMapLocation = robotInfo.getLocation();
            distanceToClosestRobotMapLocation = robotController.getLocation().distanceSquaredTo(robotInfo.getLocation());
        }
    }

    private static void getClosestEnemyMuckraker(RobotInfo robotInfo) 
    {
        if (closestEnemyMuckrakerMapLocation == null || 
        robotController.getLocation().distanceSquaredTo(robotInfo.getLocation()) <= robotController.getLocation().distanceSquaredTo(closestEnemyMuckrakerMapLocation)) 
        {
            enemyMuckrakerConviction = robotInfo.getConviction();
            closestEnemyMuckrakerMapLocation = robotInfo.getLocation();
            distanceToNearestMuckraker = robotController.getLocation().distanceSquaredTo(robotInfo.getLocation());
        }
    }

    protected static void getSumOfEnemyConvictionInEnemyEnlightenmentRadiusSquared() 
    {
        countOfEnemiesInActionRadiusAroundEnemyEnlightenmentcenter = 0;
        countOfFriendliesInActionRadiusAroundEnemyEnlightenmentcenter = 0;

        RobotInfo[] allRobotInfos = robotController.senseNearbyRobots(distanceToEnemyEnlightenmentCenter);
        for (RobotInfo robotInfo : allRobotInfos) 
        {
            if (robotInfo.getTeam() == enemy) 
            {
                countOfEnemiesInActionRadiusAroundEnemyEnlightenmentcenter++;
            } 
            else if (robotInfo.getTeam() == friendly 
                && robotInfo.getType() == RobotType.POLITICIAN
                && robotInfo.getLocation().distanceSquaredTo(enemyCurrentEnlightenmentCenterGoingFor) 
                    < robotController.getLocation().distanceSquaredTo(enemyCurrentEnlightenmentCenterGoingFor)
                && robotInfo.getInfluence() >= POLITICIAN_EC_BOMB)
            {
                politicianECBombNearby = true;
            }
            else if (robotInfo.getTeam() == friendly) 
            {
                countOfFriendliesInActionRadiusAroundEnemyEnlightenmentcenter++;
            }
        }
    }

    protected static void getSumOfConvictionInNeutralEnlightenmentRadiusSquared() 
    {
        countOfEnemiesInActionRadiusAroundNeutralEnlightenmentcenter = 0;
        countOfFriendliesInActionRadiusAroundNeutralEnlightenmentcenter = 0;

        RobotInfo[] allRobotInfos = robotController.senseNearbyRobots(distanceToNeutralEnlightenmentCenter);
        for (RobotInfo robotInfo : allRobotInfos) 
        {
            if (robotInfo.getTeam() == enemy) 
            {
                countOfEnemiesInActionRadiusAroundNeutralEnlightenmentcenter++;
            } 
            else if (robotInfo.getTeam() == friendly || robotInfo.getTeam() == Team.NEUTRAL) 
            {
                countOfFriendliesInActionRadiusAroundNeutralEnlightenmentcenter++;
            }
        }
    }

    protected static void getSumOfConvictionInEnemyMuckrakerRadiusSquared()
    {
        countOfEnemiesInActionRadiusAroundEnemyMuckraker = 0;
        sumOfEnemyConviciontInActionRadiusAroundEnemyMuckraker = 0;
        countOfFriendliesInActionRadiusAroundEnemyMuckraker = 0;

        RobotInfo[] allRobotInfos = robotController.senseNearbyRobots(distanceToNearestMuckraker);
        for (RobotInfo robotInfo : allRobotInfos) 
        {
            if (robotInfo.getTeam() == enemy) 
            {
                countOfEnemiesInActionRadiusAroundEnemyMuckraker++;
                sumOfEnemyConviciontInActionRadiusAroundEnemyMuckraker += robotInfo.getConviction();
            } 
            else if (robotInfo.getTeam() == friendly) 
            {
                countOfFriendliesInActionRadiusAroundEnemyMuckraker++;
            }
        }
    }

    protected static boolean homeEnlightenmentCenterSurrounded() throws GameActionException
    {
        boolean homeSurrounded = false;

        if (countOfEnemyMuckrakerInActionRadius >= 5 
            && nearFriendlyEnlightenmentCenter
            && muckrakerAdjacentToSpawn >= 3) 
        {
            homeSurrounded = true;    
        }
        else if (countOfEnemyPoliticiansInActionRadius >= 5
            && nearFriendlyEnlightenmentCenter
            && enemyPoliticianAdjacentToSpawn >= 3)
        {
            homeSurrounded = true;
        }

        return homeSurrounded;
    }

    protected static boolean checkIfPoliticianShouldEmpower() 
    {
        return ((countOfEnemiesInActionRadius >= 2) || (countOfEnemiesInActionRadius != 0))
                && robotController.canEmpower(ACTION_RADIUS_POLITICIAN)
                && (empowerCanConvertEnemyAtMaxRadius() || turnCount > 350);
    }

    protected static void moveAwayFromEnemyEnlightenmentCenter() throws GameActionException {
        Movement.moveAwayFromLocation(enemyCurrentEnlightenmentCenterGoingFor);
    }

    protected static boolean empowerCanConvertEnemyAtMaxRadius() 
    {
        boolean empowerCanConvertEnemy = false;
        int countOfAllRobotsInActionRadius = countOfEnemiesInActionRadius + countOfFriendliesInActionRadius;
        int remainderOfEnemyConviction = 0;

        if (countOfAllRobotsInActionRadius > 0) {
            remainderOfEnemyConviction = (int) (sumOfEnemyConvictionNearby - (getCurrentConvictionWithEmpower() / countOfAllRobotsInActionRadius));
        }

        if (remainderOfEnemyConviction < 0) {
            empowerCanConvertEnemy = true;
        }

        return empowerCanConvertEnemy;
    }

    protected static double getCurrentConvictionWithEmpower() 
    {
        return (robotCurrentConviction - POLITICIAN_TAX) * empowerFactor;
    }

    // RobotRole == Converted
    protected static void attackOrScoutAround() throws GameActionException
    {
        if (enemyEnlightenmentCenterIsAround && robotController.canEmpower(distanceToEnemyEnlightenmentCenter)) 
        {
            robotController.empower(distanceToEnemyEnlightenmentCenter);
            return;
        }
        else if (countOfEnemiesInActionRadius != 0 && robotController.canEmpower(distanceToClosestRobotMapLocation)) 
        {
            robotController.empower(distanceToClosestRobotMapLocation);
            return;
        }
        else 
        {
            Movement.scoutAction();
        }
    }

    static void setup() 
    {
        setConstants();
        assignHomeEnlightenmentCenterLocation();
        assignRobotRole();

        if (spawnEnlightenmentCenterHomeLocation != null) 
        {
            setSquaresAroundEnlightenmentCenter();             
        }

        robotCurrentInfluence = robotController.getInfluence();
    }

    static void assignRobotRole() 
    {
        if (robotCurrentInfluence >= POLITICIAN_EC_BOMB) 
        {
            robotRole = RobotRoles.PoliticianEnlightenmentCenterBomb;
        } 
        else if (robotCurrentInfluence <= POLITICIAN_SCOUT) 
        {
            robotRole = RobotRoles.Scout;
        } 
        else if (robotCurrentInfluence <= POLITICIAN_DEFEND_SLANDERER) 
        {
            robotRole = RobotRoles.DefendSlanderer;
        }
        else if (spawnEnlightenmentCenterHomeLocation == null)
        {
            robotRole = RobotRoles.Converted;
        }
        else
        {
            robotRole = RobotRoles.Scout;
        }
    }
}
