package testPlayerv01;

import battlecode.common.*;
import testPlayerv01.Service.Movement;
import testPlayerv01.Service.Communication;

public class MuckrakerTest01 extends RobotPlayer 
{
    static boolean enemyEnlightenmentCenterIsAround;

    static boolean enemyEnlightenmentCenterHasBeenConverted;

    static int turnsAroundEnemyEnlightenmentCenter = 0;

    @SuppressWarnings("unused")
    public static void run() throws GameActionException 
    {
        tryExpose();
        senseNearbyRobots();

        if (!enemyEnlightenmentCenterFound) 
        {
            checkIfRobotCanSenseEnemyEnlightenmentCenter();
        }
        else if (!neutralEnlightenmentCenterFound)
        {
            //checkIfRobotCanSenseNeutralEnlightenmentCenter();
        }

        if (haveMessageToSend) 
        {
            setFlagMessage();
        }
        else
        {
            robotController.setFlag(0);
        }

        // Scout Role
        if (robotRole == RobotRoles.Scout && !enemyEnlightenmentCenterFound) {

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

    private static void senseNearbyRobots() throws GameActionException {
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;
        RobotInfo[] robots = robotController.senseNearbyRobots(sensorRadiusSquared);
        politicianECBombNearby = false;
        enemyEnlightenmentCenterIsAround = false;

        for (RobotInfo robotInfo : robots) 
        {
            if (robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER) 
            {
                processEnlightenmentCenterFinding(robotInfo);
            } 
            else if (checkIfPoliticianBombNearby(robotInfo)) 
            {
                politicianECBombNearby = true;
            }
        }
    }

    private static void processEnlightenmentCenterFinding(RobotInfo robotInfo) throws GameActionException
    {
        if (robotInfo.getTeam() == enemy) 
        {
            enemyEnlightenmentCenterCurrentInfluence = robotInfo.getInfluence();
            enemyEnlightenmentCenterIsAround = true;
            turnsAroundEnemyEnlightenmentCenter++;
            haveMessageToSend = true;

            // haveMessageToSend = true;
            // neturalEnlightenmentCenter();
        }
        else if (robotInfo.getTeam() == friendly 
            && robotInfo.getLocation().equals(currentEnemyEnlightenmentCenterGoingFor)
            && !convertedEnemyEnlightenmentCenterMapLocation.contains(robotInfo.getLocation()))
        {
            Communication.processEnemyEnlightenmentCenterHasBeenConverted();
            enemyEnlightenmentCenterHasBeenConverted = true;
            turnsAroundEnemyEnlightenmentCenter = 0;
            haveMessageToSend = true;
        }
        else if (robotInfo.getTeam() == Team.NEUTRAL) 
        {
            neutralEnlightenmentCenterFound = true;
            neutralEnlightenmentCenterMapLocation.add(robotInfo.getLocation());
            haveMessageToSend = true;
        }
    }

    private static boolean checkIfPoliticianBombNearby(RobotInfo robotInfo) 
    {
        if (robotInfo.getType() == RobotType.POLITICIAN && robotInfo.getTeam() == friendly
        && robotInfo.getConviction() >= POLITICIAN_EC_BOMB
        && (currentEnemyEnlightenmentCenterGoingFor != null 
        && currentEnemyEnlightenmentCenterGoingFor.distanceSquaredTo(robotInfo.getLocation()) <= 10)) {
            return true;
        } else {
            return false;
        }
    }

    private static void checkIfRobotCanSenseEnemyEnlightenmentCenter() {
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;

        RobotInfo[] robots = robotController.senseNearbyRobots(sensorRadiusSquared, enemy);

        for (RobotInfo robotInfo : robots) {
            if (robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER
                    && (!enemyEnlightenmentCenterMapLocation.contains(robotInfo.getLocation())
                    || enemyEnlightenmentCenterMapLocation.size() < 1)) 
            {

                enemyEnlightenmentCenterMapLocation.add(robotInfo.getLocation());
                haveMessageToSend = true;
                enemyEnlightenmentCenterFound = true;
                currentEnemyEnlightenmentCenterGoingFor = robotInfo.getLocation();
            }
        }
    }
    
    private static void setFlagMessage() throws GameActionException 
    {
        if (enemyEnlightenmentCenterFound && turnsAroundEnemyEnlightenmentCenter < 2) 
        {
            announceEnemyEnlightenmentCenterLocation();
            haveMessageToSend = false;
        } 
        else if (enemyEnlightenmentCenterIsAround) 
        {
            Communication.announceEnemyEnlightenmentCenterCurrentInfluence(Communication.ENEMY_ENLIGHTENMENT_CENTER_INFLUENCE);
            haveMessageToSend = false;
        } 
        else if (enemyEnlightenmentCenterHasBeenConverted) 
        {
            Communication.announceEnemyEnlightenmentCenterHasBeenConverted();
            haveMessageToSend = false;
        }
    }

    private static void announceEnemyEnlightenmentCenterLocation() throws GameActionException 
    {
        MapLocation enemyCenterLocation = enemyEnlightenmentCenterMapLocation.get(0);
        currentEnemyEnlightenmentCenterGoingFor = enemyCenterLocation;
        Communication.sendLocation(enemyCenterLocation, Communication.ENEMY_ENLIGHTENMENT_CENTER_FOUND);
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