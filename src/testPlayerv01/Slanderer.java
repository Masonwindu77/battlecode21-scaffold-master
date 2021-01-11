package testPlayerv01;
import battlecode.common.*;
import testPlayerv01.Service.Movement;

public class Slanderer extends RobotPlayer
{
    public static void run() throws GameActionException
    {
        if (turnCount <= 10) 
        {
            tryMove(randomDirection());
        }
        else
        {
            checkIfEnemeyEnlightenmentCenterHasBeenFound(spawnEnlightenmentCenterRobotId);
        }

        if (enemyEnlightenmentCenterFound) 
        {
            Movement.moveAwayFromLocation(enemyEnlightenmentCenterMapLocation.get(0));   
        }

        // TODO: Make Slanderer stuff
        /* Make a check for a flag nearby of the muckraker. 
         If the muckraker shows a position, it means an enemy is coming

        Also, need to figure out where we are so we can go to a corner....?
        */ 
    }
 
    public static void setup()
    {
        assignHomeEnlightenmentCenterLocation();
        robotCurrentInfluence = robotController.getInfluence();
        assignRobotRole();        
        enemy = robotController.getTeam().opponent();
        friendly = robotController.getTeam();
    }

    private static void assignRobotRole() 
    {
        if (robotController.getInfluence() >= POLITICIAN_EC_BOMB) {
            robotRole = RobotRoles.PoliticianEnlightenmentCenterBomb;
        }
        else
        {
            robotRole = RobotRoles.Follower;
        }
        
	}
}
