/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.4.4
 */
package matsu.num.matrix.base;

import java.util.Objects;
import java.util.function.DoubleFunction;

import matsu.num.matrix.base.common.ArraysUtil;
import matsu.num.matrix.base.validation.ElementsTooManyException;
import matsu.num.matrix.base.validation.MatrixFormatMismatchException;

/**
 * <p>
 * 矩形 (長方形) の (密) 行列を扱う.
 * </p>
 * 
 * <p>
 * このクラスのインスタンスはビルダを用いて生成する.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 21.0
 */
public final class GeneralMatrix extends SkeletalMatrix implements EntryReadableMatrix {

    /*
     * 行列の各要素を, 内部では1次元配列として,
     * {@code double[rowIndex * columnDimension + columnIndex]}の順番で保持する.
     */
    private final MatrixDimension matrixDimension;
    private final double[] entry;

    private final double entryNormMax;

    /**
     * ビルダから呼ばれる.
     */
    private GeneralMatrix(final MatrixDimension matrixDimension, final double[] entry) {
        this.matrixDimension = matrixDimension;
        this.entry = entry;

        this.entryNormMax = this.calcEntryNormMax();
    }

    @Override
    public MatrixDimension matrixDimension() {
        return this.matrixDimension;
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc }
     */
    @Override
    public double valueAt(final int row, final int column) {
        if (!(matrixDimension.isValidIndexes(row, column))) {
            throw new IndexOutOfBoundsException(
                    String.format(
                            "行列外:matrix:%s, (row, column)=(%s, %s)",
                            matrixDimension, row, column));
        }
        return entry[row * matrixDimension.columnAsIntValue() + column];
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     */
    @Override
    public Vector operate(Vector operand) {
        if (!matrixDimension.rightOperable(operand.vectorDimension())) {
            throw new MatrixFormatMismatchException(
                    String.format(
                            "右から演算不可:matrix:%s, operand:%s",
                            matrixDimension, operand.vectorDimension()));
        }

        final int rowDimension = matrixDimension.rowAsIntValue();
        final int columnDimension = matrixDimension.columnAsIntValue();
        final double[] operandEntry = operand.entry();

        final double[] resultEntry = new double[rowDimension];
        final double[] matrixEntry = this.entry;

        int jn = -columnDimension;
        for (int j = 0; j < rowDimension; j++) {
            double sumProduct = 0.0;
            int k;
            jn += columnDimension;
            for (k = 0; k < columnDimension - 3; k += 4) {
                final double v0 = matrixEntry[jn + k] * operandEntry[k];
                final double v1 = matrixEntry[jn + k + 1] * operandEntry[k + 1];
                final double v2 = matrixEntry[jn + k + 2] * operandEntry[k + 2];
                final double v3 = matrixEntry[jn + k + 3] * operandEntry[k + 3];
                sumProduct += (v0 + v1) + (v2 + v3);
            }
            for (; k < columnDimension; k++) {
                sumProduct += matrixEntry[jn + k] * operandEntry[k];
            }
            resultEntry[j] = sumProduct;
        }

        Vector.Builder builder = Vector.Builder.zeroBuilder(this.matrixDimension.leftOperableVectorDimension());
        builder.setEntryValue(resultEntry);
        return builder.build();
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     */
    @Override
    public Vector operateTranspose(Vector operand) {
        if (!matrixDimension.leftOperable(operand.vectorDimension())) {
            throw new MatrixFormatMismatchException(
                    String.format(
                            "左から演算不可:matrix:%s, operand:%s",
                            matrixDimension, operand.vectorDimension()));
        }

        final int rowDimension = matrixDimension.rowAsIntValue();
        final int columnDimension = matrixDimension.columnAsIntValue();

        final double[] operandEntry = operand.entry();

        final double[] resultEntry = new double[columnDimension];
        final double[] matrixEntry = this.entry;

        for (int k = 0; k < columnDimension; k++) {
            resultEntry[k] = 0;
        }
        int jn = -columnDimension;
        for (int j = 0; j < rowDimension; j++) {
            jn += columnDimension;
            final double operandEntry_j = operandEntry[j];
            for (int k = 0; k < columnDimension; k++) {
                resultEntry[k] += matrixEntry[jn + k] * operandEntry_j;
            }
        }

        Vector.Builder builder = Vector.Builder.zeroBuilder(this.matrixDimension.rightOperableVectorDimension());
        builder.setEntryValue(resultEntry);
        return builder.build();
    }

    @Override
    public double entryNormMax() {
        return this.entryNormMax;
    }

    private double calcEntryNormMax() {
        return ArraysUtil.normMax(entry);
    }

    /**
     * このインスタンスの文字列表現を返す.
     */
    @Override
    public String toString() {
        return EntryReadableMatrix.toString(this);
    }

    /**
     * <p>
     * 矩形 (長方形) の (密) 行列を生成するビルダ. <br>
     * このビルダはミュータブルであり, スレッドセーフでない.
     * </p>
     * 
     * <p>
     * ビルダの生成時に有効要素数が大きすぎる場合は例外がスローされる. <br>
     * 有効要素数が大きすぎるとは, <br>
     * 行列の行数を <i>r</i>, 列数を <i>c</i> として, <br>
     * <i>r</i> * <i>c</i> &gt; {@linkplain Integer#MAX_VALUE} <br>
     * である状態である.
     * </p>
     */
    public static final class Builder {

        private MatrixDimension matrixDimension;
        private double[] entry;

        /**
         * 与えられた次元(サイズ)の矩形(長方形)行列ビルダを生成する. <br>
         * 初期値は零行列.
         *
         * @param matrixDimension 行列サイズ
         * @throws ElementsTooManyException 行列の有効要素数が大きすぎる場合(r * c > IntMax)
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        private Builder(MatrixDimension matrixDimension) {

            final int thisRowDimension = matrixDimension.rowAsIntValue();
            final int thisColumnDimension = matrixDimension.columnAsIntValue();
            this.matrixDimension = matrixDimension;

            final long long_entrySize = (long) thisRowDimension * (long) thisColumnDimension;
            if (long_entrySize > Integer.MAX_VALUE) {
                throw new ElementsTooManyException("サイズが大きすぎる");
            }
            this.entry = new double[(int) long_entrySize];
        }

        /**
         * 与えられたソースから矩形行列のビルダを作成する.
         *
         * @param src ソース
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        private Builder(final GeneralMatrix src) {
            this.matrixDimension = src.matrixDimension;
            this.entry = src.entry.clone();
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
         * @throws IndexOutOfBoundsException (<i>i</i>, <i>j</i>) が行列の内部でない場合
         * @throws IllegalStateException すでにビルドされている場合
         * @see EntryReadableMatrix#acceptValue(double)
         */
        public void setValue(final int row, final int column, double value) {

            if (Objects.isNull(this.entry)) {
                throw new IllegalStateException("すでにビルドされています");
            }
            if (!(matrixDimension.isValidIndexes(row, column))) {
                throw new IndexOutOfBoundsException(
                        String.format(
                                "行列外:matrix:%s, (row, column)=(%s, %s)",
                                matrixDimension, row, column));
            }

            //値を修正する
            value = EntryReadableMatrix.modified(value);

            entry[row * matrixDimension.columnAsIntValue() + column] = value;
        }

        /**
         * (<i>i</i>, <i>j</i>) 要素を指定した値に置き換える. <br>
         * 値が不正の場合は, 与えたファンクションにより例外を生成してスローする.
         *
         * @param row <i>i</i>, 行index
         * @param column <i>j</i>, 列index
         * @param value 置き換えた後の値
         * @param invalidValueExceptionGetter valueが不正な値の場合にスローする例外の生成器
         * @param <X> スローされる例外の型
         * @throws IndexOutOfBoundsException (<i>i</i>, <i>j</i>) が行列の内部でない場合
         * @throws X valueが不正な値である場合
         * @throws IllegalStateException すでにビルドされている場合
         * @throws NullPointerException 引数にnullが含まれる場合
         * @see EntryReadableMatrix#acceptValue(double)
         */
        public <X extends Exception> void setValueOrElseThrow(final int row, final int column, double value,
                DoubleFunction<X> invalidValueExceptionGetter) throws X {

            Objects.requireNonNull(invalidValueExceptionGetter);
            if (Objects.isNull(this.entry)) {
                throw new IllegalStateException("すでにビルドされています");
            }
            if (!EntryReadableMatrix.acceptValue(value)) {
                throw invalidValueExceptionGetter.apply(value);
            }
            if (!(matrixDimension.isValidIndexes(row, column))) {
                throw new IndexOutOfBoundsException(
                        String.format(
                                "行列外:matrix:%s, (row, column)=(%s, %s)",
                                matrixDimension, row, column));
            }
            entry[row * matrixDimension.columnAsIntValue() + column] = value;
        }

        /**
         * 第 <i>i</i> 行と第 <i>j</i> 行を交換して新しい行列として返す.
         *
         * @param row1 <i>i</i>, 行index1
         * @param row2 <i>j</i>, 行index2
         * @throws IndexOutOfBoundsException <i>i</i>, <i>j</i> が行列の内部でない場合
         * @throws IllegalStateException すでにビルドされている場合
         */
        public void swapRows(final int row1, final int row2) {
            if (Objects.isNull(this.entry)) {
                throw new IllegalStateException("すでにビルドされています");
            }
            if (!(matrixDimension.isValidRowIndex(row1)
                    && matrixDimension.isValidRowIndex(row2))) {
                throw new IndexOutOfBoundsException(
                        String.format(
                                "行列外:matrix:%s, (row1, row2)=(%s, %s)",
                                matrixDimension, row1, row2));
            }

            if (row1 == row2) {
                return;
            }

            final int columnDimension = matrixDimension.columnAsIntValue();
            final int rn1 = row1 * columnDimension;
            final int rn2 = row2 * columnDimension;
            for (int columnIndex = 0; columnIndex < columnDimension; columnIndex++) {
                final int i1 = rn1 + columnIndex;
                final int i2 = rn2 + columnIndex;
                final double temp = entry[i1];
                entry[i1] = entry[i2];
                entry[i2] = temp;
            }
        }

        /**
         * 第 <i>i</i> 列と第 <i>j</i> 列を交換して新しい行列として返す.
         *
         * @param column1 <i>i</i>, 列index1
         * @param column2 <i>j</i>, 列index2
         * @throws IndexOutOfBoundsException <i>i</i>, <i>j</i> が行列の内部でない場合
         * @throws IllegalStateException すでにビルドされている場合
         */
        public void swapColumns(final int column1, final int column2) {
            if (Objects.isNull(this.entry)) {
                throw new IllegalStateException("すでにビルドされています");
            }
            if (!(matrixDimension.isValidColumnIndex(column1)
                    && matrixDimension.isValidColumnIndex(column2))) {
                throw new IndexOutOfBoundsException(
                        String.format(
                                "行列外:matrix:%s, (column1, column2)=(%s, %s)",
                                matrixDimension, column1, column2));
            }

            if (column1 == column2) {
                return;
            }

            final int rowDimension = matrixDimension.rowAsIntValue();
            final int columnDimension = matrixDimension.columnAsIntValue();

            int ri = -columnDimension;
            for (int rowIndex = 0; rowIndex < rowDimension; rowIndex++) {
                ri += columnDimension;
                final int i1 = ri + column1;
                final int i2 = ri + column2;
                final double temp = entry[i1];
                entry[i1] = entry[i2];
                entry[i2] = temp;
            }
        }

        /**
         * 矩形(長方形)行列をビルドする.
         *
         * @return 矩形(長方形)行列
         * @throws IllegalStateException すでにビルドされている場合
         */
        public GeneralMatrix build() {
            if (Objects.isNull(this.entry)) {
                throw new IllegalStateException("すでにビルドされています");
            }
            GeneralMatrix out = new GeneralMatrix(this.matrixDimension, this.entry);
            this.entry = null;
            return out;
        }

        /**
         * 与えられた次元(サイズ)を持つ, 零行列で初期化された矩形(長方形)行列ビルダを生成する.
         * 
         *
         * @param matrixDimension 行列サイズ
         * @return 零行列で初期化されたビルダ
         * @throws ElementsTooManyException 行列の有効要素数が大きすぎる場合(クラス説明文)
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        public static Builder zero(MatrixDimension matrixDimension) {
            return new Builder(matrixDimension);
        }

        /**
         * 与えられた次元(サイズ)を持つ, 単位行列で初期化されたビルダを作成する.
         *
         * @param matrixDimension 行列サイズ
         * @return 単位行列で初期化したビルダ
         * @throws MatrixFormatMismatchException 行列サイズが正方形でない場合
         * @throws ElementsTooManyException 行列の有効要素数が大きすぎる場合(クラス説明文)
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        public static Builder unit(final MatrixDimension matrixDimension) {
            if (!matrixDimension.isSquare()) {
                throw new MatrixFormatMismatchException(
                        String.format("正方形ではない行列サイズ:%s", matrixDimension));
            }

            final Builder unitBuilder = new Builder(matrixDimension);
            for (int i = 0, dimension = matrixDimension.rowAsIntValue(); i < dimension; i++) {
                unitBuilder.setValue(i, i, 1.0);
            }
            return unitBuilder;
        }

        /**
         * 与えられたインスタンスの成分で初期化されたビルダを作成する.
         *
         * @param src 元行列
         * @return 元行列と等価なビルダ
         * @throws ElementsTooManyException 行列の有効要素数が大きすぎる場合(クラス説明文),
         *             成分に不正な値が入り込む場合
         * @throws NullPointerException 引数にnullが含まれる場合
         * @see EntryReadableMatrix#acceptValue(double)
         */
        public static Builder from(final Matrix src) {
            if (src instanceof EntryReadableMatrix) {
                return Builder.from((EntryReadableMatrix) src);
            }

            final MatrixDimension srcMatrixDimension = src.matrixDimension();
            final int srcRowDimension = srcMatrixDimension.rowAsIntValue();
            final int srcColumnDimension = srcMatrixDimension.columnAsIntValue();

            final Builder outBuilder = new Builder(srcMatrixDimension);

            //転置に対して基本単位ベクトルを乗算する
            //配列の方向が列方向であるため(速さは不明である)
            for (int j = 0; j < srcRowDimension; j++) {
                double[] rightArray = new double[srcRowDimension];
                rightArray[j] = 1;

                Vector.Builder builder = Vector.Builder.zeroBuilder(srcMatrixDimension.leftOperableVectorDimension());
                builder.setEntryValue(rightArray);
                final double[] resultArray = src.operateTranspose(builder.build()).entry();
                for (int k = 0; k < srcColumnDimension; k++) {
                    outBuilder.setValue(j, k, resultArray[k]);
                }
            }
            return outBuilder;
        }

        /**
         * 与えられたインスタンスの成分で初期化されたビルダを作成する.
         *
         * @param src 元行列
         * @return 元行列と等価なビルダ
         * @throws ElementsTooManyException 行列の有効要素数が大きすぎる場合(クラス説明文)
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        public static Builder from(final EntryReadableMatrix src) {

            final MatrixDimension srcMatrixDimension = src.matrixDimension();
            final int srcRowDimension = srcMatrixDimension.rowAsIntValue();
            final int srcColumnDimension = srcMatrixDimension.columnAsIntValue();
            if (src instanceof GeneralMatrix) {
                return new Builder((GeneralMatrix) src);
            }

            final Builder outBuilder = new Builder(srcMatrixDimension);

            //転置に対して基本単位ベクトルを乗算する
            //配列の方向が列方向であるため(速さは不明である)
            for (int j = 0; j < srcRowDimension; j++) {
                for (int k = 0; k < srcColumnDimension; k++) {
                    double value = src.valueAt(j, k);
                    if (!EntryReadableMatrix.acceptValue(value)) {
                        throw new AssertionError(
                                String.format(
                                        "EntryReadableMatrixが適切に実装されていない: entryValue=%s", value));
                    }
                    outBuilder.setValue(j, k, value);
                }
            }
            return outBuilder;
        }
    }
}
