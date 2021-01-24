package testPlayerv01;

import java.util.ArrayList;
import java.util.List;

import battlecode.common.*;
import testPlayerv01.Service.Movement;
import testPlayerv01.Service.Sense;
import testPlayerv01.Roles.Scout;
import testPlayerv01.Service.Communication;

public class MuckrakerTest01 extends RobotPlayer 
{
    static MapLocation targetSlanderer;
    static MapLocation currentDetectedRobotLocationGoingFor;
    
    static List<MapLocation> mapLocationOfRobotsThatHaveBeenSensed = new ArrayList<>();

    static MapLocation closestPoliticianBomb;
    public static boolean enemyMuckrakersNearby;
    public static MapLocation enemyMuckrakerMapLocation;

    @SuppressWarnings("unused")
    public static void run() throws GameActionException 
    {
        resetVariablesForSensing();
        senseNearbyRobots();
        tryExpose();

        if ((neutralEnlightenmentCenterFound && turnCount > 75) || (robotController.getRoundNum() >= END_GAME_ROUND_STRAT) || robotRole == RobotRoles.SlandererAttacker) 
        {
            Sense.checkForCommunications();    
        }

        if (neutralEnlightenmentCenterFound) 
        {
            Scout.senseIfRobotsAreCloseToNeutralEnlightenmentCenter();    
        }
        else if (!neutralEnlightenmentCenterIsAround)
        {
            Scout.friendlyMuckrakerIsCloserOnTeam = false;
            Scout.friendlyPoliticianIsCloserOnTeam = false;
        }             

        if (haveMessageToSend) 
        {
            Communication.setFlagMessageForScout();
        }
        else
        {
            robotController.setFlag(0);
        }

        // Scout Role
        if (robotRole == RobotRoles.Scout 
            && !enemyEnlightenmentCenterFound
            && !neutralEnlightenmentCenterFound
            && targetSlanderer == null) 
        {
            // This is so that the robot will go the direction it spawns. Hopefully this will make it go all over.
            if (robotController.getRoundNum() < 25 && spawnEnlightenmentCenterHomeLocation != null) 
            {
                directionToScout = spawnEnlightenmentCenterHomeLocation.directionTo(robotController.getLocation());
                // directionTo can be from a different location. Like the enemyEC to where you are.
            }

            Movement.scoutAction();
        } 
        // Attack Slanderer
        else if (targetSlanderer != null) 
        {
            Movement.moveToTargetLocation(targetSlanderer);    
        }
        
        Scout.neutralOrEnemyBaseFound();   

        // For the not scouts (Influence > 1)
        if ((!neutralEnlightenmentCenterFound && !enemyEnlightenmentCenterFound) || Scout.friendlyMuckrakerIsCloserOnTeam)
        {
            if (turnCount < 15 && spawnEnlightenmentCenterHomeLocation != null) 
            {
                directionToScout = spawnEnlightenmentCenterHomeLocation.directionTo(robotController.getLocation());
            }
            else if (directionToScout == null)
            {
                directionToScout = Movement.getRandomDirection();
            }            
            
            Movement.scoutAction();
        }

        // TODO:
        /*
         * So these guys are mostly the scouts since they can sense. what should they
         * scout for? 1. enemy EC 2. enemy slanderer... though they will probably run
         * away 3. big Polis? 4. large groups of enemies. 5. Map edge & corners.
         */
    }

    private static void resetVariablesForSensing() 
    {
        politicianECBombNearby = false;
        closestPoliticianBomb = null;
        targetSlanderer = null;
        enemyMuckrakersNearby = false;

        enemyEnlightenmentCenterFound = false;
        enemyEnlightenmentCenterIsAround = false;

        neutralEnlightenmentCenterIsAround = false;
        neutralEnlightenmentCenterFound = false; 

        currentDetectedRobotLocationGoingFor = null;       
    }

    private static void tryExpose() throws GameActionException 
    {
        Team enemy = robotController.getTeam().opponent();
        int actionRadius = robotController.getType().actionRadiusSquared;
        RobotInfo[] enemyRobots = robotController.senseNearbyRobots(actionRadius, enemy);

        for (RobotInfo robotInfo : enemyRobots) 
        {
            if (robotInfo.type.canBeExposed()) 
            {
                if (robotController.canExpose(robotInfo.location)) 
                {
                    robotController.expose(robotInfo.location);
                    if (targetSlanderer != null && targetSlanderer == robotInfo.location) 
                    {
                        targetSlanderer = null;
                    }
                    return;
                }
            }
        }
    }

    private static void senseNearbyRobots() throws GameActionException
    {
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;
        RobotInfo[] robots = robotController.senseNearbyRobots(sensorRadiusSquared);

        for (RobotInfo robotInfo : robots) 
        {
            if (robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER) 
            {
                Sense.processEnlightenmentCenterFinding(robotInfo);
            } 
            else if (Sense.checkIfPoliticianBombNearby(robotInfo)) 
            {
                politicianECBombNearby = true;

                if (closestPoliticianBomb == null 
                || robotController.getLocation().distanceSquaredTo(closestPoliticianBomb) 
                    < robotController.getLocation().distanceSquaredTo(closestPoliticianBomb)) 
                {
                    closestPoliticianBomb = robotInfo.getLocation();
                }                
            }
            else if (robotInfo.getType() == RobotType.SLANDERER 
                && robotInfo.getTeam() == enemy
                && targetSlanderer == null) 
            {
                targetSlanderer = robotInfo.getLocation();    
            }
            else if (robotInfo.getType() == RobotType.MUCKRAKER
                && robotInfo.getTeam() == enemy) 
            {
                enemyMuckrakersNearby = true;
                enemyMuckrakerMapLocation = robotInfo.getLocation(); 
                haveMessageToSend = true;             
            }
            if (robotController.getRoundNum() < BEGINNING_ROUND_STRAT
                && Clock.getBytecodesLeft() >= 4500) 
            {
                if (!mapLocationOfRobotsThatHaveBeenSensed.contains(robotInfo.getLocation())) 
                {
                    mapLocationOfRobotsThatHaveBeenSensed.add(robotInfo.getLocation());
                }
            }
            
        }
    }    

    

    protected static void detectNearbyRobots() throws GameActionException
    {
        int detectRadiusSquared = robotController.getType().detectionRadiusSquared;
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;
        MapLocation[] mapLocationOfRobots = robotController.detectNearbyRobots(detectRadiusSquared);
        
        for (MapLocation mapLocation : mapLocationOfRobots) 
        {
            if ((robotController.getLocation().distanceSquaredTo(mapLocation) > sensorRadiusSquared) 
                && !mapLocationOfRobotsThatHaveBeenSensed.contains(mapLocation)
                && Clock.getBytecodesLeft() >= 3000) 
            {
                currentDetectedRobotLocationGoingFor = mapLocation;
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
        if (robotController.getInfluence() < 4) 
        {
            robotRole = RobotRoles.Scout;
        }
        else 
        {
            robotRole = RobotRoles.SlandererAttacker;
        }
    }
}