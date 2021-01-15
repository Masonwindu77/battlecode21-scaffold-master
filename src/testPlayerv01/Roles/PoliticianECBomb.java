package testPlayerv01.Service;
import battlecode.common.*;
import testPlayerv01.Service.Movement;

public class PoliticianECBomb extends PoliticianTest01
{
    // If has enemy ec location, 
    ///go to it and decide to attack or not
    
    // If location is friendly, setMessage to that
    
    // get ID of friendly EC && periodically check it responds. 
    /// check if spawn EC responds as well, if not, go back to it. 

    // Check spawn for new coordinates && if not new coordinates, 
    /// Scout around for enemies... 

    // If enemies found, get close and empower if can convert
    /// or if there are more than 1 enemies nearby. 

    static void run() throws GameActionException
    {
        robotCurrentConviction = robotController.getConviction();
        empowerFactor = robotController.getEmpowerFactor(friendly, 0);

        if (!enemyEnlightenmentCenterFound) 
        {
            checkIfSpawnEnlightenmentCenterHasEnemyLocation(); 
        }

        senseAreaForRobots();
        senseActionRadiusForRobots();

        decideIfEmpowerForEnlightenmentCenterBombs();

        if (enemyEnlightenmentCenterMapLocation.size() > 0 
        && (robotRole == RobotRoles.PoliticianEnlightenmentCenterBomb || robotController.getConviction() > MAX_NORMAL_POLITICIAN)
        && (!robotController.getLocation().isAdjacentTo(currentEnemyEnlightenmentCenterGoingFor) && !politicianECBombNearby))
        {
            Movement.moveToEnemyEnlightenmentCenter(currentEnemyEnlightenmentCenterGoingFor);                               
        }
    }
}