/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2025.6.27
 */
package matsu.num.matrix.core.block;

import java.util.Objects;
import java.util.Optional;

import matsu.num.matrix.core.EntryReadableMatrix;
import matsu.num.matrix.core.Matrix;
import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.SkeletalAsymmetricMatrix;
import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.helper.value.MatrixValidationSupport;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * 成分にアクセス可能なブロック行列を表す.
 * 
 * @author Matsuura Y.
 */
public final class BlockMatrixEntryReadable
        extends SkeletalAsymmetricMatrix<EntryReadableMatrix>
        implements EntryReadableMatrix {

    private final BlockMatrixStructure<? extends EntryReadableMatrix> blockStructure;
    private final Matrix wrappedMatrix;

    //遅延初期化用ロックオブジェクト
    private final Object lock = new Object();
    private volatile Double entryNormMax;

    /**
     * 唯一のコンストラクタ.
     * 
     * @throws NullPointerException 引数がnull
     */
    private BlockMatrixEntryReadable(
            BlockMatrixStructure<? extends EntryReadableMatrix> blockStructure) {
        super();

        this.blockStructure = Objects.requireNonNull(blockStructure);
        this.wrappedMatrix = BlockMatrix.of(blockStructure);
    }

    /**
     * {@link EntryReadableMatrix} のブロック構造を持つブロック行列を返す.
     * 
     * @param structure ブロック構造
     * @return ブロック行列
     * @throws NullPointerException 引数がnullの場合
     */
    public static BlockMatrixEntryReadable of(
            BlockMatrixStructure<? extends EntryReadableMatrix> structure) {
        return new BlockMatrixEntryReadable(structure);
    }

    @Override
    public MatrixDimension matrixDimension() {
        return this.wrappedMatrix.matrixDimension();
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public Vector operate(Vector operand) {
        return this.wrappedMatrix.operate(operand);
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public Vector operateTranspose(Vector operand) {
        return this.wrappedMatrix.operateTranspose(operand);
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc }
     */
    @Override
    public double valueAt(int row, int column) {

        MatrixValidationSupport.validateIndexInMatrix(this.matrixDimension(), row, column);

        /**
         * 線形探索で読みだすべき行列を特定する.
         */
        MatrixDimension structureDimension = this.blockStructure.structureDimension();
        //行の特定
        int structure_row = 0;
        for (int j = 0, len_j = structureDimension.rowAsIntValue(); j < len_j; j++) {
            int elementRows = this.blockStructure.leftOperableVectorDimensionAt(j).intValue();
            if (row < elementRows) {
                structure_row = j;
                break;
            }
            row -= elementRows;
        }

        //列の特定
        int structure_column = 0;
        for (int k = 0, len_k = structureDimension.columnAsIntValue(); k < len_k; k++) {
            int elementColumns = this.blockStructure.rightOperableVectorDimensionAt(k).intValue();
            if (column < elementColumns) {
                structure_column = k;
                break;
            }
            column -= elementColumns;
        }

        Optional<? extends EntryReadableMatrix> element =
                this.blockStructure.matrixAt(structure_row, structure_column);

        return element.isPresent()
                ? element.get().valueAt(row, column)
                : 0d;
    }

    @Override
    public double entryNormMax() {
        Double out = this.entryNormMax;
        if (Objects.nonNull(out)) {
            return out.doubleValue();
        }

        synchronized (this.lock) {
            out = this.entryNormMax;
            if (Objects.nonNull(out)) {
                return out.doubleValue();
            }

            out = Double.valueOf(this.calcEntryNormMax());
            this.entryNormMax = out;
            return out.doubleValue();
        }
    }

    private double calcEntryNormMax() {
        double normMax = 0d;
        MatrixDimension structureDimension = this.blockStructure.structureDimension();

        for (int j = 0, len_j = structureDimension.rowAsIntValue(); j < len_j; j++) {
            for (int k = 0, len_k = structureDimension.columnAsIntValue(); k < len_k; k++) {
                Optional<? extends EntryReadableMatrix> e = this.blockStructure.matrixAt(j, k);
                if (e.isPresent()) {
                    normMax = Math.max(normMax, e.get().entryNormMax());
                }
            }
        }
        return normMax;
    }

    /**
     * -
     * 
     * <p>
     * (外部からの呼び出し不可)
     * </p>
     * 
     * @return -
     */
    @Override
    protected EntryReadableMatrix createTranspose() {
        return EntryReadableMatrix.createTransposedOf(this);
    }

    @Override
    public String toString() {
        return "Matrix[dim: %s, %s]"
                .formatted(
                        this.matrixDimension(),
                        EntryReadableMatrix.toSimplifiedEntryString(this));
    }
}
