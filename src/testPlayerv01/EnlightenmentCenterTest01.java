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
    protected static List<RobotInfo> builtRobotsInfos = new ArrayList<>(); // TODO: Make this an arraylist of robotInfo....
    protected static int whereIteratorStopped = 0;
    protected static String flagExtraInfoToSkip = "18;";

    protected static int numberOfEnlightenmentCenters = 0;

    protected static double empowerFactorInThirtyTurns = 1;
    protected static final int AMOUNT_OF_VOTES_NEEDED_TO_WIN = 751;

    protected static int convertedMessageSent = 0;

    // Count of My robots
    protected static int countOfPoliticianBomb = 0;
    protected static int countOfSlanderer = 0;
    protected static int countOfPoliticians = 0;
    protected static int countOfMuckrakers = 0;
    protected static int countOfBuffMucks = 0;
    protected static int countOfDefenderPolitician;
    protected static int countOfFriendlySlandererNearby;
    protected static int countOfDefenderPoliticianNearby;

    // Slanderer
    protected static final int[] slandererInfluenceAmount = {85, 107, 130, 153, 
        178, 203, 228, 282, 310, 339, 369, 399, 431, 463, 497, 
        568, 606, 644, 683, 724, 767, 810, 855, 902, 949};
    
    protected static final int[] newEnlightenmentCenterSlandererInfluenceAmount = 
    {21, 41, 63, 85, 107, 130, 153, 178, 203, 228, 282, 310, 339, 369, 399, 431, 463, 497, 568, 606, 644, 683, 724, 767, 810, 855, 902, 949};

    // Friendly
    protected static boolean friendlySlandererNearby;
    protected static boolean defenderPoliticianNearby;    

    // Neutral
    /// MapLocation & EnligthenmentCenterInfo
    protected static Map<MapLocation, EnlightenmentCenterInfo> neutralEnlightenmentCenters = new HashMap<MapLocation, EnlightenmentCenterInfo>();
    protected static int alreadyProcessedNeutralCenters = 0;
    protected static final int LOWEST_INFLUENCE_VALUE_FOR_NEUTRAL_LOCATION = 50;
    protected static final int MIDDLE_INFLUENCE_VALUE_FOR_NEUTRAL_LOCATION = 250;
    protected static final int HIGHEST_INFLUENCE_VALUE_FOR_NEUTRAL_LOCATION = 500;

    // Enemy robots in the area
    protected static int countOfEnemyPoliticiansOverFiftyInfluence = 0;
    protected static int countOfEnemyPoliticiansUnderTwentyInfluence = 0;
    protected static int countOfEnemyMuckraker = 0;
    protected static int countOfOtherRobots = 0;
    protected static List<MapLocation> enemyTargetNearby = new ArrayList<>();
    protected static boolean weSpawnedRightNextToEnemy = false;

    // Beginning
    protected static final int NUMBER_OF_MUCKRAKERS_IN_BEGINNING = 7;
    protected static int numberOfMuckrakersToCreateInBeginning = 0;
    protected static int scoutIterator = 0;

    // Middle
    protected static final int BUFF_MUCKRAKER_MIN_INFLUENCE = 150;

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
        resetVariablesForSensing();
        countEnemiesNearby();        
        checkFlagsFromRobots();

        if (!neutralEnlightenmentCenterMapLocation.isEmpty())
        {
            setneutralCurrentEnlightenmentCenterGoingFor();
            neutralEnlightenmentCenterFound = true;
        }

        if (!enemyEnlightenmentCenterFound && !enemyEnlightenmentCenterMapLocation.isEmpty()) 
        {
            enemyEnlightenmentCenterFound = true;    
        }
        
        setRateOfInfluenceReceived();
        decideIfShouldBuildSlanderer();
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
                && robotController.getTeamVotes() < AMOUNT_OF_VOTES_NEEDED_TO_WIN 
                && isItSafe()) 
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

            if (currentVoteCount >= robotController.getTeamVotes()) 
            {
                if (robotCurrentInfluence > 100000) 
                {
                    amountToBid *= 2;
                }
                amountToBid += 2;
            } 
            else 
            {
                currentVoteCount = robotController.getTeamVotes();
            }
        }
        else if (robotController.getRoundNum() > 50
        && robotController.getTeamVotes() < AMOUNT_OF_VOTES_NEEDED_TO_WIN
        && isItSafe())
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

    private static void setneutralCurrentEnlightenmentCenterGoingFor() 
    {
        if (neutralEnlightenmentCenters.size() == 1) 
        {
            EnlightenmentCenterInfo enlightenmentCenterInfo = neutralEnlightenmentCenters.get(neutralEnlightenmentCenterMapLocation.get(0));
            neutralCurrentEnlightenmentCenterGoingFor = enlightenmentCenterInfo.mapLocation;
            neutralEnlightenmentCenterCurrentInfluence = enlightenmentCenterInfo.currentInfluence;
        }
        else if (!neutralEnlightenmentCenters.isEmpty())
        {
            int lowestInfluenceForNeutral = 501;
            int closestDistanceSquaredToNeutral = 100000;

            for (Map.Entry<MapLocation, EnlightenmentCenterInfo> enlightenmentCenter : neutralEnlightenmentCenters.entrySet()) 
            {
                EnlightenmentCenterInfo enlightenmentCenterInfo = enlightenmentCenter.getValue();

                // Lowest influence and the closest. We want that
                if (enlightenmentCenterInfo.currentInfluence == LOWEST_INFLUENCE_VALUE_FOR_NEUTRAL_LOCATION 
                    && enlightenmentCenterInfo.distanceSquaredToEnlightenmentCenter 
                        <= closestDistanceSquaredToNeutral)
                {
                    lowestInfluenceForNeutral = enlightenmentCenterInfo.currentInfluence;
                    closestDistanceSquaredToNeutral = enlightenmentCenterInfo.distanceSquaredToEnlightenmentCenter;

                    neutralCurrentEnlightenmentCenterGoingFor = enlightenmentCenterInfo.mapLocation;
                    neutralEnlightenmentCenterCurrentInfluence = enlightenmentCenterInfo.currentInfluence;
                }
                // Less than lowest current influence and closest
                /// Will automatically set the first in the Map
                else if(enlightenmentCenterInfo.currentInfluence <= lowestInfluenceForNeutral 
                    && enlightenmentCenterInfo.currentInfluence >= LOWEST_INFLUENCE_VALUE_FOR_NEUTRAL_LOCATION 
                    && enlightenmentCenterInfo.distanceSquaredToEnlightenmentCenter 
                        <= closestDistanceSquaredToNeutral)
                {
                    lowestInfluenceForNeutral = enlightenmentCenterInfo.currentInfluence;
                    closestDistanceSquaredToNeutral = enlightenmentCenterInfo.distanceSquaredToEnlightenmentCenter;

                    neutralCurrentEnlightenmentCenterGoingFor = enlightenmentCenterInfo.mapLocation;
                    neutralEnlightenmentCenterCurrentInfluence = enlightenmentCenterInfo.currentInfluence;
                }
                // Less than most influence and closest
                else if (enlightenmentCenterInfo.currentInfluence >= lowestInfluenceForNeutral 
                    && enlightenmentCenterInfo.currentInfluence < HIGHEST_INFLUENCE_VALUE_FOR_NEUTRAL_LOCATION
                    && enlightenmentCenterInfo.distanceSquaredToEnlightenmentCenter 
                        <= closestDistanceSquaredToNeutral) 
                {
                    closestDistanceSquaredToNeutral = enlightenmentCenterInfo.distanceSquaredToEnlightenmentCenter;

                    neutralCurrentEnlightenmentCenterGoingFor = enlightenmentCenterInfo.mapLocation;
                    neutralEnlightenmentCenterCurrentInfluence = enlightenmentCenterInfo.currentInfluence;
                }
                // Lower influence but farther away
                // else if (enlightenmentCenterInfo.currentInfluence <= lowestInfluenceForNeutral 
                //     && enlightenmentCenterInfo.currentInfluence >= LOWEST_INFLUENCE_VALUE_FOR_NEUTRAL_LOCATION
                //     && enlightenmentCenterInfo.distanceSquaredToEnlightenmentCenter 
                //         >= closestDistanceSquaredToNeutral) 
                // {
                //     lowestInfluenceForNeutral = enlightenmentCenterInfo.currentInfluence;

                //     neutralCurrentEnlightenmentCenterGoingFor = enlightenmentCenterInfo.mapLocation;
                //     neutralEnlightenmentCenterCurrentInfluence = enlightenmentCenterInfo.currentInfluence;
                // }     
                else if (neutralCurrentEnlightenmentCenterGoingFor ==  null)
                {
                    neutralCurrentEnlightenmentCenterGoingFor = enlightenmentCenterInfo.mapLocation;
                    neutralEnlightenmentCenterCurrentInfluence = enlightenmentCenterInfo.currentInfluence >= LOWEST_INFLUENCE_VALUE_FOR_NEUTRAL_LOCATION ? enlightenmentCenterInfo.currentInfluence : LOWEST_INFLUENCE_VALUE_FOR_NEUTRAL_LOCATION;
                }           
            }
        }
        
    }

    protected static void decideIfShouldBuildSlanderer()
    {
        if (!buildSlanderer && shouldBuildSlanderer())
        {
            buildSlanderer = true;
        }
    }

    protected static boolean shouldBuildSlanderer() 
    {
        boolean shouldBuildSlanderer = false;

        if ((income < 175 || robotCurrentInfluence < 3000) 
        && robotController.getRoundNum() >= 300)
        {
            shouldBuildSlanderer = true;
        }
        else if
        ((income < 25 || robotCurrentInfluence < 500) 
        && (robotController.getRoundNum() < 75 || turnCount < 50))
        {
            shouldBuildSlanderer = true;
        }
        else if
        ((income < 100 || robotCurrentInfluence < 1750) 
        && robotController.getRoundNum() < BEGINNING_ROUND_STRAT && turnCount > 50)
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
        if (turnCount > 25 || robotController.getRoundNum() < 50)
        {
            for (int iterator = (slandererInfluenceAmount.length - 1); iterator > 0; --iterator) 
            {
                if (slandererInfluenceAmount[iterator] > robotCurrentInfluence 
                && slandererInfluenceAmount[iterator - 1] < robotCurrentInfluence) 
                {
                    amountNeededForSlanderer = slandererInfluenceAmount[iterator - 1];
                    break;
                } 
                else if (robotCurrentInfluence >= slandererInfluenceAmount[(slandererInfluenceAmount.length - 1)])
                {
                    amountNeededForSlanderer = slandererInfluenceAmount[(slandererInfluenceAmount.length - 1)];
                    break;
                }
            }
        }
        else if(turnCount < 25 && robotController.getRoundNum() > 50)
        {
            for (int iterator = (newEnlightenmentCenterSlandererInfluenceAmount.length - 1); iterator > 0; --iterator) 
            {
                if (newEnlightenmentCenterSlandererInfluenceAmount[iterator] > robotCurrentInfluence 
                && newEnlightenmentCenterSlandererInfluenceAmount[iterator - 1] < robotCurrentInfluence) 
                {
                    amountNeededForSlanderer = newEnlightenmentCenterSlandererInfluenceAmount[iterator - 1];
                    break;
                } 
                else if (robotCurrentInfluence >= newEnlightenmentCenterSlandererInfluenceAmount[(newEnlightenmentCenterSlandererInfluenceAmount.length - 1)])
                {
                    amountNeededForSlanderer = newEnlightenmentCenterSlandererInfluenceAmount[(newEnlightenmentCenterSlandererInfluenceAmount.length - 1)];
                    break;
                }
            }
        }
        
    }

    protected static void decideIfShouldBuildMoreScouts()
    {
        if (!buildScout 
        && (countOfMuckrakers < countOfPoliticians 
            || (!enemyEnlightenmentCenterFound && robotController.getRoundNum() > 150
            && income > 25)))
        {
            buildScout = true;
        }
    }

    private static void checkFlagsFromRobots() throws GameActionException 
    {
        for (; (whereIteratorStopped < builtRobotsInfos.size() && Clock.getBytecodesLeft() >= 3500); whereIteratorStopped++) 
        {
            RobotInfo robotInfo = builtRobotsInfos.get(whereIteratorStopped);
            int robotId = robotInfo.ID;

            if (robotController.canGetFlag(robotId)) 
            {
                int flag = robotController.getFlag(robotId);

                if (flag != 0 && flag != Communication.SLANDERER_FLAG && flag != Communication.SLANDERER_IN_TROUBLE_FLAG) 
                {
                    int extraInformation = Communication.getExtraInformationFromFlag(flag); 
                    if (!flagExtraInfoToSkip.contains(Integer.toString(extraInformation))) 
                    {
                        EnlightenmentCenterHelper.checkFlagsForSignals(extraInformation, flag, robotId);
                    }                   
                }           
            } 
            else 
            {
                updateCountOfRobots(robotInfo);

                builtRobotsInfos.remove(whereIteratorStopped); // TODO: this could be where you count the # of robots alive.
            }
        }

        // reset iterator
        if (whereIteratorStopped == builtRobotsInfos.size()) 
        {
            whereIteratorStopped = 0;    
        }
    }

    private static void updateCountOfRobots(RobotInfo robotInfo) 
    {
        if (robotInfo.type == RobotType.POLITICIAN 
            && robotInfo.influence > POLITICIAN_SCOUT && robotInfo.influence <= POLITICIAN_DEFEND_SLANDERER) 
        {
            countOfDefenderPolitician--;    
        }
        else if (robotInfo.type == RobotType.MUCKRAKER) 
        {
            if (robotInfo.getInfluence() >= BUFF_MUCKRAKER_MIN_INFLUENCE) 
            {
                countOfBuffMucks--;    
            }
            else 
            {
                countOfMuckrakers--;
            }            
        }
        else if (robotInfo.type == RobotType.SLANDERER)
        {
            countOfSlanderer--;
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
            if (neutralCurrentEnlightenmentCenterGoingFor != null) 
            {
                Communication.sendLocation(neutralCurrentEnlightenmentCenterGoingFor, Communication.NUETRAL_ENLIGHTENMENT_CENTER_FOUND);            
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

    protected static void resetVariablesForSensing()
    {
        countOfEnemyMuckraker = 0;
        countOfEnemyPoliticiansOverFiftyInfluence = 0;
        countOfEnemyPoliticiansUnderTwentyInfluence = 0;
        countOfOtherRobots = 0;

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
    }

    private static void countEnemiesNearby() 
    {
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;
        RobotInfo[] enemyRobots = robotController.senseNearbyRobots(sensorRadiusSquared, enemy);        

        for (RobotInfo enemyRobotInfo : enemyRobots) 
        {
            if (enemyRobotInfo.getType() == RobotType.POLITICIAN && enemyRobotInfo.getConviction() > 60) 
            {
                enemyTargetNearby.add(enemyRobotInfo.getLocation());
                countOfEnemyPoliticiansOverFiftyInfluence++;
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

    protected static boolean isThereEnoughForBomb() throws GameActionException {
        boolean isThereEnoughInfluence = false;
        int currentInfluence = robotController.getInfluence();
        int enemyEnlightenmentCenterInfluenceWithPoliticianTax = enemyEnlightenmentCenterCurrentInfluence + POLITICIAN_TAX + 1;

        if (enemyEnlightenmentCenterInfluenceWithPoliticianTax < POLITICIAN_EC_BOMB) 
        {
            enemyEnlightenmentCenterInfluenceWithPoliticianTax += POLITICIAN_EC_BOMB;
        }

        if (enemyEnlightenmentCenterCurrentInfluence != 0
        && currentInfluence > enemyEnlightenmentCenterInfluenceWithPoliticianTax)
        {
            isThereEnoughInfluence = true;
        }

        return isThereEnoughInfluence;
    }

    protected static boolean isThereEnoughForNeutralEnlightenmentCenterCapture() throws GameActionException 
    {
        boolean isThereEnoughInfluence = false;
        int currentInfluence = robotController.getInfluence();

        if ((neutralEnlightenmentCenterCurrentInfluence != 0) 
        && (((currentInfluence * empowerFactorInThirtyTurns) - POLITICIAN_TAX) >= neutralEnlightenmentCenterCurrentInfluence)
        && (currentInfluence - POLITICIAN_TAX) >= POLITICIAN_EC_BOMB)
        {
            isThereEnoughInfluence = true;
        }

        return isThereEnoughInfluence;
    }

    protected static boolean isItSafe() throws GameActionException 
    {
        return countOfEnemyPoliticiansOverFiftyInfluence == 0 || robotCurrentInfluence > 200;
    }

    protected static int getAmountToMakePoliticianBomb() 
    {
        int currentInfluence = robotController.getInfluence();
        int influenceToBuildWith = POLITICIAN_EC_BOMB;
        int enemyEnlightenmentCenterInfluenceWithPoliticianTax = enemyEnlightenmentCenterCurrentInfluence + POLITICIAN_TAX + 1;

        if (enemyEnlightenmentCenterInfluenceWithPoliticianTax < POLITICIAN_EC_BOMB) 
        {
            enemyEnlightenmentCenterInfluenceWithPoliticianTax += POLITICIAN_EC_BOMB;
        }

        if (currentInfluence > enemyEnlightenmentCenterInfluenceWithPoliticianTax * 10) 
        {
            influenceToBuildWith = enemyEnlightenmentCenterInfluenceWithPoliticianTax * 10;
        }
        else if (currentInfluence > enemyEnlightenmentCenterInfluenceWithPoliticianTax * 5) 
        {
            influenceToBuildWith = enemyEnlightenmentCenterInfluenceWithPoliticianTax * 5;
        }
        else if (currentInfluence > enemyEnlightenmentCenterInfluenceWithPoliticianTax * 2) 
        {
            influenceToBuildWith = enemyEnlightenmentCenterInfluenceWithPoliticianTax * 2;
        }
        else if (enemyEnlightenmentCenterInfluenceWithPoliticianTax > POLITICIAN_EC_BOMB) 
        {
            influenceToBuildWith = enemyEnlightenmentCenterInfluenceWithPoliticianTax;
        }       

        return influenceToBuildWith;
    }

    protected static int getAmountToMakePoliticianBombForNeutral() 
    {
        int currentInfluence = robotController.getInfluence();
        int influenceToBuildWith = POLITICIAN_EC_BOMB;

        if (currentInfluence > neutralEnlightenmentCenterCurrentInfluence * 2) 
        {
            influenceToBuildWith = neutralEnlightenmentCenterCurrentInfluence * 2;
        }
        else if (neutralEnlightenmentCenterCurrentInfluence != 0 && neutralEnlightenmentCenterCurrentInfluence > POLITICIAN_EC_BOMB) 
        {
            influenceToBuildWith = ((neutralEnlightenmentCenterCurrentInfluence + 1) + POLITICIAN_TAX);
        }       

        return influenceToBuildWith;
    }

    // protected static MapLocation getClosestEnemyRobotOverTwentyInfluenceLocation() {
    //     MapLocation targetLocation = enemyTargetNearby.get(0);
    //     int closestRobot = robotController.getType().sensorRadiusSquared;
    //     int distanceSquaredTo;

    //     for (int iterator = 0; iterator < enemyTargetNearby.size(); ++iterator) {
    //         distanceSquaredTo = robotController.getLocation().distanceSquaredTo(enemyTargetNearby.get(iterator));

    //         if (distanceSquaredTo <= closestRobot) 
    //         {
    //             closestRobot = distanceSquaredTo;
    //             targetLocation = enemyTargetNearby.get(iterator);
    //         }
    //     }

    //     return targetLocation;
    // }

    protected static boolean enoughDefenderPoliticianNearby()
    {
        boolean enoughDefenderPoliticianNearby = false;
        
        if (countOfDefenderPolitician >= countOfSlanderer)
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
        if (RobotBuilder.directionToSpawn != Direction.CENTER) 
        {
            if (robotController.canBuildRobot(RobotBuilder.robotTypeToBuild, RobotBuilder.directionToSpawn, RobotBuilder.influenceToUse)) 
            {
                robotController.buildRobot(RobotBuilder.robotTypeToBuild, RobotBuilder.directionToSpawn, RobotBuilder.influenceToUse);
                influenceUsedFromLastBuild = RobotBuilder.influenceToUse;

                RobotInfo newRobot = robotController.senseRobotAtLocation(robotController.adjacentLocation(RobotBuilder.directionToSpawn));

                builtRobotsInfos.add(newRobot);
            }
            else
            {
                println("Welp... This awk " + RobotBuilder.directionToSpawn + " " + RobotBuilder.influenceToUse);
            }
        }
        else
        {
            for (Direction direction : directions) 
            {
                if (robotController.canBuildRobot(RobotBuilder.robotTypeToBuild, direction, RobotBuilder.influenceToUse)) 
                {
                    robotController.buildRobot(RobotBuilder.robotTypeToBuild, direction, RobotBuilder.influenceToUse);
                    influenceUsedFromLastBuild = RobotBuilder.influenceToUse;

                    RobotInfo newRobot = robotController.senseRobotAtLocation(robotController.adjacentLocation(direction));

                    builtRobotsInfos.add(newRobot);           
                }
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
        checkIfSpawnIsNearEnlightenmentCenter();
    }

    private static void checkIfSpawnIsNearEnlightenmentCenter()
    {
        RobotInfo[] nearbyRobotInfos = robotController.senseNearbyRobots(robotController.getType().sensorRadiusSquared);

        for (RobotInfo robotInfo : nearbyRobotInfos) 
        {
            if (robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER) 
            {
                if(robotInfo.getTeam() == enemy)
                {
                    weSpawnedRightNextToEnemy = true;
                    enemyEnlightenmentCenterFound = true;
                    enemyEnlightenmentCenterMapLocation.add(robotInfo.getLocation());
                }
                else if (robotInfo.getTeam() == Team.NEUTRAL)
                {
                    neutralEnlightenmentCenterFound = true;
                    EnlightenmentCenterInfo enlightenmentCenterInfo = new EnlightenmentCenterInfo();
                    enlightenmentCenterInfo.currentInfluence = robotInfo.getInfluence();
                    enlightenmentCenterInfo.mapLocation = robotInfo.getLocation();
                    enlightenmentCenterInfo.distanceSquaredToEnlightenmentCenter = robotController.getLocation().distanceSquaredTo(robotInfo.getLocation());
                    neutralEnlightenmentCenters.put(robotInfo.getLocation(), enlightenmentCenterInfo);
                    neutralEnlightenmentCenterMapLocation.add(robotInfo.getLocation());
                    neutralCurrentEnlightenmentCenterGoingFor = robotInfo.getLocation();
                    neutralEnlightenmentCenterCurrentInfluence = robotInfo.getInfluence();
                }                
            }
        }
        
    }

}