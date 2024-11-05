/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.3
 */
package matsu.num.matrix.base.helper.matrix.transpose;

import java.util.Objects;
import java.util.Optional;

import matsu.num.matrix.base.MatrixDimension;
import matsu.num.matrix.base.OrthogonalMatrix;
import matsu.num.matrix.base.Symmetric;
import matsu.num.matrix.base.Vector;

/**
 * {@link OrthogonalMatrix}の転置を扱う.
 * 
 * @author Matsuura Y.
 * @version 22.0
 */
public final class TranspositionOrthogonal {

    private static final TranspositionOrthogonal INSTANCE = new TranspositionOrthogonal();

    private TranspositionOrthogonal() {
        if (Objects.nonNull(INSTANCE)) {
            throw new AssertionError();
        }
    }

    /**
     * {@link OrthogonalMatrix} の推奨される実装規約に則った転置行列を返す. <br>
     * {@link Symmetric} が付与されている場合は, 引数をそのまま返す.
     *
     * @param original 元の行列
     * @return 転置行列
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public OrthogonalMatrix apply(OrthogonalMatrix original) {
        if (original instanceof Symmetric) {
            return original;
        }

        if (original instanceof TransposedOrthogonal) {
            return ((TransposedOrthogonal) original).original.get();
        }

        return new TransposedOrthogonal(original);
    }

    /**
     * このインスタンスを生成する.
     * 
     * @return インスタンス
     */
    public static TranspositionOrthogonal instance() {
        return INSTANCE;
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