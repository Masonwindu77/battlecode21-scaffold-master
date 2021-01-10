package testPlayerv01;
import java.util.HashMap;
import java.util.Map;

import battlecode.common.*;

 @SuppressWarnings("unused")
public strictfp class RobotPlayer {
    static RobotController robotController;

    static final RobotType[] spawnableRobot = {
        RobotType.POLITICIAN,
        //RobotType.SLANDERER,
        RobotType.MUCKRAKER,
    };

    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    static final int[][] translateCoordinates = {
        {-128,0}, 
        {128, 0}, 
        {0, -128}, 
        {0, 128}};

    static int turnCount;
    static boolean debug = true;

    static Map<Integer, MapLocation> enemyEnlightenmentCenterMapLocation = new HashMap<Integer, MapLocation>();

    private static MapLocation enlightenmentCenterHomeLocation;
    public static int spawnEnlightenmentCenterRobotId;
    private static int currentEnlightenmentCenterFlag;

    static final int NBITS = 7;
    static final int BITMASK = (1 << NBITS) - 1;

    private static int xOffset;
    private static int yOffset;

    protected static boolean messageReceived = false;
    protected static boolean haveMessageToSend = false;

    static final int MIDDLE_GAME_ROUND_START = 100;

    // Max bit is 16|77|72|16
    // 10000 to 30000    
    static final int ENEMY_ENLIGHTENMENT_CENTER_FOUND = 11;
    static final int ENEMY_ENLIGHTENMENT_CENTER_FOUND_X_COORDINATE = 112;
    static final int ENEMY_ENLIGHTENMENT_CENTER_FOUND_Y_COORDINATE = 113;
    static final int RECEIVED_MESSAGE = 99;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    
    public static void run(RobotController robotController) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.robotController = robotController;
        
        turnCount = 0;

        if (debug) {
            System.out.println("I'm a " + robotController.getType() + " and I just got created!");
        }
        
        switch (robotController.getType()) 
        {
            case ENLIGHTENMENT_CENTER: EnlightenmentCenterTest01.setup(); break;
            case POLITICIAN:           PoliticianTest01.setup();          break;
            case SLANDERER:             //runSlanderer();                  
            break;
            case MUCKRAKER:            MuckrakerTest01.setup();           break;
        }

        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                
                //---System.out.println("I'm a " + robotController.getType() + "! Location " + robotController.getLocation());

                switch (robotController.getType()) {
                    case ENLIGHTENMENT_CENTER: EnlightenmentCenterTest01.run(); break;
                    case POLITICIAN:           PoliticianTest01.run();          break;
                    case SLANDERER:            runSlanderer();           break;
                    case MUCKRAKER:            MuckrakerTest01.run();           break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(robotController.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    static void runSlanderer() throws GameActionException {
        tryMove(randomDirection());
            //System.out.println("I moved!");
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
    static boolean tryMove(Direction dir) throws GameActionException {
        // System.out.println("I am trying to move " + dir + 
        // "; IsReady:" + robotController.isReady() + 
        // " Cooldown" + robotController.getCooldownTurns() + 
        // " canMove:" + robotController.canMove(dir));

        if (robotController.canMove(dir)) {
            robotController.move(dir);
            return true;
        } else return false;
    }

    static void checkIfSpawnEnlightenmentCenterHasEnemyLocation(int flag) throws GameActionException
    {
        if(robotController.canGetFlag(spawnEnlightenmentCenterRobotId))
        {
            currentEnlightenmentCenterFlag = robotController.getFlag(spawnEnlightenmentCenterRobotId);

            if(checkIfEnemeyEnlightenmentCenterHasBeenFound(currentEnlightenmentCenterFlag))
            {
                MapLocation enemyCenterLocation = getLocationFromFlag(flag);
            }
            //checkIfSpawnEnlightenmentCenterHasEnemyLocation(currentEnlightenmentCenterFlag);
        }
        String flagOfEnlightenmentCenter = String.valueOf(flag);

        char[] arrayOfTheFlag = flagOfEnlightenmentCenter.toCharArray();

        int tryingThis = arrayOfTheFlag[0] + arrayOfTheFlag[1];

        if (tryingThis == ENEMY_ENLIGHTENMENT_CENTER_FOUND) {

            int x = arrayOfTheFlag[2] + arrayOfTheFlag[3];
            int y = arrayOfTheFlag[4] + arrayOfTheFlag[5];            
            MapLocation enemyEnlightenmentCenterLocation = new MapLocation(x, y);
            int iterator = enemyEnlightenmentCenterMapLocation.size()+1;

            enemyEnlightenmentCenterMapLocation.put(iterator, enemyEnlightenmentCenterLocation);
        }

    }

	protected static boolean checkIfEnemeyEnlightenmentCenterHasBeenFound(int flag) throws GameActionException
    {
        boolean foundTheCenter = false;
        int extraInformation = flag / 128 / 128;

        if (extraInformation == ENEMY_ENLIGHTENMENT_CENTER_FOUND) 
        {
            foundTheCenter = true;
        }

        return foundTheCenter;
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

    static void sendLocation(MapLocation location, int extraInformation) throws GameActionException
    {
        int x = location.x;
        int y = location.y;
        int encodedLocation = ((x & BITMASK) << NBITS) + (y & BITMASK) + (extraInformation * 128 * 128);

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

        int offsetX128 = currentLocation.x / 128; // TODO: Test, can I put bitmask here?
        int offsetY128 = currentLocation.y / 128;

        MapLocation actualLocation = new MapLocation(offsetX128 * 128 + x, offsetY128 * 128 + y);

        MapLocation alternative = actualLocation.translate(-128, 0);

        for (int iterator = 0; iterator < translateCoordinates.length; ++iterator) 
        {
            for (int innerIterator = 0; innerIterator < 1; ++innerIterator) {
                alternative = actualLocation.translate(translateCoordinates[iterator][innerIterator], translateCoordinates[iterator][innerIterator + 1]);

                if (currentLocation.distanceSquaredTo(alternative) < currentLocation.distanceSquaredTo(actualLocation)) {
                    actualLocation = alternative;
                    break;
                }
            }            
        }

        return actualLocation;
    }

}
