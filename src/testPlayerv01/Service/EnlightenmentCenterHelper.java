package testPlayerv01.Service;

import testPlayerv01.EnlightenmentCenterTest01;
import battlecode.common.*;

public class EnlightenmentCenterHelper extends EnlightenmentCenterTest01
{

    public static void checkFlagsForSignals(int extraInformation, int flag) throws GameActionException
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
            Communication.processNeutralEnlightenmentCenterHasBeenFound(flag);
        }
        else if (checkIfRobotSignallingNeutralEnlightenmentCenterInfluence(extraInformation)) 
        {
            neutralEnlightenmentCenterCurrentInfluence = Communication.getNeutralEnlightenmentCenterInfluenceFromFlag(flag);
        }
        else if (checkIfRobotSignallingNeutralEnlightenmentCenterConverted(extraInformation))
        {
            Communication.processNeutralEnlightenmentCenterHasBeenConverted(Communication.getLocationFromFlag(flag));
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

    // TODO: Notes for 1/20/21 -- I need more scouts I believe. 
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

            
            if (buildSlanderer) 
            {
                buildEconomy();
            }            
            
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

            // Build EC Bomb for Enemy Enlightenment Center
            if (enemyEnlightenmentCenterFound
                && !buildThisTurn) 
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
                    && shouldBuildSlanderer()) 
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

            if (neutralEnlightenmentCenterFound
                && !buildThisTurn) 
            {
                if (isThereEnoughForNeutralEnlightenmentCenterCapture() && isItSafe()) 
                {
                    RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
                    RobotBuilder.influenceToUse = getAmountToMakePoliticianBombForNeutral();
                    RobotBuilder.robotTypeToBuild = RobotType.POLITICIAN;

                    turnsNotBuilding = 0;
                    stockUp = false;
                    buildThisTurn = true;
                } 
                else if (!isThereEnoughForNeutralEnlightenmentCenterCapture() 
                    && isItSafe()) 
                {
                    stockUp = true;
                    buildSlanderer = true;
                    turnsNotBuilding++;
                }
            }   

            if (robotController.getRoundNum() >= MIDDLE_GAME_ROUND_START 
                && robotController.getRobotCount() > 150
                && !stockUp
                && !buildThisTurn) 
            {
                RobotType toBuild = RobotType.POLITICIAN;

                int influence = 0;
                if (lastTurnInfluence > MAX_NORMAL_POLITICIAN) 
                {
                    int max = lastTurnInfluence;
                    int min = MAX_NORMAL_POLITICIAN;
                    influence = randomInteger.nextInt(max - min + 1) + min;

                    turnsNotBuilding = 0;
                    if (robotCurrentInfluence >= influence * 1.1) 
                    {
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
            }

            buildScouts();
        }
    }

    protected static void buildEconomy() throws GameActionException
    {
        if (buildSlanderer
                && isItSafeForSlanderer() 
                && enoughDefenderPoliticianNearby()
                && !buildThisTurn) 
        {
            if (robotCurrentInfluence < amountNeededForSlanderer) 
            {
                stockUp = true;
            } 
            else 
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
        else if (buildSlanderer && !enoughDefenderPoliticianNearby())
        {
            RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
            int min = POLITICIAN_SCOUT + 1;
            int max = POLITICIAN_DEFEND_SLANDERER;
            int influence = randomInteger.nextInt(max - min + 1) + min;
            RobotBuilder.influenceToUse = influence;
            RobotBuilder.robotTypeToBuild = RobotType.POLITICIAN;

            turnsNotBuilding = 0;
            buildThisTurn = true;
            countOfDefenderPolitician++;
        } 
        else if(!isItSafeForSlanderer())
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

        if (!stockUp 
            && countOfPoliticians < countOfMuckrakers
            && !buildThisTurn) 
            {
                RobotType toBuild = RobotType.POLITICIAN;
                int influence = 0;
                if (lastTurnInfluence >= MIN_NORMAL_POLITICIAN) {
                    int min = MIN_NORMAL_POLITICIAN;
                    int max = MAX_NORMAL_POLITICIAN;
                    influence = randomInteger.nextInt(max - min + 1) + min;

                    turnsNotBuilding = 0;
                    if (robotCurrentInfluence > influence && influence != 0) 
                    {
                        buildThisTurn = true;
                        RobotBuilder.influenceToUse = influence;
                        RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
                        RobotBuilder.robotTypeToBuild = toBuild;
                        countOfPoliticians++;
                    }

                    if (influence <= POLITICIAN_DEFEND_SLANDERER && influence >= POLITICIAN_SCOUT) 
                    {
                        countOfDefenderPolitician++;    
                    }
                }
            }

        // Build a scout if stocking up
        if (stockUp && !buildThisTurn)
        {
            RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
            RobotBuilder.influenceToUse =  (int) (robotController.getInfluence() * .01) >= 1 ? (int) (robotController.getInfluence() * .01) : INFLUENCE_FOR_SCOUT;
            RobotBuilder.robotTypeToBuild = RobotType.MUCKRAKER;

            builtScoutLastTurn = true;
            turnsNotBuilding = 0;
            buildThisTurn = true;
            countOfMuckrakers++;
        }
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
