/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2025.1.18
 */
package matsu.num.matrix.core.helper.matrix.householder;

import matsu.num.matrix.core.HouseholderMatrix;
import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.VectorDimension;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * 1次元の Householder 行列の実装を扱う.
 * 
 * <p>
 * 1次元の Householder 行列は唯一 {{-1}} であるので,
 * シングルトンとして扱うことができる.
 * </p>
 * 
 * @author Matsuura Y.
 */
final class OneDimensionHouseholderHolder {

    static final VectorDimension DIMENSION = VectorDimension.valueOf(1);
    static final HouseholderMatrix INSTANCE = new OneDimensionHouseholder();

    private static final class OneDimensionHouseholder
            extends SkeletalHouseholderMatrix<OneDimensionHouseholder> {

        private final VectorDimension vecDim = DIMENSION;
        private final MatrixDimension mxDim = MatrixDimension.square(this.vecDim);

        /**
         * 唯一のコンストラクタ.
         */
        OneDimensionHouseholder() {
            super();
        }

        @Override
        public MatrixDimension matrixDimension() {
            return this.mxDim;
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

            return operand.negated();
        }

        @Override
        protected OneDimensionHouseholder self() {
            return this;
        }
    }
}
