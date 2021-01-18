package testPlayerv01.Service;

import battlecode.common.*;
import testPlayerv01.RobotPlayer;

public class Communication extends RobotPlayer
{

    // Sending Flags
    static final int NBITS = 7;
    static final int BITMASK = (1 << NBITS) - 1;
    static final int[][] translateCoordinates = { 
        { -(1 << NBITS), 0 }
        ,{ (1 << NBITS), 0 }
        ,{ 0, -(1 << NBITS) }
        ,{ 0, (1 << NBITS) } };

    public static void announceEnemyEnlightenmentCenterCurrentInfluence(int extraInformation) throws GameActionException 
    {
        int encodedflag = (extraInformation << (2 * NBITS)) + enemyEnlightenmentCenterCurrentInfluence;

        if (robotController.canSetFlag(encodedflag)) {
            robotController.setFlag(encodedflag);
        }
    }

    public static void checkIfSpawnEnlightenmentCenterHasEnemyLocation() throws GameActionException {
        if (robotController.canGetFlag(spawnEnlightenmentCenterRobotId)) 
        {
            currentEnlightenmentCenterFlag = robotController.getFlag(spawnEnlightenmentCenterRobotId);

            if (currentEnlightenmentCenterFlag != 0 && checkIfEnemeyEnlightenmentCenterHasBeenFound(currentEnlightenmentCenterFlag)) 
            {
                MapLocation enemyEnlightenmentCenterLocation = getLocationFromFlag(currentEnlightenmentCenterFlag);

                if (!enemyEnlightenmentCenterMapLocation.contains(enemyEnlightenmentCenterLocation) 
                    || enemyEnlightenmentCenterMapLocation.isEmpty()) 
                {
                    enemyEnlightenmentCenterMapLocation.add(enemyEnlightenmentCenterLocation);
                    enemyEnlightenmentCenterFound = true;
                    currentEnemyEnlightenmentCenterGoingFor = enemyEnlightenmentCenterLocation;
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

    public static void checkIfSpawnEnlightenmentCenterHasNeutralLocation() throws GameActionException 
    {
        if (robotController.canGetFlag(spawnEnlightenmentCenterRobotId)) 
        {
            currentEnlightenmentCenterFlag = robotController.getFlag(spawnEnlightenmentCenterRobotId);

            if (currentEnlightenmentCenterFlag != 0 && checkIfNeutralEnlightenmentCenterHasBeenFound(currentEnlightenmentCenterFlag)) 
            {
                MapLocation neutralEnlightenmentCenterLocation = getLocationFromFlag(currentEnlightenmentCenterFlag);

                if (!neutralEnlightenmentCenterMapLocation.contains(neutralEnlightenmentCenterLocation) 
                    || neutralEnlightenmentCenterMapLocation.isEmpty()) 
                {
                    neutralEnlightenmentCenterMapLocation.add(neutralEnlightenmentCenterLocation);
                    neutralEnlightenmentCenterFound = true;
                    currentNeutralEnlightenmentCenterGoingFor = neutralEnlightenmentCenterLocation;
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

    

    public static void announceEnemyEnlightenmentCenterHasBeenConverted() throws GameActionException {
        sendLocation(currentEnemyEnlightenmentCenterGoingFor, ENEMY_ENLIGHTENMENT_CENTER_CONVERTED);
    }

    public static boolean checkRobotFlagForEnemyECInfluence(int flag) {
        boolean sentEnlightenmentInfluence = false;
        int extraInformation = flag >> (2 * Communication.NBITS);

        if (extraInformation == ENEMY_ENLIGHTENMENT_CENTER_INFLUENCE) {
            sentEnlightenmentInfluence = true;
        }

        return sentEnlightenmentInfluence;
    }

    // TODO: Still nee dto use this
    public static int getEnemyEnlightenmentCenterInfluenceFromFlag(int flag) throws GameActionException {
        int enemyEnlightenmentCenterCurrentInfluence = flag - (ENEMY_ENLIGHTENMENT_CENTER_INFLUENCE << (2 * NBITS));

        return enemyEnlightenmentCenterCurrentInfluence;
    }

    public static boolean checkIfEnemeyEnlightenmentCenterHasBeenConverted(int flag) throws GameActionException {
        boolean hasBeenConverted = false;
        int extraInformation = flag >> (2 * NBITS);

        if (extraInformation == ENEMY_ENLIGHTENMENT_CENTER_CONVERTED) {
            hasBeenConverted = true;
        }

        return hasBeenConverted;
    }

    public static boolean hasEnemyEnlightenmentCenterBeenConverted() throws GameActionException 
    {
        boolean hasBeenConverted = false;
        if (robotController.canSenseLocation(currentEnemyEnlightenmentCenterGoingFor)) 
        {
            RobotInfo[] robots = robotController.senseNearbyRobots();
            for (RobotInfo robotInfo : robots) 
            {
                if (robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER && robotInfo.getTeam() == friendly
                        && robotInfo.getLocation() == currentEnemyEnlightenmentCenterGoingFor) 
                {
                    hasBeenConverted = true;
                }
            }
        }

        return hasBeenConverted;
    }

    public static boolean hasEnemyEnlightenmentCenterBeenConverted(RobotInfo robotInfo) throws GameActionException
    {
        boolean hasBeenConverted = false;
        if (currentEnemyEnlightenmentCenterGoingFor == robotInfo.getLocation() 
            && robotInfo.getTeam() == friendly
            && robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER) 
            {
                hasBeenConverted = true;
            }
        return hasBeenConverted;
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

                if (currentLocation.distanceSquaredTo(alternative) < currentLocation
                        .distanceSquaredTo(actualLocation)) {
                    actualLocation = alternative;
                }
            }
        }

        return actualLocation;
    }
}
