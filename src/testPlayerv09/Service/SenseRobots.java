package testPlayerv09.Service;

import testPlayerv09.RobotPlayer;
import battlecode.common.*;

public class SenseRobots extends RobotPlayer
{
    public static void checkIfRobotCanSenseEnemyEnlightenmentCenter() 
    {
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;

        RobotInfo[] robots = robotController.senseNearbyRobots(sensorRadiusSquared, enemy);

        for (RobotInfo robotInfo : robots) {
            if (robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER
                    && (!enemyEnlightenmentCenterMapLocation.contains(robotInfo.getLocation())
                    || enemyEnlightenmentCenterMapLocation.isEmpty())) 
            {

                enemyEnlightenmentCenterMapLocation.add(robotInfo.getLocation());
                haveMessageToSend = true;
                enemyEnlightenmentCenterFound = true;
                currentEnemyEnlightenmentCenterGoingFor = robotInfo.getLocation();
            }
        }
    }

    public static boolean checkIfPoliticianBombNearby(RobotInfo robotInfo) 
    {
        if (robotInfo.getType() == RobotType.POLITICIAN && robotInfo.getTeam() == friendly
        && robotInfo.getConviction() >= POLITICIAN_EC_BOMB
        && (currentEnemyEnlightenmentCenterGoingFor != null 
        && currentEnemyEnlightenmentCenterGoingFor.distanceSquaredTo(robotInfo.getLocation()) <= 10)) 
        {
            return true;
        } else {
            return false;
        }
    }

    public static void processEnlightenmentCenterFinding(RobotInfo robotInfo) throws GameActionException
    {
        if (robotInfo.getTeam() == enemy) 
        {
            if (enemyEnlightenmentCenterMapLocation.isEmpty() 
                || !enemyEnlightenmentCenterMapLocation.contains(robotInfo.getLocation())) 
            {
                enemyEnlightenmentCenterMapLocation.add(robotInfo.getLocation());
            }

            enemyEnlightenmentCenterCurrentInfluence = robotInfo.getInfluence();
            currentEnemyEnlightenmentCenterGoingFor = robotInfo.getLocation();
            enemyEnlightenmentCenterIsAround = true;
            enemyEnlightenmentCenterFound = true;
            turnsAroundEnemyEnlightenmentCenter++;
            haveMessageToSend = true;
        }
        else if (robotInfo.getTeam() == Team.NEUTRAL) 
        {
            if (neutralEnlightenmentCenterMapLocation.isEmpty() || !neutralEnlightenmentCenterMapLocation.contains(robotInfo.getLocation())) 
            {
                neutralEnlightenmentCenterMapLocation.add(robotInfo.getLocation());
                currentNeutralEnlightenmentCenterGoingFor = robotInfo.getLocation();
                haveMessageToSend = true;
            }

            neutralEnlightenmentCenterCurrentInfluence = robotInfo.getInfluence();
            neutralEnlightenmentCenterFound = true;
            neutralEnlightenmentCenterIsAround = true;
            turnsAroundNeutralEnlightenmentCenter++;
        }

        if (Communication.hasEnemyEnlightenmentCenterBeenConverted(robotInfo)
            && (convertedEnemyEnlightenmentCenterMapLocation.isEmpty() 
            || !convertedEnemyEnlightenmentCenterMapLocation.contains(robotInfo.getLocation())))
        {
            Communication.processEnemyEnlightenmentCenterHasBeenConverted(robotInfo.getLocation());
            enemyEnlightenmentCenterHasBeenConverted = true;
            messageLastTwoTurnsForConverted = 2;
            turnsAroundEnemyEnlightenmentCenter = 0;
            haveMessageToSend = true;
        }

        if (Communication.hasNeutralEnlightenmentCenterBeenConverted(robotInfo)
            && (convertedNeutralEnlightenmentCenterMapLocation.isEmpty() || !convertedNeutralEnlightenmentCenterMapLocation.contains(robotInfo.getLocation())))
        {
            Communication.processNeutralEnlightenmentCenterHasBeenConverted(robotInfo.getLocation());
            neutralEnlightenmentCenterHasBeenConverted = true;
            messageLastTwoTurnsForConverted = 2;
            turnsAroundNeutralEnlightenmentCenter = 0;
            haveMessageToSend = true;
        }

        if (spawnEnlightenmentCenterHomeLocation == null && robotInfo.getTeam() == friendly) 
        {
            spawnEnlightenmentCenterHomeLocation = robotInfo.getLocation();
            spawnEnlightenmentCenterRobotId = robotInfo.getID();
        }
        
    }

    public static void checkForCommunications() throws GameActionException
    {
        if (!enemyEnlightenmentCenterFound) 
        {
            Communication.checkIfSpawnEnlightenmentCenterHasEnemyLocation();
            setCurrentEnemyEnlightenmentCenterGoingFor();
        }

        if (!enemyEnlightenmentCenterHasBeenConverted && enemyEnlightenmentCenterFound) 
        {
            Communication.checkIfSpawnEnlightenmentCenterHasEnemyLocationConverted();
        }

        if (!neutralEnlightenmentCenterFound) 
        {
            Communication.checkIfSpawnEnlightenmentCenterHasNeutralLocation();
            setCurrentNeutralEnlightenmentCenterGoingFor();
        }
    }

    protected static void setCurrentEnemyEnlightenmentCenterGoingFor()
    {
        if (currentEnemyEnlightenmentCenterGoingFor == null && !enemyEnlightenmentCenterMapLocation.isEmpty()) 
        {
            MapLocation enemyCenterLocation = enemyEnlightenmentCenterMapLocation.get(0);
            currentEnemyEnlightenmentCenterGoingFor = enemyCenterLocation; // TODO: can make this cleaner
        }
    }

    protected static void setCurrentNeutralEnlightenmentCenterGoingFor() 
    {
        if (currentNeutralEnlightenmentCenterGoingFor == null && !neutralEnlightenmentCenterMapLocation.isEmpty()) 
        {
            MapLocation neutralCenterLocation = neutralEnlightenmentCenterMapLocation.get(0);
            currentNeutralEnlightenmentCenterGoingFor = neutralCenterLocation; // TODO: can make this cleaner
        }
    }
}
