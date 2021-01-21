package testPlayerv01;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import battlecode.common.*;
import testPlayerv01.Model.EnlightenmentCenterInfo;
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
    protected static List<Integer> robotIds = new ArrayList<>(); // TODO: Make this an arraylist of robotInfo....
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
    protected static int countOfDefenderPolitician;
    protected static int countOfFriendlySlandererNearby;
    protected static int countOfDefenderPoliticianNearby;

    // Slanderer
    protected static final int[] slandererInfluenceAmount = { 21, 41, 62, 85, 107, 130, 153, 
        178, 203, 228, 282, 310, 339, 369, 399, 431, 463, 497, 
        568, 606, 644, 683, 724, 767, 810, 855, 902, 949};

    // Friendly
    protected static boolean friendlySlandererNearby;
    protected static boolean defenderPoliticianNearby;    

    // Neutral
    //protected static Map<MapLocation, Pair<Integer, Integer>> neutralEnlightenmentCenters = new HashMap<>();

    // Enemy robots in the area
    protected static int countOfEnemyPoliticiansOverFiftyInfluence = 0;
    protected static int countOfEnemyPoliticiansUnderTwentyInfluence = 0;
    protected static int countOfEnemyMuckraker = 0;
    protected static int countOfOtherRobots = 0;
    protected static List<MapLocation> enemyTargetNearby = new ArrayList<>();
    protected static boolean weSpawnedRightNextToEnemy = false;

    // Beginning
    protected static final int NUMBER_OF_MUCKRAKERS_IN_BEGINNING = 8;
    protected static int numberOfMuckrakersToCreateInBeginning = 0;
    protected static int scoutIterator = 0;

    // Building
    protected static boolean buildThisTurn = false;
    protected static boolean builtLastTurn = false;
    protected static boolean builtScoutLastTurn = false;
    protected static int turnsNotBuilding;
    protected static boolean stockUp = false;
    protected static boolean buildSlanderer = false;
    protected static int amountNeededForSlanderer;
    protected static boolean buildScout = false;
    protected static int differenceOfInfluenceBetweenRounds;

    // Bidding
    protected static int currentVoteCount = 0;
    protected static int amountToBid = 2;

    // Influence
    protected static int lastTurnInfluence = 150;
    protected static double generatedRoundInfluence;
    protected static int influenceUsedFromLastBuild;
    protected static int income;

    // This keeps looping
    public static void run() throws GameActionException 
    {
        countEnemiesNearby();
        buildThisTurn = false;
        enemyEnlightenmentCenterInfluenceHasBeenUpdated = false;
        convertedEnemyEnlightenmentCenterHasBeenProcessedThisTurn = false;

        lastTurnInfluence = robotCurrentInfluence;
        robotCurrentInfluence = robotController.getInfluence();

        countOfDefenderPoliticianNearby = 0;
        countOfFriendlySlandererNearby = 0;

        empowerFactorInThirtyTurns = robotController.getEmpowerFactor(friendly, 30);
        empowerFactor = robotController.getEmpowerFactor(friendly, 0);
        enemyEnlightenmentCenterCurrentInfluence = 0;

        checkFlagsFromRobots();
        setRateOfInfluenceReceived();
        decideIfShouldBuildSlanderer();
        decideIfShouldBuildMoreScouts();
        EnlightenmentCenterHelper.decideWhatToBuild();

        if (buildThisTurn) 
        {
            buildRobot();
            builtLastTurn = true;
        } 
        else
        {
            influenceUsedFromLastBuild = 0;
            builtLastTurn = false;
        }

        // Bidding
        if (robotController.getRoundNum() >= 400
                && robotController.getTeamVotes() < AMOUNT_OF_VOTES_NEEDED_TO_WIN && isItSafe()) 
        {
            int influenceToBid = amountToBid;
            if (robotCurrentInfluence > 100000 && influenceToBid < 500) 
            {
                influenceToBid = 500;
            }
            
            if (robotController.canBid(influenceToBid)) 
            {
                robotController.bid(influenceToBid);
            }

            if (currentVoteCount >= robotController.getTeamVotes()) {
                amountToBid += 2;
            } else {
                currentVoteCount = robotController.getTeamVotes();
            }
        }
        else
        {
            int influenceToBid = amountToBid;
            if (robotController.canBid(influenceToBid)) 
            {
                robotController.bid(influenceToBid);
            }
        }
        
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
            getAmountNeededForSlanderer();
        }
    }

    protected static boolean shouldBuildSlanderer() {

        boolean shouldBuildSlanderer = false;
        robotCurrentInfluence = robotController.getInfluence();

        if ((robotCurrentInfluence < 1000 || income < 200) 
        && robotController.getRoundNum() >= 300)
        {
            shouldBuildSlanderer = true;
        }
        else if
        ((robotCurrentInfluence < 300 || income < 20) 
        && robotController.getRoundNum() < 300)
        {
            shouldBuildSlanderer = true;
        }
        else if(!enemyEnlightenmentCenterFound 
        && countOfSlanderer <= 2)
        {
            shouldBuildSlanderer = true;
        }

        return shouldBuildSlanderer;
    }

    protected static void getAmountNeededForSlanderer()
    {
        for (int iterator = (slandererInfluenceAmount.length - 1); iterator > 0; --iterator) 
        {
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

    protected static void decideIfShouldBuildMoreScouts()
    {
        if (!buildScout 
        && (countOfMuckrakers < countOfPoliticians 
            || (!enemyEnlightenmentCenterFound && robotController.getRoundNum() > 150
            && income < 25)))
        {
            buildScout = true;
        }
    }

    private static void checkFlagsFromRobots() throws GameActionException 
    {
        for (; (whereIteratorStopped < robotIds.size() && Clock.getBytecodesLeft() >= 3500); whereIteratorStopped++) 
        {
            int robotId = robotIds.get(whereIteratorStopped);

            if (robotController.canGetFlag(robotId)) 
            {
                int flag = robotController.getFlag(robotId);

                if (flag != 0 && flag != Communication.SLANDERER_FLAG) 
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
            //neutralEnlightenmentCenterMapLocation.sort(Comparator.);
            if (neutralEnlightenmentCenterMapLocation.size() > 1) 
            {
                int distanceSquaredFrom = 1000;
                MapLocation closestNeutralLocation = neutralEnlightenmentCenterMapLocation.get(0);
                for (MapLocation mapLocation : neutralEnlightenmentCenterMapLocation) 
                {
                    if (robotController.getLocation().distanceSquaredTo(mapLocation) < distanceSquaredFrom) 
                    {
                        closestNeutralLocation = mapLocation;
                        distanceSquaredFrom = robotController.getLocation().distanceSquaredTo(mapLocation);
                    }
                }

                Communication.sendLocation(closestNeutralLocation, Communication.NUETRAL_ENLIGHTENMENT_CENTER_FOUND);
            }
            else
            {
                Communication.sendLocation(neutralEnlightenmentCenterMapLocation.get(0), Communication.NUETRAL_ENLIGHTENMENT_CENTER_FOUND);
            }
            
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

    private static void setRateOfInfluenceReceived() 
    {
        differenceOfInfluenceBetweenRounds = robotCurrentInfluence - lastTurnInfluence;
        generatedRoundInfluence = Math.ceil((.2) * (Math.sqrt(robotController.getRoundNum())));
        income = (int) (differenceOfInfluenceBetweenRounds + influenceUsedFromLastBuild);
    }

    private static void countEnemiesNearby() 
    {
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;
        RobotInfo[] enemyRobots = robotController.senseNearbyRobots(sensorRadiusSquared, enemy);
        enemyTargetNearby.clear();
        countOfEnemyMuckraker = 0;
        countOfEnemyPoliticiansOverFiftyInfluence = 0;
        countOfEnemyPoliticiansUnderTwentyInfluence = 0;
        countOfOtherRobots = 0;

        for (RobotInfo enemyRobotInfo : enemyRobots) 
        {
            if (enemyRobotInfo.getType() == RobotType.POLITICIAN && enemyRobotInfo.getConviction() > 50) 
            {
                enemyTargetNearby.add(enemyRobotInfo.getLocation());
                countOfEnemyPoliticiansOverFiftyInfluence++;
            } 
            else if (enemyRobotInfo.getType() == RobotType.POLITICIAN && enemyRobotInfo.getConviction() <= 20)
            {
                countOfEnemyPoliticiansUnderTwentyInfluence++;
            } else if (enemyRobotInfo.getType() == RobotType.MUCKRAKER) 
            {
                countOfEnemyMuckraker++;
            } 
            else 
            {
                countOfOtherRobots++;
            }
        }
    }

    protected static boolean isThereEnoughForBomb() throws GameActionException {
        boolean isThereEnoughInfluence = false;
        int currentInfluence = robotController.getInfluence();

        if ((enemyEnlightenmentCenterCurrentInfluence != 0) 
        && (((currentInfluence * empowerFactorInThirtyTurns) - POLITICIAN_TAX) >= enemyEnlightenmentCenterCurrentInfluence)
        && (currentInfluence - POLITICIAN_TAX) >= POLITICIAN_EC_BOMB)
        {
            isThereEnoughInfluence = true;
        } 
        else if (enemyEnlightenmentCenterCurrentInfluence == 0 && currentInfluence > (POLITICIAN_EC_BOMB + 70) )
        {
            isThereEnoughInfluence = true;
        }

        return isThereEnoughInfluence;
    }

    protected static boolean isThereEnoughForNeutralEnlightenmentCenterCapture() throws GameActionException {
        boolean isThereEnoughInfluence = false;
        int currentInfluence = robotController.getInfluence();

        if ((neutralEnlightenmentCenterCurrentInfluence != 0) 
        && (((currentInfluence * empowerFactorInThirtyTurns) - POLITICIAN_TAX) >= neutralEnlightenmentCenterCurrentInfluence)
        && (currentInfluence - POLITICIAN_TAX) >= POLITICIAN_EC_BOMB)
        {
            isThereEnoughInfluence = true;
        } 
        else if (neutralEnlightenmentCenterCurrentInfluence == 0 && currentInfluence > (POLITICIAN_EC_BOMB + 70) )
        {
            isThereEnoughInfluence = true;
        }

        return isThereEnoughInfluence;
    }

    protected static boolean isItSafe() throws GameActionException {
        return countOfEnemyPoliticiansOverFiftyInfluence == 0;
    }

    protected static int getAmountToMakePoliticianBomb() {
        int currentInfluence = robotController.getInfluence();

        if (enemyEnlightenmentCenterCurrentInfluence != 0 && enemyEnlightenmentCenterCurrentInfluence > POLITICIAN_EC_BOMB) 
        {
            currentInfluence = enemyEnlightenmentCenterCurrentInfluence;
        }       

        return currentInfluence;
    }

    protected static int getAmountToMakePoliticianBombForNeutral() {
        int currentInfluence = robotController.getInfluence();

        if (neutralEnlightenmentCenterCurrentInfluence != 0 && neutralEnlightenmentCenterCurrentInfluence > POLITICIAN_EC_BOMB) 
        {
            currentInfluence = neutralEnlightenmentCenterCurrentInfluence;
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

    protected static boolean enoughDefenderPoliticianNearby()
    {
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;
        RobotInfo[] friendlyRobots = robotController.senseNearbyRobots(sensorRadiusSquared, friendly);
        boolean enoughDefenderPoliticianNearby = false;

        for (RobotInfo friendlyRobotInfo : friendlyRobots) 
        {
            if (friendlyRobotInfo.getType() == RobotType.POLITICIAN
                && friendlyRobotInfo.getInfluence() <= POLITICIAN_DEFEND_SLANDERER 
                && friendlyRobotInfo.getInfluence() > POLITICIAN_SCOUT) 
            {
                defenderPoliticianNearby = true;
                countOfDefenderPoliticianNearby++;
            } else if (friendlyRobotInfo.getType() == RobotType.SLANDERER) 
            {
                friendlySlandererNearby = true;
                countOfFriendlySlandererNearby++;
            }
        }
        
        if ((countOfDefenderPoliticianNearby) >= countOfFriendlySlandererNearby || (countOfDefenderPolitician > (countOfSlanderer * 1.5)))
        {
            enoughDefenderPoliticianNearby = true;
        }

        return enoughDefenderPoliticianNearby;
    }

    protected static Direction getAvailableDirectionToSpawn() throws GameActionException 
    {
        Direction directionToSpawn = Direction.CENTER;

        for (MapLocation location : squaresAroundEnlightenmentCenter) 
        {
            if (robotController.onTheMap(location) && !robotController.isLocationOccupied(location)) 
            {
                directionToSpawn = robotController.getLocation().directionTo(location);
                break;
            }
        }

        return directionToSpawn;
    }

    protected static Direction getDirectionToScout() throws GameActionException 
    {
        Direction directionToSpawn = Direction.CENTER;

        for (; scoutIterator < directions.length; scoutIterator++) 
        {
            MapLocation possibleLocation = robotController.getLocation().add(directions[scoutIterator]);
            if (robotController.onTheMap(possibleLocation) && !robotController.isLocationOccupied(possibleLocation)) 
            {
                directionToSpawn = robotController.getLocation().directionTo(possibleLocation);
                break;
            }
        }

        if (scoutIterator == directions.length) 
        {
            scoutIterator = 0;    
        }

        return directionToSpawn;
    }


    private static void buildRobot() throws GameActionException 
    {
        if (robotController.canBuildRobot(RobotBuilder.robotTypeToBuild, RobotBuilder.directionToSpawn, RobotBuilder.influenceToUse)) 
        {
            robotController.buildRobot(RobotBuilder.robotTypeToBuild, RobotBuilder.directionToSpawn, RobotBuilder.influenceToUse);
            influenceUsedFromLastBuild = RobotBuilder.influenceToUse;

            RobotInfo newRobot = robotController.senseRobotAtLocation(robotController.adjacentLocation(RobotBuilder.directionToSpawn));

            if (!robotIds.contains(newRobot.getID())) 
            {
                robotIds.add(newRobot.getID());
            }            
        }
    }

    public static void setup() throws GameActionException {
        if (robotController.getRoundNum() == 1) 
        {
            numberOfEnlightenmentCenters = robotController.getRobotCount();
        }

        numberOfMuckrakersToCreateInBeginning = NUMBER_OF_MUCKRAKERS_IN_BEGINNING;        

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