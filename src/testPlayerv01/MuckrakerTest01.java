package testPlayerv01;
import battlecode.common.*;

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

        if (enemyEnlightenmentCenterMapLocation.size() == 0) {
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
        }

        if (tryMove(Direction.EAST))//randomDirection()))
        {
            //System.out.println("MUCKRAKER! I moved!"); 
        }
        // if (true)//positionOfEnemyEnlightenmentCenterFound()) 
        // {

        //     Direction directionTowardsEnemyEnlightenmentCenter = Direction.NORTH;//getDirectionTowardsEnemyEnlightenmentCenter();

        //     if (tryMove(directionTowardsEnemyEnlightenmentCenter)) {
        //         System.out.println("MASON! MUCKRAKER! I MOVED");
        //     }
        // }
    }

    private static void checkIfRobotCanSenseEnemyEnlightenmentCenter()
    {
        Team enemy = robotController.getTeam().opponent();
        int actionRadius = robotController.getType().actionRadiusSquared;

        // System.out.println("checking for the enemey center....");
        RobotInfo[] robots = robotController.senseNearbyRobots(actionRadius, enemy);

        for (RobotInfo robotInfo : robots) 
        {
            if (robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER 
            && (!enemyEnlightenmentCenterMapLocation.containsValue(robotInfo.getLocation()) || enemyEnlightenmentCenterMapLocation.size() < 1)) 
            {
                System.out.println("FOUND ENEMEY CENTER....");
                enemyEnlightenmentCenterMapLocation.put(enemyEnlightenmentCenterMapLocation.size() + 1, robotInfo.getLocation());
                haveMessageToSend = true;
                messageReceived = false;
            }
        }
    }

    private static void announceEnemyEnlightenmentCenterLocation() throws GameActionException 
    {
        int flagToSend = 0;
        if (robotController.getRoundNum() % 2 == 0) 
        {
            flagToSend = createFlagWithXCoordinate(ENEMY_ENLIGHTENMENT_CENTER_FOUND_X_COORDINATE
            , enemyEnlightenmentCenterMapLocation.get(enemyEnlightenmentCenterMapLocation.size()));
        }
        else
        {
            flagToSend = createFlagWithYCoordinate(ENEMY_ENLIGHTENMENT_CENTER_FOUND_Y_COORDINATE
            , enemyEnlightenmentCenterMapLocation.get(enemyEnlightenmentCenterMapLocation.size()));
        }

        if (robotController.canSetFlag(flagToSend)) {
            robotController.setFlag(flagToSend);
        }
	}

	public static void setup() throws GameActionException
    {
        //checkIfEnemeyEnlightenmentCenterHasBeenFound();
        assignHomeEnlightenmentCenterLocation();        
    }
}