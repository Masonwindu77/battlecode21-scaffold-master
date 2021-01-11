package testPlayerv01;
import battlecode.common.*;
import testPlayerv01.Service.Movement;

public class PoliticianTest01 extends RobotPlayer 
{
    // Who is around
    static boolean enemyPoliticianIsAround;
    static boolean enemyEnlightenmentCenterIsAround;
    static int enemyEnlightenmentCenterConviction;
    static boolean enemyEnlightenmentCenterIsLessThanTwoDistanceAway;
    static int enemyEnlightenmentCenterDistanceSquared;

    // Target Following
    static boolean hasTarget;
    static int distanceToclosestRobotMapLocation;
    static MapLocation closestRobotMapLocation;

    // Counting robots
    static int countOfEnemies;
    static int countOfFriendlies;
    static int countOfEnemiesInActionRadiusAroundEnemyEnlightenmentcenter;
    static int countOfFrienliesInActionRadiusAroundEnemyEnlightenmentcenter;

    static int turnsNearEnemyEnlightenmentCenter;

    // Sum of enemy robot conviction
    static int sumOfEnemyConvictionNearby;
    static int sumOfEnemyConviciontInActionRadiusAroundEnemyEnlightenmentcenter;    

    static void run() throws GameActionException 
    {
        enemyEnlightenmentCenterConviction = 0;        
        robotCurrentConviction = robotController.getConviction();
        empowerFactor = robotController.getEmpowerFactor(friendly, 0);

        senseAreaForRobots();
        
        if (robotController.getRoundNum() % 2 == 0 && !enemyEnlightenmentCenterFound) 
        {
            checkIfSpawnEnlightenmentCenterHasEnemyLocation(); 
        }

        // TODO: Make a check for if the EC has been converted. 
        // TODO: Make a check for attacking enemies even with allies around
        
        if (robotRole != RobotRoles.PoliticianEnlightenmentCenterBomb && !hasTarget)
        {
            decideIfEmpowerForNonEnlightenmentCenterBombs();
        }
        else if (robotRole == RobotRoles.PoliticianEnlightenmentCenterBomb) 
        {
            decideIfEmpowerForEnlightenmentCenterBombs();
        }

        if (hasTarget) 
        {
            if (empowerCanConvertEnemyAtMaxRadius() && robotController.canEmpower(distanceToclosestRobotMapLocation)) 
            {
                robotController.empower(distanceToclosestRobotMapLocation);
                return;
            }
            else if (robotController.getRoundNum() >= MIDDLE_GAME_ROUND_START 
                && robotController.canEmpower(distanceToclosestRobotMapLocation))
            {
                robotController.empower(distanceToclosestRobotMapLocation);
                return;
            }
            else if (closestRobotMapLocation != null)
            {
                Movement.basicBugMovement(closestRobotMapLocation);
            }
            else
            {
                hasTarget = false;
            }
        }

        if (moveRobot) // Movement
        {
            if (enemyEnlightenmentCenterMapLocation.size() > 0 
            && (robotRole == RobotRoles.PoliticianEnlightenmentCenterBomb || robotController.getConviction() > MAX_NORMAL_POLITICIAN))
            {
                MapLocation enemyCenterLocation = enemyEnlightenmentCenterMapLocation.get(enemyEnlightenmentCenterMapLocation.size());
                currentEnemyEnlightenmentCenterGoingFor = enemyCenterLocation; //TODO: can make this cleaner
                moveToEnemyEnlightenmentCenter(enemyCenterLocation);                               
            }
            else if (enemyEnlightenmentCenterIsAround && politicianECBombNearby)
            {
                moveAwayFromEnemyEnlightenmentCenter();
            }
            else
            {
                Movement.scoutAction();
            } 
        }        
        // Follower Role

        // Leader Role

        // Defender Role

        // Scout Role
        if (robotRole == RobotRoles.Scout) 
        {
            Movement.scoutAction();
        }
        // Testing
        else if (turnCount < 75)
        {
            tryMove(randomDirection());
        }  

    }

    private static void senseAreaForRobots() throws GameActionException
    {
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;
        distanceToclosestRobotMapLocation = 64;
        closestRobotMapLocation = null;
        enemyEnlightenmentCenterIsAround = false;
        countOfEnemies = 0;
        countOfFriendlies = 0;
        sumOfEnemyConvictionNearby = 0;
        hasTarget = false;
        RobotInfo[] allRobotInfos = robotController.senseNearbyRobots(sensorRadiusSquared);

        for (RobotInfo robotInfo : allRobotInfos) {
            if (robotInfo.getType() == RobotType.POLITICIAN 
            && robotInfo.getTeam() == enemy) 
            {
                enemyPoliticianIsAround = true;
                countOfEnemies++;
                sumOfEnemyConvictionNearby += robotInfo.getConviction();
                getClosestEnemyRobot(robotInfo);                
            }
            else if (robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER 
            && robotInfo.getTeam() == enemy
            && robotInfo.getLocation().isWithinDistanceSquared(robotController.getLocation(), sensorRadiusSquared)) 
            {
                enemyEnlightenmentCenterIsAround = true;
                enemyEnlightenmentCenterDistanceSquared = robotInfo.getLocation().distanceSquaredTo(robotController.getLocation());
                enemyEnlightenmentCenterConviction = robotInfo.getConviction();
                countOfEnemies++;
                turnsNearEnemyEnlightenmentCenter++;
            }
            else if (robotInfo.getTeam() == enemy) 
            {
                sumOfEnemyConvictionNearby += robotInfo.getConviction();
                getClosestEnemyRobot(robotInfo);
            }
            else if (robotInfo.getTeam() == friendly 
            && robotInfo.getType() == RobotType.POLITICIAN
            && robotInfo.getConviction() >= POLITICIAN_EC_BOMB
            && enemyEnlightenmentCenterIsAround)
            {
                politicianECBombNearby = true;
                if (robotInfo.getID() > robotController.getID() && robotRole == RobotRoles.PoliticianEnlightenmentCenterBomb) {
                    lowestRobotIdOfFriendlies = true;
                }               
            }
            else if (robotInfo.getTeam() == friendly)
            {
                countOfFriendlies++;
            }
        }
    }

    private static void getClosestEnemyRobot(RobotInfo robotInfo)
    {
        if (robotController.getLocation().distanceSquaredTo(robotInfo.getLocation()) <= distanceToclosestRobotMapLocation) 
        {
            closestRobotMapLocation = robotInfo.getLocation();
            distanceToclosestRobotMapLocation = robotController.getLocation().distanceSquaredTo(robotInfo.getLocation()); 
        }
    }

    private static void getSumOfEnemyConvictionInEnemyEnlightenmentRadiusSquared()
    {
        countOfEnemiesInActionRadiusAroundEnemyEnlightenmentcenter = 0;
        sumOfEnemyConviciontInActionRadiusAroundEnemyEnlightenmentcenter = 0;
        countOfFrienliesInActionRadiusAroundEnemyEnlightenmentcenter = 0;

        RobotInfo[] allRobotInfos = robotController.senseNearbyRobots(enemyEnlightenmentCenterDistanceSquared);
        for (RobotInfo robotInfo : allRobotInfos) {
            if (robotInfo.getTeam() == enemy && robotInfo.getType() != RobotType.ENLIGHTENMENT_CENTER) 
            {
                countOfEnemiesInActionRadiusAroundEnemyEnlightenmentcenter++;
                sumOfEnemyConviciontInActionRadiusAroundEnemyEnlightenmentcenter += robotInfo.getConviction();
            }
            else if(robotInfo.getTeam() == friendly)
            {
                countOfFrienliesInActionRadiusAroundEnemyEnlightenmentcenter++;
            }
        }
    }

    static void decideIfEmpowerForNonEnlightenmentCenterBombs() throws GameActionException
    {
        if (((countOfEnemies >= 2) || (countOfEnemies !=0 && empowerCanConvertEnemyAtMaxRadius()))
        && robotController.canEmpower(ACTION_RADIUS_POLITICIAN)) 
        {
            robotController.empower(ACTION_RADIUS_POLITICIAN); // TODO: Get the actual radius not the full thing
            return;
        }
        else if (countOfEnemies != 0)
        {            
            hasTarget = true;            
        }
        else
        {
            moveRobot = true;
        }   
    }

    static void decideIfEmpowerForEnlightenmentCenterBombs() throws GameActionException
    {
        if (robotController.canEmpower(enemyEnlightenmentCenterDistanceSquared) 
        && enemyEnlightenmentCenterIsAround
        && enemyEnlightenmentCenterDistanceSquared <= 9) 
        {
            if ((hasEnoughConvictionToConvertEnlightenmentCenter() || 
                (lowestRobotIdOfFriendlies && enemyEnlightenmentCenterDistanceSquared <= 2)))
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
            else if (turnsNearEnemyEnlightenmentCenter >= 10 
            || countOfFrienliesInActionRadiusAroundEnemyEnlightenmentcenter <= 1) 
            {
                robotController.empower(enemyEnlightenmentCenterDistanceSquared);
                return;
            }        
        }
        else
        {
            moveRobot = true;
        }  
    }

    static void moveToEnemyEnlightenmentCenter(MapLocation enemyCenterLocation) throws GameActionException
    {
        Movement.basicBugMovement(enemyCenterLocation);
    }

    private static void moveAwayFromEnemyEnlightenmentCenter() throws GameActionException
    {
        Movement.moveAwayFromLocation(currentEnemyEnlightenmentCenterGoingFor);
    }
    
    static boolean empowerCanConvertEnemyAtMaxRadius()
    {
        boolean empowerCanConvertEnemy = false;
        int countOfAllRobotsNearby = countOfEnemies + countOfFriendlies;
        int remainderOfEnemyConviction = 0;

        if (countOfAllRobotsNearby > 0) 
        {
            remainderOfEnemyConviction = (int) (sumOfEnemyConvictionNearby - (getCurrentConviction()/countOfAllRobotsNearby));
        }

        if (remainderOfEnemyConviction < 0) 
        {
            empowerCanConvertEnemy = true;
        }

        return empowerCanConvertEnemy;
    }

    static boolean hasEnoughConvictionToConvertEnlightenmentCenter()
    {
        getSumOfEnemyConvictionInEnemyEnlightenmentRadiusSquared();
        int countOfAllRobotsNearby = countOfEnemiesInActionRadiusAroundEnemyEnlightenmentcenter + countOfFrienliesInActionRadiusAroundEnemyEnlightenmentcenter;
        int remainderOfEnemyConviction = 0;
        boolean hasEnoughToEmpower = false;

        if (countOfEnemiesInActionRadiusAroundEnemyEnlightenmentcenter > 0) 
        {
            remainderOfEnemyConviction = (int) (enemyEnlightenmentCenterConviction - (getCurrentConviction()/countOfAllRobotsNearby));
        }
        else 
        {
            remainderOfEnemyConviction = enemyEnlightenmentCenterConviction -  robotCurrentConviction;
        }

        if (remainderOfEnemyConviction < 0) 
        {
            hasEnoughToEmpower = true;
        }
        
        return hasEnoughToEmpower;
    }

    private static double getCurrentConviction()
    {
        return (robotCurrentConviction * empowerFactor) - POLITICIAN_TAX;
    }

    static void setup()
    {
        assignHomeEnlightenmentCenterLocation();
        robotCurrentInfluence = robotController.getInfluence();
        assignRobotRole();        
        enemy = robotController.getTeam().opponent();
        friendly = robotController.getTeam();
    }

    static void assignRobotRole()
    {
        if (robotCurrentInfluence >= POLITICIAN_EC_BOMB) 
        {
            robotRole = RobotRoles.PoliticianEnlightenmentCenterBomb;
        }
        else if (robotCurrentInfluence == POLITICIAN_LEADER) 
        {
            robotRole = RobotRoles.Leader;
        }
        else if (robotCurrentInfluence >= POLITICIAN_FOLLOWER) 
        {
            robotRole = RobotRoles.Follower;
        }
    }
}
