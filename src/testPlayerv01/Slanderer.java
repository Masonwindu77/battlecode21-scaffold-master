package testPlayerv01;

import battlecode.common.*;
import testPlayerv01.Service.Communication;
import testPlayerv01.Service.Movement;
import testPlayerv01.Service.Sense;

public class Slanderer extends RobotPlayer 
{

    static Direction nextDirection;
    static boolean enemyMuckrakersNearby = false;
    static int recentlySeenMuckrakers;
    static int slandererFlag;

    static MapLocation enemyMuckrakerLocationFromFlag;
    static MapLocation closestEnemyMuckraker;
    static MapLocation lastSeenEnemyMuckraker;
    static Direction directionToEdgeOfMap;

    static boolean nearFriendlyEnlightenmentCenter;

    static int edgeOfMapLocationX;
    static int edgeOfMapLocationY;
    private static MapLocation closestDefenderPoliticianMapLocation;

    public static void run() throws GameActionException 
    {
        resetVariablesForSensing();
        senseRobotsNearby();    
        directionToEdgeOfMap = Sense.lookForEdgeOfMap();    
        setEdgeOfMap();

        // Saves the last seen enemy muck location
        setEnemyMuckrakersVariable();      
        setFlags();

        if(!enemyEnlightenmentCenterFound)
        {
            Communication.checkIfFriendlyEnlightenmentCenterHasEnemyLocation();
        }

        if (mapLocationOfEdge == null)
        {
            Communication.checkIfFriendlyEnlightenmentCenterHasEdgeOfMapLocation();
        }

        // Move away from Neutral if Poli EC Bomb is nearby
        if (politicianECBombNearby && (!enemyMuckrakersNearby && !enemyEnlightenmentCenterIsAround)) 
        {
            if (neutralEnlightenmentCenterIsAround && neutralCurrentEnlightenmentCenterGoingFor != null) 
            {
                Movement.moveAwayFromLocation(neutralCurrentEnlightenmentCenterGoingFor);
            }
        }

        // If NOT enemy Muck nearby or Enemy EC found
        if (!enemyMuckrakersNearby && !enemyEnlightenmentCenterFound && lastSeenEnemyMuckraker == null) 
        {  
            if (mapLocationOfEdge == null) 
            {
                stayNearHomeBase();  
                if (nextDirection == null) 
                {
                    Direction randomDirection = Movement.getRandomDirection();
                    while (robotController.getLocation().add(randomDirection).isAdjacentTo(spawnEnlightenmentCenterHomeLocation)) 
                    {
                        randomDirection = Movement.getRandomDirection();
                    }

                    Movement.scoutTheDirection(randomDirection);
                } 
                else 
                {
                    Movement.scoutTheDirection(nextDirection);
                }
            }
            else if (mapLocationOfEdge != null)
            {
                moveToEdgeOfTheMap();
            }
        } 
        else if (enemyMuckrakersNearby || lastSeenEnemyMuckraker != null) 
        {
            if (closestEnemyMuckraker != null) 
            {
                Movement.moveAwayFromLocation(closestEnemyMuckraker);
            }
            else if (enemyMuckrakerLocationFromFlag != null)
            {
                Movement.moveAwayFromLocation(enemyMuckrakerLocationFromFlag);
            }
            else
            {
                Movement.moveAwayFromLocation(lastSeenEnemyMuckraker);
            }
        } 
        else if (enemyEnlightenmentCenterIsAround)
        {
            Movement.moveAwayFromLocation(enemyCurrentEnlightenmentCenterGoingFor);
        }
        else if (enemyEnlightenmentCenterFound) 
        {
            // if the next move places it on the adjacent square around the EC. Don't move there.
            if(spawnEnlightenmentCenterHomeLocation != null)
            {
                if (!robotController.getLocation().add(Movement.getOppositeDirection(robotController.getLocation().directionTo(enemyCurrentEnlightenmentCenterGoingFor))).isAdjacentTo(spawnEnlightenmentCenterHomeLocation)
                && robotController.canSenseLocation(robotController.getLocation().add(enemyCurrentEnlightenmentCenterGoingFor.directionTo(robotController.getLocation()))))
                {
                    Movement.moveAwayFromLocation(enemyCurrentEnlightenmentCenterGoingFor);
                }
                else if (robotController.getLocation().add(Movement.getOppositeDirection(robotController.getLocation().directionTo(enemyCurrentEnlightenmentCenterGoingFor))).isAdjacentTo(spawnEnlightenmentCenterHomeLocation))
                {
                    Movement.moveAwayFromLocation(spawnEnlightenmentCenterHomeLocation);
                }
            }            
            else if (mapLocationOfEdge != null)
            {
                moveToEdgeOfTheMap();
            }
            else 
            {
                Movement.moveAwayFromLocation(enemyCurrentEnlightenmentCenterGoingFor);
            }
        }
    }

    private static void resetVariablesForSensing()
    {
        politicianECBombNearby = false;
        enemyMuckrakersNearby = false;
        enemyEnlightenmentCenterIsAround = false;
        neutralEnlightenmentCenterIsAround = false;
        nearFriendlyEnlightenmentCenter = false;
        slandererFlag = Communication.SLANDERER_FLAG;

        enemyMuckrakerLocationFromFlag = null;
        directionToEdgeOfMap = null;
    }

    private static void senseRobotsNearby() throws GameActionException 
    {
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;
        RobotInfo[] robots = robotController.senseNearbyRobots(sensorRadiusSquared);        

        for (RobotInfo robotInfo : robots) 
        {
            if (robotInfo.getTeam() == enemy && robotInfo.getType() == RobotType.MUCKRAKER)
            {
                enemyMuckrakersNearby = true;
                slandererFlag = Communication.SLANDERER_IN_TROUBLE_FLAG;

                if (closestEnemyMuckraker != null 
                && robotController.getLocation().distanceSquaredTo(closestEnemyMuckraker) >= robotController.getLocation().distanceSquaredTo(robotInfo.getLocation())) 
                {
                    closestEnemyMuckraker = robotInfo.getLocation();
                }
                else if(closestEnemyMuckraker == null)
                {
                    closestEnemyMuckraker = robotInfo.getLocation();
                }
            }
            else if (robotInfo.getTeam() == friendly 
                && robotInfo.getType() == RobotType.POLITICIAN
                && robotInfo.getInfluence() >= POLITICIAN_EC_BOMB 
                && (enemyEnlightenmentCenterIsAround || neutralEnlightenmentCenterIsAround)) 
            {
                politicianECBombNearby = true;    
            }
            else if (robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER) 
            {
                Sense.processEnlightenmentCenterFinding(robotInfo);
                if (robotInfo.getTeam() == friendly 
                    && robotController.getLocation().subtract(robotController.getLocation().directionTo(robotInfo.getLocation())).distanceSquaredTo(spawnEnlightenmentCenterHomeLocation)
                         < sensorRadiusSquared )
                {
                    nearFriendlyEnlightenmentCenter = true;
                }                
            }
            else if (robotInfo.getTeam() == friendly)
            {
                if (robotController.canGetFlag(robotInfo.getID())) 
                {
                    checkNearbyFlagsForEnemy(robotController.getFlag(robotInfo.getID()));
                }                

                if (robotInfo.getType() == RobotType.POLITICIAN
                && robotInfo.getInfluence() <= POLITICIAN_DEFEND_SLANDERER
                && robotInfo.getInfluence() >= POLITICIAN_SCOUT)
                {
                    closestDefenderPoliticianMapLocation = robotInfo.getLocation();
                }
            }
        }
    }

    private static void checkNearbyFlagsForEnemy(int flag) throws GameActionException 
    {
        int extraInformation = Communication.getExtraInformationFromFlag(flag);

        if (flag != 0 && extraInformation == Communication.ENEMY_MUCKRAKER_NEARBY_FLAG) 
        {
            enemyMuckrakersNearby = true;
            enemyMuckrakerLocationFromFlag = Communication.getLocationFromFlag(flag);    
        }
    }

    private static void setFlags() throws GameActionException 
    {
        if (robotController.getRoundNum() % 2 == 0) 
        {
            if (robotController.canSetFlag(slandererFlag)) 
            {
                robotController.setFlag(slandererFlag);    
            }    
        }
        else
        {
            if (haveMessageToSend) 
            {
                Communication.setFlagMessageForScout();
            }            
        }
    }

    private static void setEnemyMuckrakersVariable() 
    {
        if (enemyMuckrakersNearby || (recentlySeenMuckrakers > 0 && recentlySeenMuckrakers < 5)) 
        {
            recentlySeenMuckrakers++;
            lastSeenEnemyMuckraker = closestEnemyMuckraker;    
        }
        else if(recentlySeenMuckrakers >= 4)
        {
            recentlySeenMuckrakers = 0;
            lastSeenEnemyMuckraker = null; 
        }
    }

    private static void stayNearHomeBase() 
    {
        int sensorRadiusSquared = robotController.getType().sensorRadiusSquared;
        if ((robotController.getLocation().subtract(robotController.getLocation().directionTo(spawnEnlightenmentCenterHomeLocation)).distanceSquaredTo(spawnEnlightenmentCenterHomeLocation) 
            < sensorRadiusSquared || nearFriendlyEnlightenmentCenter)) 
        {
            nextDirection = null;
        } 
        else if (robotController.getLocation().isAdjacentTo(spawnEnlightenmentCenterHomeLocation))
        {
            nextDirection = spawnEnlightenmentCenterHomeLocation.directionTo(robotController.getLocation());
        }
        else 
        {
            nextDirection = robotController.getLocation().directionTo(spawnEnlightenmentCenterHomeLocation);
        }
    }

    protected static void setEdgeOfMap()
    {
        if (directionToEdgeOfMap != null) 
        {
            directionToScout = directionToEdgeOfMap;
        }        
    }

    protected static void moveToEdgeOfTheMap() throws GameActionException
    {
        if (robotController.canSenseLocation(mapLocationOfEdge)
            && !robotController.onTheMap(mapLocationOfEdge) 
            && robotController.onTheMap(robotController.getLocation().add(robotController.getLocation().directionTo(mapLocationOfEdge)))
            && !robotController.getLocation().isAdjacentTo(spawnEnlightenmentCenterHomeLocation))
        {
            Movement.moveToTargetLocation(mapLocationOfEdge);
        }           
        else if(robotController.getLocation().isAdjacentTo(spawnEnlightenmentCenterHomeLocation))
        {
            Movement.scoutTheDirection(Movement.getRandomDirection());
        } 
        else if (robotController.onTheMap(robotController.getLocation().add(robotController.getLocation().directionTo(mapLocationOfEdge))))
        {
            Movement.moveToTargetLocation(mapLocationOfEdge);
        }
        else 
        {
            stayNearHomeBase();
            if (nextDirection == null) 
            {
                Direction randomDirection = Movement.getRandomDirection();
                while (robotController.getLocation().add(randomDirection).isAdjacentTo(spawnEnlightenmentCenterHomeLocation)) 
                {
                    randomDirection = Movement.getRandomDirection();
                }

                Movement.scoutTheDirection(randomDirection);
            } 
            else 
            {
                Movement.scoutTheDirection(nextDirection);
            }
        }
    }

    public static void setup() 
    {
        setConstants();
        assignHomeEnlightenmentCenterLocation();
        setSquaresAroundEnlightenmentCenter();   
        robotCurrentInfluence = robotController.getInfluence();
        assignRobotRole();
        
    }

    private static void assignRobotRole() 
    {
        robotRole = RobotRoles.PoliticianEnlightenmentCenterBomb;
    }
}
