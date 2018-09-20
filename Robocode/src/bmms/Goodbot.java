package bmms;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

/*
 * SuperBoxBot - a SuperSampleBot by Exauge
 *
 * This robot demonstrates basic melee strategy such as
 *  -Picking targets
 *  -Melee movement strategy
 *
 *  It also demonstrates some of the Java API operators including
 *  remainder and ternary.
 *
 *  If you need help with any of these operators look here:
 *  http://download.oracle.com/javase/tutorial/java/nutsandbolts/opsummary.html
 *  A bit of googling always helps too.
 *
 * Movement:
 * This robot goes to the nearest corner, and then moves in a box.
 * There is a 15% chance it will change direction when the enemy fires.
 *
 * Targeting:
 * At first this robot will target the first robot it sees, but it switches
 * to any robot that hits it. It uses Head-on targeting which is popular
 * among lightweight melee bots.
 */
public class Goodbot extends AdvancedRobot {
    //
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


        public static int BINS = 47;

        // Segmentation
        // distance: 13 bins
        public static double[][] stats = new double[13][BINS];

        public Double _myLocation;
        public Double _enemyLocation;
        public Double _lastGoToPoint;
        public double direction = 1.0D;
        public ArrayList _enemyWaves;
        public ArrayList _surfDirections;
        public ArrayList _surfAbsBearings;
        public static double _oppEnergy;
        public static java.awt.geom.Rectangle2D.Double _fieldRect;
        public static double WALL_STICK;

        static {
            //_surfStats = new double[BINS];
            _oppEnergy = 100.0D;
            _fieldRect = new java.awt.geom.Rectangle2D.Double(18.0D, 18.0D, 764.0D, 564.0D);
            WALL_STICK = 160.0D;
        }



        public void run() {
            this._enemyWaves = new ArrayList();
            this._surfDirections = new ArrayList();
            this._surfAbsBearings = new ArrayList();
            this.setAdjustGunForRobotTurn(true);
            this.setAdjustRadarForGunTurn(true);

            while(true) {
                this.turnRadarRightRadians(1.0D / 0.0);
            }
        }

        public void onScannedRobot(ScannedRobotEvent e) {
            this._myLocation = new Double(this.getX(), this.getY());
            double lateralVelocity = this.getVelocity() * Math.sin(e.getBearingRadians());
            double absBearing = e.getBearingRadians() + this.getHeadingRadians();
            this.setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing - this.getRadarHeadingRadians()) * 2.0D);
            this._surfDirections.add(0, new Integer(lateralVelocity >= 0.0D ? 1 : -1));
            this._surfAbsBearings.add(0, new java.lang.Double(absBearing + 3.141592653589793D));
            double bulletPower = _oppEnergy - e.getEnergy();
            if (bulletPower < 3.01D && bulletPower > 0.09D && this._surfDirections.size() > 2) {
                Goodbot.EnemyWave ew = new Goodbot.EnemyWave();
                ew._surfStats = stats[(int)(e.getDistance() / 100)];
                ew.fireTime = this.getTime() - 1L;
                ew.bulletVelocity = bulletVelocity(bulletPower);
                ew.distanceTraveled = bulletVelocity(bulletPower);
                ew.direction = (Integer)this._surfDirections.get(2);
                ew.directAngle = (java.lang.Double)this._surfAbsBearings.get(2);
                ew.fireLocation = (Double)this._enemyLocation.clone();
                this._enemyWaves.add(ew);
                Math.min(3.0D, this.getEnergy());
                double myX = this.getX();
                double myY = this.getY();
                double absoluteBearing = this.getHeadingRadians() + e.getBearingRadians();
                double enemyX = this.getX() + e.getDistance() * Math.sin(absoluteBearing);
                double enemyY = this.getY() + e.getDistance() * Math.cos(absoluteBearing);
                double enemyHeading = e.getHeadingRadians();
                double enemyVelocity = e.getVelocity();
                double deltaTime = 0.0D;
                double battleFieldHeight = this.getBattleFieldHeight();
                double battleFieldWidth = this.getBattleFieldWidth();
                double predictedX = enemyX;
                double predictedY = enemyY;

                label32: {
                    do {
                        if (++deltaTime * (20.0D - 3.0D * bulletPower) >= Double.distance(myX, myY, predictedX, predictedY)) {
                            break label32;
                        }

                        predictedX += Math.sin(enemyHeading) * enemyVelocity;
                        predictedY += Math.cos(enemyHeading) * enemyVelocity;
                    } while(predictedX >= 18.0D && predictedY >= 18.0D && predictedX <= battleFieldWidth - 18.0D && predictedY <= battleFieldHeight - 18.0D);

                    predictedX = Math.min(Math.max(18.0D, predictedX), battleFieldWidth - 18.0D);
                    predictedY = Math.min(Math.max(18.0D, predictedY), battleFieldHeight - 18.0D);
                }

                double theta = Utils.normalAbsoluteAngle(Math.atan2(predictedX - this.getX(), predictedY - this.getY()));
                this.setTurnRadarRightRadians(Utils.normalRelativeAngle(absoluteBearing - this.getRadarHeadingRadians()));
                this.setTurnGunRightRadians(Utils.normalRelativeAngle(theta - this.getGunHeadingRadians()));
                this.fire(bulletPower);
            }

            _oppEnergy = e.getEnergy();
            this._enemyLocation = project(this._myLocation, absBearing, e.getDistance());
            this.updateWaves();
            this.doSurfing();
        }

        public void updateWaves() {
            for(int x = 0; x < this._enemyWaves.size(); ++x) {
                Goodbot.EnemyWave ew = (Goodbot.EnemyWave)this._enemyWaves.get(x);
                ew.distanceTraveled = (double)(this.getTime() - ew.fireTime) * ew.bulletVelocity;
                if (ew.distanceTraveled > this._myLocation.distance(ew.fireLocation) + 50.0D) {
                    this._enemyWaves.remove(x);
                    --x;
                }
            }

        }

        public Goodbot.EnemyWave getClosestSurfableWave() {
            double closestDistance = 50000.0D;
            Goodbot.EnemyWave surfWave = null;

            for(int x = 0; x < this._enemyWaves.size(); ++x) {
                Goodbot.EnemyWave ew = (Goodbot.EnemyWave)this._enemyWaves.get(x);
                double distance = this._myLocation.distance(ew.fireLocation) - ew.distanceTraveled;
                if (distance > ew.bulletVelocity && distance < closestDistance) {
                    surfWave = ew;
                    closestDistance = distance;
                }
            }

            return surfWave;
        }

        public static int getFactorIndex(Goodbot.EnemyWave ew, Double targetLocation) {
            double offsetAngle = absoluteBearing(ew.fireLocation, targetLocation) - ew.directAngle;
            double factor = Utils.normalRelativeAngle(offsetAngle) / maxEscapeAngle(ew.bulletVelocity) * (double)ew.direction;
            return (int)limit(0.0D, factor * (double)((BINS - 1) / 2) + (double)((BINS - 1) / 2), (double)(BINS - 1));
        }

        public void logHit(Goodbot.EnemyWave ew, Double targetLocation) {
            int index = getFactorIndex(ew, targetLocation);

            for(int x = 0; x < BINS; ++x) {
                ew._surfStats[x] += 1.0D / (Math.pow((double)(index - x), 2.0D) + 1.0D);
            }

        }

        public void onHitByBullet(HitByBulletEvent e) {
            if (!this._enemyWaves.isEmpty()) {
                Double hitBulletLocation = new Double(e.getBullet().getX(), e.getBullet().getY());
                Goodbot.EnemyWave hitWave = null;

                for(int x = 0; x < this._enemyWaves.size(); ++x) {
                    Goodbot.EnemyWave ew = (Goodbot.EnemyWave)this._enemyWaves.get(x);
                    if (Math.abs(ew.distanceTraveled - this._myLocation.distance(ew.fireLocation)) < 50.0D && Math.abs(bulletVelocity(e.getBullet().getPower()) - ew.bulletVelocity) < 0.001D) {
                        hitWave = ew;
                        break;
                    }
                }

                if (hitWave != null) {
                    this.logHit(hitWave, hitBulletLocation);
                    this._enemyWaves.remove(this._enemyWaves.lastIndexOf(hitWave));
                }
            }

        }

        public ArrayList predictPositions(Goodbot.EnemyWave surfWave, int direction) {
            Double predictedPosition = (Double)this._myLocation.clone();
            double predictedVelocity = this.getVelocity();
            double predictedHeading = this.getHeadingRadians();
            ArrayList traveledPoints = new ArrayList();
            int counter = 0;
            boolean intercepted = false;

            do {
                double distance = predictedPosition.distance(surfWave.fireLocation);
                double offset = 0.5707963267948966D + distance / 400.0D;
                double moveAngle = this.wallSmoothing(predictedPosition, absoluteBearing(surfWave.fireLocation, predictedPosition) + (double)direction * offset, direction) - predictedHeading;
                double moveDir = 1.0D;
                if (Math.cos(moveAngle) < 0.0D) {
                    moveAngle += 3.141592653589793D;
                    moveDir = -1.0D;
                }

                moveAngle = Utils.normalRelativeAngle(moveAngle);
                double maxTurning = 0.004363323129985824D * (40.0D - 3.0D * Math.abs(predictedVelocity));
                predictedHeading = Utils.normalRelativeAngle(predictedHeading + limit(-maxTurning, moveAngle, maxTurning));
                predictedVelocity += predictedVelocity * moveDir < 0.0D ? 2.0D * moveDir : moveDir;
                predictedVelocity = limit(-8.0D, predictedVelocity, 8.0D);
                predictedPosition = project(predictedPosition, predictedHeading, predictedVelocity);
                traveledPoints.add(predictedPosition);
                ++counter;
                if (predictedPosition.distance(surfWave.fireLocation) - 20.0D < surfWave.distanceTraveled + (double)counter * surfWave.bulletVelocity) {
                    intercepted = true;
                }
            } while(!intercepted && counter < 500);

            if (traveledPoints.size() > 1) {
                traveledPoints.remove(traveledPoints.size() - 1);
            }

            return traveledPoints;
        }

        public double checkDanger(Goodbot.EnemyWave surfWave, Double position) {
            int index = getFactorIndex(surfWave, position);
            double distance = position.distance(surfWave.fireLocation);
            return surfWave._surfStats[index] / distance;
        }

        public Double getBestPoint(Goodbot.EnemyWave surfWave) {
            if (surfWave.safePoints == null) {
                ArrayList forwardPoints = this.predictPositions(surfWave, 1);
                ArrayList reversePoints = this.predictPositions(surfWave, -1);
                int FminDangerIndex = 0;
                int RminDangerIndex = 0;
                double FminDanger = 1.0D / 0.0;
                double RminDanger = 1.0D / 0.0;
                int i = 0;

                int minDangerIndex;
                double thisDanger;
                for(minDangerIndex = forwardPoints.size(); i < minDangerIndex; ++i) {
                    thisDanger = this.checkDanger(surfWave, (Double)forwardPoints.get(i));
                    if (thisDanger <= FminDanger) {
                        FminDangerIndex = i;
                        FminDanger = thisDanger;
                    }
                }

                i = 0;

                for(minDangerIndex = reversePoints.size(); i < minDangerIndex; ++i) {
                    thisDanger = this.checkDanger(surfWave, (Double)reversePoints.get(i));
                    if (thisDanger <= RminDanger) {
                        RminDangerIndex = i;
                        RminDanger = thisDanger;
                    }
                }

                ArrayList bestPoints;
                if (FminDanger < RminDanger) {
                    bestPoints = forwardPoints;
                    minDangerIndex = FminDangerIndex;
                } else {
                    bestPoints = reversePoints;
                    minDangerIndex = RminDangerIndex;
                }

                Double bestPoint = (Double)bestPoints.get(minDangerIndex);

                while(bestPoints.indexOf(bestPoint) != -1) {
                    bestPoints.remove(bestPoints.size() - 1);
                }

                bestPoints.add(bestPoint);
                surfWave.safePoints = bestPoints;
                bestPoints.add(0, new Double(this.getX(), this.getY()));
            } else if (surfWave.safePoints.size() > 1) {
                surfWave.safePoints.remove(0);
            }

            if (surfWave.safePoints.size() < 1) {
                return null;
            } else {
                int i = 0;

                for(int k = surfWave.safePoints.size(); i < k; ++i) {
                    Double goToPoint = (Double)surfWave.safePoints.get(i);
                    if (goToPoint.distanceSq(this._myLocation) > 440.00000000000006D) {
                        return goToPoint;
                    }
                }

                return (Double)surfWave.safePoints.get(surfWave.safePoints.size() - 1);
            }
        }

        public void doSurfing() {
            Goodbot.EnemyWave surfWave = this.getClosestSurfableWave();
            double distance = this._enemyLocation.distance(this._myLocation);
            if (surfWave != null && distance >= 50.0D) {
                this.goTo(this.getBestPoint(surfWave));
            } else {
                double absBearing = absoluteBearing(this._myLocation, this._enemyLocation);
                double headingRadians = this.getHeadingRadians();
                double stick = 160.0D;
                double offset = 2.5707963267948966D - distance / 400.0D;

                double v2;
                while(!_fieldRect.contains(project(this._myLocation, v2 = absBearing + this.direction * (offset -= 0.02D), stick))) {
                    ;
                }

                if (offset < 1.0471975511965976D) {
                    this.direction = -this.direction;
                }

                this.setAhead(50.0D * Math.cos(v2 - headingRadians));
                this.setTurnRightRadians(Math.tan(v2 - headingRadians));
            }

        }

        private void goTo(Double destination) {
            if (destination == null) {
                if (this._lastGoToPoint == null) {
                    return;
                }

                destination = this._lastGoToPoint;
            }

            this._lastGoToPoint = destination;
            Double location = new Double(this.getX(), this.getY());
            double distance = location.distance(destination);
            double angle = Utils.normalRelativeAngle(absoluteBearing(location, destination) - this.getHeadingRadians());
            if (Math.abs(angle) > 1.5707963267948966D) {
                distance = -distance;
                if (angle > 0.0D) {
                    angle -= 3.141592653589793D;
                } else {
                    angle += 3.141592653589793D;
                }
            }

            this.setTurnRightRadians(angle * (double)Math.signum((float)Math.abs((int)distance)));
            this.setAhead(distance);
        }

        public double wallSmoothing(Double botLocation, double angle, int orientation) {
            while(!_fieldRect.contains(project(botLocation, angle, 160.0D))) {
                angle += (double)orientation * 0.05D;
            }

            return angle;
        }

        public static Double project(Double sourceLocation, double angle, double length) {
            return new Double(sourceLocation.x + Math.sin(angle) * length, sourceLocation.y + Math.cos(angle) * length);
        }

        public static double absoluteBearing(Double source, Double target) {
            return Math.atan2(target.x - source.x, target.y - source.y);
        }

        public static double limit(double min, double value, double max) {
            return Math.max(min, Math.min(value, max));
        }

        public static double bulletVelocity(double power) {
            return 20.0D - 3.0D * power;
        }

        public static double maxEscapeAngle(double velocity) {
            return Math.asin(8.0D / velocity);
        }

        public static void setBackAsFront(AdvancedRobot robot, double goAngle) {
            double angle = Utils.normalRelativeAngle(goAngle - robot.getHeadingRadians());
            if (Math.abs(angle) > 1.5707963267948966D) {
                if (angle < 0.0D) {
                    robot.setTurnRightRadians(3.141592653589793D + angle);
                } else {
                    robot.setTurnLeftRadians(3.141592653589793D - angle);
                }

                robot.setBack(100.0D);
            } else {
                if (angle < 0.0D) {
                    robot.setTurnLeftRadians(-1.0D * angle);
                } else {
                    robot.setTurnRightRadians(angle);
                }

                robot.setAhead(100.0D);
            }

        }

        public void onPaint(Graphics2D g) {
            g.setColor(Color.red);

            for(int i = 0; i < this._enemyWaves.size(); ++i) {
                Goodbot.EnemyWave w = (Goodbot.EnemyWave)this._enemyWaves.get(i);
                Double center = w.fireLocation;
                int radius = (int)w.distanceTraveled;
                if ((double)(radius - 40) < center.distance(this._myLocation)) {
                    g.drawOval((int)(center.x - (double)radius), (int)(center.y - (double)radius), radius * 2, radius * 2);
                }
            }

        }

        class EnemyWave {
            Double fireLocation;
            long fireTime;
            double bulletVelocity;
            double directAngle;
            double distanceTraveled;
            int direction;
            ArrayList safePoints;
            public double[] _surfStats;

            public EnemyWave() {
            }
        }
    }


    /*/
    private boolean moved = false; // if we need to move or turn
    private boolean inCorner = false; // if we are in a corner
    private String targ; // what robot to target
    private byte spins = 0; // spin counter
    private byte dir = 1; // direction to move
    private short prevE; // previous energy of robot we're targeting
    private double previousEnergy = 100;
    private int movementDirection = 1;
    private int gunDirection = 1;

    @Override
    public void run(){
        setColors(Color.PINK, Color.BLACK, Color.CYAN); // set the colors
        setAdjustGunForRobotTurn(true); // when the robot turns, adjust gun in opposite dir
        setAdjustRadarForGunTurn(true); // when the gun turns, adjust radar in opposite dir
        while(true){ // for radar lock (aka "Narrow Lock")
            turnRadarLeftRadians(1); // continually turn the radar left
        }
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e){ // if hit buy a bullet
        targ = e.getName(); // target the one who hit us!
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e){
        if(targ == null || spins > 6){ // if we don't have a target
            targ = e.getName(); // choose the first robot scanned
        }

        // Stay at right angles to the opponent
        setTurnRight(e.getBearing()+90-
                20*movementDirection);

        // If the bot has small energy drop,
        // assume it fired
        double changeInEnergy =
                previousEnergy-e.getEnergy();
        if (changeInEnergy>0 &&
                changeInEnergy<=3) {
            // Dodge!
            movementDirection =-movementDirection;
            setAhead((e.getDistance()/4+25)*movementDirection);
        }


        // Track the energy level
        previousEnergy = e.getEnergy();

        if(e.getName().equals(targ)){ // if the robot scanned is our target
            spins = 0; // reset radar spin counter

            // if the enemy fires, with a 15% chance,
            if((prevE < (prevE = (short)e.getEnergy())) && Math.random() > .85){
                dir *= -1; // change direction
            }

            setTurnGunRightRadians(Utils.normalRelativeAngle((getHeadingRadians() + e
                    .getBearingRadians()) - getGunHeadingRadians())); // move gun toward them

            if(e.getDistance() < 200){ // the the enemy is further than 200px
                setFire(3); // fire full power
            }
            else{
                setFire(2.4); // else fire 2.4
            }

            double radarTurn = getHeadingRadians() + e.getBearingRadians()
                    - getRadarHeadingRadians();
            setTurnRadarRightRadians(2 * Utils.normalRelativeAngle(radarTurn)); // lock radar
        }
        else if(targ != null){ // else
            spins++; // add one to spin count
        }
    }


}/*/


