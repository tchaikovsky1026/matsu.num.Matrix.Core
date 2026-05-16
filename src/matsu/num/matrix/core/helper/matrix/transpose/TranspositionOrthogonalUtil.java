/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2026.5.9
 */
package matsu.num.matrix.core.helper.matrix.transpose;

import java.util.Optional;

import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.OrthogonalMatrix;
import matsu.num.matrix.core.Symmetric;
import matsu.num.matrix.core.Vector;

/**
 * {@link OrthogonalMatrix} の転置を扱うユーティリティ.
 * 
 * @author Matsuura Y.
 */
public final class TranspositionOrthogonalUtil {

    private TranspositionOrthogonalUtil() {
        //インスタンス化不可
        throw new AssertionError();
    }

    /**
     * {@link OrthogonalMatrix} の推奨される実装規約に則った転置行列を返す.
     * 
     * <p>
     * 引数 {@code original}, 戻り値 {@code returnValue} について,
     * 次が {@code true} である.
     * </p>
     * 
     * <ul>
     * <li>{@code returnValue.transpose() == original}</li>
     * <li>{@code returnValue.inverse().get() == original}</li>
     * </ul>
     * 
     * <p>
     * {@code original} に {@link Symmetric} が付与されている場合,
     * {@code returnValue == original} が {@code true} である.
     * </p>
     * 
     * <p>
     * <u>
     * <i>
     * このメソッドは {@link OrthogonalMatrix#transpose()} や
     * {@link OrthogonalMatrix#inverse()}
     * の実装を補助するために用意されている. <br>
     * {@link OrthogonalMatrix} およびそのサブタイプのインスタンスの転置行列, 逆行列を得る場合は,
     * このメソッドではなくインスタンスメソッドである {@link OrthogonalMatrix#transpose()},
     * {@link OrthogonalMatrix#inverse()}
     * を呼ばなければならない.
     * </i>
     * </u>
     * </p>
     * 
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
        @SuppressWarnings("removal")
        public Optional<? extends OrthogonalMatrix> inverse() {
            return this.original;
        }

        @Override
        @SuppressWarnings("removal")
        public final OrthogonalMatrix transpose() {
            return this.original.get();
        }

        @Override
        public OrthogonalMatrix transposeAsOrthogonal() {
            return this.original.get();
        }

        @Override
        public String toString() {
            return String.format(
                    "Matrix[dim: %s, orthogonal]",
                    this.matrixDimension());
        }
    }
}
