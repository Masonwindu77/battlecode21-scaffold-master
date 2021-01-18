package testPlayerv01;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import battlecode.common.*;
import testPlayerv01.Service.Communication;
import testPlayerv01.Service.EnlightenmentCenterHelper;

@SuppressWarnings("unused")
public class EnlightenmentCenterTest01 extends RobotPlayer 
{

    protected static class RobotBuilder 
    {
        public static RobotType robotTypeToBuild;
        public static Direction directionToSpawn;
        public static int influenceToUse;
    }

    // Gotta sava bytecode somehow
    protected static List<Integer> robotIds = new ArrayList<>();
    protected static int whereIteratorStopped = 0;

    protected static int numberOfEnlightenmentCenters = 0;

    protected static double empowerFactorInThirtyTurns = 1;
    protected static final int AMOUNT_OF_VOTES_NEEDED_TO_WIN = 750;

    protected static int convertedMessageSent = 0;

    // Count of My robots
    protected static int countOfPoliticianBomb = 0;
    protected static int countOfSlanderer = 0;
    protected static int countOfPoliticians = 0;
    protected static int countOfMuckrakers = 0;

    // Slanderer
    protected static final int[] slandererInfluenceAmount = { 21, 41, 62, 85, 107, 130, 153, 
        178, 203, 228, 282, 310, 339, 369, 399, 431, 463, 497, 
        568, 606, 644, 683, 724, 767, 810, 855, 902, 949};

    // Friendly
    protected static boolean friendlySlandererNearby;

    // Enemy robots in the area
    protected static int countOfEnemyPoliticiansOverTwentyInfluence = 0;
    protected static int countOfEnemyPoliticiansUnderTwentyInfluence = 0;
    protected static int countOfEnemyMuckraker = 0;
    protected static int countOfOtherRobots = 0;
    protected static List<MapLocation> enemyTargetNearby = new ArrayList<>();
    protected static boolean weSpawnedRightNextToEnemy = false;

    // Beginning
    protected static final int NUMBER_OF_MUCKRAKERS_IN_BEGINNING = 30;
    protected static int numberOfMuckrakersToCreateInBeginning = 0;

    // Building
    protected static boolean buildThisTurn = false;
    protected static boolean builtLastTurn = false;
    protected static boolean builtScoutLastTurn = false;
    protected static int turnsNotBuilding;
    protected static boolean stockUp = false;
    protected static boolean buildSlanderer = false;
    protected static int amountNeededForSlanderer;
    protected static double differenceOfInfluenceBetweenRounds;

    // Bidding
    protected static int currentVoteCount = 0;
    protected static int amountToBid = 1;

    // Influence
    protected static int lastTurnInfluence = 150;
    protected static double rateOfInfluence;
    protected static double generatedRoundInfluence;
    protected static int income;

    // This keeps looping
    @SuppressWarnings("unused")
    public static void run() throws GameActionException 
    {
        countEnemiesNearby();
        buildThisTurn = false;
        enemyEnlightenmentCenterInfluenceHasBeenUpdated = false;
        convertedEnemyEnlightenmentCenterHasBeenProcessedThisTurn = false;

        empowerFactorInThirtyTurns = robotController.getEmpowerFactor(friendly, 30);
        empowerFactor = robotController.getEmpowerFactor(friendly, 0);
        enemyEnlightenmentCenterCurrentInfluence = 0;
        checkFlagsFromRobots();

        // TODO: get this to work buildThisTurn = decideWhatRobotToBuild();

        if (!builtLastTurn || builtScoutLastTurn) {
            lastTurnInfluence = robotCurrentInfluence;
            robotCurrentInfluence = robotController.getInfluence();
            setRateOfInfluenceReceived();
        }

        EnlightenmentCenterHelper.decideWhatToBuild();
        
        // else if (friendlySlandererNearby && !defenderMuckrakerNearby())
        // {
        // RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
        // RobotBuilder.influenceToUse = INFLUENCE_FOR_DEFEND_SLANDERER_MUCKRAKER;
        // RobotBuilder.robotTypeToBuild = RobotType.MUCKRAKER;

        // turnsNotBuilding = 0;
        // buildThisTurn = true;
        // countOfMuckrakers++;
        // }
        
        // Build a scout if stocking up
        if (stockUp || !buildThisTurn)
        {
            RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
            RobotBuilder.influenceToUse = INFLUENCE_FOR_SCOUT;
            RobotBuilder.robotTypeToBuild = RobotType.MUCKRAKER;

            builtScoutLastTurn = true;
            turnsNotBuilding = 0;
            buildThisTurn = true;
            countOfMuckrakers++;
        }

        if (buildThisTurn) 
        {
            buildRobot();
        } 
        else
        {
            builtLastTurn = false;
        }

        // Bidding
        if (robotController.getRoundNum() >= 400
                && robotController.getTeamVotes() < AMOUNT_OF_VOTES_NEEDED_TO_WIN) 
        {
            int influence = amountToBid;
            if (robotController.canBid(influence)) 
            {
                robotController.bid(influence);
            }

            if (currentVoteCount >= robotController.getTeamVotes()) {
                amountToBid += 2;
            } else {
                currentVoteCount = robotController.getTeamVotes();
            }
        }

        decideIfShouldBuildSlanderer();
        
        decideToSetFlags();


        // figure out high priority locations
        // decide the priority and who is going to attack there
        // there will be people not assigned, they have a certain flag, when it changes
        // ---> add it to a list of things going that way
        // ----> when that number of things going drops, build more..

        // --> For EC we want to figure out 1. the amount of conviction we are getting
        // each turn
        // 2. the amount of conviction on the enemy EC
        // 3. how big the "Bomb" polis should be...
        // 4. how many are going to the enemy EC to harry until there is a big enough
        // bomb?
    }

    // if (influence < 1000 || income < 250)
    // income == lastRoundInfluence - thisRoundInfluence
    protected static void decideIfShouldBuildSlanderer()
    {
        if (!buildSlanderer && shouldBuildSlanderer())
        {
            buildSlanderer = true;
            for (int iterator = (slandererInfluenceAmount.length - 1); iterator > 0; --iterator) {
                if (slandererInfluenceAmount[iterator] > robotCurrentInfluence 
                && slandererInfluenceAmount[iterator - 1] < robotCurrentInfluence) 
                {
                    amountNeededForSlanderer = slandererInfluenceAmount[iterator];
                    break;
                } 
                else if (robotCurrentInfluence >= slandererInfluenceAmount[(slandererInfluenceAmount.length - 1)])
                {
                    amountNeededForSlanderer = slandererInfluenceAmount[iterator];
                    break;
                }
            }
        }
    }

    protected static boolean shouldBuildSlanderer() {

        boolean buildSlanderer = false;
        robotCurrentInfluence = robotController.getInfluence();

        if ((robotCurrentInfluence < 1000 && income < 150) 
        && robotController.getRoundNum() > 300 
        && robotController.getRobotCount() > 50)
        {
            buildSlanderer = true;
        }
        else if
        ((robotCurrentInfluence < 300 || income < 25) 
        && robotController.getRoundNum() < 100 
        && (robotController.getRobotCount()) > 15
        && enemyEnlightenmentCenterFound)
        {
            buildSlanderer = true;
        }
        else if(!enemyEnlightenmentCenterFound 
        && countOfSlanderer <= 2
        && countOfPoliticians >= 15)
        {
            buildSlanderer = true;
        }

        return buildSlanderer;
    }

    private static void checkFlagsFromRobots() throws GameActionException 
    {
        for (; whereIteratorStopped < robotIds.size() && Clock.getBytecodesLeft() >= 3500; ++whereIteratorStopped) 
        {
            int robotId = robotIds.get(whereIteratorStopped);

            if (robotController.canGetFlag(robotId)) 
            {
                int flag = robotController.getFlag(robotId);

                if (flag != 0) 
                {
                    int extraInformation = Communication.getExtraInformationFromFlag(flag);
                    EnlightenmentCenterHelper.checkFlagsForSignals(extraInformation, flag);
                }           
            } 
            else 
            {
                robotIds.remove(whereIteratorStopped); // TODO: this could be where you count the # of robots alive.
            }
        }

        // reset iterator
        if (whereIteratorStopped == robotIds.size()) 
        {
            whereIteratorStopped = 0;    
        }
    }

    private static void decideToSetFlags() throws GameActionException
    {
        if (!enemyEnlightenmentCenterMapLocation.isEmpty() && robotController.getRoundNum() % 2 == 0) 
        {
            Communication.sendLocation(enemyEnlightenmentCenterMapLocation.get(0), Communication.ENEMY_ENLIGHTENMENT_CENTER_FOUND);
        }
        else if(!convertedEnemyEnlightenmentCenterMapLocation.isEmpty() 
            && robotController.getRoundNum() % 2 != 0
            && enemyEnlightenmentCenterHasBeenConverted)
        {
            Communication.announceEnemyEnlightenmentCenterHasBeenConverted();
            enemyEnlightenmentCenterHasBeenConverted = false; // reset this so it doesn't keep spamming the message
        }
        else if (!neutralEnlightenmentCenterMapLocation.isEmpty() && robotController.getRoundNum() % 2 != 0)
        {
            Communication.sendLocation(neutralEnlightenmentCenterMapLocation.get(0), Communication.NUETRAL_ENLIGHTENMENT_CENTER_FOUND);
        }
        else if (enemyEnlightenmentCenterMapLocation.isEmpty() && neutralEnlightenmentCenterMapLocation.isEmpty())
        {
            robotController.setFlag(0);
        }
    }

    protected static void setEnemyEnlightenmentCenter(int flag) throws GameActionException {
        MapLocation enemyEnlightenmentCenterLocation = Communication.getLocationFromFlag(flag);

        if ((!enemyEnlightenmentCenterMapLocation.contains(enemyEnlightenmentCenterLocation) || enemyEnlightenmentCenterMapLocation.isEmpty())
            && (convertedEnemyEnlightenmentCenterMapLocation.isEmpty() || !convertedEnemyEnlightenmentCenterMapLocation.contains(enemyEnlightenmentCenterLocation))
        )
        {
            enemyEnlightenmentCenterMapLocation.add(enemyEnlightenmentCenterLocation);
            enemyEnlightenmentCenterFound = true;
        }
    }

    protected static boolean isItSafeForSlanderer() {
        return countOfEnemyMuckraker == 0;
    }

    private static void setRateOfInfluenceReceived() {
        differenceOfInfluenceBetweenRounds = Math.abs(robotCurrentInfluence - lastTurnInfluence) * 1.0;
        generatedRoundInfluence = (.2) * (Math.sqrt(robotController.getRoundNum()));
        income = (int) differenceOfInfluenceBetweenRounds;
    }

    private static void countEnemiesNearby() {
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;
        RobotInfo[] enemyRobots = robotController.senseNearbyRobots(sensorRadiusSquared, enemy);
        enemyTargetNearby.clear();
        countOfEnemyMuckraker = 0;
        countOfEnemyPoliticiansOverTwentyInfluence = 0;
        countOfEnemyPoliticiansUnderTwentyInfluence = 0;
        countOfOtherRobots = 0;

        for (RobotInfo enemyRobotInfo : enemyRobots) {
            if (enemyRobotInfo.getType() == RobotType.POLITICIAN && enemyRobotInfo.getConviction() > 20) {
                enemyTargetNearby.add(enemyRobotInfo.getLocation());
                countOfEnemyPoliticiansOverTwentyInfluence++;
            } else if (enemyRobotInfo.getType() == RobotType.POLITICIAN && enemyRobotInfo.getConviction() <= 20) {
                countOfEnemyPoliticiansUnderTwentyInfluence++;
            } else if (enemyRobotInfo.getType() == RobotType.MUCKRAKER) {
                countOfEnemyMuckraker++;
            } else {
                countOfOtherRobots++;
            }
        }
    }

    protected static boolean isThereEnoughForBomb() throws GameActionException {
        boolean isThereEnoughInfluence = false;
        int currentInfluence = robotController.getInfluence();

        if ((enemyEnlightenmentCenterCurrentInfluence != 0) 
        && (currentInfluence * empowerFactorInThirtyTurns >= enemyEnlightenmentCenterCurrentInfluence)
        && currentInfluence >= POLITICIAN_EC_BOMB)
        {
            isThereEnoughInfluence = true;
        } 
        else if ((enemyEnlightenmentCenterCurrentInfluence - currentInfluence) > 250 && income < 100)
        {
            buildSlanderer = true;
        }
        else if (enemyEnlightenmentCenterCurrentInfluence == 0 && currentInfluence > (POLITICIAN_EC_BOMB + 70) )
        {
            isThereEnoughInfluence = true;
        }

        return isThereEnoughInfluence;
    }

    protected static boolean isItSafe() throws GameActionException {
        return countOfEnemyPoliticiansOverTwentyInfluence == 0;
    }

    protected static int getAmountToMakePoliticianBomb() {
        int currentInfluence = robotController.getInfluence();

        if (enemyEnlightenmentCenterCurrentInfluence != 0 && enemyEnlightenmentCenterCurrentInfluence > POLITICIAN_EC_BOMB) 
        {
            currentInfluence = enemyEnlightenmentCenterCurrentInfluence;
        }       

        return currentInfluence;
    }

    protected static MapLocation getClosestEnemyRobotOverTwentyInfluenceLocation() {
        MapLocation targetLocation = enemyTargetNearby.get(0);
        int closestRobot = robotController.getType().sensorRadiusSquared;
        int distanceSquaredTo;

        for (int iterator = 0; iterator < enemyTargetNearby.size(); ++iterator) {
            distanceSquaredTo = robotController.getLocation().distanceSquaredTo(enemyTargetNearby.get(iterator));

            if (distanceSquaredTo <= closestRobot) {
                closestRobot = distanceSquaredTo;
                targetLocation = enemyTargetNearby.get(iterator);
            }
        }

        return targetLocation;
    }

    private static boolean defenderMuckrakerNearby() {
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;
        RobotInfo[] friendlyRobots = robotController.senseNearbyRobots(sensorRadiusSquared, friendly);
        boolean defenderMuckrakerNearby = false;

        for (RobotInfo friendlyRobotInfo : friendlyRobots) {
            if (friendlyRobotInfo.getType() == RobotType.MUCKRAKER
                    && friendlyRobotInfo.getConviction() == INFLUENCE_FOR_DEFEND_SLANDERER_MUCKRAKER) {
                defenderMuckrakerNearby = true;
            } else if (friendlyRobotInfo.getType() == RobotType.SLANDERER) {
                friendlySlandererNearby = true;
            }
        }

        return defenderMuckrakerNearby;
    }

    protected static Direction getAvailableDirectionToSpawn() throws GameActionException {
        Direction directionToSpawn = Direction.CENTER;

        for (MapLocation possibleLocation : squaresAroundEnlightenmentCenter) 
        {
            if (robotController.onTheMap(possibleLocation) && !robotController.isLocationOccupied(possibleLocation)) 
            {
                directionToSpawn = robotController.getLocation().directionTo(possibleLocation);
                break;
            }
        }

        return directionToSpawn;
    }

    private static void buildRobot() throws GameActionException {
        if (robotController.canBuildRobot(RobotBuilder.robotTypeToBuild, RobotBuilder.directionToSpawn, RobotBuilder.influenceToUse)) 
        {
            robotController.buildRobot(RobotBuilder.robotTypeToBuild, RobotBuilder.directionToSpawn, RobotBuilder.influenceToUse);

            MapLocation currentLocation = robotController.getLocation();
            RobotInfo newRobot = robotController.senseRobotAtLocation(robotController.adjacentLocation(RobotBuilder.directionToSpawn));

            if (!robotIds.contains(newRobot.getID())) 
            {
                robotIds.add(newRobot.getID());
            }

            builtLastTurn = true;
        }
    }

    public static void setup() throws GameActionException {
        if (robotController.getRoundNum() == 1) 
        {
            numberOfEnlightenmentCenters = robotController.getRobotCount();
        }

        if (numberOfEnlightenmentCenters > 0) 
        {
            numberOfMuckrakersToCreateInBeginning = NUMBER_OF_MUCKRAKERS_IN_BEGINNING / numberOfEnlightenmentCenters;
        } 
        else 
        {
            numberOfMuckrakersToCreateInBeginning = NUMBER_OF_MUCKRAKERS_IN_BEGINNING;
        }

        setConstants();
        spawnEnlightenmentCenterHomeLocation = robotController.getLocation();
        setSquaresAroundEnlightenmentCenter();        
        checkIfSpawnIsNearEnemyEnlightenmentCenter();
    }

    private static void checkIfSpawnIsNearEnemyEnlightenmentCenter()
    {
        RobotInfo[] nearbyRobotInfos = robotController.senseNearbyRobots(robotController.getType().sensorRadiusSquared, enemy);

        for (RobotInfo robotInfo : nearbyRobotInfos) {
            if (robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                weSpawnedRightNextToEnemy = true;
                enemyEnlightenmentCenterFound = true;
                enemyEnlightenmentCenterMapLocation.add(robotInfo.getLocation());
            }
        }
        
    }

}