package testPlayerv01.Service;

import battlecode.common.*;
import testPlayerv01.RobotPlayer;

public class Movement extends RobotPlayer {
    static final Direction[] flipDirections = { Direction.NORTHEAST, Direction.SOUTHEAST, Direction.SOUTHWEST,
            Direction.NORTHWEST, };

    static final Direction[] mirrorDirections = { Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH, };

    public static void basicBugMovement(MapLocation target) throws GameActionException 
    {
        MapLocation currentLocation = robotController.getLocation();
        Direction direction = robotController.getLocation().directionTo(target);

        if (!currentLocation.equals(target) && robotController.isReady()) 
        {
            if (canRobotMoveThroughTile(direction, currentLocation)) 
            {
                robotController.move(direction);
                bugDirection = null;
            } 
            else if (!canRobotMoveThroughTile(direction, currentLocation) && rightBugMovement) 
            {
                if (bugDirection == null) {
                    bugDirection = direction;
                }
                for (int i = 0; i < 8; ++i) {
                    if (robotController.canMove(bugDirection) && robotController.sensePassability(robotController.getLocation().add(bugDirection)) >= passabilityThreshold) 
                    {
                        robotController.setIndicatorDot(robotController.getLocation().add(bugDirection), 0, 255, 255);
                        robotController.move(bugDirection);
                        bugDirection = bugDirection.rotateRight();
                        break;
                    }
                    robotController.setIndicatorDot(robotController.getLocation().add(bugDirection), 255, 0, 0);
                    bugDirection = bugDirection.rotateLeft();
                }
            }
            else if (!canRobotMoveThroughTile(direction, currentLocation) && leftBugMovement) 
            {
                if (bugDirection == null) {
                    bugDirection = direction;
                }

                for (int i = 0; i < 8; ++i) {
                    if (robotController.canMove(bugDirection) && robotController.sensePassability(robotController.getLocation().add(bugDirection)) >= passabilityThreshold) 
                    {
                        robotController.setIndicatorDot(robotController.getLocation().add(bugDirection), 0, 255, 255);
                        robotController.move(bugDirection);
                        bugDirection = bugDirection.rotateLeft();
                        break;
                    }
                    robotController.setIndicatorDot(robotController.getLocation().add(bugDirection), 255, 0, 0);
                    bugDirection = bugDirection.rotateRight();
                }
            }
            else if (!robotController.onTheMap(robotController.adjacentLocation(direction))) 
            {
                if (bugDirection == null) {
                    bugDirection = direction;
                }
                
                // stuckCount++;
                // TODO: Do BFS for getting around stuff...

                while(bugDirection == direction || !canRobotMoveThroughTile(bugDirection, currentLocation))
                {
                    bugDirection = getRandomDirection();
                    if (canRobotMoveThroughTile(bugDirection, currentLocation)) // TODO: Test stuck location..
                    {
                        robotController.move(bugDirection);
                        break;
                    }
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
            // else if (robotController.canMove(direction)) 
            // {
            //     robotController.move(direction);
            // } 
            else 
            {
                // Location is not on map. Reached an edge.
                while (directionToScout == direction) 
                {
                    Direction randomDirection = getRandomDirection();
                    if (robotController.onTheMap(currentLocation.add(randomDirection))) 
                    {
                        if (robotController.canMove(randomDirection))                         
                        {
                            robotController.move(randomDirection);
                            // change direction to scout
                            directionToScout = randomDirection;
                            break;
                        }                    
                    }
                }
            }
        } 
        else if (robotController.isReady()) 
        {
            targetInSight = true;
            // run BFS pathfinding
        }
    }

    static boolean canRobotMoveThroughTile(Direction direction, MapLocation currentLocation)
            throws GameActionException 
        {
            return robotController.canMove(direction);
        // if (robotController.canSenseLocation(currentLocation.add(direction))) {
        //      // &&
        //                                                // robotController.sensePassability(currentLocation.add(direction))
        //                                                // >= passabilityThreshold; // TODO: Need to fix movement still.
        // } else {
        //     return false;
        // }
    }

    public static void scoutTheDirection(Direction direction) throws GameActionException {
        if (robotController.onTheMap(robotController.adjacentLocation(direction))) 
        {
            basicBugMovement(robotController.getLocation().add(direction));
        }
        // Checking if still near home base and if found a wall.
        else
        {
            directionToScout = getRandomDirection();

            if (spawnEnlightenmentCenterHomeLocation != null 
                && robotController.canSenseLocation(spawnEnlightenmentCenterHomeLocation)) 
            {
                directionToScout = getOppositeDirection(direction);
            }    
            
            basicBugMovement(robotController.getLocation().add(directionToScout));
        }       
    }

    public static void scoutAction() throws GameActionException {
        // if(targetLocation != null)
        // {
        // Movement.basicBugMovement(targetLocation);
        // }
        // else
        if (directionToScout != null) 
        {
            scoutTheDirection(directionToScout);

        } else if (locationToScout != null) 
        {
            basicBugMovement(locationToScout);

        } 
        else 
        {
            // TODO: What's a better way of randomizing this?
            directionToScout = getRandomDirection();
            scoutTheDirection(directionToScout);
        }
    }

    public static Direction getRandomDirection() {
        int max = directions.length - 1;
        int min = 0;
        int randomNum = randomInteger.nextInt(max - min + 1) + min;

        return directions[randomNum];
    }

    public static void moveAwayFromLocation(MapLocation locationToMoveAwayFrom) throws GameActionException {
        Direction directionTowardsTarget = robotController.getLocation().directionTo(locationToMoveAwayFrom);
        Direction directionToMoveAway = getOppositeDirection(directionTowardsTarget);

        if (robotController.canMove(directionToMoveAway)) {
            robotController.move(directionToMoveAway);
        } else {
            scoutTheDirection(directionToMoveAway);
        }
    }

    public static Direction getOppositeDirection(Direction directionTowardsTarget) {
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

    public static void moveToNeutralEnlightenmentCenter(MapLocation neutralCenterLocation) throws GameActionException {
        Movement.basicBugMovement(neutralCenterLocation);
    }

    public static void moveToTargetLocation(MapLocation targetLocation) throws GameActionException
    {
        Movement.basicBugMovement(targetLocation);
    }

    public static void moveInFrontOfTarget(MapLocation targetLocation, MapLocation defendLocation)
            throws GameActionException
    {
        MapLocation adjacentMapLocation = targetLocation.subtract(targetLocation.directionTo(defendLocation));

        basicBugMovement(adjacentMapLocation);
    }
}