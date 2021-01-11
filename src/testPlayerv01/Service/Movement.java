package testPlayerv01.Service;

import java.util.ArrayList;
import java.util.List;

import battlecode.common.*;
import testPlayerv01.RobotPlayer;

public class Movement extends RobotPlayer
{
    static final Direction[] flipDirections = 
    {
        Direction.NORTHEAST,
        Direction.SOUTHEAST,
        Direction.SOUTHWEST,
        Direction.NORTHWEST,
    };

    private static List<MapLocation> stuckLocations = new ArrayList<>();
    
    public static void basicBugMovement(MapLocation target) throws GameActionException
    {
        MapLocation currentLocation = robotController.getLocation();
        Direction direction = robotController.getLocation().directionTo(target);

        if(!currentLocation.equals(target) && robotController.isReady())
        {
            if (canRobotMoveThroughTile(direction, currentLocation))
            {
                robotController.move(direction);
                bugDirection = null;
            }
            else if (robotController.onTheMap(robotController.adjacentLocation(direction)))
            {
                if (bugDirection == null) {
                    bugDirection = direction;
                }

                stuckLocations.add(robotController.getLocation());                

                for (MapLocation stuckLocation : stuckLocations) 
                {               
                    if (stuckLocation == robotController.getLocation()) 
                    {
                        // Rotate Left
                        for (int i = 0; i < 8; ++i) 
                        {
                            if (canRobotMoveThroughTile(bugDirection, currentLocation)) 
                            {
                                robotController.move(bugDirection);
                                bugDirection = bugDirection.rotateRight();
                                break;
                            }    

                            bugDirection = bugDirection.rotateLeft();
                        }
                    }                
                    else
                    {
                        // Rotate Right
                        for (int i = 0; i < 8; ++i) 
                        {
                            if (canRobotMoveThroughTile(bugDirection, currentLocation)) 
                            {
                                robotController.move(bugDirection);
                                bugDirection = bugDirection.rotateLeft();
                                break;
                            }       
    
                            bugDirection = bugDirection.rotateRight();
                        }
                    }    
                }            
            }
            else
            {
                // Location is not on map. Reached an edge. 
                for (Direction randomDirection : directions) 
                {
                    if (robotController.onTheMap(currentLocation.add(randomDirection)))
                    {
                        if (robotController.canMove(randomDirection) 
                        && !robotController.isLocationOccupied(robotController.getLocation().add(randomDirection)))
                        {
                            robotController.move(randomDirection);
                        }
                        
                        // change direction to scout
                        directionToScout = randomDirection;
                        break;
                    }
                }
            }
        }
        else if(robotController.isReady())
        {
            targetInSight = true;
            // run BFS pathfinding
        }
    }

    static boolean canRobotMoveThroughTile(Direction direction, MapLocation currentLocation) throws GameActionException
    {
        if(robotController.canSenseLocation(currentLocation.add(direction)))
        {
            return robotController.canMove(direction) && robotController.sensePassability(currentLocation.add(direction)) >= passabilityThreshold;
        }
        else
        {
            return false;
        }        
    }

    static void scoutTheDirection(Direction direction) throws GameActionException
    {
        if (robotController.canMove(direction)) 
        {
            robotController.move(direction);
        }
        else
        {
            basicBugMovement(robotController.getLocation().add(direction));
        }
    }

    public static void scoutAction() throws GameActionException
    {
        if(targetLocation != null)
        {
            Movement.basicBugMovement(targetLocation);
        }
        else if (directionToScout != null) 
        {
            scoutTheDirection(directionToScout);
        }
        else if (locationToScout == null) 
        {
            // TODO: What's a better way of randomizing this?
            if (robotController.getLocation().x % 2 == 0) {
                Movement.mirrorDirection();
            }
            else
            {
                Movement.flipDirection();
            }
        }
        else
        {
            Movement.basicBugMovement(locationToScout);
        }
    }

    public static void mirrorDirection() throws GameActionException
    {
        MapLocation currentLocation = robotController.getLocation();

        if (currentLocation.y % 2 == 0) 
        {
            locationToScout = currentLocation.translate(32, 0);
        }
        else
        {
            locationToScout = currentLocation.translate(-32, 0);
        } 
    }

    public static void flipDirection() throws GameActionException
    {
        MapLocation currentLocation = robotController.getLocation();

        Direction directionToScout = flipDirections[(int) (Math.random() * flipDirections.length)];
        locationToScout = currentLocation.add(directionToScout);

        for (int i = 0; i < 32; i++) {
            locationToScout = locationToScout.add(directionToScout);
        }

        // if (currentLocation.y % 2 == 0) 
        // {
        //     locationToScout = currentLocation.translate(-32, -32);
        // }
        // else
        // {
        //     locationToScout = currentLocation.translate(32, 32);
        // } 
    }   

    public static void moveAwayFromLocation(MapLocation locationToMoveAwayFrom) throws GameActionException
    {
        Direction directionToMoveAway = robotController.getLocation().directionTo(locationToMoveAwayFrom);
        MapLocation locationToMoveAwayTo = locationToMoveAwayFrom.subtract(directionToMoveAway);

        basicBugMovement(locationToMoveAwayTo);
    }
}