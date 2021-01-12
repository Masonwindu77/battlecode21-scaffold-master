package testPlayerv01;
import java.util.Random;

import battlecode.common.*;
import testPlayerv01.Service.Movement;

public class Slanderer extends RobotPlayer
{
    static Direction nextDirection;

    public static void run() throws GameActionException
    {
        if (turnCount <= 30) 
        {
            stayNearHomeBase();

            if (nextDirection == null) 
            {
                tryMove(randomDirection());
            }
            else
            {
                tryMove(nextDirection);
            }            
            
        }
        else if (!enemyEnlightenmentCenterFound)
        {
            checkIfEnemeyEnlightenmentCenterHasBeenFound(spawnEnlightenmentCenterRobotId);
        }
        else if (enemyEnlightenmentCenterFound) 
        {
            Movement.moveAwayFromLocation(currentEnemyEnlightenmentCenterGoingFor);   
        }

        // TODO: Make Slanderer stuff
        /* Make a check for a flag nearby of the muckraker. 
         If the muckraker shows a position, it means an enemy is coming

        Also, need to figure out where we are so we can go to a corner....?
        */ 
    }

    private static void stayNearHomeBase()
    {
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;
        if (robotController.getLocation().isWithinDistanceSquared(enlightenmentCenterHomeLocation, sensorRadiusSquared)) {
            nextDirection = null;
        }
        else
        {
            nextDirection = robotController.getLocation().directionTo(enlightenmentCenterHomeLocation);
        }
    }
 
    public static void setup()
    {
        assignHomeEnlightenmentCenterLocation();
        robotCurrentInfluence = robotController.getInfluence();
        assignRobotRole();        
        enemy = robotController.getTeam().opponent();
        friendly = robotController.getTeam();
        randomInteger = new Random();
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
