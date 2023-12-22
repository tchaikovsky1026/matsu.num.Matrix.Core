/**
 * 2023.11.30
 */
package matsu.num.matrix.base;

import java.util.Objects;

import matsu.num.commons.ArraysUtil;
import matsu.num.matrix.base.exception.MatrixFormatMismatchException;

/**
 * 矩形(長方形)の(密)行列を生成するビルダ.
 * 
 * <p>
 * このビルダはミュータブルである. <br>
 * また, スレッドセーフでない.
 * </p>
 * 
 * <p>
 * ビルダの生成時に有効要素数が大きすぎる場合は例外がスローされる. <br>
 * 有効要素数が大きすぎるとは, <br>
 * 行列の行数を<i>r</i>, 列数を<i>c</i>として, <br>
 * <i>r</i> * <i>c</i> {@literal >} {@linkplain Integer#MAX_VALUE} <br>
 * である状態である.
 * </p>
 *
 * @author Matsuura Y.
 * @version 17.1
 */
public final class GeneralMatrixBuilder {

    private MatrixDimension matrixDimension;
    private double[] entry;

    /**
     * 与えられた次元(サイズ)の矩形(長方形)行列ビルダを生成する. <br>
     * 初期値は零行列.
     *
     * @param matrixDimension 行列サイズ
     * @throws IllegalArgumentException 行列の有効要素数が大きすぎる場合(r * c > IntMax)
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    private GeneralMatrixBuilder(MatrixDimension matrixDimension) {

        final int thisRowDimension = matrixDimension.rowAsIntValue();
        final int thisColumnDimension = matrixDimension.columnAsIntValue();
        this.matrixDimension = matrixDimension;

        final long long_entrySize = (long) thisRowDimension * (long) thisColumnDimension;
        if (long_entrySize > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("サイズが大きすぎる");
        }
        this.entry = new double[(int) long_entrySize];
    }

    /**
     * 与えられたソースから矩形行列のビルダを作成する.
     *
     * @param src ソース
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    private GeneralMatrixBuilder(final GeneralMatrixImpl src) {
        this.matrixDimension = src.matrixDimension;
        this.entry = src.entry.clone();
    }

    /**
     * (<i>i</i>,<i>j</i>)要素を指定した値に置き換える.
     *
     * @param row i, 行index
     * @param column j, 列index
     * @param value 置き換えた後の値
     * @throws IndexOutOfBoundsException (i,j)が行列の内部でない場合
     * @throws IllegalArgumentException valueが不正な値の場合
     * @throws IllegalStateException すでにビルドされている場合
     * @see EntryReadableMatrix#acceptValue(double)
     */
    public void setValue(final int row, final int column, double value) {
        if (Objects.isNull(this.entry)) {
            throw new IllegalStateException("すでにビルドされています");
        }
        if (!EntryReadableMatrix.acceptValue(value)) {
            throw new IllegalArgumentException(String.format("不正な値:value=%.16G", value));
        }
        if (!(matrixDimension.isValidIndexes(row, column))) {
            throw new IndexOutOfBoundsException(
                    String.format(
                            "行列外:matrix:%s, (row, column)=(%d, %d)",
                            matrixDimension, row, column));
        }
        entry[row * matrixDimension.columnAsIntValue() + column] = value;
    }

    /**
     * 第<i>i</i>行と第<i>j</i>行を交換して新しい行列として返す.
     *
     * @param row1 i, 行index1
     * @param row2 j, 行index2
     * @throws IndexOutOfBoundsException i, jが行列の内部でない場合
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
                            "行列外:matrix:%s, (row1, row2)=(%d, %d)",
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
     * 第<i>i</i>列と第<i>j</i>列を交換して新しい行列として返す.
     *
     * @param column1 i, 列index1
     * @param column2 j, 列index2
     * @throws IndexOutOfBoundsException i, jが行列の内部でない場合
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
                            "行列外:matrix:%s, (column1, column2)=(%d, %d)",
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
    public EntryReadableMatrix build() {
        if (Objects.isNull(this.entry)) {
            throw new IllegalStateException("すでにビルドされています");
        }
        EntryReadableMatrix out = new GeneralMatrixImpl(this.matrixDimension, this.entry);
        this.entry = null;
        return out;
    }

    /**
     * 与えられた次元(サイズ)を持つ, 零行列で初期化された矩形(長方形)行列ビルダを生成する.
     * 
     *
     * @param matrixDimension 行列サイズ
     * @return 零行列で初期化されたビルダ
     * @throws IllegalArgumentException 行列の有効要素数が大きすぎる場合(クラス説明文)
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static GeneralMatrixBuilder zeroBuilder(MatrixDimension matrixDimension) {
        return new GeneralMatrixBuilder(matrixDimension);
    }

    /**
     * 与えられた次元(サイズ)を持つ, 単位行列で初期化されたビルダを作成する.
     *
     * @param matrixDimension 行列サイズ
     * @return 単位行列で初期化したビルダ
     * @throws MatrixFormatMismatchException 行列サイズが正方形でない場合
     * @throws IllegalArgumentException 行列の有効要素数が大きすぎる場合(クラス説明文)
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static GeneralMatrixBuilder unitBuilder(final MatrixDimension matrixDimension) {
        if (!matrixDimension.isSquare()) {
            throw new MatrixFormatMismatchException(
                    String.format("正方形ではない行列サイズ:%s", matrixDimension));
        }

        final GeneralMatrixBuilder unitBuilder = new GeneralMatrixBuilder(matrixDimension);
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
     * @throws IllegalArgumentException 行列の有効要素数が大きすぎる場合(クラス説明文), 成分に不正な値が入り込む場合
     * @throws NullPointerException 引数にnullが含まれる場合
     * @see EntryReadableMatrix#acceptValue(double)
     */
    public static GeneralMatrixBuilder from(final Matrix src) {
        if (src instanceof EntryReadableMatrix) {
            return GeneralMatrixBuilder.from((EntryReadableMatrix) src);
        }

        final MatrixDimension srcMatrixDimension = src.matrixDimension();
        final int srcRowDimension = srcMatrixDimension.rowAsIntValue();
        final int srcColumnDimension = srcMatrixDimension.columnAsIntValue();

        final GeneralMatrixBuilder outBuilder = new GeneralMatrixBuilder(srcMatrixDimension);

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
     * @throws IllegalArgumentException 行列の有効要素数が大きすぎる場合(クラス説明文)
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static GeneralMatrixBuilder from(final EntryReadableMatrix src) {

        final MatrixDimension srcMatrixDimension = src.matrixDimension();
        final int srcRowDimension = srcMatrixDimension.rowAsIntValue();
        final int srcColumnDimension = srcMatrixDimension.columnAsIntValue();
        if (src instanceof GeneralMatrixImpl) {
            return new GeneralMatrixBuilder((GeneralMatrixImpl) src);
        }

        final GeneralMatrixBuilder outBuilder = new GeneralMatrixBuilder(srcMatrixDimension);

        //転置に対して基本単位ベクトルを乗算する
        //配列の方向が列方向であるため(速さは不明である)
        for (int j = 0; j < srcRowDimension; j++) {
            for (int k = 0; k < srcColumnDimension; k++) {
                double value = src.valueAt(j, k);
                if (!EntryReadableMatrix.acceptValue(value)) {
                    throw new AssertionError(
                            String.format(
                                    "EntryReadableMatrixが適切に実装されていない: entryValue=%.16G", value));
                }
                outBuilder.setValue(j, k, value);
            }
        }
        return outBuilder;
    }

    private static final class GeneralMatrixImpl extends SkeletalMatrix implements EntryReadableMatrix {

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
        private GeneralMatrixImpl(final MatrixDimension matrixDimension, final double[] entry) {
            this.matrixDimension = matrixDimension;
            this.entry = entry;

            this.entryNormMax = this.calcEntryNormMax();
        }

        @Override
        public MatrixDimension matrixDimension() {
            return this.matrixDimension;
        }

        @Override
        public double valueAt(final int row, final int column) {
            if (!(matrixDimension.isValidIndexes(row, column))) {
                throw new IndexOutOfBoundsException(
                        String.format(
                                "行列外:matrix:%s, (row, column)=(%d, %d)",
                                matrixDimension, row, column));
            }
            return entry[row * matrixDimension.columnAsIntValue() + column];
        }

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

        @Override
        public String toString() {
            return EntryReadableMatrix.toString(this);
        }

    }

}
