/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2025.6.14
 */
package matsu.num.matrix.core;

import java.util.Objects;

import matsu.num.matrix.core.helper.value.BandDimensionPositionState;
import matsu.num.matrix.core.helper.value.MatrixValidationSupport;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * {@link SignatureMatrix} の具象を提供する. <br>
 * ビルダクラスをネストしている.
 * 
 * @author Matsuura Y.
 */
final class SignatureMatrixImpl
        extends SkeletalSymmetricOrthogonalMatrix<SignatureMatrixImpl>
        implements SignatureMatrix {

    private final BandMatrixDimension bandMatrixDimension;

    //falseなら1, trueなら-1を表現する.
    private final boolean[] signature;

    private final boolean even;

    /**
     * 唯一のコンストラクタ. <br>
     * ビルダから呼ばれる.
     */
    private SignatureMatrixImpl(Builder builder) {
        this.bandMatrixDimension = BandMatrixDimension.symmetric(builder.matrixDimension, 0);
        this.signature = builder.signature;
        this.even = builder.even;
    }

    @Override
    public BandMatrixDimension bandMatrixDimension() {
        return this.bandMatrixDimension;
    }

    @Override
    public double valueAt(int row, int column) {

        MatrixValidationSupport.validateIndexInMatrix(bandMatrixDimension.dimension(), row, column);

        switch (BandDimensionPositionState.positionStateAt(
                row, column, this.bandMatrixDimension)) {
            case DIAGONAL:
                return this.signature[row] ? -1d : 1d;
            case OUT_OF_BAND:
                return 0d;
            //OUT_OF_MATRIXは検証済み
            //$CASES-OMITTED$
            default:
                throw new AssertionError("Bug: reachable");
        }
    }

    @Override
    public double entryNormMax() {
        return 1d;
    }

    @Override
    public Vector operate(Vector operand) {
        final var vectorDimension = operand.vectorDimension();

        MatrixValidationSupport.validateOperate(bandMatrixDimension.dimension(), vectorDimension);

        final double[] entry = operand.entryAsArray();

        final int dimension = vectorDimension.intValue();
        for (int i = 0; i < dimension; i++) {
            if (this.signature[i]) {
                entry[i] = -entry[i];
            }
        }

        var builder = Vector.Builder.zeroBuilder(vectorDimension);
        builder.setEntryValue(entry);
        return builder.build();
    }

    @Override
    public double determinant() {
        return this.even
                ? 1d
                : -1d;
    }

    @Override
    public double logAbsDeterminant() {
        return 0d;
    }

    @Override
    public int signOfDeterminant() {
        return this.even
                ? 1
                : -1;
    }

    @Override
    public boolean isEven() {
        return this.even;
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
    protected SignatureMatrixImpl self() {
        return this;
    }

    /**
     * このオブジェクトの文字列説明表現を返す.
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code Matrix[dim:(%dimension), signiture(%sign)]}
     * </p>
     * 
     * @return 説明表現
     */
    @Override
    public String toString() {
        return String.format(
                "Matrix[dim: %s, signature(%s)]",
                this.matrixDimension(), this.signOfDeterminant());
    }

    /**
     * ビルダ.
     */
    static final class Builder extends SignatureMatrix.Builder {

        private final MatrixDimension matrixDimension;

        //falseなら1, trueなら-1を表現する.
        private boolean[] signature;

        private boolean even = true;
        private boolean unit = true;

        /**
         * 与えられた次元(サイズ)の符号行列ビルダを生成する. <br>
         * 初期値は単位行列.
         *
         * @param matrixDimension 行列サイズ
         * @throws MatrixFormatMismatchException 行列サイズが正方形でない場合
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        private Builder(MatrixDimension matrixDimension) {
            if (!matrixDimension.isSquare()) {
                throw new MatrixFormatMismatchException("not square: " + matrixDimension);
            }

            this.matrixDimension = matrixDimension;
            this.signature = new boolean[matrixDimension.rowAsIntValue()];
        }

        /**
         * コピーコンストラクタ.
         */
        private Builder(Builder src) {
            this.matrixDimension = src.matrixDimension;
            this.signature = src.signature.clone();
            this.even = src.even;
            this.unit = src.unit;
        }

        /**
         * {@link SignatureMatrix.Builder#unit(MatrixDimension)} の転送先.
         */
        static Builder unitImpl(MatrixDimension matrixDimension) {
            return new Builder(matrixDimension);
        }

        @Override
        public void setPositiveAt(int index) {
            this.throwISExIfCannotBeUsed();

            MatrixValidationSupport.validateIndexInMatrix(matrixDimension, index, index);

            if (this.signature[index]) {
                this.reverseAt(index);
            }
        }

        @Override
        public void setNegativeAt(int index) {
            this.throwISExIfCannotBeUsed();

            MatrixValidationSupport.validateIndexInMatrix(matrixDimension, index, index);

            if (!this.signature[index]) {
                this.reverseAt(index);
            }
        }

        @Override
        public void reverseAt(int index) {
            this.throwISExIfCannotBeUsed();

            MatrixValidationSupport.validateIndexInMatrix(matrixDimension, index, index);

            this.signature[index] = !this.signature[index];
            this.unit = false;
            this.even = !this.even;
        }

        @Override
        public boolean canBeUsed() {
            return Objects.nonNull(this.signature);
        }

        /**
         * ビルド前かを判定し, ビルド後なら例外をスロー.
         */
        private void throwISExIfCannotBeUsed() {
            if (!this.canBeUsed()) {
                throw new IllegalStateException("already built");
            }
        }

        @Override
        public Builder copy() {
            this.throwISExIfCannotBeUsed();

            return new Builder(this);
        }

        /**
         * ビルダを使用不能にする.
         */
        private void disable() {
            this.signature = null;
        }

        @Override
        public SignatureMatrix build() {
            this.throwISExIfCannotBeUsed();

            var out = this.unit
                    ? UnitMatrix.matrixOf(this.matrixDimension)
                    : new SignatureMatrixImpl(this);
            this.disable();

            return out;
        }
    }
}
