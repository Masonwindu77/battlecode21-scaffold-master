package testPlayerv01.Roles;

import testPlayerv01.MuckrakerTest01;
import battlecode.common.*;
import testPlayerv01.Service.Movement;

public class Scout extends MuckrakerTest01
{
    public static boolean friendlyMuckrakerIsCloserOnTeam;
    public static boolean friendlyPoliticianIsCloserOnTeam;

    public static void senseIfRobotsAreCloseToNeutralEnlightenmentCenter()
    {
        if (robotController.canSenseLocation(neutralCurrentEnlightenmentCenterGoingFor)) 
        {
            RobotInfo[] robots = robotController.senseNearbyRobots(neutralCurrentEnlightenmentCenterGoingFor, 5, friendly);

            for (RobotInfo robotInfo : robots) 
            {
                if (robotInfo.getType() == RobotType.MUCKRAKER
                 && ((robotController.getLocation().distanceSquaredTo(neutralCurrentEnlightenmentCenterGoingFor) 
                        > robotInfo.getLocation().distanceSquaredTo(neutralCurrentEnlightenmentCenterGoingFor)) 
                    || (robotInfo.getLocation().isAdjacentTo(neutralCurrentEnlightenmentCenterGoingFor) 
                        && !robotController.getLocation().isAdjacentTo(neutralCurrentEnlightenmentCenterGoingFor))))
                {
                    friendlyMuckrakerIsCloserOnTeam = true;
                } 
                else if (robotInfo.getType() == RobotType.POLITICIAN
                && ((robotController.getLocation().distanceSquaredTo(neutralCurrentEnlightenmentCenterGoingFor) 
                       > robotInfo.getLocation().distanceSquaredTo(neutralCurrentEnlightenmentCenterGoingFor)) 
                   || (robotInfo.getLocation().isAdjacentTo(neutralCurrentEnlightenmentCenterGoingFor) 
                       && !robotController.getLocation().isAdjacentTo(neutralCurrentEnlightenmentCenterGoingFor))))
                {
                    friendlyPoliticianIsCloserOnTeam = true;
                }
            }
        }        
    }

    public static void neutralOrEnemyBaseFound() throws GameActionException
    {
        if (enemyEnlightenmentCenterFound && enemyEnlightenmentCenterIsAround) 
        {
            if (politicianECBombNearby) 
            {
                Movement.moveAwayFromLocation(enemyCurrentEnlightenmentCenterGoingFor);
            } 
            else if (!robotController.getLocation().isAdjacentTo(enemyCurrentEnlightenmentCenterGoingFor)) 
            {
                Movement.moveToEnemyEnlightenmentCenter(enemyCurrentEnlightenmentCenterGoingFor);
            }
        }
        else if (enemyEnlightenmentCenterFound)
        {
            if (!robotController.getLocation().isAdjacentTo(enemyCurrentEnlightenmentCenterGoingFor)) 
            {
                Movement.moveToEnemyEnlightenmentCenter(enemyCurrentEnlightenmentCenterGoingFor);
            }
        }
        else if (neutralEnlightenmentCenterFound && neutralEnlightenmentCenterIsAround) 
        {
            if (politicianECBombNearby) 
            {
                Movement.moveAwayFromLocation(neutralCurrentEnlightenmentCenterGoingFor);
            } 
            else if ((!friendlyMuckrakerIsCloserOnTeam && !friendlyPoliticianIsCloserOnTeam) && !robotController.getLocation().isAdjacentTo(neutralCurrentEnlightenmentCenterGoingFor)) 
            {
                Movement.moveToNeutralEnlightenmentCenter(neutralCurrentEnlightenmentCenterGoingFor);
            }         
            else if ((friendlyMuckrakerIsCloserOnTeam || friendlyPoliticianIsCloserOnTeam) && !robotController.getLocation().isAdjacentTo(neutralCurrentEnlightenmentCenterGoingFor))
            {
                Movement.scoutAction();    
            }
            else if ((friendlyMuckrakerIsCloserOnTeam || friendlyPoliticianIsCloserOnTeam) && robotController.getLocation().isAdjacentTo(neutralCurrentEnlightenmentCenterGoingFor)) 
            {
                Movement.moveAwayFromLocation(neutralCurrentEnlightenmentCenterGoingFor);    
            }
            // TODO: Move it on to a passability that's higher...
        }
        else if (neutralEnlightenmentCenterFound)
        {
            if (!neutralCurrentEnlightenmentCenterGoingFor.isWithinDistanceSquared(robotController.getLocation(), robotController.getType().sensorRadiusSquared))
            {
                Movement.scoutAction();
            }
            else if (!robotController.getLocation().isAdjacentTo(neutralCurrentEnlightenmentCenterGoingFor)) 
            {
                Movement.moveToNeutralEnlightenmentCenter(neutralCurrentEnlightenmentCenterGoingFor);
            }
        }
    }    
}
