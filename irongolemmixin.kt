package com.example.mod.mixin

import com.example.mod.GolemLogisticsGoal
import net.minecraft.entity.EntityType
import net.minecraft.entity.ai.goal.ActiveTargetGoal
import net.minecraft.entity.mob.CreeperEntity
import net.minecraft.entity.passive.IronGolemEntity
import net.minecraft.inventory.SimpleInventory
import net.minecraft.world.World
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(IronGolemEntity::class)
abstract class IronGolemMixin(type: EntityType<out IronGolemEntity>, world: World) {
    private val lootInv = SimpleInventory(9)

    @Inject(method = ["initGoals"], at = [At("TAIL")])
    private fun addCustomAI(ci: CallbackInfo) {
        val golem = this as Any as IronGolemEntity
        // Target Creepers
        golem.targetSelector.add(3, ActiveTargetGoal(golem, CreeperEntity::class.java, true))
        // Run Logistics
        golem.goalSelector.add(4, GolemLogisticsGoal(golem, lootInv))
    }
}