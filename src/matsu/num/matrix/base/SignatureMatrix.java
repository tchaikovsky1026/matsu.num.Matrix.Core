/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.27
 */
package matsu.num.matrix.base;

import java.util.Objects;
import java.util.Optional;

import matsu.num.matrix.base.helper.value.BandDimensionPositionState;
import matsu.num.matrix.base.validation.MatrixFormatMismatchException;

/**
 * Signature matrix (符号行列) を表す.
 * 
 * <p>
 * Signature matrix とは, 対角成分が1または-1の対角行列である. <br>
 * よって, 対称行列かつ直交行列である.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 23.1
 */
public sealed interface SignatureMatrix
        extends DiagonalMatrix, OrthogonalMatrix permits SignatureMatrixSealed, UnitMatrix {

    @Override
    public abstract SignatureMatrix transpose();

    @Override
    public abstract Optional<? extends SignatureMatrix> inverse();

    /**
     * Signature matrix の対角成分に並ぶ-1の数の偶奇を取得する.
     *
     * @return -1が偶数個のときtrue
     */
    public abstract boolean isEven();

    /**
     * {@link SignatureMatrix} を生成するためのビルダ. <br>
     * このビルダはミュータブルであり, スレッドセーフでない.
     */
    public static final class Builder {

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
                throw new MatrixFormatMismatchException(
                        String.format("正方形ではない行列サイズ:%s", matrixDimension));
            }

            this.matrixDimension = matrixDimension;
            this.signature = new boolean[matrixDimension.rowAsIntValue()];
        }

        /**
         * 与えられた次元(サイズ)の符号行列ビルダを生成する. <br>
         * 初期値は単位行列.
         *
         * @param matrixDimension 行列サイズ
         * @return 新しいビルダ
         * @throws MatrixFormatMismatchException 行列サイズが正方形でない場合
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        public static Builder unit(MatrixDimension matrixDimension) {
            return new Builder(matrixDimension);
        }

        /**
         * 符号行列の対角成分 (<i>i</i>, <i>i</i>) を1に書き換える.
         * 
         * @param index <i>i</i>, 対角成分のindex
         * @throws IllegalStateException すでにビルドされている場合
         * @throws IndexOutOfBoundsException (<i>i</i>, <i>i</i>) が行列内部でない場合
         */
        public void setPositiveAt(int index) {
            if (Objects.isNull(this.signature)) {
                throw new IllegalStateException("すでにビルドされています");
            }
            if (!this.matrixDimension.isValidRowIndex(index)) {
                throw new IndexOutOfBoundsException(
                        String.format(
                                "行列外:matrix:%s, index=%d",
                                matrixDimension, index));
            }
            if (this.signature[index]) {
                this.reverseAt(index);
            }
        }

        /**
         * 符号行列の対角成分 (<i>i</i>, <i>i</i>) を-1に書き換える.
         * 
         * @param index <i>i</i>, 対角成分のindex
         * @throws IllegalStateException すでにビルドされている場合
         * @throws IndexOutOfBoundsException (<i>i</i>, <i>i</i>) が行列内部でない場合
         */
        public void setNegativeAt(int index) {
            if (Objects.isNull(this.signature)) {
                throw new IllegalStateException("すでにビルドされています");
            }
            if (!this.matrixDimension.isValidRowIndex(index)) {
                throw new IndexOutOfBoundsException(
                        String.format(
                                "行列外:matrix:%s, index=%d",
                                matrixDimension, index));
            }
            if (!this.signature[index]) {
                this.reverseAt(index);
            }
        }

        /**
         * 符号行列の対角成分 (<i>i</i>, <i>i</i>) を反転させる.
         * 
         * @param index <i>i</i>, 対角成分のindex
         * @throws IllegalStateException すでにビルドされている場合
         * @throws IndexOutOfBoundsException (<i>i</i>, <i>i</i>) が行列内部でない場合
         */
        public void reverseAt(int index) {
            if (Objects.isNull(this.signature)) {
                throw new IllegalStateException("すでにビルドされています");
            }
            if (!this.matrixDimension.isValidRowIndex(index)) {
                throw new IndexOutOfBoundsException(
                        String.format(
                                "行列外:matrix:%s, index=%d",
                                matrixDimension, index));
            }
            this.signature[index] = !this.signature[index];
            this.unit = false;
            this.even = !this.even;
        }

        /**
         * 符号行列をビルドする.
         *
         * @return 符号行列
         * @throws IllegalStateException すでにビルドされている場合
         */
        public SignatureMatrix build() {
            if (Objects.isNull(this.signature)) {
                throw new IllegalStateException("すでにビルドされています");
            }
            SignatureMatrix out = this.unit
                    ? UnitMatrix.matrixOf(this.matrixDimension)
                    : new SignatureMatrixImpl(this);
            this.signature = null;
            return out;
        }

        /**
         * 符号行列の実装.
         */
        private static final class SignatureMatrixImpl
                extends SkeletalSymmetricOrthogonalMatrix<SignatureMatrixImpl>
                implements SignatureMatrixSealed {

            private final BandMatrixDimension bandMatrixDimension;

            //falseなら1, trueなら-1を表現する.
            private final boolean[] signature;

            private final boolean even;

            /**
             * 唯一のコンストラクタ. <br>
             * ビルダから呼ばれる.
             */
            SignatureMatrixImpl(Builder builder) {
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
                switch (BandDimensionPositionState.positionStateAt(
                        row, column, this.bandMatrixDimension)) {
                case DIAGONAL:
                    return this.signature[row] ? -1d : 1d;
                case LOWER_BAND:
                    throw new AssertionError("Bug: 到達不能");
                case UPPER_BAND:
                    throw new AssertionError("Bug: 到達不能");
                case OUT_OF_BAND:
                    return 0d;
                case OUT_OF_MATRIX:
                    throw new IndexOutOfBoundsException(
                            String.format(
                                    "行列内部でない:matrix:%s, (row, column)=(%d, %d)",
                                    bandMatrixDimension.dimension(), row, column));
                default:
                    throw new AssertionError("Bug: 列挙型に想定外の値");
                }
            }

            @Override
            public double entryNormMax() {
                return 1d;
            }

            @Override
            public Vector operate(Vector operand) {
                final VectorDimension vectorDimension = operand.vectorDimension();
                if (!bandMatrixDimension.dimension().rightOperable(vectorDimension)) {
                    throw new MatrixFormatMismatchException(
                            String.format(
                                    "右から演算不可:matrix:%s, operand:%s",
                                    bandMatrixDimension, vectorDimension));
                }

                final double[] entry = operand.entry();

                final int dimension = vectorDimension.intValue();
                for (int i = 0; i < dimension; i++) {
                    if (this.signature[i]) {
                        entry[i] = -entry[i];
                    }
                }

                Vector.Builder builder = Vector.Builder.zeroBuilder(vectorDimension);
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
                        "Matrix[dim:%s, signiture(%s)]",
                        this.matrixDimension(), this.signOfDeterminant());
            }
        }
    }
}
