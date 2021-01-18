package testPlayerv01;

import battlecode.common.*;
import testPlayerv01.Service.Movement;
import testPlayerv01.Service.SenseRobots;
import testPlayerv01.Service.Communication;

public class MuckrakerTest01 extends RobotPlayer 
{
    @SuppressWarnings("unused")
    public static void run() throws GameActionException 
    {
        tryExpose();
        senseNearbyRobots();

        if (haveMessageToSend) 
        {
            Communication.setFlagMessageForScout();
        }
        else
        {
            robotController.setFlag(0);
        }

        // Scout Role
        if (robotRole == RobotRoles.Scout && !enemyEnlightenmentCenterFound) 
        {

            // This is so that the robot will go the direction it spawns. Hopefully this will make it go all over.
            if (robotController.getRoundNum() < 15) 
            {
                directionToScout = spawnEnlightenmentCenterHomeLocation.directionTo(robotController.getLocation());
                // directionTo can be from a different location. Like the enemyEC to where you are.
            }

            Movement.scoutAction();
        } 
        else if (enemyEnlightenmentCenterFound) 
        {
            if (politicianECBombNearby) 
            {
                // TODO: Try out moving away from the ECBomb too.
                Movement.moveAwayFromLocation(currentEnemyEnlightenmentCenterGoingFor);

            } else if (!robotController.getLocation().isAdjacentTo(currentEnemyEnlightenmentCenterGoingFor)) 
            {
                Movement.moveToEnemyEnlightenmentCenter(currentEnemyEnlightenmentCenterGoingFor);
            }
        } 
        else 
        {
            tryMove(randomDirection());
        }

        // TODO:
        /*
         * So these guys are mostly the scouts since they can sense. what should they
         * scout for? 1. enemy EC 2. enemy slanderer... though they will probably run
         * away 3. big Polis? 4. large groups of enemies. 5. Map edge & corners.
         */
    }

    private static void tryExpose() throws GameActionException 
    {
        Team enemy = robotController.getTeam().opponent();
        int actionRadius = robotController.getType().actionRadiusSquared;
        RobotInfo[] enemyRobots = robotController.senseNearbyRobots(actionRadius, enemy);

        for (RobotInfo robotInfo : enemyRobots) {
            if (robotInfo.type.canBeExposed()) {
                if (robotController.canExpose(robotInfo.location)) 
                {
                    robotController.expose(robotInfo.location);
                    return;
                }
            }
        }
    }

    private static void senseNearbyRobots() throws GameActionException
    {
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;
        RobotInfo[] robots = robotController.senseNearbyRobots(sensorRadiusSquared);
        politicianECBombNearby = false;
        enemyEnlightenmentCenterIsAround = false;

        for (RobotInfo robotInfo : robots) 
        {
            if (robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER) 
            {
                SenseRobots.processEnlightenmentCenterFinding(robotInfo);
            } 
            else if (SenseRobots.checkIfPoliticianBombNearby(robotInfo)) 
            {
                politicianECBombNearby = true;
            }
        }
    }    

    public static void setup() throws GameActionException 
    {
        setConstants();
        assignHomeEnlightenmentCenterLocation();
        setSquaresAroundEnlightenmentCenter();
        setRobotRole();        
    }

    private static void setRobotRole() {
        if (robotController.getInfluence() == 1) {
            robotRole = RobotRoles.Scout;
        } else if (robotController.getInfluence() == 2) {
            robotRole = RobotRoles.DefendHomeEnlightenmentCenter;
        } else {
            robotRole = RobotRoles.Follower;
        }
    }
}