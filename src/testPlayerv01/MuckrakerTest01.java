package testPlayerv01;
import battlecode.common.*;
import testPlayerv01.Service.Movement;

public class MuckrakerTest01 extends RobotPlayer
{
    @SuppressWarnings("unused")
    public static void run() throws GameActionException
    {
        Team enemy = robotController.getTeam().opponent();
        int actionRadius = robotController.getType().actionRadiusSquared;
        RobotInfo[] enemyRobots = robotController.senseNearbyRobots(actionRadius, enemy);
        
        for (RobotInfo robotInfo : enemyRobots) {
            if (robotInfo.type.canBeExposed()) {
                
                if (robotController.canExpose(robotInfo.location)) {
                    System.out.println("MASON I EXPOSED A SLANDERER");
                    robotController.expose(robotInfo.location);
                    return;
                }
            }
        }

        if (enemyEnlightenmentCenterMapLocation.size() <= 3) {
            checkIfRobotCanSenseEnemyEnlightenmentCenter();
        }
        
        // Turn off flag announcement
        if (robotController.canGetFlag(spawnEnlightenmentCenterRobotId)) {
            int enlightenmentCenterFlag = robotController.getFlag(spawnEnlightenmentCenterRobotId);
            if (enlightenmentCenterFlag == RECEIVED_MESSAGE) {
                messageReceived = true;
                haveMessageToSend = false;
            }
        }

        if (haveMessageToSend && !messageReceived) 
        {
            announceEnemyEnlightenmentCenterLocation();
            haveMessageToSend = false;
        }

        // Scout Role
        if (robotRole == RobotRoles.Scout) 
        {
            Movement.scoutAction();
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
                messageReceived = false;
            }
        }
    }

    private static void announceEnemyEnlightenmentCenterLocation() throws GameActionException 
    {
        MapLocation enemyCenterLocation = enemyEnlightenmentCenterMapLocation.get(enemyEnlightenmentCenterMapLocation.size());
        sendLocation(enemyCenterLocation, ENEMY_ENLIGHTENMENT_CENTER_FOUND);
	}

	public static void setup() throws GameActionException
    {
        //checkIfEnemeyEnlightenmentCenterHasBeenFound();
        assignHomeEnlightenmentCenterLocation();       
        setRobotRole(); 
        enemy = robotController.getTeam().opponent();
        friendly = robotController.getTeam();
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