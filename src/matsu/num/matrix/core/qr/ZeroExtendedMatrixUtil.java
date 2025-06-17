/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2025.6.17
 */
package matsu.num.matrix.core.qr;

import matsu.num.matrix.core.Matrix;
import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.ZeroMatrix;
import matsu.num.matrix.core.block.BlockMatrix;
import matsu.num.matrix.core.block.BlockMatrixStructure;

/**
 * 既存の行列を左上ブロックに含み, 右, 下を0埋めして拡大した行列を扱うユーティリティ. <br>
 * 広義拡張(サイズが同じか拡大)をサポートする.
 * 
 * @author Matsuura Y.
 */
final class ZeroExtendedMatrixUtil {

    private ZeroExtendedMatrixUtil() {
        //インスタンス化不可
        throw new AssertionError();
    }

    /**
     * 既存の行列を拡張した行列インスタンスを返す.
     * 
     * <p>
     * 引数のバリデーションは行われていない. <br>
     * 行列が拡張されていなければならない.
     * </p>
     * 
     * @param innerMatrix 左上ブロックに相当する行列
     * @param extendedMatrixDimension 拡張された直交行列の次元(サイズ)
     * @return 拡張行列
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    static Matrix instanceOf(
            final Matrix innerMatrix,
            MatrixDimension extendedMatrixDimension) {

        MatrixDimension innerDimension = innerMatrix.matrixDimension();
        if (innerDimension.equals(extendedMatrixDimension)) {
            return innerMatrix;
        }

        // 行列が拡張されていない場合にアサーション
        assert innerDimension.rowAsIntValue() <= extendedMatrixDimension.rowAsIntValue()
                && innerDimension.columnAsIntValue() <= extendedMatrixDimension.columnAsIntValue();

        if (innerDimension.rowAsIntValue() == extendedMatrixDimension.rowAsIntValue()) {
            return columnExtended(innerMatrix, extendedMatrixDimension);
        }
        if (innerDimension.columnAsIntValue() == extendedMatrixDimension.columnAsIntValue()) {
            return rowExtended(innerMatrix, extendedMatrixDimension);

        }

        return bothExtended(innerMatrix, extendedMatrixDimension);
    }

    /**
     * 列方向 (横方向) に拡張する.
     */
    private static Matrix columnExtended(final Matrix innerMatrix,
            MatrixDimension extendedMatrixDimension) {

        MatrixDimension innerDimension = innerMatrix.matrixDimension();
        Matrix extendedColumn = ZeroMatrix.matrixOf(
                MatrixDimension.rectangle(
                        innerDimension.rowAsIntValue(),
                        extendedMatrixDimension.columnAsIntValue() - innerDimension.columnAsIntValue()));

        BlockMatrixStructure.Builder<Matrix> builder = BlockMatrixStructure.Builder.of(MatrixDimension.rectangle(1, 2));
        builder.setBlockElement(0, 0, innerMatrix);
        builder.setBlockElement(0, 1, extendedColumn);

        return BlockMatrix.of(builder.build());
    }

    /**
     * 行方向 (縦方向) に拡張する.
     */
    private static Matrix rowExtended(final Matrix innerMatrix,
            MatrixDimension extendedMatrixDimension) {

        MatrixDimension innerDimension = innerMatrix.matrixDimension();
        Matrix extendedRow = ZeroMatrix.matrixOf(
                MatrixDimension.rectangle(
                        extendedMatrixDimension.rowAsIntValue() - innerDimension.rowAsIntValue(),
                        innerDimension.columnAsIntValue()));

        BlockMatrixStructure.Builder<Matrix> builder = BlockMatrixStructure.Builder.of(MatrixDimension.rectangle(2, 1));
        builder.setBlockElement(0, 0, innerMatrix);
        builder.setBlockElement(1, 0, extendedRow);

        return BlockMatrix.of(builder.build());
    }

    /**
     * 行列・列の両方向に拡張する.
     */
    private static Matrix bothExtended(final Matrix innerMatrix,
            MatrixDimension extendedMatrixDimension) {

        MatrixDimension innerDimension = innerMatrix.matrixDimension();
        Matrix extendedRightBottom = ZeroMatrix.matrixOf(
                MatrixDimension.rectangle(
                        extendedMatrixDimension.rowAsIntValue() - innerDimension.rowAsIntValue(),
                        extendedMatrixDimension.columnAsIntValue() - innerDimension.columnAsIntValue()));

        BlockMatrixStructure.Builder<Matrix> builder = BlockMatrixStructure.Builder.of(MatrixDimension.rectangle(2, 2));
        builder.setBlockElement(0, 0, innerMatrix);
        builder.setBlockElement(1, 1, extendedRightBottom);

        return BlockMatrix.of(builder.build());
    }
}
