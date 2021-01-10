package testPlayerv01;
import battlecode.common.*;

public class PoliticianTest01 extends RobotPlayer 
{
    static boolean politicianIsAround;
    static boolean enlightenmentCenterIsAround;

    static void run() throws GameActionException 
    {
        Team enemy = robotController.getTeam().opponent();
        int actionRadius = robotController.getType().actionRadiusSquared;
        RobotInfo[] attackableRobotInfos = robotController.senseNearbyRobots(actionRadius, enemy);

        for (RobotInfo robotInfo : attackableRobotInfos) {
            if (robotInfo.getType() == RobotType.POLITICIAN) 
            {
                politicianIsAround = true;
            }
            else if (robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER) 
            {
                enlightenmentCenterIsAround = true;
            }
        }

        if (((attackableRobotInfos.length > 2) 
        || (attackableRobotInfos.length != 0 && politicianIsAround)
        || enlightenmentCenterIsAround)
        && robotController.canEmpower(actionRadius)) 
        {
            robotController.empower(actionRadius);

            return;
        }

        MapLocation mapTest = new MapLocation(10026, 13005);
        
        if (enemyEnlightenmentCenterMapLocation.size() > 0) {
            MapLocation enemyCenterLocation = enemyEnlightenmentCenterMapLocation.get(enemyEnlightenmentCenterMapLocation.size());

            moveToEnemyEnlightenmentCenter(enemyCenterLocation);
        }
        

        

    }

    static void moveToEnemyEnlightenmentCenter(MapLocation enemyCenterLocation) throws GameActionException
    {
        if (tryMove(robotController.getLocation().directionTo(enemyCenterLocation))) {
            
        }
        else
        {
            tryMove(randomDirection());
        }
    }

    static void setup()
    {
        assignHomeEnlightenmentCenterLocation();
    }
}
