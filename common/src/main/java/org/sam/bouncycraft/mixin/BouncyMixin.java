/*
This is the only class responsible to make blocks bouncy, everything else is useless
 */

package org.sam.bouncycraft.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BouncyMixin {

    @Unique
    private void bouncyCraft$bounceUp(Entity pEntity) {
        Vec3 vec3 = pEntity.getDeltaMovement();
        if (vec3.y < 0.0) {
            double d0 = pEntity instanceof LivingEntity ? 1.0 : 0.8;
            pEntity.setDeltaMovement(vec3.x, -vec3.y * d0, vec3.z);
        }
    }

    @Inject(method = "stepOn", at = @At("HEAD"), cancellable = true)
    public void slimeStepOn(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity, CallbackInfo ci) {
        double d0 = Math.abs(pEntity.getDeltaMovement().y);
        if (d0 < 0.1 && !pEntity.isSteppingCarefully()) {
            double d1 = 0.4 + d0 * 0.2;
            pEntity.setDeltaMovement(pEntity.getDeltaMovement().multiply(d1, 1.0, d1));
        }
        ci.cancel();
    }

    @Inject(method = "fallOn", at = @At("HEAD"), cancellable = true)
    public void slimeFallOn(Level pLevel, BlockState pState, BlockPos pPos, Entity pEntity, float pFallDistance, CallbackInfo ci) {
        if (pEntity.isSuppressingBounce()) {
            this.bouncyCraft$fallOn(pLevel, pState, pPos, pEntity, pFallDistance);
        } else {
            pEntity.causeFallDamage(pFallDistance, 0.0F, pLevel.damageSources().fall());
        }
        ci.cancel();
    }

    @Inject(method = "updateEntityAfterFallOn", at = @At("HEAD"), cancellable = true)
    public void slimeUpdateEntityAfterFallOn(BlockGetter pLevel, Entity pEntity, CallbackInfo ci) {
        if (pEntity.isSuppressingBounce()) {
            this.bouncyCraft$updateEntityAfterFallOn(pLevel, pEntity);
        } else {
            this.bouncyCraft$bounceUp(pEntity);
        }
        ci.cancel();
    }

    @Unique
    public void bouncyCraft$updateEntityAfterFallOn(BlockGetter pLevel, Entity pEntity) {
        pEntity.setDeltaMovement(pEntity.getDeltaMovement().multiply(1.0, 0.0, 1.0));
    }

    @Unique
    public void bouncyCraft$fallOn(Level pLevel, BlockState pState, BlockPos pPos, Entity pEntity, float pFallDistance) {
        pEntity.causeFallDamage(pFallDistance, 1.0F, pEntity.damageSources().fall());
    }
}