package testPlayerv01.Model;

import battlecode.common.*;

public class EnlightenmentCenterInfo
{
    public Team team;
    public MapLocation mapLocation;
    public int currentInfluence; 
    public int distanceSquaredToEnlightenmentCenter;
    public int robotId;
    public int[] robotIdThatSpottedEnlightenmentCenter = new int[20];
    public int robotIdIterator;
    public int numberOfRobotsGoingToIt;
}
