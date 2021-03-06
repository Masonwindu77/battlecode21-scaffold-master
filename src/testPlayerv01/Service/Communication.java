package testPlayerv01.Service;

import battlecode.common.*;
import testPlayerv01.EnlightenmentCenterTest01;
import testPlayerv01.MuckrakerTest01;

public class Communication extends EnlightenmentCenterTest01
{

    // Sending Flags
    static final int NBITS = 7;
    static final int BITMASK = (1 << NBITS) - 1;
    static final int[][] translateCoordinates = { 
        { -(1 << NBITS), 0 }
        ,{ (1 << NBITS), 0 }
        ,{ 0, -(1 << NBITS) }
        ,{ 0, (1 << NBITS) } };

    // Max bit is 2^24
    // 10000 to 30000
    // Signals
    public static final int SLANDERER_FLAG = 10000000;
    public static final int SLANDERER_IN_TROUBLE_FLAG = 100000001;
    public static final int ENEMY_ENLIGHTENMENT_CENTER_FOUND = 11;
    public static final int KILL_ENEMY_TARGET = 12;
    public static final int ENEMY_ENLIGHTENMENT_CENTER_CONVERTED = 13;
    public static final int ENEMY_ENLIGHTENMENT_CENTER_INFLUENCE = 14;
    public static final int NUETRAL_ENLIGHTENMENT_CENTER_FOUND = 15;
    public static final int NUETRAL_ENLIGHTENMENT_CENTER_INFLUENCE = 16;
    public static final int NUETRAL_ENLIGHTENMENT_CENTER_CONVERTED = 17;
    public static final int ENEMY_MUCKRAKER_NEARBY_FLAG = 18;
    public static final int EDGE_OF_MAP_HAS_BEEN_FOUND_FLAG = 19;

    public static void setFlagMessageForScout() throws GameActionException 
    {
        if (enemyEnlightenmentCenterFound 
            && turnsAroundEnemyEnlightenmentCenter < 5
            && enemyEnlightenmentCenterIsAround) 
        {
            Communication.announceEnemyEnlightenmentCenterLocation();
            if (turnsAroundEnemyEnlightenmentCenter < 5) 
            {
                haveMessageToSend = false;
            }            
        } 
        else if (neutralEnlightenmentCenterHasBeenConverted) 
        {
            Communication.announceNeutralEnlightenmentCenterHasBeenConverted();
            messageLastTwoTurnsForConverted--;
            if (messageLastTwoTurnsForConverted == 0) 
            {
                haveMessageToSend = false;
                neutralEnlightenmentCenterHasBeenConverted = false;  
            } 
        }
        else if (enemyEnlightenmentCenterIsAround) 
        {
            Communication.announceEnemyEnlightenmentCenterCurrentInfluence(Communication.ENEMY_ENLIGHTENMENT_CENTER_INFLUENCE);
            haveMessageToSend = false;
        } 
        else if (enemyEnlightenmentCenterHasBeenConverted) 
        {
            Communication.announceEnemyEnlightenmentCenterHasBeenConverted();

            messageLastTwoTurnsForConverted--;
            if (messageLastTwoTurnsForConverted == 0) 
            {
                haveMessageToSend = false;
            }           
        }
        else if (neutralEnlightenmentCenterFound 
            && turnsAroundNeutralEnlightenmentCenter < 3
            && neutralEnlightenmentCenterIsAround) 
        {
            Communication.announceNeutralEnlightenmentCenterLocation(); 
            haveMessageToSend = false;            
        }
        else if (neutralEnlightenmentCenterIsAround 
            && turnsAroundNeutralEnlightenmentCenter < 5) 
        {
            Communication.announceNeutralEnlightenmentCenterCurrentInfluence(Communication.NUETRAL_ENLIGHTENMENT_CENTER_INFLUENCE);
            haveMessageToSend = false;
        }
        else if (MuckrakerTest01.enemyMuckrakerMapLocation != null)
        {
            Communication.sendLocation(MuckrakerTest01.enemyMuckrakerMapLocation, ENEMY_MUCKRAKER_NEARBY_FLAG);
            haveMessageToSend = false;
        }
        
    }

    public static void announceEnemyEnlightenmentCenterCurrentInfluence(int extraInformation) throws GameActionException 
    {
        int encodedflag = (extraInformation << (2 * NBITS)) + enemyEnlightenmentCenterCurrentInfluence;

        if (robotController.canSetFlag(encodedflag)) {
            robotController.setFlag(encodedflag);
        }
    }

    public static void announceNeutralEnlightenmentCenterCurrentInfluence(int extraInformation) throws GameActionException 
    {
        int encodedflag = (extraInformation << (2 * NBITS)) + neutralEnlightenmentCenterCurrentInfluence;

        if (robotController.canSetFlag(encodedflag)) {
            robotController.setFlag(encodedflag);
        }
    }

    public static void announceEnemyEnlightenmentCenterLocation() throws GameActionException 
    {
        Communication.sendLocation(enemyEnlightenmentCenterMapLocation.get(0), Communication.ENEMY_ENLIGHTENMENT_CENTER_FOUND);
    }

    public static void announceEnemyEnlightenmentCenterHasBeenConverted() throws GameActionException 
    {
        sendLocation(convertedEnemyEnlightenmentCenterMapLocation.get(convertedEnemyIterator - 1), ENEMY_ENLIGHTENMENT_CENTER_CONVERTED);
    }

    public static void announceNeutralEnlightenmentCenterHasBeenConverted() throws GameActionException 
    {
        sendLocation(convertedNeutralEnlightenmentCenterMapLocation.get(convertedNeutralIterator - 1), NUETRAL_ENLIGHTENMENT_CENTER_CONVERTED);
    }

    public static void announceNeutralEnlightenmentCenterLocation() throws GameActionException 
    {
        sendLocation(neutralEnlightenmentCenterMapLocation.get(neutralEnlightenmentCenterMapLocation.size() - 1), NUETRAL_ENLIGHTENMENT_CENTER_FOUND);
    }

    public static void checkIfFriendlyEnlightenmentCenterHasEnemyLocation() throws GameActionException 
    {
        for (Integer friendlyECrobotId : friendlyEnlightenmentCenterRobotIds) 
        {
            if (robotController.canGetFlag(friendlyECrobotId)) 
            {
                currentEnlightenmentCenterFlag = robotController.getFlag(friendlyECrobotId);

                if (currentEnlightenmentCenterFlag != 0 && checkIfEnemeyEnlightenmentCenterHasBeenFound(currentEnlightenmentCenterFlag)) 
                {
                    MapLocation enemyEnlightenmentCenterLocation = getLocationFromFlag(currentEnlightenmentCenterFlag);

                    if ((!enemyEnlightenmentCenterMapLocation.contains(enemyEnlightenmentCenterLocation) 
                        || enemyEnlightenmentCenterMapLocation.isEmpty())
                    && (convertedEnemyEnlightenmentCenterMapLocation.isEmpty() 
                        || !convertedEnemyEnlightenmentCenterMapLocation.contains(enemyEnlightenmentCenterLocation))) 
                    {
                        enemyEnlightenmentCenterMapLocation.add(enemyEnlightenmentCenterLocation);
                        enemyEnlightenmentCenterFound = true;
                        enemyCurrentEnlightenmentCenterGoingFor = enemyEnlightenmentCenterLocation;
                    }
                }
            }
        }        
    }

    public static boolean checkIfEnemeyEnlightenmentCenterHasBeenFound(int flag) throws GameActionException {
        boolean foundTheEnlightenmentCenter = false;
        int extraInformation = flag >> (2 * NBITS);

        if (extraInformation == ENEMY_ENLIGHTENMENT_CENTER_FOUND) {
            foundTheEnlightenmentCenter = true;
        }

        return foundTheEnlightenmentCenter;
    }

    protected static boolean processEnemyEnlightenmentCenterHasBeenFound(int flag) throws GameActionException
    {
        MapLocation enemyEnlightenmentCenterLocation = Communication.getLocationFromFlag(flag);
        boolean newEntry = false;

        if (enemyEnlightenmentCenterMapLocation.isEmpty() 
            || !enemyEnlightenmentCenterMapLocation.contains(enemyEnlightenmentCenterLocation))
        {
            enemyEnlightenmentCenterMapLocation.add(enemyEnlightenmentCenterLocation);
            enemyEnlightenmentCenterFound = true;
            newEntry = true;
            countOfEnemyPoliticianBomb = 0; 
        }

        if (!convertedEnemyEnlightenmentCenterMapLocation.isEmpty()
            && convertedEnemyEnlightenmentCenterMapLocation.contains(enemyEnlightenmentCenterLocation)) 
        {
            convertedEnemyEnlightenmentCenterMapLocation.removeIf(n -> n.equals(enemyEnlightenmentCenterLocation));
            convertedEnemyIterator--;
        }

        return newEntry;
    }

    public static void checkIfFriendlyEnlightenmentCenterHasNeutralLocation() throws GameActionException 
    {
        for (Integer friendlyECrobotId : friendlyEnlightenmentCenterRobotIds) 
        {
            if (robotController.canGetFlag(friendlyECrobotId)) 
            {
                currentEnlightenmentCenterFlag = robotController.getFlag(friendlyECrobotId);

                if (currentEnlightenmentCenterFlag != 0 && checkIfNeutralEnlightenmentCenterHasBeenFound(currentEnlightenmentCenterFlag)) 
                {
                    MapLocation neutralEnlightenmentCenterLocation = getLocationFromFlag(currentEnlightenmentCenterFlag);

                    if (neutralEnlightenmentCenterMapLocation.isEmpty() 
                            || !neutralEnlightenmentCenterMapLocation.contains(neutralEnlightenmentCenterLocation)
                        && convertedNeutralEnlightenmentCenterMapLocation.isEmpty() 
                            || !convertedNeutralEnlightenmentCenterMapLocation.contains(neutralEnlightenmentCenterLocation)) 
                    {
                        neutralEnlightenmentCenterMapLocation.add(neutralEnlightenmentCenterLocation);
                        neutralEnlightenmentCenterFound = true;
                        neutralCurrentEnlightenmentCenterGoingFor = neutralEnlightenmentCenterLocation;
                    }
                }
            }
        }   
    }

    public static boolean checkIfNeutralEnlightenmentCenterHasBeenFound(int flag) 
    {
        boolean foundNeutralCenter = false;
        int extraInformation = flag >> (2 * NBITS);

        if (extraInformation == NUETRAL_ENLIGHTENMENT_CENTER_FOUND) {
            foundNeutralCenter = true;
        }

        return foundNeutralCenter;
    }

    public static int getExtraInformationFromFlag(int flag)
    {
        return flag >> (2 * NBITS);
    }    

    public static boolean checkRobotFlagForEnemyECInfluence(int flag) 
    {
        boolean sentEnlightenmentInfluence = false;
        int extraInformation = flag >> (2 * Communication.NBITS);

        if (extraInformation == ENEMY_ENLIGHTENMENT_CENTER_INFLUENCE) {
            sentEnlightenmentInfluence = true;
        }

        return sentEnlightenmentInfluence;
    }

    public static int getEnemyEnlightenmentCenterInfluenceFromFlag(int flag) throws GameActionException 
    {
        int enemyEnlightenmentCenterCurrentInfluence = flag - (ENEMY_ENLIGHTENMENT_CENTER_INFLUENCE << (2 * NBITS));

        return enemyEnlightenmentCenterCurrentInfluence;
    }
    
    public static int getNeutralEnlightenmentCenterInfluenceFromFlag(int flag) 
    {
        int neutralEnlightenmentCenterCurrentInfluence = flag - (NUETRAL_ENLIGHTENMENT_CENTER_INFLUENCE << (2 * NBITS));

        return neutralEnlightenmentCenterCurrentInfluence;
    }

    public static void checkIfFriendlyEnlightenmentCenterHasEnemyLocationConverted() throws GameActionException
    {
        for (Integer friendlyECrobotId : friendlyEnlightenmentCenterRobotIds) 
        {
            if (robotController.canGetFlag(friendlyECrobotId)) 
            {
                currentEnlightenmentCenterFlag = robotController.getFlag(friendlyECrobotId);

                if (currentEnlightenmentCenterFlag != 0 && checkIfEnemeyEnlightenmentCenterHasBeenConverted(currentEnlightenmentCenterFlag)) 
                {
                    MapLocation convertedEnemyLocation = getLocationFromFlag(currentEnlightenmentCenterFlag);

                    if ((convertedEnemyEnlightenmentCenterMapLocation.isEmpty() 
                        || !convertedEnemyEnlightenmentCenterMapLocation.contains(convertedEnemyLocation)) 
                        && enemyCurrentEnlightenmentCenterGoingFor.equals(convertedEnemyLocation)) 
                    {
                        processEnemyEnlightenmentCenterHasBeenConverted(convertedEnemyLocation);
                    }
                }
            }
        }
    }

    public static boolean checkIfEnemeyEnlightenmentCenterHasBeenConverted(int flag) throws GameActionException {
        boolean hasBeenConverted = false;
        int extraInformation = flag >> (2 * NBITS);

        if (extraInformation == ENEMY_ENLIGHTENMENT_CENTER_CONVERTED) {
            hasBeenConverted = true;
        }

        return hasBeenConverted;
    }

    public static boolean hasEnemyEnlightenmentCenterBeenConverted(RobotInfo robotInfo) throws GameActionException
    {
        boolean hasBeenConverted = false;
        if (enemyCurrentEnlightenmentCenterGoingFor != null 
            && enemyCurrentEnlightenmentCenterGoingFor.equals(robotInfo.getLocation())
            && robotInfo.getTeam() == friendly
            && robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER) 
            {
                hasBeenConverted = true;
            }
        return hasBeenConverted;
    }

    public static void checkIfFriendlyEnlightenmentCenterHasNeutralLocationConverted() throws GameActionException
    {
        for (Integer friendlyECrobotId : friendlyEnlightenmentCenterRobotIds) 
        {
            if (robotController.canGetFlag(friendlyECrobotId)) 
            {
                currentEnlightenmentCenterFlag = robotController.getFlag(friendlyECrobotId);

                if (currentEnlightenmentCenterFlag != 0 && checkIfNeutralEnlightenmentCenterHasBeenConverted(currentEnlightenmentCenterFlag)) 
                {
                    MapLocation convertedNeutralLocation = getLocationFromFlag(currentEnlightenmentCenterFlag);

                    if ((convertedNeutralEnlightenmentCenterMapLocation.isEmpty() 
                        || !convertedNeutralEnlightenmentCenterMapLocation.contains(convertedNeutralLocation)) 
                        && neutralCurrentEnlightenmentCenterGoingFor.equals(convertedNeutralLocation)) 
                    {
                        processNeutralEnlightenmentCenterHasBeenConverted(convertedNeutralLocation);
                    }
                }
            }
        }
    }

    public static boolean checkIfNeutralEnlightenmentCenterHasBeenConverted(int flag) throws GameActionException {
        boolean hasBeenConverted = false;
        int extraInformation = flag >> (2 * NBITS);

        if (extraInformation == NUETRAL_ENLIGHTENMENT_CENTER_CONVERTED) {
            hasBeenConverted = true;
        }

        return hasBeenConverted;
    }

    public static boolean hasNeutralEnlightenmentCenterBeenConverted(RobotInfo robotInfo) throws GameActionException
    {
        boolean hasBeenConverted = false;
        if (neutralCurrentEnlightenmentCenterGoingFor != null 
            && neutralCurrentEnlightenmentCenterGoingFor.equals(robotInfo.getLocation())
            && (robotInfo.getTeam() == friendly || robotInfo.getTeam() == enemy)) 
            {
                hasBeenConverted = true;
            }
        return hasBeenConverted;
    }

    public static void checkIfFriendlyEnlightenmentCenterHasEdgeOfMapLocation() throws GameActionException 
    {
        for (Integer friendlyECrobotId : friendlyEnlightenmentCenterRobotIds) 
        {
            if (robotController.canGetFlag(friendlyECrobotId)) 
            {
                currentEnlightenmentCenterFlag = robotController.getFlag(friendlyECrobotId);

                if (currentEnlightenmentCenterFlag != 0 && checkIfEdgeOfMapLocated(currentEnlightenmentCenterFlag)) 
                {
                    MapLocation convertedNeutralLocation = getLocationFromFlag(currentEnlightenmentCenterFlag);

                    if ((convertedNeutralEnlightenmentCenterMapLocation.isEmpty() 
                        || !convertedNeutralEnlightenmentCenterMapLocation.contains(convertedNeutralLocation)) 
                        && neutralCurrentEnlightenmentCenterGoingFor.equals(convertedNeutralLocation)) 
                    {
                        processNeutralEnlightenmentCenterHasBeenConverted(convertedNeutralLocation);
                    }
                }
            }
        }
	}

    private static boolean checkIfEdgeOfMapLocated(int currentEnlightenmentCenterFlag) 
    {
        boolean foundEdgeOfMap = false;
        int extraInformation = currentEnlightenmentCenterFlag >> (2 * NBITS);

        if (extraInformation == EDGE_OF_MAP_HAS_BEEN_FOUND_FLAG) 
        {
            foundEdgeOfMap = true;
        }

        return foundEdgeOfMap;
    }

    public static boolean processNeutralEnlightenmentCenterHasBeenFound(int flag) throws GameActionException
    {
        MapLocation neutralEnlightenmentCenterLocation = Communication.getLocationFromFlag(flag);
        boolean newEntry = false;

        if ((neutralEnlightenmentCenterMapLocation.isEmpty() 
            || !neutralEnlightenmentCenterMapLocation.contains(neutralEnlightenmentCenterLocation))
        && (convertedNeutralEnlightenmentCenterMapLocation.isEmpty() 
            || !convertedNeutralEnlightenmentCenterMapLocation.contains(neutralEnlightenmentCenterLocation))) 
        {
            neutralEnlightenmentCenterMapLocation.add(neutralEnlightenmentCenterLocation);
            neutralEnlightenmentCenterFound = true;
            countOfNeutralPoliticianBomb = 0;    
            newEntry = true;
        }

        return newEntry;
    }

    public static void processEnemyEnlightenmentCenterHasBeenConverted(MapLocation convertedEnemyLocation) throws GameActionException 
    {
        if (!enemyEnlightenmentCenterMapLocation.isEmpty() 
            && (convertedEnemyEnlightenmentCenterMapLocation.isEmpty()
            || !convertedEnemyEnlightenmentCenterMapLocation.contains(convertedEnemyLocation))) 
        {
            convertedEnemyEnlightenmentCenterMapLocation.add(convertedEnemyLocation);
            enemyEnlightenmentCenterMapLocation.removeIf(n -> n.equals(convertedEnemyLocation));
            convertedEnemyIterator++;
            enemyEnlightenmentCenterFound = false; 
            enemyEnlightenmentCenterHasBeenConverted = true;      
            convertedEnemyEnlightenmentCenterHasBeenProcessedThisTurn = true;   
            countOfEnemyPoliticianBomb = 0;               
        }              
    }

    public static void processNeutralEnlightenmentCenterHasBeenConverted(MapLocation neutralConvertedMapLocation) throws GameActionException 
    {
        if ((convertedNeutralEnlightenmentCenterMapLocation.isEmpty()
            || !convertedNeutralEnlightenmentCenterMapLocation.contains(neutralConvertedMapLocation))) 
        {
            convertedNeutralEnlightenmentCenterMapLocation.add(neutralConvertedMapLocation);
            convertedNeutralIterator++;
        }
         
        if (!neutralEnlightenmentCenterMapLocation.isEmpty()) 
        {
            neutralEnlightenmentCenterMapLocation.removeIf(n -> n.equals(neutralConvertedMapLocation));                
            countOfNeutralPoliticianBomb = 0;  
        }           
        
        neutralEnlightenmentCenterFound = false; 
        neutralEnlightenmentCenterHasBeenConverted = true; 
        neutralCurrentEnlightenmentCenterGoingFor = null;   
    }

    public static void sendLocation(MapLocation location, int extraInformation) throws GameActionException {
        int x = location.x;
        int y = location.y;
        int encodedLocation = (extraInformation << (2 * NBITS)) + ((x & BITMASK) << NBITS) + (y & BITMASK);

        if (robotController.canSetFlag(encodedLocation)) {
            robotController.setFlag(encodedLocation);
        }
    }

    public static MapLocation getLocationFromFlag(int flag) throws GameActionException {
        int y = flag & BITMASK;
        int x = (flag >> NBITS) & BITMASK;
        MapLocation currentLocation = robotController.getLocation();

        int offsetX128 = currentLocation.x >> NBITS;
        int offsetY128 = currentLocation.y >> NBITS;

        MapLocation actualLocation = new MapLocation((offsetX128 << NBITS) + x, (offsetY128 << NBITS) + y);
        MapLocation alternative = actualLocation;

        for (int iterator = 0; iterator < translateCoordinates.length; ++iterator) {
            for (int innerIterator = 0; innerIterator < 1; ++innerIterator) {
                alternative = actualLocation.translate(translateCoordinates[iterator][innerIterator],
                        translateCoordinates[iterator][innerIterator + 1]);

                if (currentLocation.distanceSquaredTo(alternative) < currentLocation.distanceSquaredTo(actualLocation)) {
                    actualLocation = alternative;
                }
            }
        }

        return actualLocation;
    }
    
}
