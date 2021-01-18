package testPlayerv01;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import battlecode.common.*;
import testPlayerv01.Service.Communication;

@SuppressWarnings("unused")
public class EnlightenmentCenterTest01 extends RobotPlayer {

    private static class RobotBuilder {
        static RobotType robotTypeToBuild;
        static Direction directionToSpawn;
        static int influenceToUse;
    }

    private static List<Integer> robotIds = new ArrayList<>();

    private static int numberOfEnlightenmentCenters = 0;

    private static double empowerFactorInThirtyTurns = 1;
    private static final int AMOUNT_OF_VOTES_NEEDED_TO_WIN = 750;

    // Count of My robots
    private static int countOfPoliticianBomb = 0;
    private static int countOfSlanderer = 0;
    private static int countOfPoliticians = 0;
    private static int countOfMuckrakers = 0;

    // Slanderer
    private static int[] slandererInfluenceAmount = { 21, 41, 62, 85, 107, 130, 153, 
        178, 203, 228, 282, 310, 339, 369, 399, 431, 463, 497, 
        568, 606, 644, 683, 724, 767, 810, 855, 902, 949};

    // Friendly
    private static boolean friendlySlandererNearby;

    // Enemy robots in the area
    private static int countOfEnemyPoliticiansOverTwentyInfluence = 0;
    private static int countOfEnemyPoliticiansUnderTwentyInfluence = 0;
    private static int countOfEnemyMuckraker = 0;
    private static int countOfOtherRobots = 0;
    private static List<MapLocation> enemyTargetNearby = new ArrayList<>();
    private static boolean weSpawnedRightNextToEnemy = false;

    // Beginning
    private static final int NUMBER_OF_MUCKRAKERS_IN_BEGINNING = 30;
    private static int numberOfMuckrakersToCreateInBeginning = 0;

    // Building
    private static boolean buildThisTurn = false;
    private static boolean builtLastTurn = false;
    private static boolean builtScoutLastTurn = false;
    private static int turnsNotBuilding;
    private static boolean stockUp = false;
    private static boolean buildSlanderer = false;
    private static int amountNeededForSlanderer;
    private static double differenceOfInfluenceBetweenRounds;

    // Bidding
    private static int currentVoteCount = 0;
    private static int amountToBid = 1;

    // Influence
    private static int lastTurnInfluence = 150;
    private static double rateOfInfluence;
    private static double generatedRoundInfluence;
    private static int income;

    // This keeps looping
    @SuppressWarnings("unused")
    public static void run() throws GameActionException {
        /*
         * 1. Make slanderers 2. Create Scout Muckrakers 3. Create more slanderers if
         * safe 4. Create bomb polis 5. create more polis 6. create defent muckrakers 7.
         * create scouts
         */
        countEnemiesNearby();
        buildThisTurn = false;
        
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

        if (robotController.getRoundNum() < 2 && !weSpawnedRightNextToEnemy) {
            RobotType firstBuild = RobotType.SLANDERER;
            int firstInfluence = 130;

            turnsNotBuilding = 0;
            buildThisTurn = true;
            RobotBuilder.robotTypeToBuild = firstBuild;
            RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
            RobotBuilder.influenceToUse = firstInfluence;
            // countOfSlanderer++;
        }       
        else if (robotCurrentInfluence < AMOUNT_OF_INFLUENCE_TO_NOT_EMPOWER_SELF && empowerFactor > 5 && isItSafe())
        {
            RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
            RobotBuilder.influenceToUse = (int) (robotController.getInfluence() * .90);
            RobotBuilder.robotTypeToBuild = RobotType.POLITICIAN;

            turnsNotBuilding = 0;
            buildThisTurn = true;
        }  
        else if (robotController.getRoundNum() >= END_GAME_ROUND_STRAT 
            && robotController.getRobotCount() > 50
            && !stockUp) 
        {
            RobotType toBuild = RobotType.POLITICIAN;

            int influence = 0;
            if (lastTurnInfluence > MAX_NORMAL_POLITICIAN) 
            {
                int max = lastTurnInfluence;
                int min = MAX_NORMAL_POLITICIAN;
                influence = randomInteger.nextInt(max - min + 1) + min;

                turnsNotBuilding = 0;
                if (robotCurrentInfluence >= influence * 1.1) {
                    buildThisTurn = true;
                    RobotBuilder.influenceToUse = influence;
                    RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
                    RobotBuilder.robotTypeToBuild = toBuild;
                    countOfPoliticians++;
                }
            } 
            else 
            {
                turnsNotBuilding++;
            }
        } else if (countOfMuckrakers <= numberOfMuckrakersToCreateInBeginning
                && robotController.getRoundNum() <= MIDDLE_GAME_ROUND_START 
                && !weSpawnedRightNextToEnemy) 
        {
            RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
            RobotBuilder.influenceToUse = INFLUENCE_FOR_SCOUT;
            RobotBuilder.robotTypeToBuild = RobotType.MUCKRAKER;

            turnsNotBuilding = 0;
            buildThisTurn = true;
            builtScoutLastTurn = true;
            countOfMuckrakers++; // TODO: need actual count...
        } 
        else if (buildSlanderer && isItSafeForSlanderer()) 
        {
            if (robotCurrentInfluence < amountNeededForSlanderer) 
            {
                stockUp = true;
            } else 
            {
                turnsNotBuilding = 0;
                buildThisTurn = true;
                buildSlanderer = false;
                RobotBuilder.robotTypeToBuild = RobotType.SLANDERER;
                RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
                RobotBuilder.influenceToUse = amountNeededForSlanderer;
                countOfSlanderer++;
                stockUp = false;
            }
        } 
        else if (enemyEnlightenmentCenterFound) 
        {
            if (isThereEnoughForBomb() && isItSafe()) 
            {
                RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
                RobotBuilder.influenceToUse = getAmountToMakePoliticianBomb();
                RobotBuilder.robotTypeToBuild = RobotType.POLITICIAN;

                turnsNotBuilding = 0;
                stockUp = false;
                buildThisTurn = true;
                countOfPoliticianBomb++;
            } 
            else if (!isThereEnoughForBomb() && isItSafe()) 
            {
                stockUp = true;
                turnsNotBuilding++;
            } 
            else if (!isItSafe()) 
            {
                turnsNotBuilding++;
                MapLocation enemyRobotOverTwentyInfluenceNearby = getClosestEnemyRobotOverTwentyInfluenceLocation();
                // Communication.sendLocation(enemyRobotOverTwentyInfluenceNearby, KILL_ENEMY_TARGET); //TODO: Implement this?
            }
        } 
        else if (!stockUp && countOfPoliticians < countOfMuckrakers) 
        {
            // These are for the follower and leader groups stuff
            RobotType toBuild = RobotType.POLITICIAN;
            int influence = 0;
            if (lastTurnInfluence >= MIN_NORMAL_POLITICIAN) {
                int min = MIN_NORMAL_POLITICIAN;
                int max = MAX_NORMAL_POLITICIAN;
                influence = randomInteger.nextInt(max - min + 1) + min;

                turnsNotBuilding = 0;
                if (robotCurrentInfluence > influence * 1.1 && influence != 0) {
                    buildThisTurn = true;
                    RobotBuilder.influenceToUse = influence;
                    RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
                    RobotBuilder.robotTypeToBuild = toBuild;
                    countOfPoliticians++;
                }
            }
        }
        // else if (friendlySlandererNearby && !defenderMuckrakerNearby())
        // {
        // RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
        // RobotBuilder.influenceToUse = INFLUENCE_FOR_DEFEND_SLANDERER_MUCKRAKER;
        // RobotBuilder.robotTypeToBuild = RobotType.MUCKRAKER;

        // turnsNotBuilding = 0;
        // buildThisTurn = true;
        // countOfMuckrakers++;
        // }
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
                && robotController.getTeamVotes() < AMOUNT_OF_VOTES_NEEDED_TO_WIN) {
            int influence = amountToBid;
            if (robotController.canBid(influence)) {
                robotController.bid(influence);
            }

            if (currentVoteCount >= robotController.getTeamVotes()) {
                amountToBid += 2;
            } else {
                currentVoteCount = robotController.getTeamVotes();
            }
        }

        decideIfShouldBuildSlanderer();

        // send location of the enemey EC every 2 turns
        // TODO: add in clause about needing to protect or kill enemy
        if (enemyEnlightenmentCenterMapLocation.size() > 0 && robotController.getRoundNum() % 2 == 0) 
        {
            Communication.sendLocation(enemyEnlightenmentCenterMapLocation.get(0), ENEMY_ENLIGHTENMENT_CENTER_FOUND);
            enemyEnlightenmentCenterFound = true;
        }
        else if (!neutralEnlightenmentCenterMapLocation.isEmpty())
        {
            Communication.sendLocation(neutralEnlightenmentCenterMapLocation.get(0), NUETRAL_ENLIGHTENMENT_CENTER_FOUND);
        }
        else if (enemyEnlightenmentCenterMapLocation.isEmpty())
        {
            robotController.setFlag(0);
        }

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
        && (robotController.getRobotCount()/numberOfEnlightenmentCenters) > 15
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

    private static void checkFlagsFromRobots() throws GameActionException {
        for (int iterator = robotIds.size() - 1; iterator >= 0; --iterator) {
            int robotId = robotIds.get(iterator);
            if (robotController.canGetFlag(robotId)) {
                int flag = robotController.getFlag(robotId);

                if (flag != 0) {
                    checkIfRobotSignallingTheyFoundEnemyEnlightenmentCenter(flag);
                    checkIfRobotSignallingEnemyECInfluence(flag);
                    checkIfRobotSignallingEnlightenmentCenterConverted(flag);
                }
            } else {
                robotIds.remove(iterator); // TODO: this could be where you count the # of robots alive.
            }
        }
    }

    private static void checkIfRobotSignallingTheyFoundEnemyEnlightenmentCenter(int flag) throws GameActionException {
        if (Communication.checkIfEnemeyEnlightenmentCenterHasBeenFound(flag)) {
            setEnemyEnlightenmentCenter(flag);
        }
    }

    private static void checkIfRobotSignallingEnlightenmentCenterConverted(int flag) throws GameActionException {
        checkRobotForAllClearSignal(flag);
    }

    private static void checkRobotForAllClearSignal(int flag) throws GameActionException {
        if (Communication.checkIfEnemeyEnlightenmentCenterHasBeenConverted(flag)) {
            processEnemyEnlightenmentCenterHasBeenConverted();
        }
    }

    private static void checkIfRobotSignallingEnemyECInfluence(int flag) throws GameActionException {
        if (Communication.checkRobotFlagForEnemyECInfluence(flag)) {
            enemyEnlightenmentCenterCurrentInfluence = Communication.getEnemyEnlightenmentCenterInfluenceFromFlag(flag);
        }
    }

    private static void processEnemyEnlightenmentCenterHasBeenConverted() throws GameActionException {
        if (robotController.canSetFlag(0)) 
        {
            robotController.setFlag(0);
        }
        convertedEnemyEnlightenmentCenterMapLocation.add(enemyEnlightenmentCenterMapLocation.get(0));
        enemyEnlightenmentCenterMapLocation.remove(0);
        enemyEnlightenmentCenterFound = false;
    }

    private static void setEnemyEnlightenmentCenter(int flag) throws GameActionException {
        MapLocation enemyCenterLocation = Communication.getLocationFromFlag(flag);

        if ((!convertedEnemyEnlightenmentCenterMapLocation.contains(enemyCenterLocation) 
        || convertedEnemyEnlightenmentCenterMapLocation.size() == 0) 
        && (enemyEnlightenmentCenterMapLocation.isEmpty() || !convertedEnemyEnlightenmentCenterMapLocation.contains(enemyCenterLocation))) 
        {
            enemyEnlightenmentCenterMapLocation.add(enemyCenterLocation);
        }
    }

    private static boolean isItSafeForSlanderer() {
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

    private static boolean isThereEnoughForBomb() throws GameActionException {
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

    private static boolean isItSafe() throws GameActionException {
        return countOfEnemyPoliticiansOverTwentyInfluence == 0;
    }

    private static int getAmountToMakePoliticianBomb() {
        int currentInfluence = robotController.getInfluence();

        if (enemyEnlightenmentCenterCurrentInfluence != 0) 
        {
            currentInfluence = enemyEnlightenmentCenterCurrentInfluence;
        }       

        return currentInfluence;
    }

    private static MapLocation getClosestEnemyRobotOverTwentyInfluenceLocation() {
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

    private static Direction getAvailableDirectionToSpawn() throws GameActionException {
        Direction directionToSpawn = Direction.CENTER;

        for (MapLocation possibleLocation : squaresAroundEnlightenmentCenter) 
        {
            if (!robotController.isLocationOccupied(possibleLocation) && robotController.onTheMap(possibleLocation)) 
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
            robotController.buildRobot(RobotBuilder.robotTypeToBuild, RobotBuilder.directionToSpawn,
                    RobotBuilder.influenceToUse);

            MapLocation currentLocation = robotController.getLocation();
            RobotInfo newRobot = robotController.senseRobotAtLocation(robotController.adjacentLocation(RobotBuilder.directionToSpawn));

            if (!robotIds.contains(newRobot.getID())) {
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