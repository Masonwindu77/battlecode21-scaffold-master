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
    public static void decideWhatToBuild() throws GameActionException
    {
        if (robotController.getRoundNum() < 5 && !weSpawnedRightNextToEnemy) 
        {
            RobotType firstBuild = RobotType.SLANDERER;
            int firstInfluence = getAmountNeededForSlandererInBeginning();

            turnsNotBuilding = 0;
            buildThisTurn = true;
            RobotBuilder.robotTypeToBuild = firstBuild;
            RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
            RobotBuilder.influenceToUse = firstInfluence;
            countOfSlanderer++;
        }       
        else if (robotCurrentInfluence < AMOUNT_OF_INFLUENCE_TO_NOT_EMPOWER_SELF && empowerFactor > 10 && isItSafe())
        {
            RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
            RobotBuilder.influenceToUse = (int) (robotController.getInfluence() * .90);
            RobotBuilder.robotTypeToBuild = RobotType.POLITICIAN;

            turnsNotBuilding = 0;
            buildThisTurn = true;
        }  
        else if (robotController.getRoundNum() >= MIDDLE_GAME_ROUND_START 
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
        } 
        else if (countOfMuckrakers <= numberOfMuckrakersToCreateInBeginning
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
        else if (buildSlanderer && isItSafeForSlanderer() && notEnoughDefenderPoliticianNearby()) 
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
                amountNeededForSlanderer = 0;
            }
        } 
        else if (friendlySlandererNearby && !notEnoughDefenderPoliticianNearby())
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
                // MapLocation enemyRobotOverTwentyInfluenceNearby = getClosestEnemyRobotOverTwentyInfluenceLocation();
                // Communication.sendLocation(enemyRobotOverTwentyInfluenceNearby, KILL_ENEMY_TARGET); //TODO: Implement this?
            }
        } 
        else if (!enemyEnlightenmentCenterFound && neutralEnlightenmentCenterFound) 
        {
            if (robotCurrentInfluence >= (POLITICIAN_EC_BOMB + 80) && isItSafe()) 
            {
                RobotBuilder.directionToSpawn = getAvailableDirectionToSpawn();
                RobotBuilder.influenceToUse = POLITICIAN_EC_BOMB + 80;
                RobotBuilder.robotTypeToBuild = RobotType.POLITICIAN;

                turnsNotBuilding = 0;
                stockUp = false;
                buildThisTurn = true;
            } 
            else if (!(robotCurrentInfluence >= (POLITICIAN_EC_BOMB + 80)) && isItSafe()) 
            {
                stockUp = true;
                turnsNotBuilding++;
            }
        }
        else if (!stockUp && countOfPoliticians < countOfMuckrakers) 
        {
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

                if (influence <= POLITICIAN_DEFEND_SLANDERER && influence >= POLITICIAN_SCOUT) 
                {
                    countOfDefenderPolitician++;    
                }
            }
        }

        // Build a scout if stocking up
        if (stockUp || !buildThisTurn)
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
