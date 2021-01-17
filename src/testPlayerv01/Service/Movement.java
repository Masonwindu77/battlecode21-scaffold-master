package testPlayerv01.Service;

import java.util.ArrayList;
import java.util.List;

import battlecode.common.*;
import testPlayerv01.RobotPlayer;

public class Movement extends RobotPlayer {
    static final Direction[] flipDirections = { Direction.NORTHEAST, Direction.SOUTHEAST, Direction.SOUTHWEST,
            Direction.NORTHWEST, };

    static final Direction[] mirrorDirections = { Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH, };

    private static int stuckCount = 0;
    private static List<MapLocation> stuckLocations = new ArrayList<>();

    public static void basicBugMovement(MapLocation target) throws GameActionException {
        MapLocation currentLocation = robotController.getLocation();
        Direction direction = robotController.getLocation().directionTo(target);

        if (!currentLocation.equals(target) && robotController.isReady()) {
            if (canRobotMoveThroughTile(direction, currentLocation)) {
                robotController.move(direction);
                bugDirection = null;
            } else if (robotController.onTheMap(robotController.adjacentLocation(direction))) {
                if (bugDirection == null) {
                    bugDirection = direction;
                }

                stuckLocations.add(robotController.getLocation());
                // stuckCount++;
                // TODO: Do BFS for getting around stuff...

                for (MapLocation stuckLocation : stuckLocations) {
                    if (!stuckLocation.equals(currentLocation.add(bugDirection))) {
                        for (int i = 0; i < 8; ++i) {
                            if (canRobotMoveThroughTile(bugDirection, currentLocation)) // TODO: Test stuck location..
                            {
                                robotController.move(bugDirection);
                                bugDirection = bugDirection.rotateLeft();
                                robotController.setIndicatorDot(robotController.getLocation().add(bugDirection), 0, 255,
                                        255);
                                break;
                            }

                            robotController.setIndicatorDot(robotController.getLocation().add(bugDirection), 255, 0, 0);
                            bugDirection = bugDirection.rotateRight();
                        }

                        break;
                    }
                }

                // for (MapLocation stuckLocation : stuckLocations)
                // {
                // // if (stuckLocation == robotController.getLocation())
                // // {
                // // Rotate Left
                // for (int i = 0; i < 8; ++i)
                // {
                // if (canRobotMoveThroughTile(bugDirection, currentLocation))
                // {
                // robotController.move(bugDirection);
                // bugDirection = bugDirection.rotateLeft();
                // robotController.setIndicatorDot(robotController.getLocation().add(bugDirection),
                // 0, 255, 255);
                // break;
                // }
                // robotController.setIndicatorDot(robotController.getLocation().add(bugDirection),
                // 255, 0, 0);
                // bugDirection = bugDirection.rotateRight();
                // }
                // //}
                // // else
                // // {
                // // // Rotate Right
                // // for (int i = 0; i < 8; ++i)
                // // {
                // // if (canRobotMoveThroughTile(bugDirection, currentLocation))
                // // {
                // // robotController.move(bugDirection);
                // // bugDirection = bugDirection.rotateLeft();
                // // break;
                // // }

                // // bugDirection = bugDirection.rotateRight();
                // // }
                // // }
                // }
            } else if (robotController.canMove(direction)) {
                robotController.move(direction);
                stuckCount = 0;
            } else {
                // Location is not on map. Reached an edge.
                while (directionToScout == direction) {
                    directionToScout = getRandomDirection();
                }

                for (Direction randomDirection : directions) {
                    if (robotController.onTheMap(currentLocation.add(randomDirection))) {
                        if (robotController.canMove(randomDirection) && !robotController
                                .isLocationOccupied(robotController.getLocation().add(randomDirection))) {
                            robotController.move(randomDirection);
                        }

                        // change direction to scout
                        directionToScout = randomDirection;
                        break;
                    }
                }
            }
        } else if (robotController.isReady()) {
            targetInSight = true;
            stuckCount = 0;
            // run BFS pathfinding
        }
    }

    static boolean canRobotMoveThroughTile(Direction direction, MapLocation currentLocation)
            throws GameActionException {
        if (robotController.canSenseLocation(currentLocation.add(direction))) {
            return robotController.canMove(direction); // &&
                                                       // robotController.sensePassability(currentLocation.add(direction))
                                                       // >= passabilityThreshold; // TODO: Need to fix movement still.
        } else {
            return false;
        }
    }

    public static void scoutTheDirection(Direction direction) throws GameActionException {
        basicBugMovement(robotController.getLocation().add(direction));
    }

    public static void scoutAction() throws GameActionException {
        // if(targetLocation != null)
        // {
        // Movement.basicBugMovement(targetLocation);
        // }
        // else
        if (directionToScout != null) {

            scoutTheDirection(directionToScout);

        } else if (locationToScout != null) {

            basicBugMovement(locationToScout);

        } else {
            // TODO: What's a better way of randomizing this?
            directionToScout = getRandomDirection();
        }
    }

    public static Direction getRandomDirection() {
        int max = directions.length - 1;
        int min = 0;
        int randomNum = randomInteger.nextInt(max - min + 1) + min;

        return directions[randomNum];
    }

    // public static void mirrorDirection() throws GameActionException {
    // int randomNum = randomInteger.nextInt((mirrorDirections.length - 1) - 0 + 1)
    // + 0;

    // directionToScout = mirrorDirections[randomNum];
    // }

    // public static void flipDirection() throws GameActionException {
    // int randomNum = randomInteger.nextInt((flipDirections.length - 1) - 0 + 1) +
    // 0;

    // directionToScout = flipDirections[randomNum];
    // }

    public static void moveAwayFromLocation(MapLocation locationToMoveAwayFrom) throws GameActionException {
        Direction directionTowardsTarget = robotController.getLocation().directionTo(locationToMoveAwayFrom);
        Direction directionToMoveAway = getOppositeDirection(directionTowardsTarget);

        if (robotController.canMove(directionToMoveAway)) {
            robotController.move(directionToMoveAway);
        } else {
            scoutTheDirection(directionToMoveAway);
        }
    }

    protected static Direction getOppositeDirection(Direction directionTowardsTarget) {
        Direction oppositeDirection = Direction.CENTER;

        if (directionTowardsTarget == Direction.NORTH) {
            oppositeDirection = Direction.SOUTH;
        } else if (directionTowardsTarget == Direction.NORTHWEST) {
            oppositeDirection = Direction.SOUTHEAST;
        } else if (directionTowardsTarget == Direction.NORTHEAST) {
            oppositeDirection = Direction.SOUTHWEST;
        } else if (directionTowardsTarget == Direction.EAST) {
            oppositeDirection = Direction.WEST;
        } else if (directionTowardsTarget == Direction.WEST) {
            oppositeDirection = Direction.EAST;
        } else if (directionTowardsTarget == Direction.SOUTHWEST) {
            oppositeDirection = Direction.NORTHEAST;
        } else if (directionTowardsTarget == Direction.SOUTHEAST) {
            oppositeDirection = Direction.NORTHWEST;
        } else if (directionTowardsTarget == Direction.SOUTH) {
            oppositeDirection = Direction.NORTH;
        }

        return oppositeDirection;
    }

    public static void moveToEnemyEnlightenmentCenter(MapLocation enemyCenterLocation) throws GameActionException {
        Movement.basicBugMovement(enemyCenterLocation);
    }
}