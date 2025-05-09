package ducks;

import battlecode.common.*;

import java.util.Random;

public strictfp class AttackDuck extends RobotPlayer { // Extending RobotPlayer
    RobotController rc;
    static final Direction[] directions = Direction.allDirections();
    Random rng = new Random();

    private Direction lastDirection; // Declare the lastDirection field
    private MapLocation[] oppositeSpawnLocations; // Array to store opposite spawn locations

    public AttackDuck(RobotController rc) {
        this.rc = rc;
        this.turnCount = 0;
    }

    @Override
    public void run() throws GameActionException {
        while (true) {
            performTurn();
            Clock.yield();
        }
    }

    public void performTurn() throws GameActionException {
        turnCount += 1;
        try {
            if (!rc.isSpawned()) {
                attemptToSpawn();
            } else {
                FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam());
                for (FlagInfo flag : flags) {
                    if (rc.canPickupFlag(flag.getLocation())) {
                        rc.pickupFlag(flag.getLocation());
                        break;
                    }
                }
                if (rc.hasFlag()) {
                    moveToAllySpawnLocation();
                } else {
                    updateEnemyRobots(rc);
                    RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
                    if (enemyRobots.length > 0) {
                        attackLowestHealthRobot(enemyRobots);
                    } else {
                        movingDecision();
                    }
                }
            }
        } catch (GameActionException e) {
            System.out.println("Exception caught in AttackDuck performTurn method.");
            e.printStackTrace();
        }
    }
    
    public void attackLowestHealthRobot(RobotInfo[] enemyRobots) throws GameActionException {
        // RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        RobotInfo lowestHealthRobot = enemyRobots[0];
        for (RobotInfo robot : enemyRobots) {
            if (robot.getHealth() < lowestHealthRobot.getHealth()) {
                lowestHealthRobot = robot;
            }
        }
        if (rc.canAttack(lowestHealthRobot.getLocation())) {
            rc.attack(lowestHealthRobot.getLocation());
        }
    }

    // function for moving in a specific direction, moving forward as much as
    // possible
    public void exploreMove() throws GameActionException {
        Direction dir;
        if (lastDirection != null && rc.canMove(lastDirection)) {
            dir = lastDirection; // Prioritize the same direction as the last turn
        } else {
            dir = directions[rng.nextInt(directions.length)]; // Choose a random direction
        }

        if (rc.canMove(dir)) {
            rc.move(dir);
            lastDirection = dir; // Update lastDirection to the current direction
        }
    }

    // function for moving towards the flag's position
    public void moveToFlagsLocation(FlagInfo[] flags) throws GameActionException {
        MapLocation flagLocation = flags[0].getLocation();
        Direction toFlag = rc.getLocation().directionTo(flagLocation);
        // move(toFlag);
        if (rc.canMove(toFlag)) {
            rc.move(toFlag);
        } else {
            // If the direct path is blocked, try to move in a random direction
            moveRandomly();
        }
    }

    // function to return if the robot id divisible by 7
    public boolean isDivisibleBySeven(int robotID) {
        return robotID % 7 == 0;
    }

    // function to make decision of moving depending on if there is a flag or not
    public void movingDecision() throws GameActionException {
        // if division by 7 is true, move to explore the map
        if (isDivisibleBySeven(rc.getID())) {
            exploreMove();
        } else {
            // if division by 7 is false, move towards the opposite spawn location
            moveToOppositeSpawnLocation();
        }

    }

    // Function to calculate the opposite locations of all ally spawn positions
    public MapLocation[] calculateOppositeSpawnLocations() throws GameActionException {
        MapLocation[] allySpawnLocations = rc.getAllySpawnLocations();
        int mapWidth = rc.getMapWidth();
        int mapHeight = rc.getMapHeight();
        MapLocation[] oppositeLocations = new MapLocation[allySpawnLocations.length];

        for (int i = 0; i < allySpawnLocations.length; i++) {
            MapLocation spawnLocation = allySpawnLocations[i];
            MapLocation oppositeLocation = new MapLocation(mapWidth - spawnLocation.x - 1,
                    mapHeight - spawnLocation.y - 1);
            oppositeLocations[i] = oppositeLocation;
        }

        return oppositeLocations;
    }

    // Function to move to the opposite spawn location based on the duck's ID
    public void moveToOppositeSpawnLocation() throws GameActionException {
        if (oppositeSpawnLocations != null && oppositeSpawnLocations.length > 0) {
            // Determine the target location based on the duck's ID
            int targetIndex = rc.getID() % oppositeSpawnLocations.length;
            MapLocation targetLocation = oppositeSpawnLocations[targetIndex];
            moveToTargetMapLocation(targetLocation);
        }
    }

    // Function to move to the opposite side of the map
    public void moveToTargetMapLocation(MapLocation targetLocation) throws GameActionException {
        Direction toTarget = rc.getLocation().directionTo(targetLocation);
        if (rc.canMove(toTarget)) {
            rc.move(toTarget);
        } else {
            // If the direct path is blocked, try to move in a random direction
            moveRandomly();
        }
    }

    // Function to move randomly if the direct path is blocked
    public void moveRandomly() throws GameActionException {
        Direction randomDirection = directions[rng.nextInt(directions.length)];
        if (rc.canMove(randomDirection)) {
            rc.move(randomDirection);
        }
    }

}
