/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.2
 */
package matsu.num.matrix.base;

import java.util.Objects;

import matsu.num.matrix.base.common.ArraysUtil;
import matsu.num.matrix.base.helper.value.BandDimensionPositionState;
import matsu.num.matrix.base.validation.ElementsTooManyException;
import matsu.num.matrix.base.validation.MatrixFormatMismatchException;
import matsu.num.matrix.base.validation.MatrixNotSymmetricException;

/**
 * <p>
 * 対称帯行列を扱う.
 * </p>
 * 
 * <p>
 * このクラスのインスタンスはビルダを用いて生成する.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 22.0
 */
public final class SymmetricBandMatrix extends SkeletalSymmetricMatrix<SymmetricBandMatrix>
        implements BandMatrix, Symmetric {
    /*
     * 行列の各要素は対角成分, 副対角成分に分けて, それぞれ1次元配列として扱う.
     * 次元を<i>n</i>, 片側帯幅を<i>b</i>とすると, 各配列の長さは<i>n</i>, <i>n</i><i>b</i>である.
     * 
     * 例えば4*4行列で片側帯幅2の場合:
     * 対角成分の配列をd, 副対角成分の配列をbとすると,
     * d.length = 4, b.length = 8であり,
     * d[0] b[0] b[1] ----
     * b[0] d[1] b[2] b[3]
     * b[1] b[2] d[2] b[4] (b[5])
     * ---- b[3] b[4] d[3] (b[6] b[7])
     * (--- ---- b[5] b[6])
     * (--- ---- ---- b[7])
     * と格納される.
     */
    private final BandMatrixDimension bandMatrixDimension;

    private final double[] diagonalEntry;
    //下三角成分を保存、上三角にコピー
    private final double[] bandEntry;

    private final double entryNormMax;

    /**
     * ビルダから呼ばれる.
     */
    private SymmetricBandMatrix(BandMatrixDimension bandMatrixDimension,
            final double[] diagonalEntry, final double[] bandEntry) {
        this.bandMatrixDimension = bandMatrixDimension;
        this.diagonalEntry = diagonalEntry;
        this.bandEntry = bandEntry;

        this.entryNormMax = this.calcEntryNormMax();
    }

    @Override
    public BandMatrixDimension bandMatrixDimension() {
        return this.bandMatrixDimension;
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public double valueAt(final int row, final int column) {
        final int thisBandWidth = bandMatrixDimension.lowerBandWidth();

        switch (BandDimensionPositionState.positionStateAt(row, column, this.bandMatrixDimension)) {
        case DIAGONAL:
            return diagonalEntry[row];
        case LOWER_BAND:
            return bandEntry[column * thisBandWidth + (row - column - 1)];
        case UPPER_BAND:
            return bandEntry[row * thisBandWidth + (column - row - 1)];
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
     * 外部からの呼び出し不可.
     * 
     * @return -
     */
    @Override
    protected SymmetricBandMatrix self() {
        return this;
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public Vector operate(Vector operand) {
        final VectorDimension vectorDimension = operand.vectorDimension();
        if (!bandMatrixDimension.dimension().rightOperable(vectorDimension)) {
            throw new MatrixFormatMismatchException(
                    String.format(
                            "右から演算不可,matrix:%s, operand:%s",
                            bandMatrixDimension.dimension(), vectorDimension));
        }
        final int dimension = vectorDimension.intValue();
        final int thisBandWidth = bandMatrixDimension.lowerBandWidth();

        final double[] operandEntry = operand.entry();

        final double[] resultEntry = new double[dimension];

        final double[] thisDiagonalEntry = this.diagonalEntry;
        final double[] thisBandEntry = this.bandEntry;

        //対角成分
        for (int i = 0; i < dimension; i++) {
            resultEntry[i] += thisDiagonalEntry[i] * operandEntry[i];
        }
        //狭義下三角成分
        int in = -thisBandWidth;
        for (int i = 0; i < dimension; i++) {
            final double oe_i = operandEntry[i];
            in += thisBandWidth;
            for (int j = 0, l = Math.min(thisBandWidth, dimension - i - 1); j < l; j++) {
                resultEntry[i + j + 1] += thisBandEntry[in + j] * oe_i;
            }
        }
        //狭義上三角成分
        in = -thisBandWidth;
        for (int i = 0; i < dimension; i++) {
            double sumProduct = 0;
            int k, l;
            in += thisBandWidth;
            for (k = 0, l = Math.min(thisBandWidth, dimension - i - 1); k < l - 3; k += 4) {
                final double v0 = thisBandEntry[in + k] * operandEntry[i + k + 1];
                final double v1 = thisBandEntry[in + k + 1] * operandEntry[i + k + 2];
                final double v2 = thisBandEntry[in + k + 2] * operandEntry[i + k + 3];
                final double v3 = thisBandEntry[in + k + 3] * operandEntry[i + k + 4];
                sumProduct += (v0 + v1) + (v2 + v3);
            }
            for (; k < l; k++) {
                final double v0 = thisBandEntry[in + k] * operandEntry[i + k + 1];
                sumProduct += v0;
            }
            resultEntry[i] += sumProduct;
        }

        Vector.Builder builder = Vector.Builder.zeroBuilder(vectorDimension);
        builder.setEntryValue(resultEntry);
        return builder.build();
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public Vector operateTranspose(Vector operand) {
        return this.operate(operand);
    }

    @Override
    public double entryNormMax() {
        return this.entryNormMax;
    }

    private double calcEntryNormMax() {
        return Math.max(ArraysUtil.normMax(diagonalEntry), ArraysUtil.normMax(bandEntry));
    }

    /**
     * このインスタンスの文字列表現を返す.
     */
    @Override
    public String toString() {
        return String.format(
                "Matrix[band:%s, %s]",
                this.bandMatrixDimension(), EntryReadableMatrix.toSimplifiedEntryString(this));
    }

    /**
     * <p>
     * 対称帯行列を生成するビルダ. <br>
     * このビルダはミュータブルであり, スレッドセーフでない.
     * </p>
     * 
     * <p>
     * ビルダの生成時に有効要素数が大きすぎる場合は例外がスローされる. <br>
     * 有効要素数が大きすぎるとは, <br>
     * 行列の行数(= 列数)を <i>n</i>, 片側帯幅を <i>b</i> として, <br>
     * <i>n</i> * <i>b</i> &gt; {@link Integer#MAX_VALUE} <br>
     * である状態である.
     * </p>
     */
    public static final class Builder {

        private BandMatrixDimension bandMatrixDimension;

        private double[] diagonalEntry;
        //下三角成分を保存、上三角にコピー
        private double[] bandEntry;

        /**
         * 与えられた帯行列構造の対称帯行列ビルダを生成する. <br>
         * 初期値は零行列.
         *
         * @param bandMatrixDimension 帯行列構造
         * @throws MatrixFormatMismatchException 帯行列構造が対称でない場合
         * @throws ElementsTooManyException 行列の有効要素数が大きすぎる場合(dim * b > IntMax)
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        private Builder(final BandMatrixDimension bandMatrixDimension) {
            if (!bandMatrixDimension.isSymmetric()) {
                throw new MatrixFormatMismatchException(
                        String.format(
                                "対称でない帯構造:%s", bandMatrixDimension));
            }
            this.bandMatrixDimension = bandMatrixDimension;
            final int dimension = bandMatrixDimension.dimension().rowAsIntValue();
            final int bandWidth = bandMatrixDimension.lowerBandWidth();

            final long long_entrySize = (long) dimension * bandWidth;
            if (long_entrySize > Integer.MAX_VALUE) {
                throw new ElementsTooManyException("サイズが大きすぎる");
            }
            diagonalEntry = new double[dimension];
            bandEntry = new double[(int) long_entrySize];
        }

        /**
         * 与えられたソースから対称帯行列のビルダを作成する.
         *
         * @param src ソース
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        private Builder(final SymmetricBandMatrix src) {
            this.bandMatrixDimension = src.bandMatrixDimension;
            this.diagonalEntry = src.diagonalEntry.clone();
            this.bandEntry = src.bandEntry.clone();
        }

        /**
         * <p>
         * (<i>i</i>, <i>j</i>) 要素を指定した値に置き換える. <br>
         * 同時に (<i>j</i>, <i>i</i>) の値も置き換わる.
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
         * @throws IndexOutOfBoundsException (<i>i</i>, <i>j</i>) が行列の帯領域内でない場合
         * @see EntryReadableMatrix#acceptValue(double)
         */
        public void setValue(final int row, final int column, double value) {

            if (Objects.isNull(this.diagonalEntry)) {
                throw new IllegalStateException("すでにビルドされています");
            }

            final int thisBandWidth = bandMatrixDimension.lowerBandWidth();

            //値の修正
            value = EntryReadableMatrix.modified(value);

            switch (BandDimensionPositionState.positionStateAt(row, column, this.bandMatrixDimension)) {
            case DIAGONAL:
                diagonalEntry[row] = value;
                return;
            case LOWER_BAND:
                bandEntry[column * thisBandWidth + (row - column - 1)] = value;
                return;
            case UPPER_BAND:
                bandEntry[row * thisBandWidth + (column - row - 1)] = value;
                return;
            case OUT_OF_BAND:
                throw new IndexOutOfBoundsException(
                        String.format(
                                "帯の外側:matrix:%s, (row, column)=(%s, %s)",
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
         * 対称帯行列をビルドする.
         *
         * @return 対称帯行列, {@link Symmetric} が付与されている
         * @throws IllegalStateException すでにビルドされている場合
         */
        public SymmetricBandMatrix build() {
            if (Objects.isNull(this.diagonalEntry)) {
                throw new IllegalStateException("すでにビルドされています");
            }
            SymmetricBandMatrix out = new SymmetricBandMatrix(bandMatrixDimension, diagonalEntry, bandEntry);
            this.diagonalEntry = null;
            this.bandEntry = null;
            return out;
        }

        /**
         * 与えられた帯構造を持つ, 零行列で初期化された対称帯行列ビルダを生成する.
         *
         * @param bandMatrixDimension 帯行列構造
         * @return 零行列で初期化されたビルダ
         * @throws MatrixFormatMismatchException 帯行列構造が対称でない場合
         * @throws ElementsTooManyException 行列の有効要素数が大きすぎる場合(クラス説明文)
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        public static Builder zero(final BandMatrixDimension bandMatrixDimension) {
            return new Builder(bandMatrixDimension);
        }

        /**
         * 与えられた帯構造を持つ, 単位行列で初期化された対称帯行列ビルダを生成する.
         *
         * @param bandMatrixDimension 帯行列構造
         * @return 単位行列で初期化されたビルダ
         * @throws MatrixFormatMismatchException 帯行列構造が対称でない場合
         * @throws ElementsTooManyException 行列の有効要素数が大きすぎる場合(クラス説明文)
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        public static Builder unit(final BandMatrixDimension bandMatrixDimension) {
            Builder outBuilder = new Builder(bandMatrixDimension);
            for (int i = 0, dimension = bandMatrixDimension.dimension().rowAsIntValue(); i < dimension; i++) {
                outBuilder.setValue(i, i, 1.0);
            }
            return outBuilder;
        }

        /**
         * 与えられたインスタンスの成分で初期化されたビルダを作成する.
         *
         * @param src 元行列
         * @return 元行列と等価なビルダ
         * @throws MatrixNotSymmetricException 行列が対称行列でない場合
         * @throws ElementsTooManyException 行列の有効要素数が大きすぎる場合(クラス説明文)
         * @throws NullPointerException 引数にnullが含まれる場合
         * @see Symmetric
         */
        public static Builder from(final BandMatrix src) {
            if (!(Objects.requireNonNull(src) instanceof Symmetric)) {
                throw new MatrixNotSymmetricException("対称行列でない");
            }

            final BandMatrixDimension bandMatrixDimension = src.bandMatrixDimension();
            if (src instanceof SymmetricBandMatrix) {
                return new Builder((SymmetricBandMatrix) src);
            }

            final int srcDimension = bandMatrixDimension.dimension().rowAsIntValue();
            final int srcBandWidth = bandMatrixDimension.lowerBandWidth();

            final Builder outBuilder = new Builder(bandMatrixDimension);

            //対角成分
            for (int i = 0; i < srcDimension; i++) {
                double value = src.valueAt(i, i);
                if (!EntryReadableMatrix.acceptValue(value)) {
                    throw new AssertionError(
                            String.format(
                                    "BandMatrixが適切に実装されていない: entryValue=%s", value));
                }
                outBuilder.setValue(i, i, value);
            }

            //下三角成分
            for (int i = 0; i < srcDimension; i++) {
                for (int j = 0, l = Math.min(srcBandWidth, srcDimension - i - 1); j < l; j++) {
                    int r = j + i + 1;
                    int c = i;

                    double value = src.valueAt(r, c);
                    if (!EntryReadableMatrix.acceptValue(value)) {
                        throw new AssertionError(
                                String.format(
                                        "BandMatrixが適切に実装されていない: entryValue=%s", value));
                    }
                    outBuilder.setValue(r, c, value);
                }
            }

            return outBuilder;
        }
    }
}
