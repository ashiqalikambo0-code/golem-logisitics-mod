package com.example

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object IronGolemLoot : ModInitializer {
    private val logger = LoggerFactory.getLogger("iron-golem-loot")

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		logger.info("Hello Fabric world!")
	}
}package com.example.mod

import net.fabricmc.api.ModInitializer
import net.minecraft.block.*
import net.minecraft.block.entity.*
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.passive.IronGolemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.*
import net.minecraft.registry.*
import net.minecraft.util.*
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object GolemMod : ModInitializer {
    const val MOD_ID = "golem_logistics"

    val IRON_CHEST = IronChestBlock(AbstractBlock.Settings.copy(Blocks.CHEST))
    val IRON_CHEST_ENTITY: BlockEntityType<IronChestBlockEntity> = Registry.register(
        Registries.BLOCK_ENTITY_TYPE,
        Identifier(MOD_ID, "iron_chest_be"),
        BlockEntityType.Builder.create({ pos, state -> IronChestBlockEntity(pos, state) }, IRON_CHEST).build(null)
    )

    override fun onInitialize() {
        Registry.register(Registries.BLOCK, Identifier(MOD_ID, "iron_chest"), IRON_CHEST)
        Registry.register(Registries.ITEM, Identifier(MOD_ID, "iron_chest"), BlockItem(IRON_CHEST, Item.Settings()))
    }
}

class IronChestBlock(settings: Settings) : BlockWithEntity(settings) {
    override fun createBlockEntity(pos: BlockPos, state: BlockState) = IronChestBlockEntity(pos, state)
    override fun getRenderType(state: BlockState) = BlockRenderType.MODEL
}

class IronChestBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(GolemMod.IRON_CHEST_ENTITY, pos, state) {
    val inventory = SimpleInventory(27)
    fun addStack(stack: ItemStack): ItemStack = inventory.addStack(stack)
}

class GolemLogisticsGoal(private val golem: IronGolemEntity, private val inv: SimpleInventory) : Goal() {
    override fun canStart() = true
    override fun tick() {
        val world = golem.world
        // Pickup Logic
        world.getEntitiesByClass(ItemEntity::class.java, golem.boundingBox.expand(3.0)) { true }.forEach {
            val rem = inv.addStack(it.stack)
            if (rem.isEmpty) it.discard() else it.stack = rem
        }
        // Deposit Logic
        if (!inv.isEmpty) {
            BlockPos.findClosest(golem.blockPos, 16, 8) { world.getBlockState(it).block == GolemMod.IRON_CHEST }.ifPresent {
                golem.navigation.startMovingTo(it.x.toDouble(), it.y.toDouble(), it.z.toDouble(), 1.2)
                if (golem.blockPos.isWithinDistance(it, 2.0)) {
                    (world.getBlockEntity(it) as? IronChestBlockEntity)?.let { chest ->
                        for (i in 0 until inv.size()) inv.setStack(i, chest.addStack(inv.getStack(i)))
                    }
                }
            }
        }
    }
}