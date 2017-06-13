/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Timberjack) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.timberjack.common.entity;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockLog;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EntityTimber extends Entity implements IEntityAdditionalSpawnData {
    private static final DataParameter<BlockPos> ORIGIN = EntityDataManager.<BlockPos>createKey(EntityTimber.class, DataSerializers.BLOCK_POS);
    private IBlockState fallingBlock;
    private int fallTime;
    public boolean shouldDropItem = true;
    private boolean hurtEntities;
    private boolean log;
    private int fallHurtMax = 40;
    private float fallHurtAmount = 2.0F;
    private NBTTagCompound tileEntityData;
    private List<ItemStack> drops = new ArrayList<>();
    private EnumFacing fellingDirection = EnumFacing.UP;

    public EntityTimber(World worldIn) {
        super(worldIn);
    }

    public EntityTimber(World worldIn, double x, double y, double z, IBlockState fallingBlockState, EnumFacing fellingDirection, boolean log) {
        this(worldIn);
        this.fallingBlock = fallingBlockState;
        this.fellingDirection = fellingDirection;
        this.log = log;
        this.setPosition(x, y + (double) ((1.0F - this.height) / 2.0F), z);
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        this.setOrigin(new BlockPos(this));
    }

    {
        this.preventEntitySpawning = true;
        this.setSize(0.98F, 0.98F);
    }

    public void setOrigin(BlockPos p_184530_1_) {
        this.dataManager.set(ORIGIN, p_184530_1_);
    }

    @SideOnly(Side.CLIENT)
    public BlockPos getOrigin() {
        return (BlockPos) this.dataManager.get(ORIGIN);
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
     * prevent them from trampling crops
     */
    protected boolean canTriggerWalking() {
        return false;
    }

    protected void entityInit() {
        this.dataManager.register(ORIGIN, BlockPos.ORIGIN);
    }

    /**
     * Returns true if other Entities should be prevented from moving through this Entity.
     */
    public boolean canBeCollidedWith() {
        return !this.isDead;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate() {
        Block block = this.fallingBlock.getBlock();

        if (this.fallingBlock.getMaterial() == Material.AIR) {
            this.setDead();
        } else {
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;

            if (this.fallTime++ == 0) {
                BlockPos currentPos = new BlockPos(this);

                IBlockState state = this.worldObj.getBlockState(currentPos);
                if (state.getBlock() == block) {
                    if (!this.worldObj.isRemote && !log) {
                        drops.addAll(block.getDrops(worldObj, currentPos, state, 0));
                    }
                    this.worldObj.setBlockToAir(currentPos);
                } else if (!this.worldObj.isRemote) {
                    this.setDead();
                    return;
                }
            }

            if (!this.hasNoGravity()) {
                this.motionY -= 0.03D;
            }

            this.moveEntity(this.motionX, this.motionY, this.motionZ);
//            this.motionX *= 0.9800000190734863D;
            this.motionY *= 0.98D;
//            this.motionZ *= 0.9800000190734863D;

            if (!this.worldObj.isRemote) {
                BlockPos currentPos = new BlockPos(this);

                BlockPos belowPos = new BlockPos(this.posX, this.posY - 0.001D, this.posZ);
                if (this.onGround && isBlocked(belowPos)) {
                    if (canBreakThrough(belowPos)) {
                        worldObj.destroyBlock(belowPos, doTileDrops());
                        return;
                    }

                    IBlockState occupiedState = this.worldObj.getBlockState(currentPos);

                    this.motionX *= 0.699999988079071D;
                    this.motionZ *= 0.699999988079071D;
                    this.motionY *= -0.5D;

                    if (occupiedState.getBlock() != Blocks.PISTON_EXTENSION) {
                        this.setDead();

                        if (canPlaceBlock(occupiedState, currentPos) && isBlocked(currentPos.down())
                                && placeBlock(occupiedState, currentPos)) {

                            if (this.tileEntityData != null && block instanceof ITileEntityProvider) {
                                TileEntity tileentity = this.worldObj.getTileEntity(currentPos);

                                if (tileentity != null) {
                                    NBTTagCompound nbt = tileentity.writeToNBT(new NBTTagCompound());

                                    for (String s : this.tileEntityData.getKeySet()) {
                                        NBTBase nbtbase = this.tileEntityData.getTag(s);

                                        if (!"x".equals(s) && !"y".equals(s) && !"z".equals(s)) {
                                            nbt.setTag(s, nbtbase.copy());
                                        }
                                    }

                                    tileentity.readFromNBT(nbt);
                                    tileentity.markDirty();
                                }
                            }

                            IBlockState state = worldObj.getBlockState(currentPos);
                            if (log) {
                                rotateLog(state, currentPos);
                            }

                            state.getBlock().beginLeavesDecay(state, worldObj, currentPos);

                        } else if (this.shouldDropItem && doTileDrops()) {
                            dropItems();
                        }
                    }
                } else if (this.fallTime > 100 && (currentPos.getY() < 1 || currentPos.getY() > 256) || this.fallTime > 400) {
                    if (this.shouldDropItem && doTileDrops()) {
                        dropItems();
                    }

                    this.setDead();
                }
            }
        }
    }

    private void dropItems() {
        List<ItemStack> itemsToDrop = new ArrayList<>();
        if (log) {
            itemsToDrop.add(new ItemStack(Items.STICK, rand.nextInt(4) + 1));
        } else {
            itemsToDrop.addAll(this.drops);
        }
        itemsToDrop.forEach(d -> entityDropItem(d, 0.0F));
    }

    public void fall(float distance, float damageMultiplier) {
        if (this.hurtEntities) {
            int i = MathHelper.ceiling_float_int(distance - 1.0F);

            if (i > 0) {
                List<Entity> list = Lists.newArrayList(this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox()));
                DamageSource damagesource = DamageSource.fallingBlock;

                for (Entity entity : list) {
                    entity.attackEntityFrom(damagesource, (float) Math.min(MathHelper.floor_float((float) i * this.fallHurtAmount), this.fallHurtMax));
                }
            }
        }
    }

    private boolean canPlaceBlock(IBlockState occupiedState, BlockPos currentPos) {
        Block block = this.fallingBlock.getBlock();
        return worldObj.canBlockBePlaced(block, currentPos, true, EnumFacing.UP, null, null)
                || (log && occupiedState.getBlock().isLeaves(occupiedState, worldObj, currentPos));
    }

    private boolean placeBlock(IBlockState occupiedState, BlockPos currentPos) {
        worldObj.destroyBlock(currentPos, doTileDrops());
        return worldObj.setBlockState(currentPos, this.fallingBlock, 3);
    }

    private boolean isBlocked(BlockPos pos) {
        IBlockState state = this.worldObj.getBlockState(pos);
        return !BlockFalling.canFallThrough(state);
    }

    private boolean canBreakThrough(BlockPos pos) {
        IBlockState state = this.worldObj.getBlockState(pos);
        Material material = state.getMaterial();
        if (material == Material.PLANTS)
            return true;
        if (!log)
            return false;
        return material == Material.LEAVES || material == Material.VINE || material == Material.PLANTS || state.getBlock().isLeaves(state, worldObj, pos);
    }

    private boolean doTileDrops() {
        return worldObj.getGameRules().getBoolean("doEntityDrops");
    }

    private void rotateLog(IBlockState state, BlockPos pos) {
        if (state.getBlock() instanceof BlockLog && state.getProperties().containsKey(BlockLog.LOG_AXIS)) {
            BlockLog.EnumAxis axis;
            switch (fellingDirection.getAxis()) {
                case X:
                    axis = BlockLog.EnumAxis.X;
                    break;
                case Z:
                    axis = BlockLog.EnumAxis.Z;
                    break;
                default:
                    axis = BlockLog.EnumAxis.Y;
            }
            if (axis != BlockLog.EnumAxis.Y) {
                IBlockState newState = state.withProperty(BlockLog.LOG_AXIS, axis);
                worldObj.setBlockState(pos, newState);
            }
        }
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    protected void writeEntityToNBT(NBTTagCompound compound) {
        Block block = this.fallingBlock != null ? this.fallingBlock.getBlock() : Blocks.AIR;
        ResourceLocation resourcelocation = Block.REGISTRY.getNameForObject(block);
        compound.setString("Block", resourcelocation.toString());
        compound.setByte("Data", (byte) block.getMetaFromState(this.fallingBlock));
        compound.setInteger("Time", this.fallTime);
        compound.setBoolean("DropItem", this.shouldDropItem);
        compound.setBoolean("HurtEntities", this.hurtEntities);
        compound.setFloat("FallHurtAmount", this.fallHurtAmount);
        compound.setInteger("FallHurtMax", this.fallHurtMax);
        compound.setInteger("FellingDirection", this.fellingDirection.ordinal());

        if (this.tileEntityData != null) {
            compound.setTag("TileEntityData", this.tileEntityData);
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    protected void readEntityFromNBT(NBTTagCompound compound) {
        int i = compound.getByte("Data") & 255;

        if (compound.hasKey("Block", 8)) {
            this.fallingBlock = Block.getBlockFromName(compound.getString("Block")).getStateFromMeta(i);
        } else if (compound.hasKey("TileID", 99)) {
            this.fallingBlock = Block.getBlockById(compound.getInteger("TileID")).getStateFromMeta(i);
        } else {
            this.fallingBlock = Block.getBlockById(compound.getByte("Tile") & 255).getStateFromMeta(i);
        }

        this.fallTime = compound.getInteger("Time");
        Block block = this.fallingBlock.getBlock();

        if (compound.hasKey("HurtEntities", 99)) {
            this.hurtEntities = compound.getBoolean("HurtEntities");
            this.fallHurtAmount = compound.getFloat("FallHurtAmount");
            this.fallHurtMax = compound.getInteger("FallHurtMax");
        }

        if (compound.hasKey("DropItem", 99)) {
            this.shouldDropItem = compound.getBoolean("DropItem");
        }

        if (compound.hasKey("TileEntityData", 10)) {
            this.tileEntityData = compound.getCompoundTag("TileEntityData");
        }

        this.fellingDirection = EnumFacing.VALUES[compound.getInteger("FellingDirection")];

        if (block == null || block.getDefaultState().getMaterial() == Material.AIR) {
            this.fallingBlock = Blocks.LOG.getDefaultState();
        }
    }

    public void setHurtEntities(boolean p_145806_1_) {
        this.hurtEntities = p_145806_1_;
    }

    public void addEntityCrashInfo(CrashReportCategory category) {
        super.addEntityCrashInfo(category);

        if (this.fallingBlock != null) {
            Block block = this.fallingBlock.getBlock();
            category.addCrashSection("Imitating block ID", Block.getIdFromBlock(block));
            category.addCrashSection("Imitating block data", block.getMetaFromState(this.fallingBlock));
        }
    }

//    /**
//     * Return whether this entity should be rendered as on fire.
//     */
//    @SideOnly(Side.CLIENT)
//    public boolean canRenderOnFire() {
//        return false;
//    }

    @Nullable
    public IBlockState getBlock() {
        return this.fallingBlock;
    }

    public boolean ignoreItemEntityData() {
        return true;
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeInt(Block.getStateId(fallingBlock));
        buffer.writeBoolean(log);
        buffer.writeByte(fellingDirection.ordinal());
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        fallingBlock = Block.getStateById(additionalData.readInt() & 65535);
        log = additionalData.readBoolean();
        fellingDirection = EnumFacing.VALUES[additionalData.readByte()];
    }
}