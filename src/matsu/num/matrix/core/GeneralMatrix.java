/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2025.6.27
 */
package matsu.num.matrix.core;

import java.util.Objects;
import java.util.function.DoubleFunction;

import matsu.num.matrix.core.common.ArraysUtil;
import matsu.num.matrix.core.helper.value.MatrixRejectionConstant;
import matsu.num.matrix.core.helper.value.MatrixValidationSupport;
import matsu.num.matrix.core.validation.ElementsTooManyException;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;
import matsu.num.matrix.core.validation.MatrixStructureAcceptance;

/**
 * 一般的な矩形 (長方形) の (密) 行列を扱う具象クラス.
 * 
 * <p>
 * このクラスのインスタンスはビルダ ({@link GeneralMatrix.Builder}) を用いて生成する.
 * </p>
 * 
 * @author Matsuura Y.
 */
public final class GeneralMatrix extends SkeletalAsymmetricMatrix<EntryReadableMatrix>
        implements EntryReadableMatrix {

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
    private GeneralMatrix(Builder builder) {
        this.matrixDimension = builder.matrixDimension;
        this.entry = builder.entry;

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
        MatrixValidationSupport.validateIndexInMatrix(matrixDimension, row, column);

        return entry[row * matrixDimension.columnAsIntValue() + column];
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
    protected EntryReadableMatrix createTranspose() {
        return EntryReadableMatrix.createTransposedOf(this);
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     */
    @Override
    public Vector operate(Vector operand) {

        MatrixValidationSupport.validateOperate(
                matrixDimension, operand.vectorDimension());

        final int rowDimension = matrixDimension.rowAsIntValue();
        final int columnDimension = matrixDimension.columnAsIntValue();
        final double[] operandEntry = operand.entryAsArray();

        final double[] resultEntry = new double[rowDimension];
        final double[] matrixEntry = this.entry;

        int jn = -columnDimension;
        for (int j = 0; j < rowDimension; j++) {
            jn += columnDimension;

            /*
             * 主要ループで4成分の計算を同時に行う.
             * 影響する変数を分けることで, 並列実行できる可能性がある.
             */
            double v0 = 0d;
            double v1 = 0d;
            double v2 = 0d;
            double v3 = 0d;

            int k;
            for (k = 0; k < columnDimension - 3; k += 4) {
                v0 += matrixEntry[jn + k] * operandEntry[k];
                v1 += matrixEntry[jn + k + 1] * operandEntry[k + 1];
                v2 += matrixEntry[jn + k + 2] * operandEntry[k + 2];
                v3 += matrixEntry[jn + k + 3] * operandEntry[k + 3];
            }
            for (; k < columnDimension; k++) {
                v0 += matrixEntry[jn + k] * operandEntry[k];
            }
            resultEntry[j] = (v0 + v1) + (v2 + v3);
        }

        var builder = Vector.Builder.zeroBuilder(this.matrixDimension.leftOperableVectorDimension());
        builder.setEntryValue(resultEntry);
        return builder.build();
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     */
    @Override
    public Vector operateTranspose(Vector operand) {
        MatrixValidationSupport.validateOperateTranspose(
                matrixDimension, operand.vectorDimension());

        final int rowDimension = matrixDimension.rowAsIntValue();
        final int columnDimension = matrixDimension.columnAsIntValue();

        final double[] operandEntry = operand.entryAsArray();

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

        var builder = Vector.Builder.zeroBuilder(this.matrixDimension.rightOperableVectorDimension());
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

    @Override
    public String toString() {
        return "Matrix[dim: %s, %s]"
                .formatted(
                        this.matrixDimension(),
                        EntryReadableMatrix.toSimplifiedEntryString(this));
    }

    /**
     * 矩形 (長方形) の (密) 行列を生成するビルダ. <br>
     * このビルダはミュータブルであり, スレッドセーフでない.
     * 
     * <p>
     * このビルダインスタンスを得るには,
     * {@link #zero(MatrixDimension)}
     * をコールする. <br>
     * ビルダの生成時に有効要素数が大きすぎる場合は例外がスローされる. <br>
     * そのルールは {@link MatrixDimension#isAccepedForDenseMatrix()}
     * に従う.
     * </p>
     * 
     * <p>
     * ビルド準備ができたビルダに対して {@link #build()} をコールすることで
     * {@link GeneralMatrix} をビルドする. <br>
     * {@link #build()} を実行したビルダは使用不能となる.
     * </p>
     * 
     * <p>
     * ビルダのコピーが必要な場合, {@link #copy()} をコールする. <br>
     * ただし, このコピーはビルド前しか実行できないことに注意.
     * </p>
     */
    public static final class Builder {

        private final MatrixDimension matrixDimension;
        private double[] entry;

        /**
         * 与えられた次元(サイズ)の矩形(長方形)行列ビルダを生成する. <br>
         * 初期値は零行列.
         *
         * @param matrixDimension 行列サイズ
         * @throws ElementsTooManyException 行列の有効要素数が大きすぎる場合(クラス説明文)
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        private Builder(MatrixDimension matrixDimension) {

            final int thisRowDimension = matrixDimension.rowAsIntValue();
            final int thisColumnDimension = matrixDimension.columnAsIntValue();
            this.matrixDimension = matrixDimension;

            var acceptance = accepts(matrixDimension);
            if (acceptance.isReject()) {
                throw acceptance.getException(matrixDimension);
            }

            this.entry = new double[thisRowDimension * thisColumnDimension];
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
         * コピーコンストラクタ.
         */
        private Builder(final Builder src) {
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
            this.throwISExIfCannotBeUsed();

            MatrixValidationSupport.validateIndexInMatrix(matrixDimension, row, column);

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
         * @throws NullPointerException 例外生成器がnullでかつ例外を生成しようとした場合
         * @see EntryReadableMatrix#acceptValue(double)
         */
        public <X extends Exception> void setValueOrElseThrow(
                final int row, final int column, double value,
                DoubleFunction<X> invalidValueExceptionGetter) throws X {

            this.throwISExIfCannotBeUsed();

            MatrixValidationSupport.validateIndexInMatrix(matrixDimension, row, column);

            if (!EntryReadableMatrix.acceptValue(value)) {
                throw invalidValueExceptionGetter.apply(value);
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
            this.throwISExIfCannotBeUsed();

            if (!(matrixDimension.isValidRowIndex(row1)
                    && matrixDimension.isValidRowIndex(row2))) {
                throw new IndexOutOfBoundsException(
                        "out of matrix: matrix: %s, (row1, row2) = (%s, %s)"
                                .formatted(matrixDimension, row1, row2));
            }

            if (row1 == row2) {
                return;
            }

            final int columnDimension = matrixDimension.columnAsIntValue();
            final double[] thisEntry = this.entry;
            final int rn1 = row1 * columnDimension;
            final int rn2 = row2 * columnDimension;
            for (int columnIndex = 0; columnIndex < columnDimension; columnIndex++) {
                final int i1 = rn1 + columnIndex;
                final int i2 = rn2 + columnIndex;
                final double temp = thisEntry[i1];
                thisEntry[i1] = thisEntry[i2];
                thisEntry[i2] = temp;
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
            this.throwISExIfCannotBeUsed();

            if (!(matrixDimension.isValidColumnIndex(column1)
                    && matrixDimension.isValidColumnIndex(column2))) {
                throw new IndexOutOfBoundsException(
                        "out of matrix: matrix: %s, (column1, column2) = (%s, %s)"
                                .formatted(matrixDimension, column1, column2));
            }

            if (column1 == column2) {
                return;
            }

            final int rowDimension = matrixDimension.rowAsIntValue();
            final int columnDimension = matrixDimension.columnAsIntValue();
            final double[] thisEntry = this.entry;

            int ri = -columnDimension;
            for (int rowIndex = 0; rowIndex < rowDimension; rowIndex++) {
                ri += columnDimension;
                final int i1 = ri + column1;
                final int i2 = ri + column2;
                final double temp = thisEntry[i1];
                thisEntry[i1] = thisEntry[i2];
                thisEntry[i2] = temp;
            }
        }

        /**
         * このビルダが使用可能か (ビルド前かどうか) を判定する.
         * 
         * @return 使用可能なら {@code true}
         */
        public boolean canBeUsed() {
            return Objects.nonNull(this.entry);
        }

        /**
         * ビルド前かを判定し, ビルド後なら例外をスロー.
         */
        private void throwISExIfCannotBeUsed() {
            if (!this.canBeUsed()) {
                throw new IllegalStateException("already built");
            }
        }

        /**
         * このビルダのコピーを生成して返す.
         * 
         * @return このビルダのコピー
         * @throws IllegalStateException すでにビルドされている場合
         */
        public Builder copy() {
            this.throwISExIfCannotBeUsed();

            return new Builder(this);
        }

        /**
         * ビルダを使用不能にする.
         */
        private void disable() {
            this.entry = null;
        }

        /**
         * 矩形(長方形)行列をビルドする.
         *
         * @return 矩形(長方形)行列
         * @throws IllegalStateException すでにビルドされている場合
         */
        public GeneralMatrix build() {
            this.throwISExIfCannotBeUsed();

            var out = new GeneralMatrix(this);
            this.disable();

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
            if (!matrixDimension.isAccepedForDenseMatrix()) {
                return MatrixRejectionConstant.REJECTED_BY_TOO_MANY_ELEMENTS.get();
            }

            return MatrixStructureAcceptance.ACCEPTED;
        }

        /**
         * 与えられた次元(サイズ)を持つ, 零行列で初期化された矩形(長方形)行列ビルダを生成する.
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
         * 与えられたインスタンスの成分で初期化されたビルダを作成する. <br>
         * 成分に不正な値がある場合, 正常値に修正される.
         *
         * @param src 元行列
         * @return 元行列と等価なビルダ
         * @throws ElementsTooManyException 行列の有効要素数が大きすぎる場合(クラス説明文),
         *             成分に不正な値が入り込む場合
         * @throws NullPointerException 引数にnullが含まれる場合
         * @see EntryReadableMatrix#acceptValue(double)
         */
        public static Builder from(final Matrix src) {
            if (src instanceof EntryReadableMatrix castedSrc) {
                return Builder.from(castedSrc);
            }

            final var srcMatrixDimension = src.matrixDimension();
            final int srcRowDimension = srcMatrixDimension.rowAsIntValue();
            final int srcColumnDimension = srcMatrixDimension.columnAsIntValue();

            final var outBuilder = new Builder(srcMatrixDimension);

            //転置に対して基本単位ベクトルを乗算する
            //配列の方向が列方向であるため(速さは不明である)
            for (int j = 0; j < srcRowDimension; j++) {
                double[] rightArray = new double[srcRowDimension];
                rightArray[j] = 1;

                var builder = Vector.Builder.zeroBuilder(srcMatrixDimension.leftOperableVectorDimension());
                builder.setEntryValue(rightArray);
                final var result = src.operateTranspose(builder.build());
                for (int k = 0; k < srcColumnDimension; k++) {
                    outBuilder.setValue(j, k, result.valueAt(k));
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

            final var srcMatrixDimension = src.matrixDimension();
            final int srcRowDimension = srcMatrixDimension.rowAsIntValue();
            final int srcColumnDimension = srcMatrixDimension.columnAsIntValue();
            if (src instanceof GeneralMatrix castedSrc) {
                return new Builder(castedSrc);
            }

            final var outBuilder = new Builder(srcMatrixDimension);

            //転置に対して基本単位ベクトルを乗算する
            //配列の方向が列方向であるため(速さは不明である)
            for (int j = 0; j < srcRowDimension; j++) {
                for (int k = 0; k < srcColumnDimension; k++) {
                    outBuilder.setValue(j, k, src.valueAt(j, k));
                }
            }
            return outBuilder;
        }
    }
}
