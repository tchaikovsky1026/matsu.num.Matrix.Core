/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.12.2
 */
package matsu.num.matrix.base;

import java.util.Objects;

import matsu.num.matrix.base.common.ArraysUtil;
import matsu.num.matrix.base.common.CalcUtil;
import matsu.num.matrix.base.helper.value.MatrixRejectionConstant;
import matsu.num.matrix.base.validation.ElementsTooManyException;
import matsu.num.matrix.base.validation.MatrixFormatMismatchException;
import matsu.num.matrix.base.validation.MatrixNotSymmetricException;
import matsu.num.matrix.base.validation.MatrixStructureAcceptance;

/**
 * 対称 (密) 行列を扱う.
 * 
 * <p>
 * このクラスのインスタンスはビルダを用いて生成する.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 23.3
 */
public final class SymmetricMatrix extends SkeletalSymmetricMatrix<SymmetricMatrix>
        implements EntryReadableMatrix, Symmetric {

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
    private SymmetricMatrix(final MatrixDimension matrixDimension, final double[] entry) {
        this.matrixDimension = matrixDimension;
        this.entry = entry;

        this.entryNormMax = this.calcEntryNormMax();
    }

    @Override
    public MatrixDimension matrixDimension() {
        return this.matrixDimension;
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public double valueAt(final int row, final int column) {
        if (!(matrixDimension.isValidIndexes(row, column))) {
            throw new IndexOutOfBoundsException(
                    String.format(
                            "行列外:matrix:%s, (row, column)=(%s, %s)",
                            matrixDimension, row, column));
        }
        return entry[row >= column
                ? column + CalcUtil.sumOf1To(row)
                : row + CalcUtil.sumOf1To(column)];
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
    protected SymmetricMatrix self() {
        return this;
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public Vector operate(Vector operand) {
        final var vectorDimension = operand.vectorDimension();
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

        var builder = Vector.Builder.zeroBuilder(vectorDimension);
        builder.setEntryValue(resultEntry);
        return builder.build();
    }

    @Override
    public double entryNormMax() {
        return this.entryNormMax;
    }

    /**
     * 成分ごとのノルムの最大ノルムを計算する. <br>
     * 一度だけ呼ばれる.
     * 
     * @return ノルム
     */
    private double calcEntryNormMax() {
        return ArraysUtil.normMax(entry);
    }

    /**
     * このインスタンスの文字列表現を返す.
     */
    @Override
    public String toString() {
        return String.format(
                "Matrix[dim:%s, %s]",
                this.matrixDimension(), EntryReadableMatrix.toSimplifiedEntryString(this));
    }

    /**
     * 対称 (密) 行列を生成するビルダ. <br>
     * このビルダはミュータブルであり, スレッドセーフでない.
     * 
     * <p>
     * ビルダの生成時に有効要素数が大きすぎる場合は例外がスローされる. <br>
     * {@link MatrixDimension#isAccepedForDenseMatrix()}
     * に従う.
     * </p>
     */
    public static final class Builder {

        private MatrixDimension matrixDimension;
        private double[] entry;

        /**
         * 与えられた次元(サイズ)の対称行列ビルダを生成する. <br>
         * 初期値は零行列.
         *
         * @param matrixDimension 行列サイズ
         * @throws IllegalArgumentException (サブクラス)受け入れ拒否の場合
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        private Builder(final MatrixDimension matrixDimension) {
            var acceptance = accepts(matrixDimension);
            if (acceptance.isReject()) {
                throw acceptance.getException(matrixDimension);
            }

            this.matrixDimension = matrixDimension;

            final int dimension = matrixDimension.rowAsIntValue();
            final int entrySize = CalcUtil.sumOf1To(dimension);
            this.entry = new double[entrySize];
        }

        /**
         * 与えられたソースから矩形行列のビルダを作成する.
         *
         * @param src ソース
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        private Builder(final SymmetricMatrix src) {
            this.matrixDimension = src.matrixDimension;
            this.entry = src.entry.clone();
        }

        /**
         * (<i>i</i>, <i>j</i>) 要素を指定した値に置き換える. <br>
         * 同時に(<i>j</i>, <i>i</i>) の値も置き換わる.
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

            //値の修正
            value = EntryReadableMatrix.modified(value);

            entry[row >= column
                    ? column + CalcUtil.sumOf1To(row)
                    : row + CalcUtil.sumOf1To(column)] = value;
        }

        /**
         * 第 <i>i</i> 行と第 <i>j</i> 行, 第 <i>i</i> 列と第 <i>j</i> 列を交換する.
         *
         * @param index1 <i>i</i>, 行,列index1
         * @param index2 <i>j</i>, 行,列index2
         * @throws IllegalStateException すでにビルドされている場合
         * @throws IndexOutOfBoundsException <i>i</i>, <i>j</i>が行列の内部でない場合
         */
        public void swapRowsAndColumns(final int index1, final int index2) {
            if (Objects.isNull(this.entry)) {
                throw new IllegalStateException("すでにビルドされています");
            }

            if (!(matrixDimension.isValidRowIndex(index1)
                    && matrixDimension.isValidRowIndex(index2))) {
                throw new IndexOutOfBoundsException(
                        String.format(
                                "行列外:matrix:%s, (index1, index2)=(%s, %s)",
                                matrixDimension, index1, index2));
            }

            if (index1 == index2) {
                return;
            }

            final int dimension = matrixDimension.rowAsIntValue();
            final int indMin = Math.min(index1, index2);
            final int indMax = Math.max(index1, index2);
            //4隅以外
            final int indMinN = CalcUtil.sumOf1To(indMin);
            final int indMaxN = CalcUtil.sumOf1To(indMax);
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
         * @return 対称行列, {@link Symmetric} が付与されている
         * @throws IllegalStateException すでにビルドされている場合
         */
        public SymmetricMatrix build() {
            if (Objects.isNull(this.entry)) {
                throw new IllegalStateException("すでにビルドされています");
            }
            var out = new SymmetricMatrix(this.matrixDimension, this.entry);
            this.entry = null;
            return out;
        }

        /**
         * この行列サイズがビルダ生成に受け入れられるかを判定する.
         * 
         * @param matrixDimension 行列サイズ
         * @return ビルダ生成が受け入れられるならACCEPT
         * @throws NullPointerException 引数がnullの場合
         */
        public static MatrixStructureAcceptance accepts(MatrixDimension matrixDimension) {
            if (!matrixDimension.isSquare()) {
                return MatrixRejectionConstant.REJECTED_BY_NOT_SQUARE.get();
            }

            if (!matrixDimension.isAccepedForDenseMatrix()) {
                return MatrixRejectionConstant.REJECTED_BY_TOO_MANY_ELEMENTS.get();
            }

            return MatrixStructureAcceptance.ACCEPTED;
        }

        /**
         * 与えられた次元(サイズ)を持つ, 零行列で初期化された対称行列ビルダを生成する.
         *
         * @param matrixDimension 行列サイズ
         * @return 零行列で初期化されたビルダ
         * @throws MatrixFormatMismatchException 行列サイズが正方サイズでない場合
         * @throws ElementsTooManyException 行列の有効要素数が大きすぎる場合(クラス説明文)
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        public static Builder zero(final MatrixDimension matrixDimension) {
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
            final var unitBuilder = new Builder(matrixDimension);
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
         * @throws ElementsTooManyException 行列の有効要素数が大きすぎる場合(クラス説明文),
         *             成分に不正な値が入り込む場合
         * @throws NullPointerException 引数にnullが含まれる場合
         * @see Symmetric
         * @see EntryReadableMatrix#acceptValue(double)
         */
        public static Builder from(final Matrix src) {
            if (src instanceof EntryReadableMatrix) {
                return Builder.from((EntryReadableMatrix) src);
            }

            if (!(Objects.requireNonNull(src) instanceof Symmetric)) {
                throw new MatrixNotSymmetricException("対称行列でない");
            }

            final var srcMatrixDimension = src.matrixDimension();
            final int dimension = srcMatrixDimension.rowAsIntValue();
            final var outBuilder = new Builder(srcMatrixDimension);

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
         * @throws ElementsTooManyException 行列の有効要素数が大きすぎる場合(クラス説明文)
         * @throws NullPointerException 引数にnullが含まれる場合
         * @see Symmetric
         */
        public static Builder from(final EntryReadableMatrix src) {
            if (!(Objects.requireNonNull(src) instanceof Symmetric)) {
                throw new MatrixNotSymmetricException("対称行列でない");
            }

            final var srcMatrixDimension = src.matrixDimension();
            final int dimension = srcMatrixDimension.rowAsIntValue();
            if (src instanceof SymmetricMatrix) {
                return new Builder((SymmetricMatrix) src);
            }

            final var outBuilder = new Builder(srcMatrixDimension);

            for (int j = 0; j < dimension; j++) {
                for (int k = 0; k <= j; k++) {
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
