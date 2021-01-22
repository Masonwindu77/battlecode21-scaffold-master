package testPlayerv01.Service;

import testPlayerv01.EnlightenmentCenterTest01;
import testPlayerv01.Model.EnlightenmentCenterInfo;

import java.util.Map;

import battlecode.common.*;

public class EnlightenmentCenterHelper extends EnlightenmentCenterTest01
{

    public static void checkFlagsForSignals(int extraInformation, int flag, int robotIdThatSpotted) throws GameActionException
    {
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
            neutralEnlightenmentCenterCurrentInfluence = Communication.getNeutralEnlightenmentCenterInfluenceFromFlag(flag);
            updateNeutralEnlightenmentCenterWithInfluence(robotIdThatSpotted);            
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
        else if (!neutralEnlightenmentCenters.containsKey(neutralMapLocation)) 
        {
            EnlightenmentCenterInfo enlightenmentCenterInfo = neutralEnlightenmentCenters.get(neutralMapLocation);
            enlightenmentCenterInfo.robotIdThatSpottedEnlightenmentCenter[enlightenmentCenterInfo.robotIdIterator] = robotIdThatSpotted;
            enlightenmentCenterInfo.robotIdIterator++;
        }
    }

    protected static void updateNeutralEnlightenmentCenterWithInfluence(int robotIdThatSpotted)
    {
        if (neutralEnlightenmentCenters.size() != alreadyProcessedNeutralCenters) 
        {
            for (Map.Entry<MapLocation, EnlightenmentCenterInfo> enlightenmentCenter : neutralEnlightenmentCenters.entrySet()) 
            {
                EnlightenmentCenterInfo enlightenmentCenterInfo = enlightenmentCenter.getValue();
                for (Integer robotIdThatSpottedInMap : enlightenmentCenterInfo.robotIdThatSpottedEnlightenmentCenter) {
                    if (robotIdThatSpottedInMap == robotIdThatSpotted) 
                    {
                        enlightenmentCenterInfo.currentInfluence = neutralEnlightenmentCenterCurrentInfluence;
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
     * 1. Build slandere at 130 if not spawned next to enemy
     * 2. If empowerfactor is higher than 5 than selfempower at 90% what currentInfluence is
     * 3. Create Polis if over 400 round and greater than 50 robots and not stocking up
     * 4. If count of muckrakers is less than what I want in the beginning and we didn't spawn next to enemy && less than mid game, build more.
     * 5. If safe for slanderer and I want to build it, build it.
     * 6. If enlightenmentcenterFound -> start building for capturing it
     * 7. If friendlySlanderer around and not enough defender polis -> Build more
     * 8. If Neutral enlightenmentcenter found => build things to get it.
     * 9. Spawn random Polis if not stocking up and the count of Polis is less than mucks...
     * 10. If stocking up, use .01 of current influence to build a muck scout.
    */

    public static void decideWhatToBuild() throws GameActionException
    {
        if (robotController.isReady()) 
        {
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
                && !buildSlanderer
                && (countOfNeutralPoliticianBomb <= 2 && (robotController.getRoundNum() > 50 || countOfNeutralPoliticianBomb < 1))) 
            {
                if (isThereEnoughForNeutralEnlightenmentCenterCapture() && isItSafe()) 
                {
                    RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
                    RobotBuilder.influenceToUse = getAmountToMakePoliticianBombForNeutral();
                    RobotBuilder.robotTypeToBuild = RobotType.POLITICIAN;

                    turnsNotBuilding = 0;
                    stockUp = false;
                    buildThisTurn = true;
                    countOfNeutralPoliticianBomb++;
                } 
                else if (!isThereEnoughForNeutralEnlightenmentCenterCapture() 
                    && isItSafe()
                    && (income < 25)) 
                {
                    stockUp = true;
                    buildSlanderer = true;
                    turnsNotBuilding++;
                }
            }
            
            // Build EC Bomb for Enemy Enlightenment Center
            if (enemyEnlightenmentCenterFound
                && !buildThisTurn
                && !buildSlanderer) 
            {
                if (isThereEnoughForBomb() 
                    && isItSafe()) 
                {
                    RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
                    RobotBuilder.influenceToUse = getAmountToMakePoliticianBomb();
                    RobotBuilder.robotTypeToBuild = RobotType.POLITICIAN;

                    turnsNotBuilding = 0;
                    stockUp = false;
                    buildThisTurn = true;
                    countOfPoliticianBomb++;
                } 
                else if (!isThereEnoughForBomb() 
                    && isItSafe()
                    && (income < 25)) 
                {
                    stockUp = true;
                    turnsNotBuilding++;
                    buildSlanderer = true;
                } 
                else if (!isItSafe()) 
                {
                    turnsNotBuilding++;
                    stockUp = true;
                    // MapLocation enemyRobotOverTwentyInfluenceNearby = getClosestEnemyRobotOverTwentyInfluenceLocation();
                    // Communication.sendLocation(enemyRobotOverTwentyInfluenceNearby, KILL_ENEMY_TARGET); //TODO: Implement this?
                }
            } 

            if (buildSlanderer) 
            {
                buildEconomy();
            }            
            
            //#region Special Builds
            // Self Empower
            if (robotCurrentInfluence < AMOUNT_OF_INFLUENCE_TO_NOT_EMPOWER_SELF 
                && empowerFactor > 3 && isItSafe()
                && !buildThisTurn)
            {
                RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
                RobotBuilder.influenceToUse = (int) (robotController.getInfluence() * .90);
                RobotBuilder.robotTypeToBuild = RobotType.POLITICIAN;

                turnsNotBuilding = 0;
                buildThisTurn = true;
            }             

            // After Mid game build more polis
            if (robotController.getRoundNum() >= MIDDLE_GAME_ROUND_START 
                && robotController.getRobotCount() > 100
                && !stockUp
                && !buildThisTurn
                && isItSafe()) 
            {
                RobotType toBuild = RobotType.POLITICIAN;

                int influence = 0;
                if ((lastTurnInfluence * .25) > MAX_NORMAL_POLITICIAN) 
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
    }

    protected static void buildEconomy() throws GameActionException
    {
        if (buildSlanderer
                && isItSafeForSlanderer()
                && isItSafe()
                && ((countOfSlanderer < 2 && robotController.getRoundNum() < 25) 
                    || (enoughDefenderPoliticianNearby()))
                && !buildThisTurn) 
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
        else if (buildSlanderer 
            && isItSafe() 
            && !stockUp
            && (!enoughDefenderPoliticianNearby() || !isItSafeForSlanderer()))
        {
            RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
            int min = POLITICIAN_SCOUT + 1;
            int max = POLITICIAN_DEFEND_SLANDERER;
            int influence = randomInteger.nextInt(max - min + 1) + min;
            RobotBuilder.influenceToUse = influence;
            RobotBuilder.robotTypeToBuild = RobotType.POLITICIAN;

            if (robotCurrentInfluence >= influence)
            {
                turnsNotBuilding = 0;
                buildThisTurn = true;
                countOfDefenderPolitician++;
            }            
        } 
    }

    protected static void buildEnemyEnlightenmentCenterBomb()
    {

    }

    protected static void buildScouts() throws GameActionException
    {
        if (countOfMuckrakers <= numberOfMuckrakersToCreateInBeginning
            && robotController.getRoundNum() <= BEGINNING_ROUND_STRAT 
            && !weSpawnedRightNextToEnemy
            && !buildThisTurn) 
        {
            RobotBuilder.directionToSpawn = getDirectionToScout(); //getAvailableDirectionToSpawn();
            RobotBuilder.influenceToUse = INFLUENCE_FOR_SCOUT;
            RobotBuilder.robotTypeToBuild = RobotType.MUCKRAKER;

            turnsNotBuilding = 0;
            buildThisTurn = true;
            builtScoutLastTurn = true;
            countOfMuckrakers++; // TODO: need actual count...
        } 

         // Build a scout if stocking up
         if (stockUp || !buildThisTurn)
         {
             RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
             RobotBuilder.influenceToUse =  (int) (robotController.getInfluence() * .01) >= 1 && robotController.getRoundNum() <= BEGINNING_ROUND_STRAT ? 
                (int) (robotController.getInfluence() * .01) : INFLUENCE_FOR_SCOUT;
             RobotBuilder.robotTypeToBuild = RobotType.MUCKRAKER;
 
             builtScoutLastTurn = true;
             turnsNotBuilding = 0;
             buildThisTurn = true;
             countOfMuckrakers++;
         }

        // if (!stockUp 
        //     && countOfPoliticians < countOfMuckrakers
        //     && !buildThisTurn) 
        //     {
        //         RobotType toBuild = RobotType.POLITICIAN;
        //         int influence = 0;
        //         if (lastTurnInfluence >= MIN_NORMAL_POLITICIAN) {
        //             int min = MIN_NORMAL_POLITICIAN;
        //             int max = MAX_NORMAL_POLITICIAN;
        //             influence = randomInteger.nextInt(max - min + 1) + min;

        //             turnsNotBuilding = 0;
        //             if (robotCurrentInfluence > influence && influence != 0) 
        //             {
        //                 buildThisTurn = true;
        //                 RobotBuilder.influenceToUse = influence;
        //                 RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
        //                 RobotBuilder.robotTypeToBuild = toBuild;
        //                 countOfPoliticians++;
        //             }

        //             if (influence <= POLITICIAN_DEFEND_SLANDERER && influence >= POLITICIAN_SCOUT) 
        //             {
        //                 countOfDefenderPolitician++;    
        //             }
        //         }
        //     }       
    }

    protected static void buildDefenders()
    {

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
