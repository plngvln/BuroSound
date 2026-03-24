package net.p4pingvin4ik.burosound;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public class BlockTrigger {
    public final SoundBox box;

    public final int x;
    public final int y;
    public final int z;

    public final Identifier blockId;

    public BlockTrigger(
            SoundBox box,
            int x,
            int y,
            int z,
            Identifier blockId
    ) {
        this.box = box;
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockId = blockId;
    }

    public boolean matches(Identifier currentDim, BlockPos pos, Identifier currentBlockId) {
        if (!this.box.dimension.equals(currentDim)) return false;
        if (pos.getX() != this.x || pos.getY() != this.y || pos.getZ() != this.z) return false;
        return this.blockId.equals(currentBlockId);
    }
}
