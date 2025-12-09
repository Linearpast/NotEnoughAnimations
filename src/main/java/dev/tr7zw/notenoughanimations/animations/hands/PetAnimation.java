package dev.tr7zw.notenoughanimations.animations.hands;

import dev.tr7zw.notenoughanimations.access.PlayerData;
import dev.tr7zw.notenoughanimations.api.BasicAnimation;
import dev.tr7zw.notenoughanimations.util.AnimationUtil;
import dev.tr7zw.notenoughanimations.versionless.NEABaseMod;
import dev.tr7zw.notenoughanimations.versionless.animations.BodyPart;
import dev.tr7zw.transition.mc.EntityUtil;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
//#if MC > 11904
import net.minecraft.util.RandomSource;
//#else
import java.util.Random;
//#endif
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class PetAnimation extends BasicAnimation {

    private Entity targetPet = null;
    private double posDif = 0;

    @Override
    public boolean isEnabled() {
        return NEABaseMod.config.petAnimation;
    }

    @Override
    public boolean isValid(AbstractClientPlayer entity, PlayerData data) {
        if (!entity.isCrouching()) {
            return false;
        }
        double d = 1;// range
        Vec3 vec3 = entity.getEyePosition(0);
        Vec3 vec32 = entity.getViewVector(1.0F);
        Vec3 vec33 = vec3.add(vec32.x * d, vec32.y * d, vec32.z * d);
        AABB aABB = entity.getBoundingBox().expandTowards(vec32.scale(d)).inflate(1.0D, 1.0D, 1.0D);
        EntityHitResult entHit = ProjectileUtil.getEntityHitResult(entity, vec3, vec33, aABB, en -> (!en.isSpectator()),
                d);
        if (entHit != null) {
            if ((entHit.getEntity().getType() == EntityType.WOLF || entHit.getEntity().getType() == EntityType.CAT)) {
                TamableAnimal pet = (TamableAnimal) entHit.getEntity();
                double dif = pet.getY() - entity.getY();
                if (Math.abs(dif) < 0.6) { // Making sure they are about on the same height
                    targetPet = pet;
                    return true;
                }
            } else if (entHit.getEntity().getType() == EntityType.PLAYER) {
                Entity other = entHit.getEntity();
                double dif = pPetPValid(entity, other);
                if (dif != 0.0f) { // Making sure they are about on the same height
                    targetPet = other;
                    posDif = dif;
                    return true;
                }
            }
        }
        targetPet = null;
        return false;
    }

    public static double pPetPValid(Entity player, Entity other) {
        double dif = player.getY() + player.getEyeHeight() - other.getY() - player.getEyeHeight();
        return 1.0 >= dif && dif >= 0.3 ? dif : 0.0f;
    }

    private final BodyPart[] leftHanded = new BodyPart[] { BodyPart.LEFT_ARM };
    private final BodyPart[] rightHanded = new BodyPart[] { BodyPart.RIGHT_ARM };

    @Override
    public BodyPart[] getBodyParts(AbstractClientPlayer entity, PlayerData data) {
        return entity.getMainArm() == HumanoidArm.RIGHT ? rightHanded : leftHanded;
    }

    @Override
    public int getPriority(AbstractClientPlayer entity, PlayerData data) {
        return 2100;
    }

    @Override
    public void apply(AbstractClientPlayer entity, PlayerData data, PlayerModel model, BodyPart part, float delta,
            float tickCounter) {
        if (Math.random() < 0.005) {
            for (int i = 0; i < 7; ++i) {
                //#if MC > 11904
                RandomSource random = targetPet.getRandom();
                double d0 = random.nextGaussian() * 0.02;
                double d1 = random.nextGaussian() * 0.02;
                double d2 = random.nextGaussian() * 0.02;
                targetPet.level().addParticle(ParticleTypes.HEART, targetPet.getRandomX(1.0F), targetPet.getRandomY() + 0.5,
                        targetPet.getRandomZ(1.0F), d0, d1, d2);
                //#else
                //$$Random random = new Random(System.currentTimeMillis());
                //$$double d0 = random.nextGaussian() * 0.02;
                //$$double d1 = random.nextGaussian() * 0.02;
                //$$double d2 = random.nextGaussian() * 0.02;
                //$$double x = targetPet.getX(((double) 2.0F * random.nextDouble() - (double) 1.0F));
                //$$double y = targetPet.getX(((double) 2.0F * random.nextDouble() - (double) 1.0F));
                //$$targetPet.level.addParticle(ParticleTypes.HEART,
                //$$        x, targetPet.getY(random.nextDouble()) + 0.5, y,
                //$$        d0, d1, d2
                //$$);
                //#endif

            }
        }
        HumanoidArm arm = part == BodyPart.LEFT_ARM ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        float start = 1.0f;
        if (targetPet.getType() == EntityType.PLAYER)
            start = (float) posDif;
        AnimationUtil.applyArmTransforms(model, arm,
                -(Mth.lerp(-1f * (EntityUtil.getXRot(entity) - 90f) / 180f, start, 2f)), -0.5f,
                0.3f + Mth.sin((System.currentTimeMillis() % 20000) / 60f) * 0.2f);
        targetPet = null;
    }

}
