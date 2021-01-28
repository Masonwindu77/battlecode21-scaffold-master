package testPlayerv09;

import battlecode.common.*;
import testPlayerv09.Service.Communication;
import testPlayerv09.Service.SenseRobots;
import testPlayerv09.Service.Movement;
import testPlayerv09.Roles.PoliticianECBomb;
import testPlayerv09.Roles.PoliticianNormal;

public class PoliticianTest01 extends RobotPlayer 
{
    // Who is around
    // Friendly
    protected static boolean nearFriendlyEnlightenmentCenter;
    protected static int distanceToFriendlyEnlightenmentCenter;
    protected static int friendlyEnlightenmentCenterInfluence;
    
    // Enemy
    protected static int distanceToEnemyEnlightenmentCenter;
    //// Muckraker
    protected static boolean enemyMuckrakersNearby;
    protected static int distanceToNearestMuckraker;
    protected static MapLocation closestEnemyMuckrakerMapLocation;
    protected static int enemyMuckrakerConviction;

    // Nuetral
    protected static int distanceToNeutralEnlightenmentCenter;
    protected static int turnsNearNeutralEnlightenmentCenter; 

    // Target Following
    protected static boolean hasTarget;
    protected static int distanceToClosestRobotMapLocation;
    protected static MapLocation closestRobotMapLocation;

    // Defend Slanderer
    protected static MapLocation closestMapLocationSlandererToDefend;
    protected static boolean friendlySlandererNearby;

    // Counting robots
    protected static int countOfEnemies;
    protected static int countOfFriendlies;
    protected static int countOfEnemiesInActionRadius;
    protected static int countOfFriendliesInActionRadius;
    protected static int countOfEnemyMuckrakerInActionRadius;
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

    protected static int turnsNearEnemyEnlightenmentCenter = 0;

    // Sum of enemy robot conviction
    static int sumOfEnemyConvictionNearby;
    static int sumOfEnemyConviciontInActionRadiusAroundEnemyEnlightenmentcenter;

    static void run() throws GameActionException 
    {
        resetVariablesForSensing();
        
        SenseRobots.checkForCommunications();        
        senseAreaForRobots();
        senseActionRadiusForRobots(); 

        if (countOfEnemies == 0) 
        {
            distanceToClosestRobotMapLocation = 0;
            
            
            distanceToNearestMuckraker = 0;  
            hasTarget = false;  
        }
                
        if (haveMessageToSend) 
        {
            Communication.setFlagMessageForScout();
        }
        else
        {
            robotController.setFlag(0);
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
        enemyEnlightenmentCenterIsAround = false;
        countOfEnemies = 0;
        countOfFriendlies = 0;
        hasTarget = false; //TODO: This should be handeled better. The target stuff.
        sumOfEnemyConvictionNearby = 0;
        countOfEnemiesInActionRadius = 0;
        countOfFriendliesInActionRadius = 0;
        countOfEnemyMuckrakerInActionRadius = 0;
        politicianECBombNearby = false;
        nearFriendlyEnlightenmentCenter = false;
        lowestRobotIdOfFriendlies = true;
        turnsNearNeutralEnlightenmentCenter= 0;
        closestRobotMapLocation = null;
        // Defend From Muckraker
        enemyMuckrakersNearby = false;
        friendlySlandererNearby = false;
        closestEnemyMuckrakerMapLocation = null;
        closestMapLocationSlandererToDefend = null;
        enemyMuckrakerConviction = 0;
        

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
                SenseRobots.processEnlightenmentCenterFinding(robotInfo);

                if (robotInfo.getTeam() == enemy) 
                {
                    distanceToEnemyEnlightenmentCenter = robotInfo.getLocation().distanceSquaredTo(robotController.getLocation());
                    countOfEnemies++;
                    if (distanceToEnemyEnlightenmentCenter <= ACTION_RADIUS_POLITICIAN) 
                    {
                      turnsNearEnemyEnlightenmentCenter++;
                    } 
                }
                else if (robotInfo.getTeam() == Team.NEUTRAL) 
                {
                    distanceToNeutralEnlightenmentCenter = robotInfo.getLocation().distanceSquaredTo(robotController.getLocation());
                    if (distanceToNeutralEnlightenmentCenter <= ACTION_RADIUS_POLITICIAN) 
                    {
                        turnsNearNeutralEnlightenmentCenter++;
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
                }
            }     
            else if (robotInfo.getTeam() == friendly) 
            {
                countOfFriendlies++;                
                if (robotController.canGetFlag(robotInfo.getID()) 
                    && robotController.getFlag(robotInfo.ID) == Communication.SLANDERER_FLAG) 
                {
                    friendlySlandererNearby = true;
                    if (closestMapLocationSlandererToDefend == null || robotController.getLocation().distanceSquaredTo(robotInfo.getLocation()) <=
                        robotController.getLocation().distanceSquaredTo(closestMapLocationSlandererToDefend)) 
                    {
                        closestMapLocationSlandererToDefend = robotInfo.getLocation();
                    }                    
                }
            }
        }
    }        

    protected static void senseActionRadiusForRobots() {
        int actionRadiusSquared = robotController.getType().actionRadiusSquared;
        RobotInfo[] allRobotInfos = robotController.senseNearbyRobots(actionRadiusSquared);        

        for (RobotInfo robotInfo : allRobotInfos) {
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
                && robotInfo.getInfluence() >= POLITICIAN_EC_BOMB 
                && (enemyEnlightenmentCenterIsAround || neutralEnlightenmentCenterIsAround)) 
            {
                
                politicianECBombNearby = true;

                if (currentEnemyEnlightenmentCenterGoingFor != null) 
                {
                    if (robotInfo.getID() < robotController.getID()
                    && robotRole == RobotRoles.PoliticianEnlightenmentCenterBomb
                    && robotController.getLocation().distanceSquaredTo(currentEnemyEnlightenmentCenterGoingFor) >= robotInfo.getLocation().distanceSquaredTo(currentEnemyEnlightenmentCenterGoingFor))
                    {
                        lowestRobotIdOfFriendlies = false;
                    }
                }
                else if (currentNeutralEnlightenmentCenterGoingFor != null) 
                {
                    if (robotInfo.getID() < robotController.getID()
                    && robotRole == RobotRoles.PoliticianEnlightenmentCenterBomb
                    && robotController.getLocation().distanceSquaredTo(currentNeutralEnlightenmentCenterGoingFor) >= robotInfo.getLocation().distanceSquaredTo(currentNeutralEnlightenmentCenterGoingFor))
                    {
                        lowestRobotIdOfFriendlies = false;
                    }
                }              
            }
            else if (robotInfo.getTeam() == friendly && robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER) 
            {
                nearFriendlyEnlightenmentCenter = true;
                friendlyEnlightenmentCenterInfluence = robotInfo.getInfluence();
                distanceToFriendlyEnlightenmentCenter = robotController.getLocation().distanceSquaredTo(robotInfo.getLocation());
            } 
            else if (robotInfo.getTeam() == friendly) {
                countOfFriendliesInActionRadius++;
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

    protected static void getSumOfEnemyConvictionInEnemyEnlightenmentRadiusSquared() {
        countOfEnemiesInActionRadiusAroundEnemyEnlightenmentcenter = 0;
        sumOfEnemyConviciontInActionRadiusAroundEnemyEnlightenmentcenter = 0;
        countOfFriendliesInActionRadiusAroundEnemyEnlightenmentcenter = 0;

        RobotInfo[] allRobotInfos = robotController.senseNearbyRobots(distanceToEnemyEnlightenmentCenter);
        for (RobotInfo robotInfo : allRobotInfos) 
        {
            if (robotInfo.getTeam() == enemy && robotInfo.getType() != RobotType.ENLIGHTENMENT_CENTER) 
            {
                countOfEnemiesInActionRadiusAroundEnemyEnlightenmentcenter++;
                sumOfEnemyConviciontInActionRadiusAroundEnemyEnlightenmentcenter += robotInfo.getConviction();
            } 
            else if (robotInfo.getTeam() == friendly) 
            {
                countOfFriendliesInActionRadiusAroundEnemyEnlightenmentcenter++;
            }
        }
    }

    protected static void getSumOfConvictionInNeutralEnlightenmentRadiusSquared() {
        countOfEnemiesInActionRadiusAroundNeutralEnlightenmentcenter = 0;
        sumOfEnemyConviciontInActionRadiusAroundNeutralEnlightenmentcenter = 0;
        countOfFriendliesInActionRadiusAroundNeutralEnlightenmentcenter = 0;

        RobotInfo[] allRobotInfos = robotController.senseNearbyRobots(distanceToNeutralEnlightenmentCenter);
        for (RobotInfo robotInfo : allRobotInfos) 
        {
            if (robotInfo.getTeam() == enemy && robotInfo.getType() != RobotType.ENLIGHTENMENT_CENTER) 
            {
                countOfEnemiesInActionRadiusAroundNeutralEnlightenmentcenter++;
                sumOfEnemyConviciontInActionRadiusAroundNeutralEnlightenmentcenter += robotInfo.getConviction();
            } 
            else if (robotInfo.getTeam() == friendly) 
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

        if (countOfEnemyMuckrakerInActionRadius >= 5 && nearFriendlyEnlightenmentCenter) 
        {
            homeSurrounded = true;    
        }

        return homeSurrounded;
    }

    protected static boolean checkIfPoliticianShouldEmpower() 
    {
        return ((countOfEnemiesInActionRadius >= 2) || (countOfEnemiesInActionRadius != 0))
                && robotController.canEmpower(ACTION_RADIUS_POLITICIAN)
                && empowerCanConvertEnemyAtMaxRadius();
    }

    protected static void moveAwayFromEnemyEnlightenmentCenter() throws GameActionException {
        Movement.moveAwayFromLocation(currentEnemyEnlightenmentCenterGoingFor);
    }

    static boolean empowerCanConvertEnemyAtMaxRadius() {
        boolean empowerCanConvertEnemy = false;
        int countOfAllRobotsInActionRadius = countOfEnemiesInActionRadius + countOfFriendliesInActionRadius;
        int remainderOfEnemyConviction = 0;

        if (countOfAllRobotsInActionRadius > 0) {
            remainderOfEnemyConviction = (int) (sumOfEnemyConvictionNearby - (getCurrentConviction() / countOfAllRobotsInActionRadius));
        }

        if (remainderOfEnemyConviction < 0) {
            empowerCanConvertEnemy = true;
        }

        return empowerCanConvertEnemy;
    }

    protected static double getCurrentConviction() {
        return (robotCurrentConviction * empowerFactor) - POLITICIAN_TAX;
    }

    protected static boolean empowerTheHomeBase()
    {
        return empowerFactor > 5 
        && nearFriendlyEnlightenmentCenter 
        && robotController.getInfluence() > 50
        && friendlyEnlightenmentCenterInfluence < AMOUNT_OF_INFLUENCE_TO_NOT_EMPOWER_SELF;
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
            Movement.scoutTheDirection(Movement.getRandomDirection());
        }
    }

    static void setup() 
    {
        setConstants();
        assignHomeEnlightenmentCenterLocation();
        if (spawnEnlightenmentCenterHomeLocation != null) 
        {
            setSquaresAroundEnlightenmentCenter(); 
            assignRobotRole();
        }
        else
        {
            robotRole = RobotRoles.Converted;
        }
               
        robotCurrentInfluence = robotController.getInfluence();
    }

    static void assignRobotRole() {
        if (robotCurrentInfluence >= POLITICIAN_EC_BOMB) 
        {
            robotRole = RobotRoles.PoliticianEnlightenmentCenterBomb;
        } else if (robotCurrentInfluence <= POLITICIAN_SCOUT) 
        {
            robotRole = RobotRoles.Scout;
        } 
        else if (robotCurrentInfluence <= POLITICIAN_DEFEND_SLANDERER) 
        {
            robotRole = RobotRoles.DefendSlanderer;
        }
    }
}
