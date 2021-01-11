package testPlayerv01;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.RootPaneContainer;

import battlecode.common.*;

@SuppressWarnings("unused")
public class EnlightenmentCenterTest01 extends RobotPlayer {

    private static class RobotBuilder
    {
        static RobotType robotTypeToBuild;
        static Direction directionToSpawn;
        static int influenceToUse;
    }

    private static MapLocation[] squaresAroundEnlightenmentCenter = new MapLocation[8];

    //private static Map<Integer, RobotType> robotIds = new HashMap<Integer, RobotType>();
    private static List<Integer> robotIds = new ArrayList<>();

    private static int numberOfEnlightenmentCenters = 0;

    // Count of My robots
    private static int countOfPoliticianBomb = 0;
    private static int countOfSlanderer = 0;
    private static int countOfPoliticians = 0;
    private static int countOfMuckrakers = 0;

    // Friendly
    private static boolean friendlySlandererNearby;

    // Enemy robots in the area
    private static int countOfEnemyPoliticiansOverTwentyInfluence = 0;
    private static int countOfEnemyPoliticiansUnderTwentyInfluence = 0;
    private static int countOfEnemyMuckraker = 0;
    private static int countOfOtherRobots = 0;
    private static List<MapLocation> enemyTargetNearby = new ArrayList<>(); 

    // Beginning
    private static final int NUMBER_OF_MUCKRAKERS_IN_BEGINNING = 40;    
    private static int numberOfMuckrakersToCreateInBeginning = 0;

    private static boolean buildThisTurn = false;
    private static int turnsNotBuilding;
    private static boolean stockUp = false;

    // Influence
    private static int lastTurnInfluence = 150;
    private static double rateOfInfluence;
    
    //This keeps looping
    @SuppressWarnings("unused")
    public static void run() throws GameActionException
    {        
        /*
            1. Make slanderers
            2. Create Scout Muckrakers
            3. Create more slanderers if safe
            4. Create bomb polis
            5. create more polis
            6. create defent muckrakers
            7. create scouts
        */
        countEnemiesNearby();
        buildThisTurn = false;
        setRateOfInfluenceReceived();

        if (robotController.getRoundNum() < 3) {
            RobotType firstBuild = RobotType.SLANDERER;
            int firstInfluence = robotController.getConviction();

            turnsNotBuilding = 0;
            buildThisTurn = true;
            RobotBuilder.robotTypeToBuild = firstBuild;
            RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
            RobotBuilder.influenceToUse = firstInfluence;
            countOfSlanderer++;
        }
        else if (robotController.getRoundNum() >= MIDDLE_GAME_ROUND_START 
        && robotController.getRobotCount() > 50) 
        {
            RobotType toBuild = RobotType.POLITICIAN;
            Random random = new Random();      
            int influence = random.nextInt(POLITICIAN_EC_BOMB - MAX_NORMAL_POLITICIAN) + MAX_NORMAL_POLITICIAN;

            turnsNotBuilding = 0;
            if (robotController.getConviction() >= influence * 1.1) {
                buildThisTurn = true;
            }
            
            RobotBuilder.influenceToUse = influence;
            RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
            RobotBuilder.robotTypeToBuild = toBuild;
            countOfPoliticians++;
        }
        else if (countOfMuckrakers < numberOfMuckrakersToCreateInBeginning 
        && robotController.getRoundNum() < MIDDLE_GAME_ROUND_START)
        {            
            RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
            RobotBuilder.influenceToUse = INFLUENCE_FOR_SCOUT;
            RobotBuilder.robotTypeToBuild = RobotType.MUCKRAKER;

            turnsNotBuilding = 0;
            buildThisTurn = true;
            countOfMuckrakers++;
        }
        else if ((countOfSlanderer < 5 || rateOfInfluence < .2) 
        && (robotController.getInfluence() > 150
        && isItSafe())) 
        {
            turnsNotBuilding = 0;
            buildThisTurn = true;
            RobotBuilder.robotTypeToBuild = RobotType.SLANDERER;
            RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
            RobotBuilder.influenceToUse = robotController.getInfluence();
            countOfSlanderer++;
        }
        else if (enemyEnlightenmentCenterFound) 
        {
            if (isThereEnoughForBomb() && isItSafe()) 
            {
                RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
                RobotBuilder.influenceToUse = getAmountToMakePoliticianBomb();
                RobotBuilder.robotTypeToBuild = RobotType.POLITICIAN;

                turnsNotBuilding = 0;
                buildThisTurn = true;
                countOfPoliticianBomb++;
            }
            else if(!isThereEnoughForBomb() && isItSafe())
            {
                stockUp = true;
                turnsNotBuilding++;
            }
            else if (!isItSafe())
            {
                turnsNotBuilding++;
                MapLocation enemyRobotOverTwentyInfluenceNearby = getClosestEnemyRobotOverTwentyInfluenceLocation();
                sendLocation(enemyRobotOverTwentyInfluenceNearby, KILL_ENEMY_TARGET);
            }            
        }
        else if (!stockUp && countOfPoliticians < countOfMuckrakers)
        {
            // These are for the follower and leader groups stuff
            RobotType toBuild = RobotType.POLITICIAN;
            Random random = new Random();      
            int influence = random.nextInt(MAX_NORMAL_POLITICIAN - MIN_NORMAL_POLITICIAN) + MIN_NORMAL_POLITICIAN;

            turnsNotBuilding = 0;
            if (robotController.getConviction() > influence * 1.1) {
                buildThisTurn = true;
            }
            
            RobotBuilder.influenceToUse = influence;
            RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
            RobotBuilder.robotTypeToBuild = toBuild;
            countOfPoliticians++;
        }
        else if (friendlySlandererNearby && !defenderMuckrakerNearby()) 
        {
            RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
            RobotBuilder.influenceToUse = INFLUENCE_FOR_DEFEND_SLANDERER_MUCKRAKER;
            RobotBuilder.robotTypeToBuild = RobotType.MUCKRAKER;

            turnsNotBuilding = 0;
            buildThisTurn = true;
            countOfMuckrakers++;
        }
        else if (!stockUp) 
        {
            RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
            RobotBuilder.influenceToUse = INFLUENCE_FOR_SCOUT;
            RobotBuilder.robotTypeToBuild = RobotType.MUCKRAKER;

            turnsNotBuilding = 0;
            buildThisTurn = true;
            countOfMuckrakers++;
        }        
        

        if (buildThisTurn)
        {
            buildRobot();
        }

        if(robotController.getRoundNum() >= MIDDLE_GAME_ROUND_START && robotController.getTeamVotes() < 200)
        {
            Random random = new Random();      
            int influence = random.nextInt(20 - 2) + 2;
            if (robotController.canBid(influence))
            {
                robotController.bid(influence);
            }            
        }

        if (!enemyEnlightenmentCenterFound) 
        {
            checkIfRobotSignallingTheyFoundEnemyEnlightenmentCenter();
        }
        

        // send location of the enemey EC every 3 turns
        if (enemyEnlightenmentCenterMapLocation.size() > 0 && robotController.getRoundNum() % 3 == 0) 
        {
            sendLocation(enemyEnlightenmentCenterMapLocation.get(enemyEnlightenmentCenterMapLocation.size()), ENEMY_ENLIGHTENMENT_CENTER_FOUND);
            enemyEnlightenmentCenterFound = true;
        }

        // figure out high priority locations
        // decide the priority and who is going to attack there
        // there will be people not assigned, they have a certain flag, when it changes
        // ---> add it to a list of things going that way
        // ----> when that number of things going drops, build more..

        // --> For EC we want to figure out 1. the amount of conviction we are getting each turn
        // 2. the amount of conviction on the enemy EC
        // 3. how big the "Bomb" polis should be...
        // 4. how many are going to the enemy EC to harry until there is a big enough bomb?
    }

    private static void setRateOfInfluenceReceived() 
    {
        lastTurnInfluence = robotCurrentInfluence;
        robotCurrentInfluence = robotController.getInfluence();
        double differenceBetweenRounds = Math.abs(robotCurrentInfluence - lastTurnInfluence) * 1.0;
        double generatedRoundInfluence = (.2)*(Math.sqrt(robotController.getRoundNum()));
        rateOfInfluence = (differenceBetweenRounds - generatedRoundInfluence)/ generatedRoundInfluence;
	}

	private static void countEnemiesNearby()
    {
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;
        RobotInfo[] enemyRobots = robotController.senseNearbyRobots(sensorRadiusSquared, enemy);
        enemyTargetNearby.clear();
        countOfEnemyMuckraker = 0;
        countOfEnemyPoliticiansOverTwentyInfluence = 0;
        countOfEnemyPoliticiansUnderTwentyInfluence = 0;
        countOfOtherRobots = 0;

        for (RobotInfo enemyRobotInfo : enemyRobots) {
            if (enemyRobotInfo.getType() == RobotType.POLITICIAN && enemyRobotInfo.getConviction() > 20) 
            {
                enemyTargetNearby.add(enemyRobotInfo.getLocation());           
                countOfEnemyPoliticiansOverTwentyInfluence++;
            }
            else if (enemyRobotInfo.getType() == RobotType.POLITICIAN && enemyRobotInfo.getConviction() <= 20) 
            {
                countOfEnemyPoliticiansUnderTwentyInfluence++;
            }
            else if (enemyRobotInfo.getType() == RobotType.MUCKRAKER) 
            {
                countOfEnemyMuckraker++;    
            }
            else
            {
                countOfOtherRobots++;
            }
        }
    }

    private static boolean isThereEnoughForBomb() throws GameActionException
    {
        boolean isThereEnoughInfluence = false;
        int currentInfluence = robotController.getInfluence();

        if (currentInfluence >= POLITICIAN_EC_BOMB * 1.1) 
        {
            isThereEnoughInfluence = true;
        }        

        return isThereEnoughInfluence;
    }

    private static boolean isItSafe() throws GameActionException
    {     
        return countOfEnemyPoliticiansOverTwentyInfluence == 0;
    }

    private static int getAmountToMakePoliticianBomb()
    {
        int currentInfluence = robotController.getInfluence();

        currentInfluence *= .90; // Leave 10% incase something happens
        // TODO: In future, send the amount in the enemy EC.

        return currentInfluence;
    }

    private static MapLocation getClosestEnemyRobotOverTwentyInfluenceLocation()
    {
        MapLocation targetLocation = enemyTargetNearby.get(0); 
        int closestRobot = robotController.getType().sensorRadiusSquared;
        int distanceSquaredTo;

        for (int iterator = 0; iterator < enemyTargetNearby.size(); ++iterator) {
            distanceSquaredTo = robotController.getLocation().distanceSquaredTo(enemyTargetNearby.get(iterator));

            if (distanceSquaredTo <= closestRobot) 
            {
                closestRobot = distanceSquaredTo;
                targetLocation = enemyTargetNearby.get(iterator);
            }
        }

        return targetLocation;
    }

    private static boolean defenderMuckrakerNearby()
    {
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;
        RobotInfo[] friendlyRobots = robotController.senseNearbyRobots(sensorRadiusSquared, friendly);
        boolean defenderMuckrakerNearby = false;

        for (RobotInfo friendlyRobotInfo : friendlyRobots) 
        {
            if (friendlyRobotInfo.getType() == RobotType.MUCKRAKER && friendlyRobotInfo.getConviction() == INFLUENCE_FOR_DEFEND_SLANDERER_MUCKRAKER) 
            {
                defenderMuckrakerNearby = true;
            }
            else if (friendlyRobotInfo.getType() == RobotType.SLANDERER) 
            {
                friendlySlandererNearby = true;
            }
        }

        return defenderMuckrakerNearby;
    }

    private static Direction getAvailableDirectionToSpawn() throws GameActionException
    {
        Direction directionToSpawn = Direction.CENTER;

        for (MapLocation possibleLocation : squaresAroundEnlightenmentCenter)
        {
            if (!robotController.isLocationOccupied(possibleLocation)) 
            {
                directionToSpawn = robotController.getLocation().directionTo(possibleLocation);
                break;
            }
        }

        return directionToSpawn;
    }

    private static void checkIfRobotSignallingTheyFoundEnemyEnlightenmentCenter() throws GameActionException
    {
        for (int iterator = robotIds.size()-1; iterator >= 0; --iterator) 
        {
            int robotId = robotIds.get(iterator);
            if(robotController.canGetFlag(robotId))
            {
                checkRobotFlagsForEnemyLocation(robotId);                
            }
            else
            {
                robotIds.remove(iterator);
            }
        }
    }

    private static void checkRobotFlagsForEnemyLocation(int robotId) throws GameActionException
    {
        int flag = robotController.getFlag(robotId);

        if (flag != 0 && checkIfEnemeyEnlightenmentCenterHasBeenFound(flag)) 
        {                
            setEnemyEnlightenmentCenter(flag);
        }
    }

    private static void setEnemyEnlightenmentCenter(int flag) throws GameActionException
    {
        MapLocation enemyCenterLocation = getLocationFromFlag(flag);

        if (!enemyEnlightenmentCenterMapLocation.containsValue(enemyCenterLocation) || enemyEnlightenmentCenterMapLocation.size() == 0) 
        {
            enemyEnlightenmentCenterMapLocation.put(enemyEnlightenmentCenterMapLocation.size() + 1, enemyCenterLocation);
        }
    }

    private static void buildRobot() throws GameActionException
    {
        if (robotController.canBuildRobot(RobotBuilder.robotTypeToBuild, RobotBuilder.directionToSpawn, RobotBuilder.influenceToUse)) 
        {
            robotController.buildRobot(RobotBuilder.robotTypeToBuild, RobotBuilder.directionToSpawn, RobotBuilder.influenceToUse);

            MapLocation currentLocation = robotController.getLocation();
            RobotInfo newRobot = robotController.senseRobotAtLocation(robotController.adjacentLocation(RobotBuilder.directionToSpawn));
                
            if (!robotIds.contains(newRobot.getID())) 
            {
                robotIds.add(newRobot.getID());
            }
        }        
    }

    public static void setup() throws GameActionException
    {
        if (robotController.getRoundNum() == 1) {
            numberOfEnlightenmentCenters = robotController.getRobotCount();         
        } 
        if (numberOfEnlightenmentCenters > 0) 
        {
            numberOfMuckrakersToCreateInBeginning = NUMBER_OF_MUCKRAKERS_IN_BEGINNING/numberOfEnlightenmentCenters;
        }
        else
        {
            numberOfMuckrakersToCreateInBeginning = NUMBER_OF_MUCKRAKERS_IN_BEGINNING;
        }
        setSquaresAroundEnlightenmentCenter();
        enemy = robotController.getTeam().opponent();
        friendly = robotController.getTeam();

        // TODO: What is "announceSelfLocation?"

        //storeHQLocationAndGetConstants();
        //announceSelfLocation(1);
        // if (rc.getTeamSoup() >= RobotType.DELIVERY_DRONE.cost + RobotType.MINER.cost * 2) {
        //     boolean builtUnit = false;
        //     for (int i = 9; --i >= 1; ) {
        //         if (tryBuild(RobotType.DELIVERY_DRONE, buildDir)) {
        //             builtUnit = true;
        //             break;
        //         } else {
        //             buildDir = buildDir.rotateRight();
        //         }
        //     }
        //     if (builtUnit) {
        //         dronesBuilt++;
        //     }
        //     buildDir = buildDir.rotateRight();
        // }
    }

    private static void setSquaresAroundEnlightenmentCenter()
    {
        int iterator = 0;
        for (Direction direction : directions) {
            squaresAroundEnlightenmentCenter[iterator] = robotController.adjacentLocation(direction);
            ++iterator;
        }
        
    }
    
}