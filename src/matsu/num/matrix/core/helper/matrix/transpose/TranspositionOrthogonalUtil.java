/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2025.1.7
 */
package matsu.num.matrix.core.helper.matrix.transpose;

import java.util.Optional;

import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.OrthogonalMatrix;
import matsu.num.matrix.core.Symmetric;
import matsu.num.matrix.core.Vector;

/**
 * {@link OrthogonalMatrix}の転置を扱う.
 * 
 * @author Matsuura Y.
 * @version 26.1
 */
public final class TranspositionOrthogonalUtil {

    private TranspositionOrthogonalUtil() {
        //インスタンス化不可
        throw new AssertionError();
    }

    /**
     * {@link OrthogonalMatrix} の推奨される実装規約に則った転置行列を返す. <br>
     * {@link Symmetric} が付与されている場合は, 引数をそのまま返す.
     *
     * @param original 元の行列
     * @return 転置行列
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static OrthogonalMatrix apply(OrthogonalMatrix original) {
        if (original instanceof Symmetric) {
            return original;
        }

        if (original instanceof TransposedOrthogonal castedOriginal) {
            return (castedOriginal).original.get();
        }

        return new TransposedOrthogonal(original);
    }

    /**
     * 直交行列の転置を扱う.
     */
    private static final class TransposedOrthogonal implements OrthogonalMatrix {

        private final Optional<OrthogonalMatrix> original;

        TransposedOrthogonal(OrthogonalMatrix matrix) {
            this.original = Optional.of(matrix);
        }

        @Override
        public MatrixDimension matrixDimension() {
            //直交行列は正方行列だが,転置を意識するためにtransposeを加える
            return this.original.get().matrixDimension().transpose();
        }

        @Override
        public Vector operate(Vector operand) {
            return this.original.get().operateTranspose(operand);
        }

        @Override
        public Vector operateTranspose(Vector operand) {
            return this.original.get().operate(operand);
        }

        @Override
        public Optional<? extends OrthogonalMatrix> inverse() {
            return this.original;
        }

        @Override
        public final OrthogonalMatrix transpose() {
            return this.original.get();
        }

        @Override
        public String toString() {
            return String.format(
                    "Matrix[dim:%s, orthogonal]",
                    this.matrixDimension());
        }
    }
}
