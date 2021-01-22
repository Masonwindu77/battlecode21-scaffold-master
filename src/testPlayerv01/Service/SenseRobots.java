package testPlayerv01.Service;

import testPlayerv01.RobotPlayer;
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
        if (robotInfo.getType() == RobotType.POLITICIAN 
        && robotInfo.getTeam() == friendly
        && robotInfo.getInfluence() >= POLITICIAN_EC_BOMB
        && ((currentEnemyEnlightenmentCenterGoingFor != null 
            && currentEnemyEnlightenmentCenterGoingFor.distanceSquaredTo(robotInfo.getLocation()) <= 10) 
        || (currentNeutralEnlightenmentCenterGoingFor != null 
            && currentNeutralEnlightenmentCenterGoingFor.distanceSquaredTo(robotInfo.getLocation()) <= 10))) 
        {
            return true;
        } 
        else 
        {
            return false;
        }
    }

    public static void processEnlightenmentCenterFinding(RobotInfo robotInfo) throws GameActionException
    {
        if (robotInfo.getTeam() == enemy) 
        {
            sensedEnemyEnlightenmentCenter(robotInfo);
        }
        else if (robotInfo.getTeam() == Team.NEUTRAL) 
        {
            sensedNeutralEnlightenmentCenter(robotInfo);   
        }
        else if (robotInfo.getTeam() == friendly) 
        {
            checkIfEnemyEnlightenmentCenterHasBeenConverted(robotInfo);
            addFriendlyEnlightenmentCenterRobotId(robotInfo);
            addFriendlyEnlightenmentCenterAsHomeLocationIfNull(robotInfo);
        }

        checkIfNeutralEnlightenmentCenterHasBeenConverted(robotInfo);        
    }

    private static void sensedEnemyEnlightenmentCenter(RobotInfo robotInfo)
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

    private static void sensedNeutralEnlightenmentCenter(RobotInfo robotInfo)
    {
        if (neutralEnlightenmentCenterMapLocation.isEmpty() 
            || !neutralEnlightenmentCenterMapLocation.contains(robotInfo.getLocation())) 
        {
            neutralEnlightenmentCenterMapLocation.add(robotInfo.getLocation());
            turnsAroundNeutralEnlightenmentCenter = 0;
        }

        neutralEnlightenmentCenterCurrentInfluence = robotInfo.getInfluence();
        currentNeutralEnlightenmentCenterGoingFor = robotInfo.getLocation();
        neutralEnlightenmentCenterFound = true;
        neutralEnlightenmentCenterIsAround = true;
        if (turnsAroundNeutralEnlightenmentCenter < 3) 
        {
            haveMessageToSend = true;
        }
        turnsAroundNeutralEnlightenmentCenter++;        
    }

    private static void checkIfEnemyEnlightenmentCenterHasBeenConverted(RobotInfo robotInfo)
            throws GameActionException
    {
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
    }

    private static void checkIfNeutralEnlightenmentCenterHasBeenConverted(RobotInfo robotInfo)
            throws GameActionException
    {
        if (Communication.hasNeutralEnlightenmentCenterBeenConverted(robotInfo)
            && (convertedNeutralEnlightenmentCenterMapLocation.isEmpty() 
                || !convertedNeutralEnlightenmentCenterMapLocation.contains(robotInfo.getLocation())))
        {
            Communication.processNeutralEnlightenmentCenterHasBeenConverted(robotInfo.getLocation());
            neutralEnlightenmentCenterHasBeenConverted = true;
            messageLastTwoTurnsForConverted = 2;
            turnsAroundNeutralEnlightenmentCenter = 0;
            haveMessageToSend = true;
        }
        else if (convertedNeutralEnlightenmentCenterMapLocation.contains(robotInfo.getLocation()) 
            && neutralEnlightenmentCenterMapLocation.contains(robotInfo.getLocation())
            && Communication.hasNeutralEnlightenmentCenterBeenConverted(robotInfo)) 
        {
            Communication.processNeutralEnlightenmentCenterHasBeenConverted(robotInfo.getLocation());
            neutralEnlightenmentCenterHasBeenConverted = true;
        }
    }

    private static void addFriendlyEnlightenmentCenterRobotId(RobotInfo robotInfo)
    {
        if (!friendlyEnlightenmentCenterRobotIds.contains(robotInfo.getID())) 
        {
            friendlyEnlightenmentCenterRobotIds.add(robotInfo.getID());
        }
    }

    private static void addFriendlyEnlightenmentCenterAsHomeLocationIfNull(RobotInfo robotInfo)
    {
        if (spawnEnlightenmentCenterHomeLocation == null) 
        {
            spawnEnlightenmentCenterHomeLocation = robotInfo.getLocation();
            friendlyEnlightenmentCenterRobotIds.add(robotInfo.getID());
        }
    }

    public static void checkForCommunications() throws GameActionException
    {
        if (!enemyEnlightenmentCenterFound) 
        {
            Communication.checkIfFriendlyEnlightenmentCenterHasEnemyLocation();
            setCurrentEnemyEnlightenmentCenterGoingFor();
        }

        if (!enemyEnlightenmentCenterHasBeenConverted && enemyEnlightenmentCenterFound) 
        {
            Communication.checkIfFriendlyEnlightenmentCenterHasEnemyLocationConverted();
        }

        if (!neutralEnlightenmentCenterFound) 
        {
            Communication.checkIfFriendlyEnlightenmentCenterHasNeutralLocation();
            setCurrentNeutralEnlightenmentCenterGoingFor();
        }

        if (!neutralEnlightenmentCenterHasBeenConverted && neutralEnlightenmentCenterFound) 
        {
            Communication.checkIfFriendlyEnlightenmentCenterHasNeutralLocationConverted();
        }
    }

    protected static void setCurrentEnemyEnlightenmentCenterGoingFor()
    {
        if (currentEnemyEnlightenmentCenterGoingFor == null && !enemyEnlightenmentCenterMapLocation.isEmpty()) 
        {
            MapLocation enemyCenterLocation = enemyEnlightenmentCenterMapLocation.get(0);
            currentEnemyEnlightenmentCenterGoingFor = enemyCenterLocation; // TODO: can make this cleaner
            enemyEnlightenmentCenterFound = true;
        }
    }

    protected static void setCurrentNeutralEnlightenmentCenterGoingFor() 
    {
        if (currentNeutralEnlightenmentCenterGoingFor == null && !neutralEnlightenmentCenterMapLocation.isEmpty()) 
        {
            MapLocation neutralCenterLocation = neutralEnlightenmentCenterMapLocation.get(neutralEnlightenmentCenterMapLocation.size() - 1);
            currentNeutralEnlightenmentCenterGoingFor = neutralCenterLocation; // TODO: can make this cleaner
            neutralEnlightenmentCenterFound = true;
        }
    }
}
