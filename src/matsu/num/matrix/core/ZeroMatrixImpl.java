/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2025.6.26
 */
package matsu.num.matrix.core;

import matsu.num.matrix.core.helper.value.MatrixValidationSupport;

/**
 * 零行列の実装.
 * 
 * @author Matsuura Y.
 */
final class ZeroMatrixImpl
        extends SkeletalAsymmetricMatrix<ZeroMatrix> implements ZeroMatrix {

    private final MatrixDimension matrixDimension;
    private final Vector operatedVector;
    private final Vector transposeOperatedVector;

    /**
     * 唯一のコンストラクタ. <br>
     * 正方形の行列サイズを与えて正方零行列を生成する.
     * 
     * @throws NullPointerException null
     */
    ZeroMatrixImpl(MatrixDimension matrixDimension) {
        this.matrixDimension = matrixDimension;
        this.operatedVector = Vector.Builder
                .zeroBuilder(matrixDimension.leftOperableVectorDimension())
                .build();
        this.transposeOperatedVector = Vector.Builder
                .zeroBuilder(matrixDimension.rightOperableVectorDimension())
                .build();
    }

    @Override
    public double valueAt(int row, int column) {
        MatrixValidationSupport.validateIndexInMatrix(matrixDimension, row, column);

        return 0d;
    }

    @Override
    public double entryNormMax() {
        return 0d;
    }

    @Override
    public MatrixDimension matrixDimension() {
        return this.matrixDimension;
    }

    @Override
    public Vector operate(Vector operand) {
        MatrixValidationSupport.validateOperate(matrixDimension, operand.vectorDimension());

        return this.operatedVector;
    }

    @Override
    public Vector operateTranspose(Vector operand) {
        MatrixValidationSupport.validateOperateTranspose(matrixDimension, operand.vectorDimension());

        return this.transposeOperatedVector;
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
    protected ZeroMatrix createTranspose() {
        return new TransposeAttachedZeroMatrix(
                new ZeroMatrixImpl(this.matrixDimension.transpose()),
                this);
    }

    @Override
    public String toString() {
        return String.format(
                "Matrix[dim: %s, zero]",
                this.matrixDimension());
    }

    /**
     * 転置行列を直接結びつける零行列. <br>
     * オリジナルのtranspose()が呼ばれなくなる.
     */
    private static final class TransposeAttachedZeroMatrix implements ZeroMatrix {

        private final ZeroMatrix original;
        private final ZeroMatrix transpose;

        /**
         * 唯一のコンストラクタ. <br>
         * オリジナルと転置行列を結びつける.
         */
        private TransposeAttachedZeroMatrix(ZeroMatrix original, ZeroMatrix transpose) {
            super();
            this.original = original;
            this.transpose = transpose;
        }

        @Override
        public double valueAt(int row, int column) {
            return this.original.valueAt(row, column);
        }

        @Override
        public double entryNormMax() {
            return this.original.entryNormMax();
        }

        @Override
        public MatrixDimension matrixDimension() {
            return this.original.matrixDimension();
        }

        @Override
        public Vector operate(Vector operand) {
            return this.original.operate(operand);
        }

        @Override
        public Vector operateTranspose(Vector operand) {
            return this.original.operateTranspose(operand);
        }

        @Override
        public ZeroMatrix transpose() {
            return this.transpose;
        }

        @Override
        public String toString() {
            return this.original.toString();
        }
    }
}
