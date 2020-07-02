package com.raxdiam.lockit.mixin;

import com.google.common.collect.Sets;
import com.raxdiam.lockit.accessor.ILockableContainerBlockEntityAccessor;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.*;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {
    @Shadow @Final private World world;
    @Shadow @Final private double x;
    @Shadow @Final private double y;
    @Shadow @Final private double z;
    @Shadow @Final private float power;
    @Shadow @Final private List<BlockPos> affectedBlocks;
    @Shadow @Final private Entity entity;
    @Shadow @Final private ExplosionBehavior behavior;
    @Shadow @Final private Map<PlayerEntity, Vec3d> affectedPlayers;

    @Shadow public abstract DamageSource getDamageSource();

    /**
     * @author Raxdiam
     * @reason Explosion resistance for locked containers. Would love to know of a better way.
     */
    @Overwrite
    public void collectBlocksAndDamageEntities() {
        Set<BlockPos> set = Sets.newHashSet();

        int k;
        int l;
        for (var j = 0; j < 16; ++j) {
            for (k = 0; k < 16; ++k) {
                for (l = 0; l < 16; ++l) {
                    if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                        var d = (double) ((float) j / 15.0F * 2.0F - 1.0F);
                        var e = (double) ((float) k / 15.0F * 2.0F - 1.0F);
                        var f = (double) ((float) l / 15.0F * 2.0F - 1.0F);
                        var g = Math.sqrt(d * d + e * e + f * f);
                        d /= g;
                        e /= g;
                        f /= g;
                        var h = this.power * (0.7F + this.world.random.nextFloat() * 0.6F);
                        var m = this.x;
                        var n = this.y;
                        var o = this.z;

                        for (; h > 0.0F; h -= 0.22500001F) {
                            var blockPos = new BlockPos(m, n, o);
                            var blockState = this.world.getBlockState(blockPos);
                            var fluidState = this.world.getFluidState(blockPos);
                            var optional = this.behavior.getBlastResistance((Explosion) (Object) this, this.world, blockPos, blockState, fluidState);

                            var blockEntity = this.world.getBlockEntity(blockPos);
                            var isLocked = (blockEntity instanceof LockableContainerBlockEntity) && ((ILockableContainerBlockEntityAccessor) blockEntity).getLockable().isActive();

                            if (optional.isPresent()) {
                                h -= (optional.get() + 0.3F) * 0.3F;
                            }

                            if (!isLocked && h > 0.0F && this.behavior.canDestroyBlock((Explosion) (Object) this, this.world, blockPos, blockState, h)) {
                                set.add(blockPos);
                            }

                            m += d * 0.30000001192092896D;
                            n += e * 0.30000001192092896D;
                            o += f * 0.30000001192092896D;
                        }
                    }
                }
            }
        }

        this.affectedBlocks.addAll(set);
        var q = this.power * 2.0F;
        k = MathHelper.floor(this.x - (double) q - 1.0D);
        l = MathHelper.floor(this.x + (double) q + 1.0D);
        var t = (double) MathHelper.floor(this.y - (double) q - 1.0D);
        var u = (double) MathHelper.floor(this.y + (double) q + 1.0D);
        var v = (double) MathHelper.floor(this.z - (double) q - 1.0D);
        var w = (double) MathHelper.floor(this.z + (double) q + 1.0D);
        var list = this.world.getEntities(this.entity, new Box(k, t, v, l, u, w));
        var vec3d = new Vec3d(this.x, this.y, this.z);

        for (var x = 0; x < list.size(); ++x) {
            var entity = (Entity) list.get(x);
            if (!entity.isImmuneToExplosion()) {
                var y = (double) (MathHelper.sqrt(entity.squaredDistanceTo(vec3d)) / q);
                if (y <= 1.0D) {
                    var z = entity.getX() - this.x;
                    var aa = (entity instanceof TntEntity ? entity.getY() : entity.getEyeY()) - this.y;
                    var ab = entity.getZ() - this.z;
                    var ac = (double) MathHelper.sqrt(z * z + aa * aa + ab * ab);
                    if (ac != 0.0D) {
                        z /= ac;
                        aa /= ac;
                        ab /= ac;
                        var ad = (double) Explosion.getExposure(vec3d, entity);
                        var ae = (1.0D - y) * ad;
                        entity.damage(this.getDamageSource(), (float) ((int) ((ae * ae + ae) / 2.0D * 7.0D * (double) q + 1.0D)));
                        var af = ae;
                        if (entity instanceof LivingEntity) {
                            af = ProtectionEnchantment.transformExplosionKnockback((LivingEntity) entity, ae);
                        }

                        entity.setVelocity(entity.getVelocity().add(z * af, aa * af, ab * af));
                        if (entity instanceof PlayerEntity) {
                            var playerEntity = (PlayerEntity) entity;
                            if (!playerEntity.isSpectator() && (!playerEntity.isCreative() || !playerEntity.abilities.flying)) {
                                this.affectedPlayers.put(playerEntity, new Vec3d(z * ae, aa * ae, ab * ae));
                            }
                        }
                    }
                }
            }
        }

    }
}
