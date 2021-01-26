package testPlayerv01.Service;

import testPlayerv01.EnlightenmentCenterTest01;
import testPlayerv01.Model.EnlightenmentCenterInfo;

import java.util.Map;

import battlecode.common.*;

public class EnlightenmentCenterHelper extends EnlightenmentCenterTest01
{

    static boolean stockUpForNeutralEnlightenmentCenter;
    static boolean muckRush;
	private static boolean stockupForEnemyEnlightenmentCenter;

    public static void checkFlagsForSignals(int extraInformation, int flag, int robotIdThatSpotted) throws GameActionException
    {
        // TODO: Add in check for edge of map
        if (checkIfRobotSignallingTheyFoundEnemyEnlightenmentCenter(extraInformation)) 
        {
            setEnemyEnlightenmentCenter(flag);
        }
        else if (checkIfRobotSignallingEnlightenmentCenterConverted(extraInformation))
        {
            Communication.processEnemyEnlightenmentCenterHasBeenConverted(Communication.getLocationFromFlag(flag));
        }
        else if (checkIfRobotSignallingEnemyECInfluence(extraInformation))
        {
            enemyEnlightenmentCenterCurrentInfluence = Communication.getEnemyEnlightenmentCenterInfluenceFromFlag(flag);
        }
        else if (checkIfRobotSignallingNeutralEnlightenmentCenterFound(extraInformation))
        {
            if(Communication.processNeutralEnlightenmentCenterHasBeenFound(flag))
            {
                createNeutralEnlightenmentCenterEntry(robotIdThatSpotted, neutralEnlightenmentCenterMapLocation.get(neutralEnlightenmentCenterMapLocation.size() - 1));
            }          
        }
        else if (checkIfRobotSignallingNeutralEnlightenmentCenterInfluence(extraInformation)) 
        {
            int localNeutralEnlightenmentCenterCurrentInfluence = Communication.getNeutralEnlightenmentCenterInfluenceFromFlag(flag);
            updateNeutralEnlightenmentCenterWithInfluence(robotIdThatSpotted, localNeutralEnlightenmentCenterCurrentInfluence);            
        }
        else if (checkIfRobotSignallingNeutralEnlightenmentCenterConverted(extraInformation))
        {
            MapLocation location = Communication.getLocationFromFlag(flag);
            Communication.processNeutralEnlightenmentCenterHasBeenConverted(location);
            updateNeutralEnlightenmentCenterForConversion(location);
        }
    }
    
    //#region Check Robot Signalling
    public static boolean checkIfRobotSignallingTheyFoundEnemyEnlightenmentCenter(int extraInformation) throws GameActionException 
    {
        return extraInformation == Communication.ENEMY_ENLIGHTENMENT_CENTER_FOUND;
    }

    public static boolean checkIfRobotSignallingEnlightenmentCenterConverted(int extraInformation) throws GameActionException 
    {
        return extraInformation == Communication.ENEMY_ENLIGHTENMENT_CENTER_CONVERTED
            && !convertedEnemyEnlightenmentCenterHasBeenProcessedThisTurn;
    }

    public static boolean checkIfRobotSignallingEnemyECInfluence(int extraInformation) throws GameActionException 
    {
        return extraInformation == Communication.ENEMY_ENLIGHTENMENT_CENTER_INFLUENCE
            && !enemyEnlightenmentCenterInfluenceHasBeenUpdated;
    }    

    public static boolean checkIfRobotSignallingNeutralEnlightenmentCenterFound(int extraInformation) throws GameActionException
    {
        return extraInformation == Communication.NUETRAL_ENLIGHTENMENT_CENTER_FOUND;
    }  
    
    public static boolean checkIfRobotSignallingNeutralEnlightenmentCenterInfluence(int extraInformation) throws GameActionException
    {
        return extraInformation == Communication.NUETRAL_ENLIGHTENMENT_CENTER_INFLUENCE;
    }  

    public static boolean checkIfRobotSignallingNeutralEnlightenmentCenterConverted(int extraInformation) throws GameActionException
    {
        return extraInformation == Communication.NUETRAL_ENLIGHTENMENT_CENTER_CONVERTED;
    }

    protected static void createNeutralEnlightenmentCenterEntry(int robotIdThatSpotted, MapLocation neutralMapLocation)
    {

        if(neutralEnlightenmentCenters.isEmpty() || !neutralEnlightenmentCenters.containsKey(neutralMapLocation))
        {
            EnlightenmentCenterInfo enlightenmentCenterInfo = new EnlightenmentCenterInfo();
            enlightenmentCenterInfo.mapLocation = neutralMapLocation;
            enlightenmentCenterInfo.robotIdIterator = 0;
            enlightenmentCenterInfo.robotIdThatSpottedEnlightenmentCenter[enlightenmentCenterInfo.robotIdIterator] = robotIdThatSpotted;
            enlightenmentCenterInfo.distanceSquaredToEnlightenmentCenter = robotController.getLocation().distanceSquaredTo(neutralMapLocation);
            enlightenmentCenterInfo.team = Team.NEUTRAL;
            neutralEnlightenmentCenters.put(neutralMapLocation, enlightenmentCenterInfo);
        }
        else if (neutralEnlightenmentCenters.containsKey(neutralMapLocation)) 
        {
            EnlightenmentCenterInfo enlightenmentCenterInfo = neutralEnlightenmentCenters.get(neutralMapLocation);
            enlightenmentCenterInfo.robotIdThatSpottedEnlightenmentCenter[enlightenmentCenterInfo.robotIdIterator] = robotIdThatSpotted;
            enlightenmentCenterInfo.robotIdIterator++;
        }
    }

    protected static void updateNeutralEnlightenmentCenterWithInfluence(int robotIdThatSpotted, int localNeutralEnlightenmentCenterCurrentInfluence)
    {
        if (neutralEnlightenmentCenters.size() != alreadyProcessedNeutralCenters) 
        {
            for (Map.Entry<MapLocation, EnlightenmentCenterInfo> enlightenmentCenter : neutralEnlightenmentCenters.entrySet()) 
            {
                EnlightenmentCenterInfo enlightenmentCenterInfo = enlightenmentCenter.getValue();
                for (Integer robotIdThatSpottedInMap : enlightenmentCenterInfo.robotIdThatSpottedEnlightenmentCenter) {
                    if (robotIdThatSpottedInMap == robotIdThatSpotted) 
                    {
                        enlightenmentCenterInfo.currentInfluence = localNeutralEnlightenmentCenterCurrentInfluence;
                        alreadyProcessedNeutralCenters++;
                        enemyEnlightenmentCenterInfluenceHasBeenUpdated = true;
                    }
                }
            }
        }        
    }

    protected static void updateNeutralEnlightenmentCenterForConversion(MapLocation neutralMapLocationConverted)
    {
        neutralEnlightenmentCenters.remove(neutralMapLocationConverted);
    }

    //#endregion

    /** 

    */

    public static void decideWhatToBuild() throws GameActionException
    {
        muckRush = false;
        if (!neutralEnlightenmentCenterFound || neutralEnlightenmentCenterHasBeenConverted) 
        {
            stockUpForNeutralEnlightenmentCenter = false;
        }

        if (robotController.isReady()) 
        {
            if (countOfEnemyMuckraker >= 10 
                && robotCurrentInfluence >= (countOfEnemyMuckraker * 3)  + POLITICIAN_TAX) 
            {
                muckRush = true;
                buildBigPoliticians();    
                // TODO: Signal to kill enemies in radius if nearby
            }
            else 
            {
                buildNormalDefenseEconomy();
            }
        }
    }

    protected static void buildBigPoliticians() throws GameActionException
    {
        RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
        RobotBuilder.influenceToUse = (countOfEnemyMuckraker * 3)  + POLITICIAN_TAX;
        RobotBuilder.robotTypeToBuild = RobotType.POLITICIAN;
        if (robotCurrentInfluence >= RobotBuilder.influenceToUse) 
        {
            buildThisTurn = true;
        }
    }

    protected static void buildNormalDefenseEconomy() throws GameActionException
    {
        int countOfNeutralPoliticianBombAllowed = (neutralEnlightenmentCenters.size() * 2) != 0 ? (neutralEnlightenmentCenterMapLocation.size() * 2) : 2;

        if (turnCount < 3 && !weSpawnedRightNextToEnemy && isItSafeForSlanderer() && isItSafe()) 
        {
            int firstInfluence = getAmountNeededForSlandererInBeginning();

            if (robotCurrentInfluence >= firstInfluence)
            {
                turnsNotBuilding = 0;
                buildThisTurn = true;
                RobotBuilder.robotTypeToBuild = RobotType.SLANDERER;
                RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
                RobotBuilder.influenceToUse = firstInfluence;
                countOfSlanderer++;
            }
            else
            {
                buildSlanderer = true;
            }                
        }

        if (neutralEnlightenmentCenterFound
            && !buildThisTurn
            && decideToBuildNeutralEnlightenmentCenterBasedOnIncome()
            && (countOfNeutralPoliticianBomb <= countOfNeutralPoliticianBombAllowed)) 
        {
            if (isThereEnoughForNeutralEnlightenmentCenterCapture() && isItSafe()) 
            {
                buildNeutralECBomb();
            } 
            else if (!isThereEnoughForNeutralEnlightenmentCenterCapture())
            {
                stockUpForNeutralEnlightenmentCenter = true;
            }
            else if (isItSafe() && hasEnoughInfluenceToBeSafe(getAmountToMakePoliticianBombForNeutral()))
            {
                buildNeutralECBomb();
            }
            else if (!isItSafe() && !hasEnoughInfluenceToBeSafe(getAmountToMakePoliticianBombForNeutral()))
            {
                stockUp = true;
            }
        }
        
        // Build EC Bomb for Enemy Enlightenment Center
        if (enemyEnlightenmentCenterFound
            && !buildThisTurn
            && !buildSlanderer
            && !stockUpForNeutralEnlightenmentCenter) 
        {
            if (isThereEnoughForEnemyBomb() && isItSafe()) 
            {
                buildEnemyECBomb();
            }
            else if (!isThereEnoughForEnemyBomb())
            {
                stockupForEnemyEnlightenmentCenter = true;
            }
            else if (!isItSafe() && hasEnoughInfluenceToBeSafe(getAmountToMakePoliticianBombForEnemy()))
            {
                buildEnemyECBomb();
            }
            else if (!isItSafe() && !hasEnoughInfluenceToBeSafe(getAmountToMakePoliticianBombForEnemy()))
            {
                stockUp = true;
            } 
        } 

        if (buildSlanderer
            && !buildThisTurn 
            && !muckRush
            && !weSpawnedRightNextToEnemy
            && isItSafeForSlanderer() 
            && isItSafe()
            && (enoughDefenderPolitician() || countOfSlanderer <= 2 && robotController.getRoundNum() < 25)
            && (!stockUpForNeutralEnlightenmentCenter && !stockupForEnemyEnlightenmentCenter))
        {
            buildEconomy();
        }
        else if (!isItSafe() && isItSafeForSlanderer() && hasEnoughInfluenceToBeSafe(getAmountNeededForSlanderer()))  
        {
            buildEconomy();
        }          
        else if (buildSlanderer 
        && !buildThisTurn 
        && isItSafe()
        && (!isItSafeForSlanderer() || muckRush) 
        && (!stockUpForNeutralEnlightenmentCenter && !stockupForEnemyEnlightenmentCenter))
        {
            buildDefenders();
        }          
        
        //#region Special Builds         

        // After Mid game build more polis
        if (robotController.getRoundNum() <= MIDDLE_GAME_ROUND_START 
            && robotController.getRoundNum() > BEGINNING_ROUND_STRAT
            && robotController.getRobotCount() > 40
            && (robotCurrentInfluence >= 1000 || income > 50)
            && !buildThisTurn
            && countOfBuffMucks < 10
            && !stockUp
            && isItSafe()) 
        {
            RobotType toBuild = RobotType.MUCKRAKER;

            int influence = 0;
            if ((robotCurrentInfluence * .15) > BUFF_MUCKRAKER_MIN_INFLUENCE) 
            {
                int max = (int) Math.ceil(robotCurrentInfluence * .15);
                int min = BUFF_MUCKRAKER_MIN_INFLUENCE;
                influence = randomInteger.nextInt(max - min + 1) + min;

                turnsNotBuilding = 0;
                if (robotCurrentInfluence >= influence) 
                {
                    buildThisTurn = true;
                    RobotBuilder.influenceToUse = influence;
                    RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
                    RobotBuilder.robotTypeToBuild = toBuild;
                    countOfBuffMucks++;
                }
            } 
        }

        if (robotController.getRoundNum() >= END_GAME_ROUND_STRAT 
            && robotController.getRobotCount() > 100
            && (!buildThisTurn && !stockUpForNeutralEnlightenmentCenter && !stockupForEnemyEnlightenmentCenter && !stockUp)
            && isItSafe()) 
        {
            RobotType toBuild = RobotType.POLITICIAN;

            int influence = 0;
            if ((lastTurnInfluence * .15) > MAX_NORMAL_POLITICIAN) 
            {
                int max = (int) Math.ceil(lastTurnInfluence * .25);
                int min = MAX_NORMAL_POLITICIAN;
                influence = randomInteger.nextInt(max - min + 1) + min;

                turnsNotBuilding = 0;
                if (robotCurrentInfluence >= influence) 
                {
                    buildThisTurn = true;
                    RobotBuilder.influenceToUse = influence;
                    RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
                    RobotBuilder.robotTypeToBuild = toBuild;
                    countOfPoliticians++;
                }
            } 
        }
        //#endregion

        buildScouts();
    }

    private static void buildEnemyECBomb() throws GameActionException {
        RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
        RobotBuilder.influenceToUse = getAmountToMakePoliticianBombForEnemy();
        RobotBuilder.robotTypeToBuild = RobotType.POLITICIAN;

        turnsNotBuilding = 0;
        stockupForEnemyEnlightenmentCenter = false;
        buildThisTurn = true;
        countOfPoliticianBomb++;
    }

	private static boolean decideToBuildNeutralEnlightenmentCenterBasedOnIncome() 
    {
        return (neutralEnlightenmentCenterCurrentInfluence != 0 
        && ((income >= 50 && neutralEnlightenmentCenterCurrentInfluence < HIGHEST_INFLUENCE_VALUE_FOR_NEUTRAL_LOCATION)
            || (robotCurrentInfluence > 200 && neutralEnlightenmentCenterCurrentInfluence < MIDDLE_INFLUENCE_VALUE_FOR_NEUTRAL_LOCATION)
            || (robotCurrentInfluence > 400 && income >= 25)
            || (neutralEnlightenmentCenterCurrentInfluence <= (LOWEST_INFLUENCE_VALUE_FOR_NEUTRAL_LOCATION * 2))));
    }

    private static void buildNeutralECBomb() throws GameActionException 
    {
        RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
        RobotBuilder.influenceToUse = getAmountToMakePoliticianBombForNeutral();
        RobotBuilder.robotTypeToBuild = RobotType.POLITICIAN;

        turnsNotBuilding = 0;
        stockUpForNeutralEnlightenmentCenter = false;
        buildThisTurn = true;
        countOfNeutralPoliticianBomb++;
    }

    protected static void buildEconomy() throws GameActionException
    {
        getAmountNeededForSlanderer();

        if (robotCurrentInfluence < amountNeededForSlanderer) 
        {
            stockUp = true;
        } 
        else if (amountNeededForSlanderer != 0) 
        {
            turnsNotBuilding = 0;
            buildThisTurn = true;
            buildSlanderer = false;
            RobotBuilder.robotTypeToBuild = RobotType.SLANDERER;
            RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
            RobotBuilder.influenceToUse = amountNeededForSlanderer;
            countOfSlanderer++;
            stockUp = false;
            amountNeededForSlanderer = 0;
        }        
    }

    protected static void buildScouts() throws GameActionException
    {
        if (countOfScouts <= numberOfScoutsToCreateInBeginning
            && robotController.getRoundNum() <= BEGINNING_ROUND_STRAT 
            && !weSpawnedRightNextToEnemy
            && !buildThisTurn) 
        {
            RobotBuilder.directionToSpawn = getDirectionToScout(); //getAvailableDirectionToSpawn();
            RobotBuilder.influenceToUse = INFLUENCE_FOR_SCOUT;
            RobotBuilder.robotTypeToBuild = randomSpawnableScoutRobotType();

            turnsNotBuilding = 0;
            buildThisTurn = true;
            builtScoutLastTurn = true;
            if (RobotBuilder.robotTypeToBuild == RobotType.MUCKRAKER) 
            {
                countOfMuckrakers++;
            }

            countOfScouts++;            
        } 

         // Build a scout if stocking up
        if (!buildThisTurn && countOfMuckrakers <= 200)
        {
             RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
             RobotBuilder.influenceToUse =  INFLUENCE_FOR_SCOUT;
             RobotBuilder.robotTypeToBuild = RobotType.MUCKRAKER;
 
             builtScoutLastTurn = true;
             turnsNotBuilding = 0;
             buildThisTurn = true;
             countOfMuckrakers++;
        }     
    }

    protected static void buildDefenders() throws GameActionException
    {
        RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
        int min = POLITICIAN_SCOUT + 1;
        int max = POLITICIAN_DEFEND_SLANDERER;
        int influence = randomInteger.nextInt(max - min + 1) + min;
        RobotBuilder.influenceToUse = influence;
        RobotBuilder.robotTypeToBuild = RobotType.POLITICIAN;

        if (robotCurrentInfluence >= influence)
        {
            buildThisTurn = true;
            countOfDefenderPolitician++;
        } 
    }

    protected static int getAmountNeededForSlandererInBeginning()
    {
        for (int iterator = 0; iterator < (slandererInfluenceAmount.length - 1); iterator++) 
        {
            if (slandererInfluenceAmount[iterator] < robotCurrentInfluence 
            && slandererInfluenceAmount[iterator + 1] > robotCurrentInfluence) 
            {
                amountNeededForSlanderer = slandererInfluenceAmount[iterator];
                break;
            } 
            else if (slandererInfluenceAmount[iterator] > robotCurrentInfluence)
            {
                amountNeededForSlanderer = slandererInfluenceAmount[iterator];
                break;
            }
        }
        return amountNeededForSlanderer;
    }
}
