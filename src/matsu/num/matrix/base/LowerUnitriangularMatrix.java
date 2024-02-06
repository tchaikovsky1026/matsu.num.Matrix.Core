/**
 * 2024.2.5
 */
package matsu.num.matrix.base;

import java.util.Objects;
import java.util.Optional;

import matsu.num.matrix.base.common.ArraysUtil;
import matsu.num.matrix.base.validation.ElementsTooManyException;
import matsu.num.matrix.base.validation.MatrixFormatMismatchException;

/**
 * <p>
 * 単位下三角の密行列を扱う.
 * </p>
 * 
 * <p>
 * このクラスのインスタンスはビルダを用いて生成する.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 20.0
 */
public final class LowerUnitriangularMatrix
        extends SkeletalMatrix implements LowerUnitriangular {

    /*
     * 行列の各要素は, 内部では1次元配列として,
     * 1.0
     * [0] 1.0
     * [1] [2] 1.0
     * [3] [4] [5] 1.0
     * [6] [7] [8] [9] 1.0
     * の形で狭義下三角成分を保持し, 対角成分, 狭義上三角成分は省略する.
     */
    private final MatrixDimension matrixDimension;

    private final double[] lowerEntry;

    private final Optional<Matrix> inverse;

    /**
     * ビルダから呼ばれる.
     * 
     * @param matrixDimension 行列次元
     * @param lowerEntry 成分
     */
    private LowerUnitriangularMatrix(final MatrixDimension matrixDimension, final double[] lowerEntry) {
        this.matrixDimension = matrixDimension;
        this.lowerEntry = lowerEntry;

        this.inverse = Optional.of(this.createInverse());
    }

    @Override
    public MatrixDimension matrixDimension() {
        return this.matrixDimension;
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

        if (row == column) {
            return 1;
        } else if (column < row) {
            return lowerEntry[column + (row * (row - 1)) / 2];
        } else {
            return 0;
        }
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     */
    @Override
    public Vector operate(Vector operand) {
        final VectorDimension vectorDimension = operand.vectorDimension();
        if (!matrixDimension.rightOperable(vectorDimension)) {
            throw new MatrixFormatMismatchException(
                    String.format(
                            "右から演算不可:matrix:%s, operand:%s",
                            matrixDimension, vectorDimension));
        }

        final int dimension = vectorDimension.intValue();

        final double[] operandEntry = operand.entry();
        final double[] resultEntry = new double[dimension];
        final double[] thisLowerEntry = this.lowerEntry;

        //対角成分
        System.arraycopy(operandEntry, 0, resultEntry, 0, dimension);
        //狭義下三角成分
        int in = (dimension * (dimension - 1)) / 2;
        for (int i = dimension - 1; i >= 0; i--) {
            in -= i;
            int j;
            double sumProduct = 0;
            for (j = i - 1; j >= 3; j -= 4) {
                final double v0 = thisLowerEntry[in + j] * operandEntry[j];
                final double v1 = thisLowerEntry[in + j - 1] * operandEntry[j - 1];
                final double v2 = thisLowerEntry[in + j - 2] * operandEntry[j - 2];
                final double v3 = thisLowerEntry[in + j - 3] * operandEntry[j - 3];
                sumProduct += (v0 + v1) + (v2 + v3);
            }
            for (; j >= 0; j--) {
                final double v0 = thisLowerEntry[in + j] * operandEntry[j];
                sumProduct += v0;
            }
            resultEntry[i] += sumProduct;
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
        if (!matrixDimension.leftOperable(vectorDimension)) {
            throw new MatrixFormatMismatchException(
                    String.format(
                            "左から演算不可:matrix:%s, operand:%s",
                            matrixDimension, vectorDimension));
        }

        final int dimension = vectorDimension.intValue();

        final double[] operandEntry = operand.entry();
        final double[] resultEntry = new double[dimension];
        final double[] thisLowerEntry = this.lowerEntry;

        //対角成分
        System.arraycopy(operandEntry, 0, resultEntry, 0, dimension);
        //狭義下三角成分
        int in = 1;
        for (int i = 0; i < dimension; i++) {
            final double oe_i = operandEntry[i];
            in += i - 1;
            for (int j = 0; j < i; j++) {
                resultEntry[j] += thisLowerEntry[in + j] * oe_i;
            }
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
    public Optional<Matrix> inverse() {
        return this.inverse;
    }

    /**
     * このインスタンスの文字列説明表現を返す.
     */
    @Override
    public String toString() {
        return EntryReadableMatrix.toString(this, "lower_unitriangular");
    }

    /**
     * この下三角行列の逆行列を作成する.
     * 
     * @return 逆行列
     */
    private Matrix createInverse() {

        return new SkeletalMatrix() {

            @Override
            public MatrixDimension matrixDimension() {
                return matrixDimension;
            }

            @Override
            public Vector operate(Vector operand) {
                final VectorDimension vectorDimension = operand.vectorDimension();
                if (!matrixDimension.leftOperable(vectorDimension)) {
                    throw new MatrixFormatMismatchException(
                            String.format(
                                    "左から演算不可:matrix:%s, operand:%s",
                                    matrixDimension, vectorDimension));
                }

                final int dimension = vectorDimension.intValue();
                final double[] resultEntry = new double[dimension];
                final double[] thisLowerEntry = lowerEntry;

                System.arraycopy(operand.entry(), 0, resultEntry, 0, dimension);
                int in = 1;
                for (int i = 0; i < dimension; i++) {
                    in += i - 1;
                    int j;
                    double sumProduct = 0;
                    for (j = 0; j < i - 3; j += 4) {
                        final double v0 = thisLowerEntry[in + j] * resultEntry[j];
                        final double v1 = thisLowerEntry[in + j + 1] * resultEntry[j + 1];
                        final double v2 = thisLowerEntry[in + j + 2] * resultEntry[j + 2];
                        final double v3 = thisLowerEntry[in + j + 3] * resultEntry[j + 3];
                        sumProduct += (v0 + v1) + (v2 + v3);
                    }
                    for (; j < i; j++) {
                        final double v0 = thisLowerEntry[in + j] * resultEntry[j];
                        sumProduct += v0;
                    }
                    resultEntry[i] -= sumProduct;
                }

                Vector.Builder builder = Vector.Builder.zeroBuilder(vectorDimension);
                builder.setEntryValue(resultEntry);
                return builder.build();
            }

            @Override
            public Vector operateTranspose(Vector operand) {
                final VectorDimension vectorDimension = operand.vectorDimension();
                if (!matrixDimension.rightOperable(vectorDimension)) {
                    throw new MatrixFormatMismatchException(
                            String.format(
                                    "右から演算不可:matrix:%s, operand:%s",
                                    matrixDimension, vectorDimension));
                }

                final int dimension = vectorDimension.intValue();
                final double[] resultEntry = new double[dimension];
                final double[] thisLowerEntry = lowerEntry;

                System.arraycopy(operand.entry(), 0, resultEntry, 0, dimension);
                int in = (dimension * (dimension - 1)) / 2;
                for (int i = dimension - 1; i >= 0; i--) {
                    in -= i;
                    final double re_i = resultEntry[i];
                    for (int j = i - 1; j >= 0; j--) {
                        resultEntry[j] -= thisLowerEntry[in + j] * re_i;
                    }
                }

                Vector.Builder builder = Vector.Builder.zeroBuilder(vectorDimension);
                builder.setEntryValue(resultEntry);
                return builder.build();
            }
        };
    }

    /**
     * <p>
     * 単位下三角の密行列を作成するビルダ. <br>
     * このビルダはミュータブルであり, スレッドセーフでない.
     * </p>
     * 
     * <p>
     * ビルダの生成時に有効要素数が大きすぎる場合は例外がスローされる. <br>
     * 有効要素数が大きすぎるとは, <br>
     * 行列の行数(= 列数)を <i>n</i> として, <br>
     * <i>n</i> * (<i>n</i> - 1) &gt; {@linkplain Integer#MAX_VALUE} <br>
     * である状態である.
     * </p>
     */
    public static final class Builder {
        private MatrixDimension matrixDimension;

        private double[] lowerEntry;

        /**
         * 与えられた次元(サイズ)の単位下三角行列ビルダを生成する. <br>
         * 初期値は単位行列.
         *
         * @param matrixDimension 行列サイズ
         * @throws MatrixFormatMismatchException 行列サイズが正方サイズでない場合
         * @throws ElementsTooManyException 行列の有効要素数が大きすぎる場合(dim * (dim - 1) >
         *             IntMax)
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        private Builder(final MatrixDimension matrixDimension) {
            if (!matrixDimension.isSquare()) {
                throw new MatrixFormatMismatchException(
                        String.format("正方形ではない行列サイズ:%s", matrixDimension));
            }
            this.matrixDimension = matrixDimension;

            final int dimension = matrixDimension.rowAsIntValue();
            final long long_entrySize = (long) dimension * (long) (dimension - 1) / 2;
            if (long_entrySize > Integer.MAX_VALUE / 2) {
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
         * @throws IndexOutOfBoundsException (<i>i</i>, <i>j</i>)
         *             が行列の狭義下三角成分でない場合
         * @throws IllegalStateException すでにビルドされている場合
         * @see EntryReadableMatrix#acceptValue(double)
         */
        public void setValue(final int row, final int column, double value) {
            if (Objects.isNull(this.lowerEntry)) {
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

            if (column < row) {
                lowerEntry[column + (row * (row - 1)) / 2] = value;
            } else {
                throw new IndexOutOfBoundsException(
                        String.format(
                                "狭義下側三角成分でない:matrix:%s, (row, column)=(%s, %s)",
                                matrixDimension, row, column));
            }
        }

        /**
         * 単位下三角行列をビルドする.
         *
         * @return 単位下三角行列
         * @throws IllegalStateException すでにビルドされている場合
         */
        public LowerUnitriangularMatrix build() {
            if (Objects.isNull(this.lowerEntry)) {
                throw new IllegalStateException("すでにビルドされています");
            }
            LowerUnitriangularMatrix out =
                    new LowerUnitriangularMatrix(this.matrixDimension, this.lowerEntry);
            this.lowerEntry = null;
            return out;
        }

        /**
         * 与えられた次元(サイズ)を持つ, 単位行列で初期化された単位下三角行列ビルダを生成する.
         *
         * @param matrixDimension 行列サイズ
         * @return 単位行列で初期化されたビルダ
         * @throws MatrixFormatMismatchException 行列サイズが正方サイズでない場合
         * @throws ElementsTooManyException 行列の有効要素数が大きすぎる場合(クラス説明文)
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        public static Builder unit(final MatrixDimension matrixDimension) {
            return new Builder(matrixDimension);
        }
    }
}
