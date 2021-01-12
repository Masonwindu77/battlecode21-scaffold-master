package testPlayerv01;
import java.util.Random;

import battlecode.common.*;
import testPlayerv01.Service.Movement;

public class MuckrakerTest01 extends RobotPlayer
{
    @SuppressWarnings("unused")
    public static void run() throws GameActionException
    {
        tryExpose();

        if (enemyEnlightenmentCenterMapLocation.size() <= 3) {
            checkIfRobotCanSenseEnemyEnlightenmentCenter();
        }
        
        // Turn off flag announcement
        // if (robotController.canGetFlag(spawnEnlightenmentCenterRobotId)) {
        //     int enlightenmentCenterFlag = robotController.getFlag(spawnEnlightenmentCenterRobotId);
        // }

        if (haveMessageToSend) 
        {
            if (enemyEnlightenmentCenterFound) 
            {
                announceEnemyEnlightenmentCenterLocation();
                haveMessageToSend = false;
            }
            else if (enemyEnlightenmentCenterHasBeenConverted()) 
            {
                announceEnemyEnlightenmentCenterHasBeenConverted();
                enemyEnlightenmentCenterMapLocation.clear();
                haveMessageToSend = false;
            }
            
        }

        // Scout Role
        if (robotRole == RobotRoles.Scout && !enemyEnlightenmentCenterFound) 
        {
            Movement.scoutAction();
        }
        else if (enemyEnlightenmentCenterFound) 
        {
            Movement.moveToEnemyEnlightenmentCenter(currentEnemyEnlightenmentCenterGoingFor);
        }
        else
        {
            tryMove(randomDirection());
        }


        // TODO:
        /*
            So these guys are mostly the scouts since they can sense. what should they scout for?
            1. enemy EC
            2. enemy slanderer... though they will probably run away
            3. big Polis?
            4. large groups of enemies.
            5. Map edge & corners.
        */
    }

    private static void tryExpose() throws GameActionException
    {
        Team enemy = robotController.getTeam().opponent();
        int actionRadius = robotController.getType().actionRadiusSquared;
        RobotInfo[] enemyRobots = robotController.senseNearbyRobots(actionRadius, enemy);
        
        for (RobotInfo robotInfo : enemyRobots) {
            if (robotInfo.type.canBeExposed()) {
                
                if (robotController.canExpose(robotInfo.location)) {
                    println("MASON I EXPOSED A SLANDERER");
                    robotController.expose(robotInfo.location);
                    return;
                }
            }
        }
    }

    private static void checkIfRobotCanSenseEnemyEnlightenmentCenter()
    {
        Team enemy = robotController.getTeam().opponent();
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;

        RobotInfo[] robots = robotController.senseNearbyRobots(sensorRadiusSquared, enemy);

        for (RobotInfo robotInfo : robots) 
        {
            if (robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER 
            && (!enemyEnlightenmentCenterMapLocation.containsValue(robotInfo.getLocation()) || enemyEnlightenmentCenterMapLocation.size() < 1)) 
            {
                enemyEnlightenmentCenterMapLocation.put(enemyEnlightenmentCenterMapLocation.size() + 1, robotInfo.getLocation());
                haveMessageToSend = true;
                enemyEnlightenmentCenterFound = true;
                currentEnemyEnlightenmentCenterGoingFor = robotInfo.getLocation();
            }
        }
    }

    private static void announceEnemyEnlightenmentCenterLocation() throws GameActionException 
    {
        MapLocation enemyCenterLocation = enemyEnlightenmentCenterMapLocation.get(enemyEnlightenmentCenterMapLocation.size());
        currentEnemyEnlightenmentCenterGoingFor = enemyCenterLocation;
        sendLocation(enemyCenterLocation, ENEMY_ENLIGHTENMENT_CENTER_FOUND);
    }
    
    private static void announceEnemyEnlightenmentCenterHasBeenConverted() throws GameActionException
    {
        sendLocation(currentEnemyEnlightenmentCenterGoingFor, ENEMY_ENLIGHTENMENT_CENTER_CONVERTED);
    }

	public static void setup() throws GameActionException
    {
        //checkIfEnemeyEnlightenmentCenterHasBeenFound();
        assignHomeEnlightenmentCenterLocation();       
        setRobotRole(); 
        enemy = robotController.getTeam().opponent();
        friendly = robotController.getTeam();
        randomInteger = new Random();
    }

    private static void setRobotRole()
    {
        if (robotController.getInfluence() == 1) {
            robotRole = RobotRoles.Scout;
        }
        else if (robotController.getInfluence() == 2) 
        {
            robotRole = RobotRoles.DefendHomeEnlightenmentCenter;
        }
        else
        {
            robotRole = RobotRoles.Follower;
        }
    }
}