package us.corenetwork.tradecraft;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.server.v1_10_R1.BiomeBase;
import net.minecraft.server.v1_10_R1.EntityTypes;
import net.minecraft.server.v1_10_R1.EntityVillager;
import net.minecraft.server.v1_10_R1.NBTReadLimiter;
import net.minecraft.server.v1_10_R1.World;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * Created by Matej on 23.2.2014.
 */
public class NMSVillagerManager {
    public static void register()
    {
        //Replace "Villager" entity type
        ((Map) ReflectionUtils.getStatic(EntityTypes.class, "c")).put("Villager", CustomVillager.class);
        ((Map) ReflectionUtils.getStatic(EntityTypes.class, "d")).put(CustomVillager.class, "Villager");
        ((Map) ReflectionUtils.getStatic(EntityTypes.class, "e")).put(120, CustomVillager.class);
        ((Map) ReflectionUtils.getStatic(EntityTypes.class, "f")).put(CustomVillager.class, 120);

        //Replace all villagers in biomes
        Set<BiomeBase> biomes = (Set<BiomeBase>) ReflectionUtils.getStatic(BiomeBase.class, "i");
        for (BiomeBase biome : biomes)
        {
            if (biome == null)
                continue;

            fixBiomeMeta((List<BiomeBase.BiomeMeta>) ReflectionUtils.get(BiomeBase.class, biome, "x"));
            fixBiomeMeta((List<BiomeBase.BiomeMeta>) ReflectionUtils.get(BiomeBase.class, biome, "u"));
            fixBiomeMeta((List<BiomeBase.BiomeMeta>) ReflectionUtils.get(BiomeBase.class, biome, "v"));
            fixBiomeMeta((List<BiomeBase.BiomeMeta>) ReflectionUtils.get(BiomeBase.class, biome, "w"));

        }
    }

    private static void fixBiomeMeta(List<BiomeBase.BiomeMeta> meta)
    {
        for (BiomeBase.BiomeMeta m : meta)
        {
            if (m.b.equals(EntityVillager.class))
            {
                m.b = CustomVillager.class;
            }
        }
    }

    /***
     * Convert regular villager into custom villager.
     */
    public static void convert(Entity entity)
    {
        if (entity.getType() != EntityType.VILLAGER)
            return;

        EntityVillager nmsVillager = ((CraftVillager) entity).getHandle();

        World world = nmsVillager.world;
        Location location = entity.getLocation();
        int profession = nmsVillager.getProfession();

        CustomVillager newVillager = new CustomVillager(world, profession);
        newVillager.setPosition(location.getX(), location.getY(), location.getZ());
        newVillager.setAgeRaw(nmsVillager.getAge());

        world.removeEntity(nmsVillager);
        world.addEntity(newVillager, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    public static boolean isCustomVillager(Entity entity)
    {
        return ((CraftEntity) entity).getHandle() instanceof CustomVillager;
    }

    public static int getType(Villager villager)
    {
        EntityVillager nmsVillager = ((CraftVillager) villager).getHandle();
        return nmsVillager.getProfession();
    }


    public static NBTReadLimiter UNLIMTED_NBT_READER_INSTANCE = new UnlimitedNBTLimiter();
    private static class UnlimitedNBTLimiter extends NBTReadLimiter
    {
        public UnlimitedNBTLimiter()
        {
            super(0);
        }

        @Override
        public void a(long l)
        {
        }
    }
}
