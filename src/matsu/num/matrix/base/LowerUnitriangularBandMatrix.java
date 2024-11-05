/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.5
 */
package matsu.num.matrix.base;

import java.util.Objects;
import java.util.Optional;

import matsu.num.matrix.base.common.ArraysUtil;
import matsu.num.matrix.base.helper.value.BandDimensionPositionState;
import matsu.num.matrix.base.validation.ElementsTooManyException;
import matsu.num.matrix.base.validation.MatrixFormatMismatchException;

/**
 * <p>
 * 単位下三角の帯行列を扱う.
 * </p>
 * 
 * <p>
 * このクラスのインスタンスはビルダを用いて生成する.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 22.0
 */
public final class LowerUnitriangularBandMatrix
        extends SkeletalAsymmetricMatrix<BandMatrix> implements LowerUnitriangular, BandMatrix {

    /*
     * 行列の各要素は, 内部では狭義下三角成分を1次元配列として扱う.
     * 次元を<i>n</i>, 下側帯幅を<i>b</i><sub>l</sub>とすると,
     * 配列の長さは<i>n</i><i>b</i><sub>l</sub>である.
     * 
     * 例えば4*4行列で下側帯幅2の場合:
     * 副対角成分の配列を{@code b}とすると{@code b.length = 8}であり, 以下のように格納する。
     * 1.0 --- --- ---
     * [0] 1.0 --- ---
     * [1] [2] 1.0 ---
     * --- [3] [4] 1.0
     * (-- --- [5] [6])
     * (-- --- --- [7])
     */
    private final BandMatrixDimension bandMatrixDimension;

    private final double[] lowerEntry;

    private final Optional<Matrix> inverse;

    /**
     * ビルダから呼ばれる.
     */
    private LowerUnitriangularBandMatrix(final BandMatrixDimension bandMatrixDimension,
            final double[] lowerEntry) {
        this.bandMatrixDimension = bandMatrixDimension;
        this.lowerEntry = lowerEntry;

        this.inverse = Optional.of(this.createInverse());
    }

    @Override
    public BandMatrixDimension bandMatrixDimension() {
        return this.bandMatrixDimension;
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc }
     */
    @Override
    public double valueAt(final int row, final int column) {

        final int thisLowerBandWidth = bandMatrixDimension.lowerBandWidth();

        switch (BandDimensionPositionState.positionStateAt(row, column, this.bandMatrixDimension)) {
        case DIAGONAL:
            return 1;
        case LOWER_BAND:
            return lowerEntry[column * thisLowerBandWidth + (row - column - 1)];
        case UPPER_BAND:
            throw new AssertionError("Bug: 到達不能");
        case OUT_OF_BAND:
            return 0;
        case OUT_OF_MATRIX:
            throw new IndexOutOfBoundsException(
                    String.format(
                            "行列内部でない:matrix:%s, (row, column)=(%s, %s)",
                            bandMatrixDimension.dimension(), row, column));
        default:
            throw new AssertionError("Bug: 列挙型に想定外の値");
        }
    }

    /**
     * <i>(外部からの呼び出し不可)</i>
     * 
     * @return -
     */
    @Override
    protected BandMatrix createTranspose() {
        return BandMatrix.createTransposedOf(this);
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     */
    @Override
    public Vector operate(Vector operand) {
        final VectorDimension vectorDimension = operand.vectorDimension();
        if (!bandMatrixDimension.dimension().rightOperable(vectorDimension)) {
            throw new MatrixFormatMismatchException(
                    String.format(
                            "右から演算不可:matrix:%s, operand:%s",
                            bandMatrixDimension.dimension(), vectorDimension));
        }
        final int dimension = vectorDimension.intValue();
        final int thisLowerBandWidth = bandMatrixDimension.lowerBandWidth();

        final double[] operandEntry = operand.entry();
        final double[] resultEntry = new double[dimension];
        final double[] thisLowerEntry = this.lowerEntry;

        //対角成分
        System.arraycopy(operandEntry, 0, resultEntry, 0, dimension);
        //狭義下三角成分
        int in = dimension * thisLowerBandWidth;
        for (int i = dimension - 1; i >= 0; i--) {
            final double oe_i = operandEntry[i];
            in -= thisLowerBandWidth;
            for (int j = Math.min(thisLowerBandWidth, dimension - i - 1) - 1; j >= 0; j--) {
                resultEntry[i + j + 1] += thisLowerEntry[in + j] * oe_i;
            }
        }

        Vector.Builder builder = Vector.Builder.zeroBuilder(vectorDimension);
        builder.setEntryValue(resultEntry);
        return builder.build();
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     */
    @Override
    public Vector operateTranspose(Vector operand) {
        final VectorDimension vectorDimension = operand.vectorDimension();
        if (!bandMatrixDimension.dimension().leftOperable(vectorDimension)) {
            throw new MatrixFormatMismatchException(
                    String.format(
                            "左から演算不可:matrix:%s, operand:%s",
                            bandMatrixDimension.dimension(), vectorDimension));
        }

        final int dimension = vectorDimension.intValue();
        final int thisLowerBandWidth = bandMatrixDimension.lowerBandWidth();

        final double[] operandEntry = operand.entry();
        final double[] resultEntry = new double[dimension];
        final double[] thisLowerEntry = this.lowerEntry;

        //対角成分
        System.arraycopy(operandEntry, 0, resultEntry, 0, dimension);
        //狭義下三角成分
        int in = -thisLowerBandWidth;
        for (int i = 0; i < dimension; i++) {
            double sumProduct = 0;
            int k, l;
            in += thisLowerBandWidth;
            for (k = 0, l = Math.min(thisLowerBandWidth, dimension - i - 1); k < l - 3; k += 4) {
                final double v0 = thisLowerEntry[in + k] * operandEntry[i + k + 1];
                final double v1 = thisLowerEntry[in + k + 1] * operandEntry[i + k + 2];
                final double v2 = thisLowerEntry[in + k + 2] * operandEntry[i + k + 3];
                final double v3 = thisLowerEntry[in + k + 3] * operandEntry[i + k + 4];
                sumProduct += (v0 + v1) + (v2 + v3);
            }
            for (; k < l; k++) {
                final double v0 = thisLowerEntry[in + k] * operandEntry[i + k + 1];
                sumProduct += v0;
            }
            resultEntry[i] += sumProduct;
        }

        Vector.Builder builder = Vector.Builder.zeroBuilder(vectorDimension);
        builder.setEntryValue(resultEntry);
        return builder.build();
    }

    @Override
    public double entryNormMax() {
        return Math.max(ArraysUtil.normMax(lowerEntry), 1);
    }

    @Override
    public double determinant() {
        return 1.0;
    }

    @Override
    public double logAbsDeterminant() {
        return 0.0;
    }

    @Override
    public int signOfDeterminant() {
        return 1;
    }

    @Override
    public Optional<Matrix> inverse() {
        return this.inverse;
    }

    /**
     * このインスタンスの文字列説明表現を返す.
     */
    @Override
    public String toString() {
        return String.format(
                "Matrix[band:%s, %s, lower, unitriangular]",
                this.bandMatrixDimension(), EntryReadableMatrix.toSimplifiedEntryString(this));
    }

    /**
     * この下三角行列の逆行列を作成する.
     * 
     * @return 逆行列
     */
    private Matrix createInverse() {
        return new SkeletalAsymmetricMatrix<Matrix>() {

            @Override
            public MatrixDimension matrixDimension() {
                return bandMatrixDimension.dimension();
            }

            @Override
            protected Matrix createTranspose() {
                return Matrix.createTransposedOf(this);
            }

            @Override
            public Vector operate(Vector operand) {
                final VectorDimension vectorDimension = operand.vectorDimension();
                if (!bandMatrixDimension.dimension().leftOperable(vectorDimension)) {
                    throw new MatrixFormatMismatchException(
                            String.format(
                                    "左から演算不可:matrix:%s, operand:%s",
                                    bandMatrixDimension.dimension(), vectorDimension));
                }

                final int dimension = vectorDimension.intValue();
                final int thisLowerBandWidth = bandMatrixDimension.lowerBandWidth();
                final double[] resultEntry = new double[dimension];

                final double[] thisLowerEntry = lowerEntry;

                System.arraycopy(operand.entry(), 0, resultEntry, 0, dimension);
                int in = -thisLowerBandWidth;
                for (int i = 0; i < dimension; i++) {
                    final double re_i = resultEntry[i];
                    in += thisLowerBandWidth;
                    for (int j = 0, l = Math.min(thisLowerBandWidth, dimension - i - 1); j < l; j++) {
                        resultEntry[i + j + 1] -= thisLowerEntry[in + j] * re_i;
                    }
                }

                Vector.Builder builder = Vector.Builder.zeroBuilder(vectorDimension);
                builder.setEntryValue(resultEntry);
                return builder.build();
            }

            @Override
            public Vector operateTranspose(Vector operand) {
                final VectorDimension vectorDimension = operand.vectorDimension();
                if (!bandMatrixDimension.dimension().rightOperable(vectorDimension)) {
                    throw new MatrixFormatMismatchException(
                            String.format(
                                    "右から演算不可:matrix:%s, operand:%s",
                                    bandMatrixDimension.dimension(), vectorDimension));
                }

                final int dimension = vectorDimension.intValue();
                final int thisLowerBandWidth = bandMatrixDimension.lowerBandWidth();
                final double[] resultEntry = new double[dimension];

                final double[] thisLowerEntry = lowerEntry;

                System.arraycopy(operand.entry(), 0, resultEntry, 0, dimension);
                int in = dimension * thisLowerBandWidth;
                for (int i = dimension - 1; i >= 0; i--) {
                    double sumProduct = 0;
                    int k, l;
                    in -= thisLowerBandWidth;
                    for (l = Math.min(thisLowerBandWidth, dimension - i - 1), k = l - 1; k >= 3; k -= 4) {
                        final double v0 = thisLowerEntry[in + k] * resultEntry[i + k + 1];
                        final double v1 = thisLowerEntry[in + k + 1] * resultEntry[i + k + 2];
                        final double v2 = thisLowerEntry[in + k + 2] * resultEntry[i + k + 3];
                        final double v3 = thisLowerEntry[in + k + 3] * resultEntry[i + k + 4];
                        sumProduct += (v0 + v1) + (v2 + v3);
                    }
                    for (; k >= 0; k--) {
                        final double v0 = thisLowerEntry[in + k] * resultEntry[i + k + 1];
                        sumProduct += v0;
                    }
                    resultEntry[i] -= sumProduct;
                }

                Vector.Builder builder = Vector.Builder.zeroBuilder(vectorDimension);
                builder.setEntryValue(resultEntry);
                return builder.build();
            }
        };

    }

    /**
     * <p>
     * 単位下三角の帯行列を作成するビルダ. <br>
     * このビルダはミュータブルであり, スレッドセーフでない.
     * </p>
     * 
     * <p>
     * ビルダの生成時に有効要素数が大きすぎる場合は例外がスローされる. <br>
     * 有効要素数が大きすぎるとは, <br>
     * 行列の行数(= 列数)を <i>n</i>, 下側帯幅を <i>b</i> として, <br>
     * <i>n</i> * <i>b</i> &gt; {@link Integer#MAX_VALUE} <br>
     * である状態である.
     * </p>
     */
    public static final class Builder {
        private BandMatrixDimension bandMatrixDimension;
        private double[] lowerEntry;

        /**
         * 与えられた帯行列構造の単位下三角帯行列ビルダを生成する. <br>
         * 初期値は単位行列.
         *
         * @param bandMatrixDimension 下三角である帯行列構造
         * @throws MatrixFormatMismatchException 帯行列構造が下三角構造でない,
         *             {@link BandMatrixDimension#upperBandWidth} &gt;
         *             0である場合
         * @throws ElementsTooManyException 行列の有効要素数が大きすぎる場合(dim * lb > IntMax)
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        private Builder(final BandMatrixDimension bandMatrixDimension) {
            if (bandMatrixDimension.upperBandWidth() > 0) {
                throw new MatrixFormatMismatchException(String.format("下三角行列でない:%s", bandMatrixDimension));
            }
            this.bandMatrixDimension = bandMatrixDimension;

            final long long_entrySize = (long) bandMatrixDimension.dimension().rowAsIntValue()
                    * bandMatrixDimension.lowerBandWidth();
            if (long_entrySize > Integer.MAX_VALUE) {
                throw new ElementsTooManyException("サイズが大きすぎる");
            }
            this.lowerEntry = new double[(int) long_entrySize];
        }

        /**
         * <p>
         * (<i>i</i>, <i>j</i>) 要素を指定した値に置き換える.
         * </p>
         * 
         * <p>
         * 値が不正ならば, 正常値に修正される.
         * </p>
         *
         * @param row <i>i</i>, 行index
         * @param column <i>j</i>, 列index
         * @param value 置き換えた後の値
         * @throws IllegalStateException すでにビルドされている場合
         * @throws IndexOutOfBoundsException (<i>i</i>, <i>j</i>)
         *             が行列の狭義下側帯領域内でない場合
         * @see EntryReadableMatrix#acceptValue(double)
         */
        public void setValue(final int row, final int column, double value) {
            if (Objects.isNull(this.lowerEntry)) {
                throw new IllegalStateException("すでにビルドされています");
            }

            final int thisLowerBandWidth = bandMatrixDimension.lowerBandWidth();

            //値を修正する
            value = EntryReadableMatrix.modified(value);

            switch (BandDimensionPositionState.positionStateAt(row, column, this.bandMatrixDimension)) {
            case DIAGONAL:
                throw new IndexOutOfBoundsException(
                        String.format(
                                "対角成分は変更不可:matrix:%s, (row, column)=(%s, %s)",
                                bandMatrixDimension, row, column));
            case LOWER_BAND:
                lowerEntry[column * thisLowerBandWidth + (row - column - 1)] = value;
                return;
            case UPPER_BAND:
                throw new AssertionError("Bug: 到達不能");
            case OUT_OF_BAND:
                throw new IndexOutOfBoundsException(
                        String.format(
                                "帯の外側:matrix:%s, (row, column)=(%d, %d)",
                                bandMatrixDimension, row, column));
            case OUT_OF_MATRIX:
                throw new IndexOutOfBoundsException(
                        String.format(
                                "行列内部でない:matrix:%s, (row, column)=(%s, %s)",
                                bandMatrixDimension.dimension(), row, column));
            default:
                throw new AssertionError("Bug: 列挙型に想定外の値");
            }
        }

        /**
         * 単位下三角行列をビルドする.
         * 
         * @return 単位下三角行列
         * @throws IllegalStateException すでにビルドされている場合
         */
        public LowerUnitriangularBandMatrix build() {
            if (Objects.isNull(this.lowerEntry)) {
                throw new IllegalStateException("すでにビルドされています");
            }
            LowerUnitriangularBandMatrix out = new LowerUnitriangularBandMatrix(
                    this.bandMatrixDimension, this.lowerEntry);
            this.lowerEntry = null;
            return out;
        }

        /**
         * 与えられた帯行列構造を持つ, 単位行列で初期化された単位下三角帯行列ビルダを生成する.
         *
         * @param bandMatrixDimension 下三角である帯行列構造
         * @return 単位行列で初期化されたビルダ
         * @throws MatrixFormatMismatchException 帯行列構造が下三角構造でない,
         *             {@link BandMatrixDimension#upperBandWidth} &gt;
         *             0である場合
         * @throws ElementsTooManyException 行列の有効要素数が大きすぎる場合(クラス説明文)
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        public static Builder unit(final BandMatrixDimension bandMatrixDimension) {
            return new Builder(bandMatrixDimension);
        }
    }

}
