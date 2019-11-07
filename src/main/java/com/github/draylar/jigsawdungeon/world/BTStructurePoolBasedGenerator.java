package com.github.draylar.jigsawdungeon.world;

import com.github.draylar.jigsawdungeon.world.features.jigsaw_dungeon.JigsawDungeonStructureStart;
import com.github.draylar.jigsawdungeon.util.LocationAccessor;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import net.minecraft.block.JigsawBlock;
import net.minecraft.structure.*;
import net.minecraft.structure.pool.*;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.BooleanBiFunction;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MutableIntBoundingBox;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class BTStructurePoolBasedGenerator {

    private BTStructurePoolBasedGenerator() {
        // NO-OP
    }

    private static final Logger LOGGER = LogManager.getLogger();
    public static final StructurePoolRegistry REGISTRY = new StructurePoolRegistry();

    public static void addPieces(Identifier startingPool, int size, StructurePoolBasedGenerator.PieceFactory factory, ChunkGenerator<?> chunkGenerator, StructureManager structureManager, BlockPos pos, List<StructurePiece> children, Random random) {
        StructureFeatures.initialize();
        new StructureGenerator(startingPool, size, factory, chunkGenerator, structureManager, pos, children, random);
    }

    static {
        REGISTRY.add(StructurePool.EMPTY);
    }

    static final class StructureGenerator {

        private final int maxSize;
        private final StructurePoolBasedGenerator.PieceFactory pieceFactory;
        private final ChunkGenerator<?> chunkGenerator;
        private final StructureManager structureManager;
        private final List<StructurePiece> children;
        private final Random random;
        private final Deque<PieceWithBoundingBox> structurePieces = Queues.newArrayDeque();

        StructureGenerator(Identifier startingPool, int maxSize, StructurePoolBasedGenerator.PieceFactory pieceFactory, ChunkGenerator<?> chunkGenerator, StructureManager structureManager, BlockPos pos, List<StructurePiece> children, Random random) {
            this.maxSize = maxSize;
            this.pieceFactory = pieceFactory;
            this.chunkGenerator = chunkGenerator;
            this.structureManager = structureManager;
            this.children = children;
            this.random = random;

            BlockRotation randomRotation = BlockRotation.random(random);
            StructurePool startingStructurePool = BTStructurePoolBasedGenerator.REGISTRY.get(startingPool);
            StructurePoolElement randomStartingElement = startingStructurePool.getRandomElement(random);
            PoolStructurePiece startingPiece = pieceFactory.create(structureManager, randomStartingElement, pos, randomStartingElement.method_19308(), randomRotation, randomStartingElement.getBoundingBox(structureManager, pos, randomRotation));
            MutableIntBoundingBox startingPieceBounds = startingPiece.getBoundingBox();

            int middleX = (startingPieceBounds.maxX + startingPieceBounds.minX) / 2;
            int middleZ = (startingPieceBounds.maxZ + startingPieceBounds.minZ) / 2;
            int y = chunkGenerator.method_20402(middleX, middleZ, Heightmap.Type.WORLD_SURFACE_WG);

            startingPiece.translate(0, y - (startingPieceBounds.minY + startingPiece.getGroundLevelDelta()), 0);
            children.add(startingPiece);

            if (maxSize > 0) {
                Box box = new Box(middleX - 80, y - 80, middleZ - 80, middleX + 81, y + 81, middleZ + 81);

                this.structurePieces.addLast(
                        new PieceWithBoundingBox(
                                startingPiece,
                                new AtomicReference<>(
                                        VoxelShapes.combineAndSimplify(
                                                VoxelShapes.cuboid(box),
                                                VoxelShapes.cuboid(Box.from(startingPieceBounds)),
                                                BooleanBiFunction.ONLY_FIRST
                                        )
                                ),
                                y + 80,
                                0
                        )
                );

                while(!this.structurePieces.isEmpty()) {
                    PieceWithBoundingBox boundedPiece = this.structurePieces.removeFirst();
                    this.generatePiece(boundedPiece.piece, boundedPiece.pieceBounds, boundedPiece.minY, boundedPiece.currentSize);
                }
            }
        }

        private void generatePiece(PoolStructurePiece piece, AtomicReference<VoxelShape> pieceShape, int minY, int currentSize) {
            StructurePoolElement piecePoolElement = piece.getPoolElement();
            BlockPos piecePos = piece.getPos();
            BlockRotation pieceRotation = piece.getRotation();
            StructurePool.Projection pieceProjection = piecePoolElement.getProjection();
            boolean isRigid = pieceProjection == StructurePool.Projection.RIGID;

            AtomicReference<VoxelShape> atomicReference_2 = new AtomicReference<>();
            MutableIntBoundingBox pieceBounds = piece.getBoundingBox();
            int boundsMinY = pieceBounds.minY;
            Iterator jigsaws = piecePoolElement.getStructureBlockInfos(this.structureManager, piecePos, pieceRotation, this.random).iterator();

            while(true) {
                while(true) {
                    label90:
                    while(jigsaws.hasNext()) {
                        // get jigsaw information
                        Structure.StructureBlockInfo jigsaw = (Structure.StructureBlockInfo)  jigsaws.next();
                        Direction jigsawDirection = jigsaw.state.get(JigsawBlock.FACING);
                        BlockPos jigsawPos = jigsaw.pos;
                        BlockPos jigsawOffsetPos = jigsawPos.offset(jigsawDirection);

                        int int_4 = jigsawPos.getY() - boundsMinY;
                        int int_5 = -1;

                        // get pools from jigsaw
                        StructurePool jigsawTargetPool = BTStructurePoolBasedGenerator.REGISTRY.get(new Identifier(jigsaw.tag.getString("target_pool")));
                        StructurePool jigsawTerminatorPool = BTStructurePoolBasedGenerator.REGISTRY.get(jigsawTargetPool.getTerminatorsId());

                        // potential for custom terminator in the case of hallway/stairs
                        jigsawTerminatorPool = getReplacementTerminatorPool(piece) != null ? getReplacementTerminatorPool(piece) : jigsawTerminatorPool;

                        if (jigsawTargetPool != StructurePool.INVALID && (jigsawTargetPool.getElementCount() != 0 || jigsawTargetPool == StructurePool.EMPTY)) {
                            boolean isJigsawTargetPosInsidePiece = pieceBounds.contains(jigsawOffsetPos);
                            AtomicReference atomicReference_4;
                            int int_7;
                            if (isJigsawTargetPosInsidePiece) {
                                atomicReference_4 = atomicReference_2;
                                int_7 = boundsMinY;
                                if (atomicReference_2.get() == null) {
                                    atomicReference_2.set(VoxelShapes.cuboid(Box.from(pieceBounds)));
                                }
                            } else {
                                atomicReference_4 = pieceShape;
                                int_7 = minY;
                            }

                            List<StructurePoolElement> targetPoolElements = Lists.newArrayList();
                            if (currentSize != this.maxSize) {
                                targetPoolElements.addAll(jigsawTargetPool.getElementIndicesInRandomOrder(this.random));
                            }

                            targetPoolElements.addAll(jigsawTerminatorPool.getElementIndicesInRandomOrder(this.random));

                            for (StructurePoolElement poolElement : targetPoolElements) {
                                if (poolElement == EmptyPoolElement.INSTANCE) {
                                    break;
                                }

                                Iterator rotationList = BlockRotation.randomRotationOrder(this.random).iterator();

                                label117:
                                while (rotationList.hasNext()) {
                                    BlockRotation nextRotation = (BlockRotation) rotationList.next();
                                    List<Structure.StructureBlockInfo> list_2 = poolElement.getStructureBlockInfos(this.structureManager, BlockPos.ORIGIN, nextRotation, this.random);
                                    MutableIntBoundingBox mutableIntBoundingBox_2 = poolElement.getBoundingBox(this.structureManager, BlockPos.ORIGIN, nextRotation);
                                    int int_9;
                                    if (mutableIntBoundingBox_2.getBlockCountY() > 16) {
                                        int_9 = 0;
                                    } else {
                                        int_9 = list_2.stream().mapToInt((structure$StructureBlockInfo_1x) -> {
                                            if (!mutableIntBoundingBox_2.contains(structure$StructureBlockInfo_1x.pos.offset((Direction) structure$StructureBlockInfo_1x.state.get(JigsawBlock.FACING)))) {
                                                return 0;
                                            } else {
                                                Identifier identifier_1 = new Identifier(structure$StructureBlockInfo_1x.tag.getString("target_pool"));
                                                StructurePool structurePool = BTStructurePoolBasedGenerator.REGISTRY.get(identifier_1);
                                                StructurePool terminatorPool = BTStructurePoolBasedGenerator.REGISTRY.get(structurePool.getTerminatorsId());

                                                // potential for custom terminator in the case of hallway/stairs
                                                terminatorPool = getReplacementTerminatorPool(piece) != null ? getReplacementTerminatorPool(piece) : terminatorPool;

                                                return Math.max(structurePool.method_19309(this.structureManager), terminatorPool.method_19309(this.structureManager));
                                            }
                                        }).max().orElse(0);
                                    }

                                    Iterator var33 = list_2.iterator();

                                    StructurePool.Projection structurePool$Projection_2;
                                    boolean boolean_3;
                                    int int_11;
                                    int int_12;
                                    int int_14;
                                    MutableIntBoundingBox mutableIntBoundingBox_4;
                                    BlockPos blockPos_6;
                                    int int_17;
                                    do {
                                        Structure.StructureBlockInfo structure$StructureBlockInfo_2;
                                        do {
                                            if (!var33.hasNext()) {
                                                continue label117;
                                            }

                                            structure$StructureBlockInfo_2 = (Structure.StructureBlockInfo) var33.next();
                                        } while (!JigsawBlock.attachmentMatches(jigsaw, structure$StructureBlockInfo_2));

                                        BlockPos blockPos_4 = structure$StructureBlockInfo_2.pos;
                                        BlockPos blockPos_5 = new BlockPos(jigsawOffsetPos.getX() - blockPos_4.getX(), jigsawOffsetPos.getY() - blockPos_4.getY(), jigsawOffsetPos.getZ() - blockPos_4.getZ());
                                        MutableIntBoundingBox mutableIntBoundingBox_3 = poolElement.getBoundingBox(this.structureManager, blockPos_5, nextRotation);
                                        int int_10 = mutableIntBoundingBox_3.minY;
                                        structurePool$Projection_2 = poolElement.getProjection();
                                        boolean_3 = structurePool$Projection_2 == StructurePool.Projection.RIGID;
                                        int_11 = blockPos_4.getY();
                                        int_12 = int_4 - int_11 + ((Direction) jigsaw.state.get(JigsawBlock.FACING)).getOffsetY();
                                        if (isRigid && boolean_3) {
                                            int_14 = boundsMinY + int_12;
                                        } else {
                                            if (int_5 == -1) {
                                                int_5 = this.chunkGenerator.method_20402(jigsawPos.getX(), jigsawPos.getZ(), Heightmap.Type.WORLD_SURFACE_WG);
                                            }

                                            int_14 = int_5 - int_11;
                                        }

                                        int int_15 = int_14 - int_10;
                                        mutableIntBoundingBox_4 = mutableIntBoundingBox_3.method_19311(0, int_15, 0);
                                        blockPos_6 = blockPos_5.add(0, int_15, 0);
                                        if (int_9 > 0) {
                                            int_17 = Math.max(int_9 + 1, mutableIntBoundingBox_4.maxY - mutableIntBoundingBox_4.minY);
                                            mutableIntBoundingBox_4.maxY = mutableIntBoundingBox_4.minY + int_17;
                                        }
                                    } while (VoxelShapes.matchesAnywhere((VoxelShape) atomicReference_4.get(), VoxelShapes.cuboid(Box.from(mutableIntBoundingBox_4).contract(0.25D)), BooleanBiFunction.ONLY_SECOND));

                                    atomicReference_4.set(VoxelShapes.combine((VoxelShape) atomicReference_4.get(), VoxelShapes.cuboid(Box.from(mutableIntBoundingBox_4)), BooleanBiFunction.ONLY_FIRST));
                                    int_17 = piece.getGroundLevelDelta();
                                    int int_19;
                                    if (boolean_3) {
                                        int_19 = int_17 - int_12;
                                    } else {
                                        int_19 = poolElement.method_19308();
                                    }

                                    PoolStructurePiece poolStructurePiece_2 = this.pieceFactory.create(this.structureManager, poolElement, blockPos_6, int_19, nextRotation, mutableIntBoundingBox_4);
                                    int int_22;
                                    if (isRigid) {
                                        int_22 = boundsMinY + int_4;
                                    } else if (boolean_3) {
                                        int_22 = int_14 + int_11;
                                    } else {
                                        if (int_5 == -1) {
                                            int_5 = this.chunkGenerator.method_20402(jigsawPos.getX(), jigsawPos.getZ(), Heightmap.Type.WORLD_SURFACE_WG);
                                        }

                                        int_22 = int_5 + int_12 / 2;
                                    }

                                    piece.addJunction(new JigsawJunction(jigsawOffsetPos.getX(), int_22 - int_4 + int_17, jigsawOffsetPos.getZ(), int_12, structurePool$Projection_2));
                                    poolStructurePiece_2.addJunction(new JigsawJunction(jigsawPos.getX(), int_22 - int_11 + int_19, jigsawPos.getZ(), -int_12, pieceProjection));
                                    this.children.add(poolStructurePiece_2);
                                    if (currentSize + 1 <= this.maxSize) {
                                        this.structurePieces.addLast(new PieceWithBoundingBox(poolStructurePiece_2, atomicReference_4, int_7, currentSize + 1));
                                    }
                                    continue label90;
                                }
                            }
                        } else {
                            LOGGER.warn("Empty or none existent pool: {}", jigsaw.tag.getString("target_pool"));
                        }
                    }

                    return;
                }
            }
        }

        private StructurePool getReplacementTerminatorPool(PoolStructurePiece piece) {
            StructurePool replacement = null;

            // check if we're stairs or hallway; if so, use different terminator
            StructurePoolElement checkElement = piece.getPoolElement();
            if(checkElement instanceof SinglePoolElement) {
                SinglePoolElement singlePoolElement = (SinglePoolElement) checkElement;
                Identifier location = ((LocationAccessor) singlePoolElement).getLocation();

                if(location.toString().contains("stairs") || location.toString().contains("tunnel")) {
                    replacement = BTStructurePoolBasedGenerator.REGISTRY.get(JigsawDungeonStructureStart.HALLWAY_TERMINATORS);
                }
            }

            return replacement;
        }
    }

    private static final class PieceWithBoundingBox {

        private final PoolStructurePiece piece;
        private final AtomicReference<VoxelShape> pieceBounds;
        private final int minY;
        private final int currentSize;

        private PieceWithBoundingBox(PoolStructurePiece piece, AtomicReference<VoxelShape> pieceBounds, int minY, int currentSize) {
            this.piece = piece;
            this.pieceBounds = pieceBounds;
            this.minY = minY;
            this.currentSize = currentSize;
        }
    }
}
