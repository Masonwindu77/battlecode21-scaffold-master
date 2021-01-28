package testPlayerv09;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import battlecode.common.*;

@SuppressWarnings("unused")
public strictfp class RobotPlayer {
    protected static RobotController robotController;

    static final RobotType[] spawnableRobot = { RobotType.POLITICIAN, RobotType.SLANDERER, RobotType.MUCKRAKER, };

    public static final Direction[] directions = { Direction.NORTH, Direction.NORTHEAST, Direction.EAST,
            Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, };

    public static enum RobotRoles {
        PoliticianEnlightenmentCenterBomb, // influence >= 100
        SlandererAttacker, // Muckraker & Polis team
        Scout, // Muckraker OR Polis
        //DefendHomeEnlightenmentCenter, // Muckraker w/influence > 1
        //Leader, // Polis w/ 20 influence
        //Follower, // Polis w/ < 20
        DefendSlanderer, // Muckraker will scout
        Converted,
    }

    public static void println(String string) {
        if (debug) {
            System.out.println(string);
        }
    }

    static int turnCount;
    static boolean debug = true;

    protected static Random randomInteger;
    protected static boolean rightBugMovement = true;
    protected static boolean leftBugMovement;

    protected static Team enemy;
    protected static Team friendly;
    protected static double empowerFactor = 1;
    static final int POLITICIAN_TAX = 10;
    protected static final int AMOUNT_OF_INFLUENCE_TO_NOT_EMPOWER_SELF = 1000000;
    
    protected static boolean haveMessageToSend = false;    
    protected static int turnsAroundEnemyEnlightenmentCenter = 0;
    protected static int turnsAroundNeutralEnlightenmentCenter = 0;    
    protected static int messageLastTwoTurnsForConverted;

    // Saving Bytecode
    protected static boolean convertedEnemyEnlightenmentCenterHasBeenProcessedThisTurn = false;

    // Robotinfo
    protected static int robotCurrentInfluence;
    protected static int robotCurrentConviction;
    protected static boolean lowestRobotIdOfFriendlies;
    protected static final int ACTION_RADIUS_POLITICIAN = 9;

    // Enemy
    protected static int enemyEnlightenmentCenterCurrentInfluence;
    protected static boolean enemyEnlightenmentCenterInfluenceHasBeenUpdated;
    protected static int convertedEnemyIterator = 0;
    protected static int convertedNeutralIterator = 0;


    // Roles
    protected static RobotRoles robotRole;
    protected static final int POLITICIAN_EC_BOMB = 30;
    protected static final int POLITICIAN_DEFEND_SLANDERER = 20;
    protected static final int POLITICIAN_SCOUT = 15;
    protected static final int INFLUENCE_FOR_SCOUT = 1;
    protected static final int INFLUENCE_FOR_DEFEND_SLANDERER_MUCKRAKER = 3;

    // Troops
    protected static boolean politicianECBombNearby;

    // Movement
    protected static boolean targetInSight = false;
    protected static final double passabilityThreshold = 0.15;
    protected static Direction bugDirection = null;
    protected static MapLocation locationToScout = null;
    protected static MapLocation targetLocation = null;
    protected static Direction directionToScout = null;
    protected static MapLocation stuckLocation = null;

    // Enemy Enlightenment Center
    protected static List<MapLocation> enemyEnlightenmentCenterMapLocation = new ArrayList<>();
    protected static List<MapLocation> convertedEnemyEnlightenmentCenterMapLocation = new ArrayList<>();
    protected static boolean enemyEnlightenmentCenterFound = false;
    protected static MapLocation currentEnemyEnlightenmentCenterGoingFor;
    protected static boolean enemyEnlightenmentCenterHasBeenConverted = false;
    protected static boolean enemyEnlightenmentCenterIsAround;

    // Home Enlightenment Center
    public static MapLocation spawnEnlightenmentCenterHomeLocation;
    public static int spawnEnlightenmentCenterRobotId;
    protected static int currentEnlightenmentCenterFlag;
    protected static MapLocation[] squaresAroundEnlightenmentCenter = new MapLocation[8];

    // Neutral Enlightenment Center
    protected static boolean neutralEnlightenmentCenterFound = false;
    protected static List<MapLocation> neutralEnlightenmentCenterMapLocation = new ArrayList<>();
    protected static MapLocation currentNeutralEnlightenmentCenterGoingFor;
    protected static List<MapLocation> convertedNeutralEnlightenmentCenterMapLocation = new ArrayList<>();
    protected static boolean neutralEnlightenmentCenterHasBeenConverted;
    protected static int neutralEnlightenmentCenterCurrentInfluence;
    protected static boolean neutralEnlightenmentCenterIsAround;

    protected static final int MIDDLE_GAME_ROUND_START = 600;
    protected static final int END_GAME_ROUND_STRAT = 1000;

    // POLITICIAN
    public static final int MIN_NORMAL_POLITICIAN = 12;
    protected static final int MAX_NORMAL_POLITICIAN = 20;
    protected static boolean moveRobot;

    // TODO: ROLES in Flags?

    /**
     * run() is the method that is called when a robot is instantiated in the
     * Battlecode world. If this method returns, the robot dies!
     **/

    public static void run(RobotController robotController) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this
        // robot,
        // and to get information on its current status.
        RobotPlayer.robotController = robotController;

        turnCount = 0;

        switch (robotController.getType()) {
            case ENLIGHTENMENT_CENTER:
                EnlightenmentCenterTest01.setup();
                break;
            case POLITICIAN:
                PoliticianTest01.setup();
                break;
            case SLANDERER:
                Slanderer.setup();
                break;
            case MUCKRAKER:
                MuckrakerTest01.setup();
                break;
        }

        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each
                // RobotType.
                // You may rewrite this into your own control structure if you wish.

                switch (robotController.getType()) {
                    case ENLIGHTENMENT_CENTER:
                        EnlightenmentCenterTest01.run();
                        break;
                    case POLITICIAN:
                        PoliticianTest01.run();
                        break;
                    case SLANDERER:
                        Slanderer.run();
                        break;
                    case MUCKRAKER:
                        MuckrakerTest01.run();
                        break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform
                // this loop again
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
    static boolean tryMove(Direction dir) throws GameActionException {
        if (robotController.canMove(dir)) {
            robotController.move(dir);
            return true;
        } else
            return false;
    }

    protected static void setConstants()
    {
        enemy = robotController.getTeam().opponent();
        robotCurrentInfluence = robotController.getInfluence();
        friendly = robotController.getTeam();
        randomInteger = new Random();
        if (randomInteger.nextInt(1) == 0) 
        {
            rightBugMovement = true;  
        }
        else 
        {
            leftBugMovement = true; 
        }
    }

    static void assignHomeEnlightenmentCenterLocation() 
    {
        RobotInfo[] robots = robotController.senseNearbyRobots(robotController.getType().sensorRadiusSquared);
        for (RobotInfo robotInfo : robots) 
        {
            if (robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER && robotInfo.getTeam() == friendly) 
            {
                spawnEnlightenmentCenterHomeLocation = robotInfo.getLocation();
                spawnEnlightenmentCenterRobotId = robotInfo.getID();
            }
        }
    }  

    protected static void setSquaresAroundEnlightenmentCenter() 
    {
        int iterator = 0;
        for (Direction direction : directions) 
        {
            squaresAroundEnlightenmentCenter[iterator] = spawnEnlightenmentCenterHomeLocation.add(direction);
            ++iterator;
        }
    }
}
