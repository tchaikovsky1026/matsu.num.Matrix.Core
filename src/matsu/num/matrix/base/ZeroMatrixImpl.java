/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.10
 */
package matsu.num.matrix.base;

import matsu.num.matrix.base.validation.MatrixFormatMismatchException;

/**
 * 零行列の実装
 * 
 * @author Matsuura Y.
 * @version 22.4
 */
final class ZeroMatrixImpl extends SkeletalAsymmetricMatrix<ZeroMatrix> implements ZeroMatrix {

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
        if (!this.matrixDimension.isValidIndexes(row, column)) {
            throw new IndexOutOfBoundsException(
                    String.format(
                            "行列外:matrix:%s, (row, column)=(%s, %s)",
                            this.matrixDimension, row, column));
        }

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
        if (!this.matrixDimension.rightOperable(operand.vectorDimension())) {
            throw new MatrixFormatMismatchException(
                    String.format(
                            "右からの演算不可:matrix:%s, operand:%s",
                            this.matrixDimension, operand.vectorDimension()));
        }
        return this.operatedVector;
    }

    @Override
    public Vector operateTranspose(Vector operand) {
        if (!this.matrixDimension.leftOperable(operand.vectorDimension())) {
            throw new MatrixFormatMismatchException(
                    String.format(
                            "左からの演算不可:matrix:%s, operand:%s",
                            this.matrixDimension, operand.vectorDimension()));
        }
        return this.transposeOperatedVector;
    }

    @Override
    protected ZeroMatrix createTranspose() {
        return new TransposeAttachedZeroMatrix(
                new ZeroMatrixImpl(this.matrixDimension.transpose()),
                this);
    }

    @Override
    public String toString() {
        return String.format(
                "Matrix[dim:%s, zero]",
                this.matrixDimension());
    }

    /**
     * 転置行列を直接結びつける零行列. <br>
     * オリジナルのtranspose()が呼ばれなくなる.
     * 
     * <p>
     * シール可能にするためにクラスはパッケージプライベート公開とするが,
     * 生成はエンクロージングクラスからしか行わないのでコンストラクタは非公開.
     * </p>
     */
    static final class TransposeAttachedZeroMatrix implements ZeroMatrix {

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
