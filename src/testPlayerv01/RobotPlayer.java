package testPlayerv01;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import battlecode.common.*;

 @SuppressWarnings("unused")
public strictfp class RobotPlayer {
    protected static RobotController robotController;

    static final RobotType[] spawnableRobot = {
        RobotType.POLITICIAN,
        RobotType.SLANDERER,
        RobotType.MUCKRAKER,
    };

    public static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    public static enum RobotRoles{
        PoliticianEnlightenmentCenterBomb, // influence >= 100
        SlandererAttacker, // Muckraker & Polis team
        Scout, // Muckraker OR Polis
        DefendHomeEnlightenmentCenter, // Muckraker w/influence > 1
        Leader, // Polis w/ 20 influence
        Follower, // Polis w/ < 20
        DefendSlanderer, // Muckraker will scout
    }

    public static void println(String string)
    {
        if (debug) 
        {
            System.out.println(string);
        }		
    }    

    static int turnCount;
    static boolean debug = true;

    protected static Random randomInteger;

    protected static Team enemy;
    protected static Team friendly;
    protected static double empowerFactor = 1;
    static final int POLITICIAN_TAX = 10;

    static boolean moveRobot;

    // Robotinfo
    static int robotCurrentInfluence;
    static int robotCurrentConviction;
    static boolean lowestRobotIdOfFriendlies;
    static final int ACTION_RADIUS_POLITICIAN = 9;

    // Enemy
    protected static int enemyEnlightenmentCenterCurrentInfluence;

    // Roles
    protected static RobotRoles robotRole;
    protected static final int POLITICIAN_EC_BOMB = 160;
    protected static final int POLITICIAN_LEADER = 20;
    protected static final int POLITICIAN_FOLLOWER = 12;
    public static final int INFLUENCE_FOR_SCOUT = 1;
    protected static final int INFLUENCE_FOR_DEFEND_SLANDERER_MUCKRAKER = 3;

    // Troops
    static boolean politicianECBombNearby;

    // Movement
    protected static boolean targetInSight = false;
    protected static final double passabilityThreshold = 0.15;
    protected static Direction bugDirection = null;
    protected static MapLocation locationToScout = null;
    protected static MapLocation targetLocation = null;
    protected static Direction directionToScout = null;
    protected static MapLocation stuckLocation = null;

    // Enemy Enlightenment Center
    static Map<Integer, MapLocation> enemyEnlightenmentCenterMapLocation = new HashMap<Integer, MapLocation>();
    static boolean enemyEnlightenmentCenterFound = false;
    static MapLocation currentEnemyEnlightenmentCenterGoingFor;

    // Home Enlightenment Center
    public static MapLocation enlightenmentCenterHomeLocation;
    public static int spawnEnlightenmentCenterRobotId;
    private static int currentEnlightenmentCenterFlag;

    static final int MIDDLE_GAME_ROUND_START = 400;
    static final int END_GAME_ROUND_STRAT = 800;

    // Sending Flags
    static final int NBITS = 7;
    static final int BITMASK = (1 << NBITS) - 1;
    protected static boolean haveMessageToSend = false;
    static final int[][] translateCoordinates = {
        {-(1 << NBITS),0}, 
        {(1 << NBITS), 0}, 
        {0, -(1 << NBITS)}, 
        {0, (1 << NBITS)}};

    // Max bit is 2^24
    // 10000 to 30000    
    // Signals
    static final int ENEMY_ENLIGHTENMENT_CENTER_FOUND = 11;
    static final int KILL_ENEMY_TARGET = 12;
    static final int ENEMY_ENLIGHTENMENT_CENTER_CONVERTED = 13;
    static final int ENEMY_ENLIGHTENMENT_CENTER_INFLUENCE = 14;
    static final int RECEIVED_MESSAGE = 99;

    // POLITICIAN
    static final int MIN_NORMAL_POLITICIAN = 12;
    static final int MAX_NORMAL_POLITICIAN = 20;

    // TODO: ROLES in Flags? 

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    
    public static void run(RobotController robotController) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.robotController = robotController;
        
        turnCount = 0;
        
        switch (robotController.getType()) 
        {
            case ENLIGHTENMENT_CENTER: EnlightenmentCenterTest01.setup(); break;
            case POLITICIAN:           PoliticianTest01.setup();          break;
            case SLANDERER:            Slanderer.setup();                 break;
            case MUCKRAKER:            MuckrakerTest01.setup();           break;
        }

        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.

                switch (robotController.getType()) {
                    case ENLIGHTENMENT_CENTER: EnlightenmentCenterTest01.run(); break;
                    case POLITICIAN:           PoliticianTest01.run();          break;
                    case SLANDERER:            Slanderer.run();                 break;
                    case MUCKRAKER:            MuckrakerTest01.run();           break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                println(robotController.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }
    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random spawnable RobotType
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnableRobotType() {
        return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException 
    {
        if (robotController.canMove(dir)) {
            robotController.move(dir);
            return true;
        } else return false;
    }

    static void assignHomeEnlightenmentCenterLocation()
    {
        RobotInfo[] robots = robotController.senseNearbyRobots();
        for (RobotInfo robotInfo : robots) {
            if (robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                enlightenmentCenterHomeLocation = robotInfo.getLocation();
                spawnEnlightenmentCenterRobotId = robotInfo.getID();
            }
        }
    }

    static void checkIfSpawnEnlightenmentCenterHasEnemyLocation() throws GameActionException
    {
        if(robotController.canGetFlag(spawnEnlightenmentCenterRobotId))
        {
            currentEnlightenmentCenterFlag = robotController.getFlag(spawnEnlightenmentCenterRobotId);

            if(currentEnlightenmentCenterFlag != 0 && checkIfEnemeyEnlightenmentCenterHasBeenFound(currentEnlightenmentCenterFlag))
            {
                MapLocation enemyEnlightenmentCenterLocation = getLocationFromFlag(currentEnlightenmentCenterFlag);
                if (!enemyEnlightenmentCenterMapLocation.containsValue(enemyEnlightenmentCenterLocation)) 
                {
                    enemyEnlightenmentCenterMapLocation.put(enemyEnlightenmentCenterMapLocation.size() + 1, enemyEnlightenmentCenterLocation);
                    enemyEnlightenmentCenterFound = true; // TODO:see if this is a good idea...
                    currentEnemyEnlightenmentCenterGoingFor = enemyEnlightenmentCenterLocation;
                }
                
            }
        }
    }

	protected static boolean checkIfEnemeyEnlightenmentCenterHasBeenFound(int flag) throws GameActionException
    {
        boolean foundTheCenter = false;
        int extraInformation = flag >> (2*NBITS);
        
        if (extraInformation == ENEMY_ENLIGHTENMENT_CENTER_FOUND) 
        {
            foundTheCenter = true;
        }

        return foundTheCenter;
    }

    protected static int getEnemyEnlightenmentCenterInfluenceFromFlag(int flag) throws GameActionException
    {
        int enemyEnlightenmentCenterCurrentInfluence = flag - (flag >> 2*NBITS);
        println("Current Influence ENEMY: " + enemyEnlightenmentCenterCurrentInfluence);

        return enemyEnlightenmentCenterCurrentInfluence;
    }

    protected static boolean checkIfEnemeyEnlightenmentCenterHasBeenConverted(int flag) throws GameActionException
    {
        boolean hasBeenConverted = false;
        int extraInformation = flag >> (2*NBITS);

        if (extraInformation == ENEMY_ENLIGHTENMENT_CENTER_CONVERTED) 
        {
            hasBeenConverted = true;
        }

        return hasBeenConverted;
    }

    protected static boolean enemyEnlightenmentCenterHasBeenConverted() throws GameActionException
    {
        boolean hasBeenConverted = false;
        if (robotController.canSenseLocation(currentEnemyEnlightenmentCenterGoingFor)) 
        {
            RobotInfo[] robots = robotController.senseNearbyRobots();
            for (RobotInfo robotInfo : robots) 
            {
                if (robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER 
                && robotInfo.getTeam() == friendly
                && robotInfo.getLocation() == currentEnemyEnlightenmentCenterGoingFor) 
                {
                    hasBeenConverted = true;
                }
            }
        }

        return hasBeenConverted;
    }

    static void sendLocation(MapLocation location,  int extraInformation) throws GameActionException
    {
        int x = location.x;
        int y = location.y;
        int encodedLocation = (extraInformation << (2*NBITS)) + ((x & BITMASK) << NBITS) + (y & BITMASK);

        if (robotController.canSetFlag(encodedLocation)) 
        {
            robotController.setFlag(encodedLocation);    
        }
    }

    static void sendLocationWithoutInfluence(MapLocation location, int extraInformation) throws GameActionException
    {
        int x = location.x;
        int y = location.y;
        int encodedLocation = (extraInformation << (2*NBITS)) + ((x & BITMASK) << NBITS) + (y & BITMASK);

        if (robotController.canSetFlag(encodedLocation)) 
        {
            robotController.setFlag(encodedLocation);    
        }
    }

    static MapLocation getLocationFromFlag(int flag) throws GameActionException
    {
        int y = flag & BITMASK;
        int x = (flag >> NBITS) & BITMASK;
        MapLocation currentLocation = robotController.getLocation();

        int offsetX128 = currentLocation.x >> NBITS;
        int offsetY128 = currentLocation.y >> NBITS;

        MapLocation actualLocation = new MapLocation((offsetX128 << NBITS) + x, (offsetY128 << NBITS)  + y);
        MapLocation alternative = actualLocation;

        for (int iterator = 0; iterator < translateCoordinates.length; ++iterator) 
        {
            for (int innerIterator = 0; innerIterator < 1; ++innerIterator) {
                alternative = actualLocation.translate(translateCoordinates[iterator][innerIterator], translateCoordinates[iterator][innerIterator + 1]);

                if (currentLocation.distanceSquaredTo(alternative) < currentLocation.distanceSquaredTo(actualLocation)) {
                    actualLocation = alternative;
                }
            }            
        }

        return actualLocation;
    }

}
