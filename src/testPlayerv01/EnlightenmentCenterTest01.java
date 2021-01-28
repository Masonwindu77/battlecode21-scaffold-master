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
    protected static List<RobotInfo> builtRobotsInfos = new ArrayList<>();
    protected static int whereIteratorStopped = 0;
    protected static String flagExtraInfoToSkip = "18;";

    protected static int numberOfEnlightenmentCenters = 0;

    protected static double empowerFactorInThirtyTurns = 1;
    protected static final int AMOUNT_OF_VOTES_NEEDED_TO_WIN = 751;

    protected static int convertedMessageSent = 0;

    // Count of My robots
    protected static int countOfSlanderer = 0;
    protected static int countOfPoliticians = 0;
    protected static int countOfMuckrakers = 0;
    protected static int countOfScouts = 0;
    protected static int countOfBuffMucks = 0;
    protected static int countOfNeutralPoliticianBomb = 0;
    protected static int countOfEnemyPoliticianBomb = 0;
    protected static int countOfDefenderPolitician;
    protected static int countOfFriendlySlandererNearby;
    protected static int countOfDefenderPoliticianNearby;

    // Slanderer
    protected static final int[] slandererInfluenceAmount = {107, 130, 153, 
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
    protected static boolean neutralEnlightenmentCenterInfluenceHasBeenUpdated;
    protected static final int LOWEST_INFLUENCE_VALUE_FOR_NEUTRAL_LOCATION = 50;
    protected static final int MIDDLE_INFLUENCE_VALUE_FOR_NEUTRAL_LOCATION = 250;
    protected static final int HIGHEST_INFLUENCE_VALUE_FOR_NEUTRAL_LOCATION = 500;

    // Enemy EC
    protected static Map<MapLocation, EnlightenmentCenterInfo> enemyEnlightenmentCenters = new HashMap<MapLocation, EnlightenmentCenterInfo>();
    protected static int alreadyProcessedEnemyCenters = 0;

    // Enemy robots in the area
    protected static int countOfEnemyPoliticiansOverThirtyInfluence = 0;
    protected static int countOfEnemyPoliticiansUnderTwentyInfluence = 0;
    protected static int countOfEnemyMuckraker = 0;
    protected static int countOfOtherRobots = 0;
    protected static List<MapLocation> enemyTargetNearby = new ArrayList<>();
    protected static boolean weSpawnedRightNextToEnemy = false;

    // Beginning
    protected static final int NUMBER_OF_MUCKRAKERS_IN_BEGINNING = 7;
    protected static int numberOfScoutsToCreateInBeginning = 0;
    protected static int scoutIterator = 0;

    // Middle
    protected static final int BUFF_MUCKRAKER_MIN_INFLUENCE = 50;

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
    protected static boolean enemyEnlightenmentCenterIsCloser;
    private static int lastTurnIncome;

    // This keeps looping
    public static void run() throws GameActionException 
    {
        resetVariablesForSensing();
        countEnemiesNearby();        
        checkFlagsFromRobots();
        setEnemyAndNeutralEnlightenmentCenterOrResetThem();
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

        decideWhatToBid();        
        decideToSetFlags();
    }

    protected static void resetVariablesForSensing()
    {
        // counting robots
        countOfEnemyMuckraker = 0;
        countOfEnemyPoliticiansOverThirtyInfluence = 0;
        countOfEnemyPoliticiansUnderTwentyInfluence = 0;
        countOfOtherRobots = 0;

        buildThisTurn = false;
        // signals
        enemyEnlightenmentCenterInfluenceHasBeenUpdated = false;
        neutralEnlightenmentCenterInfluenceHasBeenUpdated = false;
        convertedEnemyEnlightenmentCenterHasBeenProcessedThisTurn = false;
        
        // influence 
        lastTurnInfluence = robotCurrentInfluence;
        robotCurrentInfluence = robotController.getInfluence();
        empowerFactorInThirtyTurns = robotController.getEmpowerFactor(friendly, 30);
        empowerFactor = robotController.getEmpowerFactor(friendly, 0);
        enemyEnlightenmentCenterCurrentInfluence = 0;

        enemyEnlightenmentCenterIsCloser = false;
    }

    private static void countEnemiesNearby() 
    {
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;
        RobotInfo[] enemyRobots = robotController.senseNearbyRobots(sensorRadiusSquared);        

        for (RobotInfo robotInfo : enemyRobots) 
        {
            if (robotInfo.getTeam() == enemy) 
            {
                processEnemyNearBase(robotInfo);
            }
            else if (robotInfo.getTeam() == friendly)
            {
                if (robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER) 
                {
                    builtRobotsInfos.add(robotInfo);
                }
            }
            
        }
    }

    private static void processEnemyNearBase(RobotInfo robotInfo) {
        if (robotInfo.getType() == RobotType.POLITICIAN && robotInfo.getConviction() > 30) 
        {
            enemyTargetNearby.add(robotInfo.getLocation());
            countOfEnemyPoliticiansOverThirtyInfluence++;
        } 
        else if (robotInfo.getType() == RobotType.POLITICIAN && robotInfo.getConviction() <= 20)
        {
            countOfEnemyPoliticiansUnderTwentyInfluence++;
        } 
        else if (robotInfo.getType() == RobotType.MUCKRAKER) 
        {
            countOfEnemyMuckraker++;
        } 
        else 
        {
            countOfOtherRobots++;
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
                builtRobotsInfos.remove(whereIteratorStopped);
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
        else if (robotInfo.type == RobotType.POLITICIAN
            && robotInfo.influence == INFLUENCE_FOR_SCOUT)
        {
            countOfScouts--;
        }
        else if (robotInfo.type == RobotType.MUCKRAKER) 
        {
            if (robotInfo.getInfluence() >= BUFF_MUCKRAKER_MIN_INFLUENCE) 
            {
                countOfBuffMucks--;    
            }
            else 
            {
                countOfScouts--;
                countOfMuckrakers--;
            }            
        }
        else if (robotInfo.type == RobotType.SLANDERER)
        {
            countOfSlanderer--;
        }
    }

    private static void setEnemyAndNeutralEnlightenmentCenterOrResetThem() {
        if (!neutralEnlightenmentCenterMapLocation.isEmpty())
        {
            setNeutralCurrentEnlightenmentCenterGoingFor();
            neutralEnlightenmentCenterFound = true;
        }

        if (!enemyEnlightenmentCenterMapLocation.isEmpty())
        {
            setEnemyCurrentEnlightenmentCenterGoingFor();
            enemyEnlightenmentCenterFound = true;
        }

        if (!enemyEnlightenmentCenterFound && !enemyEnlightenmentCenterMapLocation.isEmpty()) 
        {
            enemyEnlightenmentCenterFound = true;    
        }

        if(enemyEnlightenmentCenterFound && neutralEnlightenmentCenterFound)
        {
            if (enemyEnlightenmentCenters.get(enemyCurrentEnlightenmentCenterGoingFor).distanceSquaredToEnlightenmentCenter 
                < neutralEnlightenmentCenters.get(neutralCurrentEnlightenmentCenterGoingFor).distanceSquaredToEnlightenmentCenter)
            {
                enemyEnlightenmentCenterIsCloser = true;
            }
        }
    }

    private static void setNeutralCurrentEnlightenmentCenterGoingFor() 
    {
        if (neutralEnlightenmentCenters.size() == 1) 
        {
            EnlightenmentCenterInfo enlightenmentCenterInfo = neutralEnlightenmentCenters.get(neutralEnlightenmentCenterMapLocation.get(0));
            neutralCurrentEnlightenmentCenterGoingFor = enlightenmentCenterInfo.mapLocation;
            neutralEnlightenmentCenterCurrentInfluence = enlightenmentCenterInfo.currentInfluence;
        }
        else if (!neutralEnlightenmentCenters.isEmpty())
        {
            int lowestInfluenceForNeutral = Integer.MAX_VALUE;
            int closestDistanceSquaredToNeutral = Integer.MAX_VALUE;

            for (Map.Entry<MapLocation, EnlightenmentCenterInfo> enlightenmentCenter : neutralEnlightenmentCenters.entrySet()) 
            {
                EnlightenmentCenterInfo enlightenmentCenterInfo = enlightenmentCenter.getValue();
                int numberOfRobotsGoingToCenter = enlightenmentCenterInfo.numberOfRobotsGoingToIt;

                if (numberOfRobotsGoingToCenter > 0 && enlightenmentCenterInfo.currentInfluence == HIGHEST_INFLUENCE_VALUE_FOR_NEUTRAL_LOCATION) 
                {
                    continue;
                }
                else
                {
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
                    // Less than most influence but greater than least and closest
                    else if (enlightenmentCenterInfo.currentInfluence >= lowestInfluenceForNeutral 
                        && enlightenmentCenterInfo.currentInfluence < HIGHEST_INFLUENCE_VALUE_FOR_NEUTRAL_LOCATION
                        && enlightenmentCenterInfo.distanceSquaredToEnlightenmentCenter 
                            <= closestDistanceSquaredToNeutral) 
                    {
                        closestDistanceSquaredToNeutral = enlightenmentCenterInfo.distanceSquaredToEnlightenmentCenter;

                        neutralCurrentEnlightenmentCenterGoingFor = enlightenmentCenterInfo.mapLocation;
                        neutralEnlightenmentCenterCurrentInfluence = enlightenmentCenterInfo.currentInfluence;
                    }  
                    // Less than the most possible influence and farther
                    else if (enlightenmentCenterInfo.currentInfluence <= lowestInfluenceForNeutral 
                        && enlightenmentCenterInfo.currentInfluence < HIGHEST_INFLUENCE_VALUE_FOR_NEUTRAL_LOCATION
                        && enlightenmentCenterInfo.distanceSquaredToEnlightenmentCenter 
                            >= closestDistanceSquaredToNeutral) 
                    {
                        lowestInfluenceForNeutral = enlightenmentCenterInfo.currentInfluence;
                        
                        neutralCurrentEnlightenmentCenterGoingFor = enlightenmentCenterInfo.mapLocation;
                        neutralEnlightenmentCenterCurrentInfluence = enlightenmentCenterInfo.currentInfluence;
                    }  
                    else if (neutralCurrentEnlightenmentCenterGoingFor ==  null)
                    {
                        neutralCurrentEnlightenmentCenterGoingFor = enlightenmentCenterInfo.mapLocation;
                        neutralEnlightenmentCenterCurrentInfluence = enlightenmentCenterInfo.currentInfluence >= LOWEST_INFLUENCE_VALUE_FOR_NEUTRAL_LOCATION ? enlightenmentCenterInfo.currentInfluence : LOWEST_INFLUENCE_VALUE_FOR_NEUTRAL_LOCATION;
                    }   
                }                       
            }
        }   
    }

    // TODO: Make it choose what enemy to go for next...
    private static void setEnemyCurrentEnlightenmentCenterGoingFor() 
    {
        if (enemyEnlightenmentCenters.size() == 1) 
        {
            EnlightenmentCenterInfo enlightenmentCenterInfo = enemyEnlightenmentCenters.get(enemyEnlightenmentCenterMapLocation.get(0));
            enemyCurrentEnlightenmentCenterGoingFor = enlightenmentCenterInfo.mapLocation;
            enemyEnlightenmentCenterCurrentInfluence = enlightenmentCenterInfo.currentInfluence;
        }
        else if (!enemyEnlightenmentCenters.isEmpty())
        {
            int lowestInfluenceForEnemy = Integer.MAX_VALUE;
            int closestDistanceSquaredToEnemy = Integer.MAX_VALUE;

            for (Map.Entry<MapLocation, EnlightenmentCenterInfo> enlightenmentCenter : enemyEnlightenmentCenters.entrySet()) 
            {
                EnlightenmentCenterInfo enlightenmentCenterInfo = enlightenmentCenter.getValue();

                // Lowest influence and the closest. We want that
                if (enlightenmentCenterInfo.currentInfluence <= lowestInfluenceForEnemy
                    && enlightenmentCenterInfo.distanceSquaredToEnlightenmentCenter 
                        <= closestDistanceSquaredToEnemy)
                {
                    lowestInfluenceForEnemy = enlightenmentCenterInfo.currentInfluence;
                    closestDistanceSquaredToEnemy = enlightenmentCenterInfo.distanceSquaredToEnlightenmentCenter;

                    enemyCurrentEnlightenmentCenterGoingFor = enlightenmentCenterInfo.mapLocation;
                    enemyEnlightenmentCenterCurrentInfluence = enlightenmentCenterInfo.currentInfluence;
                }
                // Less than most influence and closest
                else if (enlightenmentCenterInfo.currentInfluence >= lowestInfluenceForEnemy 
                    && enlightenmentCenterInfo.currentInfluence < (lowestInfluenceForEnemy * 5)
                    && enlightenmentCenterInfo.distanceSquaredToEnlightenmentCenter 
                        <= closestDistanceSquaredToEnemy) 
                {
                    closestDistanceSquaredToEnemy = enlightenmentCenterInfo.distanceSquaredToEnlightenmentCenter;

                    enemyCurrentEnlightenmentCenterGoingFor = enlightenmentCenterInfo.mapLocation;
                    enemyEnlightenmentCenterCurrentInfluence = enlightenmentCenterInfo.currentInfluence;
                }    
                else if (enemyCurrentEnlightenmentCenterGoingFor ==  null)
                {
                    enemyCurrentEnlightenmentCenterGoingFor = enlightenmentCenterInfo.mapLocation;
                    enemyEnlightenmentCenterCurrentInfluence = enlightenmentCenterInfo.currentInfluence;
                }           
            }
        }
    }

    protected static void decideIfShouldBuildSlanderer()
    {
        if (shouldBuildSlanderer())
        {
            buildSlanderer = true;
        }
        else
        {
            buildSlanderer = false;
        }
    }

    protected static boolean shouldBuildSlanderer() 
    {
        boolean shouldBuildSlanderer = false;

        if
        ((income < 25 || robotCurrentInfluence < 300) 
            && turnCount <= 100)
        {
            shouldBuildSlanderer = true;
        }
        else if
        ((income < 75 || robotCurrentInfluence < 700) 
            && turnCount > 100)
        {
            shouldBuildSlanderer = true;
        }   
        else if
        ((income < 100 || robotCurrentInfluence < 1000) 
            && turnCount > 225)
        {
            shouldBuildSlanderer = true;
        } 
        else if ((income < 175 || robotCurrentInfluence < 2000) 
        && robotController.getRoundNum() >= MIDDLE_GAME_ROUND_START && turnCount > 550)
        {
            shouldBuildSlanderer = true;
        }
        else if ((income < 225 || robotCurrentInfluence < 4000) 
        && robotController.getRoundNum() >= MIDDLE_GAME_ROUND_START && turnCount > 700)
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

    protected static int getAmountNeededForSlanderer() throws GameActionException
    {
        if ((turnCount > 25 || robotController.getRoundNum() < 50) && isItSafe())
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
        else if(turnCount < 25 && robotController.getRoundNum() > 50 && isItSafe())
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
        else if (!isItSafe())
        {
            for (int iterator = (newEnlightenmentCenterSlandererInfluenceAmount.length - 1); iterator > 0; --iterator) 
            {
                if (newEnlightenmentCenterSlandererInfluenceAmount[iterator] > (robotCurrentInfluence - 200) 
                && newEnlightenmentCenterSlandererInfluenceAmount[iterator - 1] < (robotCurrentInfluence - 200)) 
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

        return amountNeededForSlanderer;        
    }

    private static void decideToSetFlags() throws GameActionException
    {
        if (!enemyEnlightenmentCenterMapLocation.isEmpty() && robotController.getRoundNum() % 2 == 0) 
        {
            if (enemyCurrentEnlightenmentCenterGoingFor != null) 
            {
                Communication.sendLocation(enemyCurrentEnlightenmentCenterGoingFor, Communication.ENEMY_ENLIGHTENMENT_CENTER_FOUND);
            }
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

    private static void decideWhatToBid() throws GameActionException {
        if (robotController.getRoundNum() >= MIDDLE_GAME_ROUND_START
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
                amountToBid -= 1;
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
    }

    protected static boolean isItSafeForSlanderer() {
        return countOfEnemyMuckraker == 0;
    }

    private static void setRateOfInfluenceReceived() 
    {
        differenceOfInfluenceBetweenRounds = robotCurrentInfluence - lastTurnInfluence;
        generatedRoundInfluence = Math.ceil((.2) * (Math.sqrt(robotController.getRoundNum())));
        
        lastTurnIncome = income;
        int newIncome = differenceOfInfluenceBetweenRounds + influenceUsedFromLastBuild + amountToBid;
        if(newIncome > -1)
        {
            income = (int) (differenceOfInfluenceBetweenRounds + influenceUsedFromLastBuild + amountToBid);
        }
        else 
        {
            income = lastTurnIncome;
        }
    }



    protected static boolean isThereEnoughForEnemyECCapture() throws GameActionException {
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
        return countOfEnemyPoliticiansOverThirtyInfluence == 0;
    }

    protected static boolean hasEnoughInfluenceToBeSafe(int influenceToUse)
    {
        return (robotCurrentInfluence - influenceToUse) > 200;
    }

    protected static int getAmountToMakePoliticianBombForEnemy() 
    {
        int currentInfluence = robotController.getInfluence();
        int influenceToBuildWith = POLITICIAN_EC_BOMB;
        int enemyEnlightenmentCenterInfluenceWithPoliticianTax = enemyEnlightenmentCenterCurrentInfluence + POLITICIAN_TAX + 1;

        if (enemyEnlightenmentCenterInfluenceWithPoliticianTax < POLITICIAN_EC_BOMB) 
        {
            enemyEnlightenmentCenterInfluenceWithPoliticianTax += POLITICIAN_EC_BOMB;
        }

        if (200 < (currentInfluence - (enemyEnlightenmentCenterInfluenceWithPoliticianTax * 10))) 
        {
            influenceToBuildWith = enemyEnlightenmentCenterInfluenceWithPoliticianTax * 10;
        }
        else if (200 < (currentInfluence - (enemyEnlightenmentCenterInfluenceWithPoliticianTax * 5))) 
        {
            influenceToBuildWith = enemyEnlightenmentCenterInfluenceWithPoliticianTax * 5;
        }
        else if (200 < (currentInfluence - (enemyEnlightenmentCenterInfluenceWithPoliticianTax * 2))) 
        {
            influenceToBuildWith = enemyEnlightenmentCenterInfluenceWithPoliticianTax * 2;
        }
        else if (enemyEnlightenmentCenterInfluenceWithPoliticianTax > POLITICIAN_EC_BOMB) 
        {
            influenceToBuildWith = enemyEnlightenmentCenterInfluenceWithPoliticianTax;
        }       

        return influenceToBuildWith;
    }

    protected static int getAmountToMakePoliticianBombForNeutral() throws GameActionException 
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

    protected static boolean enoughDefenderPolitician()
    {
        boolean enoughDefenderPolitician = false;
        
        if (countOfDefenderPolitician >= Math.ceil(countOfSlanderer * 0.75))
        {
            enoughDefenderPolitician = true;
        }

        return enoughDefenderPolitician;
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

                if (RobotBuilder.robotTypeToBuild == RobotType.POLITICIAN && influenceUsedFromLastBuild > 1) 
                {
                    checkIfRobotWasPoliticianBomb();
                }
                
            }
            else
            {
                println("Welp... This awk " + RobotBuilder.directionToSpawn + " " + RobotBuilder.influenceToUse + " " + RobotBuilder.robotTypeToBuild);
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

    private static void checkIfRobotWasPoliticianBomb() 
    {
        if (neutralCurrentEnlightenmentCenterGoingFor != null 
            && neutralEnlightenmentCenterCurrentInfluence != 0 
            && influenceUsedFromLastBuild == neutralEnlightenmentCenterCurrentInfluence + POLITICIAN_TAX + 1) 
        {
            neutralEnlightenmentCenters.get(neutralCurrentEnlightenmentCenterGoingFor).numberOfRobotsGoingToIt++;
        }
        else if (enemyCurrentEnlightenmentCenterGoingFor != null 
            && enemyEnlightenmentCenterCurrentInfluence != 0 
            && influenceUsedFromLastBuild == enemyEnlightenmentCenterCurrentInfluence + POLITICIAN_TAX + 1)
        {
            enemyEnlightenmentCenters.get(enemyCurrentEnlightenmentCenterGoingFor).numberOfRobotsGoingToIt++;
        }
    }

    public static void setup() throws GameActionException {
        if (robotController.getRoundNum() == 1) 
        {
            numberOfEnlightenmentCenters = robotController.getRobotCount();
        }

        numberOfScoutsToCreateInBeginning = NUMBER_OF_MUCKRAKERS_IN_BEGINNING;        

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

                    EnlightenmentCenterInfo enlightenmentCenterInfo = new EnlightenmentCenterInfo();
                    enlightenmentCenterInfo.currentInfluence = robotInfo.getInfluence();
                    enlightenmentCenterInfo.mapLocation = robotInfo.getLocation();
                    enlightenmentCenterInfo.distanceSquaredToEnlightenmentCenter = robotController.getLocation().distanceSquaredTo(robotInfo.getLocation());
                    enemyEnlightenmentCenters.put(robotInfo.getLocation(), enlightenmentCenterInfo);

                    enemyCurrentEnlightenmentCenterGoingFor = robotInfo.getLocation();
                    enemyEnlightenmentCenterCurrentInfluence = robotInfo.getInfluence();
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
                else if (robotInfo.getTeam() == friendly)
                {
                    builtRobotsInfos.add(robotInfo);
                }                
            }
        }
        
    }

}