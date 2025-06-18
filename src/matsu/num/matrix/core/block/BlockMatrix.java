/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.12.17
 */
package matsu.num.matrix.core.block;

import java.util.Objects;
import java.util.Optional;

import matsu.num.matrix.core.Matrix;
import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.SkeletalAsymmetricMatrix;
import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.ZeroMatrix;
import matsu.num.matrix.core.common.OptionalUtil;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * ブロック行列を表す.
 * 
 * @author Matsuura Y.
 */
public final class BlockMatrix
        extends SkeletalAsymmetricMatrix<Matrix>
        implements Matrix {

    private final BlockMatrixStructure<? extends Matrix> blockStructure;
    private final Matrix[][] blockMatrix;

    /**
     * 唯一のコンストラクタ.
     * 
     * @throws NullPointerException null
     */
    private BlockMatrix(BlockMatrixStructure<? extends Matrix> blockStructure) {
        this.blockStructure = blockStructure;

        // 空白ブロックに零行列を入れつつ, 配列化する
        MatrixDimension structureDimension = blockStructure.structureDimension();
        this.blockMatrix = new Matrix[structureDimension.rowAsIntValue()][structureDimension.columnAsIntValue()];
        for (int j = 0; j < this.blockMatrix.length; j++) {
            Matrix[] blockMatrix_j = this.blockMatrix[j];
            for (int k = 0; k < blockMatrix_j.length; k++) {
                MatrixDimension elementDimension_j_k = blockStructure.elementDimensionAt(j, k);

                Optional<Matrix> matrixAt_jk =
                        OptionalUtil.castSafe(blockStructure.matrixAt(j, k));
                blockMatrix_j[k] = matrixAt_jk
                        .orElseGet(() -> ZeroMatrix.matrixOf(elementDimension_j_k));
            }
        }
    }

    /**
     * {@link Matrix} のブロック構造を持つブロック行列を返す.
     * 
     * @param structure ブロック構造
     * @return ブロック行列
     * @throws NullPointerException 引数がnullの場合
     */
    public static BlockMatrix of(BlockMatrixStructure<? extends Matrix> structure) {
        return new BlockMatrix(structure);
    }

    @Override
    public MatrixDimension matrixDimension() {
        return this.blockStructure.entireMatrixDimension();
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public Vector operate(Vector operand) {
        if (!this.matrixDimension().rightOperable(operand.vectorDimension())) {
            throw new MatrixFormatMismatchException(
                    String.format(
                            "右から演算不可:matrix:%s, operand:%s",
                            this.matrixDimension(), operand.vectorDimension()));
        }

        Vector[] operandSplittedVectors = this.blockStructure.rightSplit(operand);
        Vector[] resultSplittedVectors = new Vector[this.blockStructure.structureDimension().rowAsIntValue()];

        for (int j = 0; j < resultSplittedVectors.length; j++) {
            Vector tempResult_j = null;
            Matrix[] matrix_j = this.blockMatrix[j];

            for (int k = 0; k < matrix_j.length; k++) {
                Vector product = matrix_j[k].operate(operandSplittedVectors[k]);
                tempResult_j = Objects.isNull(tempResult_j)
                        ? product
                        : tempResult_j.plus(product);
            }

            resultSplittedVectors[j] = tempResult_j;
        }

        return this.blockStructure.mergeRowMatchedVectors(resultSplittedVectors);
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public Vector operateTranspose(Vector operand) {
        if (!this.matrixDimension().leftOperable(operand.vectorDimension())) {
            throw new MatrixFormatMismatchException(
                    String.format(
                            "左から演算不可:matrix:%s, operand:%s",
                            this.matrixDimension(), operand.vectorDimension()));
        }

        Vector[] operandSplittedVectors = this.blockStructure.leftSplit(operand);
        Vector[] resultSplittedVectors = new Vector[this.blockStructure.structureDimension().columnAsIntValue()];

        for (int k = 0; k < operandSplittedVectors.length; k++) {
            Matrix[] matrix_k = this.blockMatrix[k];
            Vector operandSplittedVectors_k = operandSplittedVectors[k];

            for (int j = 0; j < matrix_k.length; j++) {
                Vector product = matrix_k[j].operateTranspose(operandSplittedVectors_k);
                Vector resultSplittedVectors_j = resultSplittedVectors[j];
                resultSplittedVectors[j] = Objects.isNull(resultSplittedVectors_j)
                        ? product
                        : resultSplittedVectors_j.plus(product);
            }
        }

        return this.blockStructure.mergeColumnsMatchedVectors(resultSplittedVectors);
    }

    /**
     * -
     * 
     * <p>
     * (外部からの呼び出し不可)
     * </p>
     */
    @Override
    protected Matrix createTranspose() {
        return Matrix.createTransposedOf(this);
    }
}
