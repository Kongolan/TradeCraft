package us.corenetwork.tradecraft;

import java.util.Iterator;
import java.util.Set;

import net.minecraft.server.v1_9_R1.EntityInsentient;
import net.minecraft.server.v1_9_R1.PathfinderGoal;
import net.minecraft.server.v1_9_R1.PathfinderGoalAvoidTarget;
import net.minecraft.server.v1_9_R1.PathfinderGoalMoveIndoors;
import net.minecraft.server.v1_9_R1.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_9_R1.PathfinderGoalOpenDoor;
import net.minecraft.server.v1_9_R1.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_9_R1.PathfinderGoalRestrictOpenDoor;
import net.minecraft.server.v1_9_R1.PathfinderGoalSelector;
import net.minecraft.server.v1_9_R1.PathfinderGoalTakeFlower;

public class AILimiter
{
    public static void apply(EntityInsentient entityInsentient)
    {
        PathfinderGoalSelector goalSelector = (PathfinderGoalSelector) ReflectionUtils.get(EntityInsentient.class, entityInsentient, "goalSelector");
        Set goalsListB = (Set) ReflectionUtils.get(PathfinderGoalSelector.class, goalSelector, "b"); //List of all pathfinder goals
        Set goalsListC = (Set) ReflectionUtils.get(PathfinderGoalSelector.class, goalSelector, "c"); //Cache of pathfinder goals
        Class pathfinderGoalSelectorItemClass = null;
        try
        {
            pathfinderGoalSelectorItemClass = Class.forName("net.minecraft.server.v1_9_R1.PathfinderGoalSelector$PathfinderGoalSelectorItem");
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        Iterator iterator = goalsListB.iterator();
        while (iterator.hasNext())
        {
            Object pathfinderGoalSelectorItem = iterator.next();
            PathfinderGoal originalGoal = (PathfinderGoal) ReflectionUtils.get(pathfinderGoalSelectorItemClass, pathfinderGoalSelectorItem, "a");

            //Included goals
            if (!(     originalGoal instanceof PathfinderGoalAvoidTarget
                    || originalGoal instanceof PathfinderGoalMoveIndoors
                    || originalGoal instanceof PathfinderGoalMoveTowardsRestriction
                    || originalGoal instanceof PathfinderGoalTakeFlower
                    || originalGoal instanceof PathfinderGoalRandomStroll
                    || originalGoal instanceof PathfinderGoalOpenDoor
                    || originalGoal instanceof PathfinderGoalRestrictOpenDoor))
                continue;

            ReflectionUtils.set(pathfinderGoalSelectorItemClass, pathfinderGoalSelectorItem, "a", new NearbyPlayerPathfinderGoalProxy(originalGoal, entityInsentient));
        }
        goalsListC.clear(); //Clear cache
    }

    /**
     * Proxy for PathfinderGoal that will disable it if there is no player nearby
     */
    private static class NearbyPlayerPathfinderGoalProxy extends PathfinderGoal
    {
        private PathfinderGoal original;
        private EntityInsentient entity;

        /**
         * Method that determines whether PathfinderGoal should start executing or not
         *
         * @return <code>true</code> if goal should start executing
         */
        @Override
        public boolean a()
        {
            boolean canOriginalStart = original.a();
            if (!canOriginalStart)
                return false;

            if (!Settings.getBoolean(Setting.AI_LIMITER_ENABLE))
                return true;

            //If original can start, then we should check for nearby player and only start if there is one.
            return entity.world.findNearbyPlayer(entity, Settings.getDouble(Setting.AI_LIMITER_DISTANCE_TO_PLAYER)) != null;
        }

        public NearbyPlayerPathfinderGoalProxy(PathfinderGoal original, EntityInsentient entity)
        {
            super();
            this.original = original;
            this.entity = entity;
        }

        @Override
        public boolean b()
        {
            return original.b();
        }

        @Override
        public boolean g()
        {
            return original.g();
        }

        @Override
        public void c()
        {
            original.c();
        }

        @Override
        public void d()
        {
            original.d();
        }

        @Override
        public void e()
        {
            original.e();
        }

        @Override
        public void a(int i)
        {
            original.a(i);
        }

        @Override
        public int h()
        {
            return original.h();
        }
    }
}