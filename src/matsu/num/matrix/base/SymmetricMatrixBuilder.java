/**
 * 2023.11.30
 */
package matsu.num.matrix.base;

import java.util.Objects;

import matsu.num.commons.ArraysUtil;
import matsu.num.matrix.base.exception.MatrixFormatMismatchException;
import matsu.num.matrix.base.exception.MatrixNotSymmetricException;

/**
 * 対称(密)行列を生成するビルダ.
 * 
 * <p>
 * このビルダはミュータブルである. <br>
 * また, スレッドセーフでない.
 * </p>
 * 
 * <p>
 * ビルダの生成時に有効要素数が大きすぎる場合は例外がスローされる. <br>
 * 有効要素数が大きすぎるとは, <br>
 * 行列の行数(= 列数)を<i>n</i>として, <br>
 * <i>n</i> * (<i>n</i> + 1) {@literal >} {@linkplain Integer#MAX_VALUE} <br>
 * である状態である.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 17.1
 */
public final class SymmetricMatrixBuilder {

    private MatrixDimension matrixDimension;
    private double[] entry;

    /**
     * 与えられた次元(サイズ)の対称行列ビルダを生成する. <br>
     * 初期値は零行列.
     *
     * @param matrixDimension 行列サイズ
     * @throws MatrixFormatMismatchException 行列サイズが正方サイズでない場合
     * @throws IllegalArgumentException 行列の有効要素数が大きすぎる場合(dim * (dim + 1) >
     *             IntMax)
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    private SymmetricMatrixBuilder(final MatrixDimension matrixDimension) {
        if (!matrixDimension.isSquare()) {
            throw new MatrixFormatMismatchException(
                    String.format("正方形ではない行列サイズ:%s", matrixDimension));
        }
        this.matrixDimension = matrixDimension;

        final long long_dimension = matrixDimension.rowAsIntValue();
        final long long_EntrySize = long_dimension * (long_dimension + 1L) / 2L;
        if (long_EntrySize > Integer.MAX_VALUE / 2) {
            throw new IllegalArgumentException("サイズが大きすぎます");
        }
        this.entry = new double[(int) long_EntrySize];
    }

    /**
     * 与えられたソースから矩形行列のビルダを作成する.
     *
     * @param src ソース
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    private SymmetricMatrixBuilder(final SymmetricMatrixImpl src) {
        this.matrixDimension = src.matrixDimension;
        this.entry = src.entry.clone();
    }

    /**
     * (<i>i</i>,<i>j</i>)要素を指定した値に置き換える. <br>
     * 同時に(<i>j</i>,<i>i</i>)の値も置き換わる.
     *
     * @param row i, 行index
     * @param column j, 列index
     * @param value 置き換えた後の値
     * @throws IndexOutOfBoundsException (i,j)が行列の内部でない場合
     * @throws IllegalArgumentException valueが不正な値の場合
     * @see EntryReadableMatrix#acceptValue(double)
     * @throws IllegalStateException すでにビルドされている場合
     */
    public void setValue(final int row, final int column, final double value) {
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

        entry[row >= column ? column + (row * (row + 1)) / 2 : row + (column * (column + 1)) / 2] = value;
    }

    /**
     * 第<i>i</i>行と第<i>j</i>行, 第<i>i</i>列と第<i>j</i>列を交換する.
     *
     * @param index1 i, 行,列index1
     * @param index2 j, 行,列index2
     * @throws IllegalStateException すでにビルドされている場合
     * @throws IndexOutOfBoundsException i, jが行列の内部でない場合
     */
    public void swapRowsAndColumns(final int index1, final int index2) {
        if (Objects.isNull(this.entry)) {
            throw new IllegalStateException("すでにビルドされています");
        }

        if (!(matrixDimension.isValidRowIndex(index1)
                && matrixDimension.isValidRowIndex(index2))) {
            throw new IndexOutOfBoundsException(
                    String.format(
                            "行列外:matrix:%s, (index1, index2)=(%d, %d)",
                            matrixDimension, index1, index2));
        }

        if (index1 == index2) {
            return;
        }

        final int dimension = matrixDimension.rowAsIntValue();
        final int indMin = Math.min(index1, index2);
        final int indMax = Math.max(index1, index2);
        //4隅以外
        final int indMinN = (indMin * (indMin + 1)) / 2;
        final int indMaxN = (indMax * (indMax + 1)) / 2;
        int j = 0;
        for (; j < indMin; j++) {
            final double temp;
            temp = entry[indMinN + j];
            entry[indMinN + j] = entry[indMaxN + j];
            entry[indMaxN + j] = temp;
        }
        j++;
        for (; j < indMax; j++) {
            final int jn = (j * (j + 1)) / 2;
            final double temp = entry[jn + indMin];
            entry[jn + indMin] = entry[indMaxN + j];
            entry[indMaxN + j] = temp;
        }
        j++;
        for (; j < dimension; j++) {
            final int jn = (j * (j + 1)) / 2;
            final double temp = entry[jn + indMin];
            entry[jn + indMin] = entry[jn + indMax];
            entry[jn + indMax] = temp;
        }
        //4隅
        final double temp = entry[indMinN + indMin];
        entry[indMinN + indMin] = entry[indMaxN + indMax];
        entry[indMaxN + indMax] = temp;
    }

    /**
     * 対称行列をビルドする.
     *
     * @return 対称行列, {@link Symmetric}が付与されている
     * @throws IllegalStateException すでにビルドされている場合
     */
    public EntryReadableMatrix build() {
        if (Objects.isNull(this.entry)) {
            throw new IllegalStateException("すでにビルドされています");
        }
        EntryReadableMatrix out = new SymmetricMatrixImpl(this.matrixDimension, this.entry);
        this.entry = null;
        return out;
    }

    /**
     * 与えられた次元(サイズ)を持つ, 零行列で初期化された対称行列ビルダを生成する.
     *
     * @param matrixDimension 行列サイズ
     * @return 零行列で初期化されたビルダ
     * @throws MatrixFormatMismatchException 行列サイズが正方サイズでない場合
     * @throws IllegalArgumentException 行列の有効要素数が大きすぎる場合(クラス説明文)
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static SymmetricMatrixBuilder zeroBuilder(final MatrixDimension matrixDimension) {
        return new SymmetricMatrixBuilder(matrixDimension);
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
    public static SymmetricMatrixBuilder unitBuilder(final MatrixDimension matrixDimension) {
        final SymmetricMatrixBuilder unitBuilder = new SymmetricMatrixBuilder(matrixDimension);
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
     * @throws MatrixNotSymmetricException 行列が対称行列でない場合
     * @throws IllegalArgumentException 行列の有効要素数が大きすぎる場合(クラス説明文),
     *             成分に不正な値が入り込む場合
     * @throws NullPointerException 引数にnullが含まれる場合
     * @see Symmetric
     * @see EntryReadableMatrix#acceptValue(double)
     */
    public static SymmetricMatrixBuilder from(final Matrix src) {
        if (src instanceof EntryReadableMatrix) {
            return SymmetricMatrixBuilder.from((EntryReadableMatrix) src);
        }

        if (!(Objects.requireNonNull(src) instanceof Symmetric)) {
            throw new MatrixNotSymmetricException("対称行列でない");
        }

        final MatrixDimension srcMatrixDimension = src.matrixDimension();
        final int dimension = srcMatrixDimension.rowAsIntValue();
        final SymmetricMatrixBuilder outBuilder = new SymmetricMatrixBuilder(srcMatrixDimension);

        //転置に対して基本単位ベクトルを乗算する
        //配列の方向が列方向であるため(速さは不明である)
        //ただし対称行列なので, 数値的には転置する意味はない(概念上の転置)
        for (int j = 0; j < dimension; j++) {
            double[] rightArray = new double[dimension];
            rightArray[j] = 1;

            //resultには不正な値が入り込む可能性がある
            //Matrixインターフェースは成分に関する情報を持たないため
            Vector.Builder builder = Vector.Builder.zeroBuilder(srcMatrixDimension.leftOperableVectorDimension());
            builder.setEntryValue(rightArray);
            final double[] resultArray = src.operateTranspose(builder.build()).entry();

            for (int k = 0; k <= j; k++) {
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
     * @throws MatrixNotSymmetricException 行列が対称行列でない場合
     * @throws IllegalArgumentException 行列の有効要素数が大きすぎる場合(クラス説明文)
     * @throws NullPointerException 引数にnullが含まれる場合
     * @see Symmetric
     */
    public static SymmetricMatrixBuilder from(final EntryReadableMatrix src) {
        if (!(Objects.requireNonNull(src) instanceof Symmetric)) {
            throw new MatrixNotSymmetricException("対称行列でない");
        }

        final MatrixDimension srcMatrixDimension = src.matrixDimension();
        final int dimension = srcMatrixDimension.rowAsIntValue();
        if (src instanceof SymmetricMatrixImpl) {
            return new SymmetricMatrixBuilder((SymmetricMatrixImpl) src);
        }

        final SymmetricMatrixBuilder outBuilder = new SymmetricMatrixBuilder(srcMatrixDimension);

        for (int j = 0; j < dimension; j++) {
            for (int k = 0; k <= j; k++) {
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

    private static final class SymmetricMatrixImpl extends SkeletalMatrix implements EntryReadableMatrix, Symmetric {

        /*
         * 行列の各要素は, 内部では1次元配列として,
         * [0]
         * [1][2]
         * [3][4][5]
         * [6][7][8][9]
         * の形で対角 + 下三角成分を保持し, 狭義上三角成分は省略する.
         */
        private final MatrixDimension matrixDimension;
        private final double[] entry;

        private final double entryNormMax;

        /**
         * ビルダから呼ばれる.
         */
        private SymmetricMatrixImpl(final MatrixDimension matrixDimension, final double[] entry) {
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
            return entry[row >= column ? column + (row * (row + 1)) / 2 : row + (column * (column + 1)) / 2];
        }

        @Override
        public Vector operate(Vector operand) {
            final VectorDimension vectorDimension = operand.vectorDimension();
            final int dimension = vectorDimension.intValue();
            if (!matrixDimension.rightOperable(vectorDimension)) {
                throw new MatrixFormatMismatchException(
                        String.format(
                                "右から演算不可:matrix:%s, operand:%s",
                                matrixDimension, vectorDimension));
            }

            final double[] operandEntry = operand.entry();
            final double[] resultEntry = new double[dimension];

            final double[] matrixEntry = entry;

            int jn = 0;
            for (int j = 0; j < dimension; j++) {
                double sumProduct = 0.0;
                jn += j;
                int k;
                for (k = 0; k <= j - 3; k += 4) {
                    double v0 = matrixEntry[jn + k] * operandEntry[k];
                    double v1 = matrixEntry[jn + k + 1] * operandEntry[k + 1];
                    double v2 = matrixEntry[jn + k + 2] * operandEntry[k + 2];
                    double v3 = matrixEntry[jn + k + 3] * operandEntry[k + 3];
                    sumProduct += (v0 + v1) + (v2 + v3);
                }
                for (; k <= j; k++) {
                    sumProduct += matrixEntry[jn + k] * operandEntry[k];
                }
                resultEntry[j] = sumProduct;
            }
            jn = 0;
            for (int j = 0; j < dimension; j++) {
                jn += j;
                final double operandEntry_j = operandEntry[j];
                for (int k = 0; k < j; k++) {
                    resultEntry[k] += matrixEntry[jn + k] * operandEntry_j;
                }
            }

            Vector.Builder builder = Vector.Builder.zeroBuilder(vectorDimension);
            builder.setEntryValue(resultEntry);
            return builder.build();
        }

        @Override
        public Vector operateTranspose(Vector operand) {
            return this.operate(operand);
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
            return EntryReadableMatrix.toString(this, "symmetric");
        }

    }
}
