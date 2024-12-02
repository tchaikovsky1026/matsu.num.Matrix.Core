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
import java.util.Optional;

import matsu.num.matrix.base.validation.MatrixFormatMismatchException;

/**
 * 置換行列を扱う.
 *
 * @author Matsuura Y.
 * @version 23.3
 */
public sealed interface PermutationMatrix extends EntryReadableMatrix,
        OrthogonalMatrix, Determinantable permits PermutationMatrixSealed {

    /**
     * 行列の偶奇を取得する.
     *
     * @return 偶置換のときtrue
     */
    public abstract boolean isEven();

    @Override
    public abstract PermutationMatrix transpose();

    @Override
    public abstract Optional<? extends PermutationMatrix> inverse();

    /**
     * 置換行列の実装を提供するビルダ. <br>
     * このビルダはミュータブルであり, スレッドセーフでない.
     * 
     * <p>
     * このビルダインスタンスを得るには,
     * {@link #unitBuilder(MatrixDimension)}
     * をコールする.
     * </p>
     * 
     * <p>
     * ビルド準備ができたビルダに対して {@link #build()} をコールすることで
     * {@link PermutationMatrix} をビルドする. <br>
     * {@link #build()} を実行したビルダは使用不能となる.
     * </p>
     * 
     * <p>
     * ビルダのコピーが必要な場合, {@link #copy()} をコールする. <br>
     * ただし, このコピーはビルド前しか実行できないことに注意.
     * </p>
     */
    public static final class Builder {

        private MatrixDimension matrixDimension;

        //P=(e_{p_1},...,e_{p_n})で表示したときの, p_1,...,p_n
        private int[] permutationVertical;
        //P^{T}=(e_{q_1},...,e_{q_n})で表示したときの, q_1,...,q_n
        private int[] permutationHorizontal;

        private boolean even = true;
        private boolean unit = true;

        /**
         * 与えられた次元(サイズ)の置換行列ビルダを生成する. <br>
         * 初期値は単位行列.
         *
         * @param matrixDimension 行列サイズ
         * @throws MatrixFormatMismatchException 行列サイズが正方形でない場合
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        private Builder(final MatrixDimension matrixDimension) {
            if (!matrixDimension.isSquare()) {
                throw new MatrixFormatMismatchException(
                        String.format("正方形ではない行列サイズ:%s", matrixDimension));
            }
            this.matrixDimension = matrixDimension;

            final int thisDimension = matrixDimension.rowAsIntValue();
            permutationVertical = new int[thisDimension];
            permutationHorizontal = new int[thisDimension];
            final int[] thisPermutationVertical, thisPermutationHorizontal;
            thisPermutationVertical = permutationVertical;
            thisPermutationHorizontal = permutationHorizontal;
            for (int i = 0; i < thisDimension; i++) {
                thisPermutationVertical[i] = i;
                thisPermutationHorizontal[i] = i;
            }
        }

        /**
         * コピーコンストラクタ.
         */
        private Builder(Builder src) {
            this.matrixDimension = src.matrixDimension;
            this.permutationHorizontal = src.permutationHorizontal.clone();
            this.permutationVertical = src.permutationVertical.clone();
            this.even = src.even;
            this.unit = src.unit;
        }

        /**
         * 第 <i>i</i> 行と第 <i>j</i> 行を交換した行列を返す.
         *
         * @param row1 i, 行index1
         * @param row2 j, 行index2
         * @throws IllegalStateException すでにビルドされている場合
         * @throws IndexOutOfBoundsException i, jが行列の内部でない場合
         */
        public void swapRows(final int row1, final int row2) {
            this.throwISExIfCannotBeUsed();

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
            final int column1 = permutationHorizontal[row1];
            final int column2 = permutationHorizontal[row2];
            permutationHorizontal[row1] = column2;
            permutationHorizontal[row2] = column1;
            permutationVertical[column1] = row2;
            permutationVertical[column2] = row1;
            this.even = !this.even;
            this.unit = false;
        }

        /**
         * 第 <i>i</i> 列と第 <i>j</i> 列を交換した行列を返す.
         *
         * @param column1 i, 列index1
         * @param column2 j, 列index2
         * @throws IllegalStateException すでにビルドされている場合
         * @throws IndexOutOfBoundsException i, jが行列の内部でない場合
         */
        public void swapColumns(final int column1, final int column2) {
            this.throwISExIfCannotBeUsed();

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
            final int row1 = permutationVertical[column1];
            final int row2 = permutationVertical[column2];
            permutationVertical[column1] = row2;
            permutationVertical[column2] = row1;
            permutationHorizontal[row1] = column2;
            permutationHorizontal[row2] = column1;
            this.even = !this.even;
            this.unit = false;
        }

        /**
         * このビルダが使用可能か (ビルド前かどうか) を判定する.
         * 
         * @return 使用可能なら {@code true}
         */
        public boolean canBeUsed() {
            return Objects.nonNull(this.permutationHorizontal);
        }

        /**
         * ビルド前かを判定し, ビルド後なら例外をスロー.
         */
        private void throwISExIfCannotBeUsed() {
            if (!this.canBeUsed()) {
                throw new IllegalStateException("すでにビルドされています");
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
            this.permutationHorizontal = null;
            this.permutationVertical = null;
        }

        /**
         * 置換行列をビルドする.
         *
         * @return 置換行列
         * @throws IllegalStateException すでにビルドされている場合
         */
        public PermutationMatrix build() {
            this.throwISExIfCannotBeUsed();

            var out = this.unit
                    ? UnitMatrix.matrixOf(this.matrixDimension)
                    : new PermutationMatrixImpl(this);
            this.disable();

            return out;
        }

        /**
         * 与えられた次元(サイズ)を持つ, 単位行列で初期化された置換行列ビルダを生成する.
         *
         * @param matrixDimension 行列サイズ
         * @return 単位行列で初期化されたビルダ
         * @throws MatrixFormatMismatchException 行列サイズが正方形でない場合
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        public static Builder unitBuilder(final MatrixDimension matrixDimension) {
            return new Builder(matrixDimension);
        }

        private static final class PermutationMatrixImpl
                extends SkeletalAsymmetricOrthogonalMatrix<PermutationMatrix> implements PermutationMatrixSealed {

            private final MatrixDimension matrixDimension;

            //P=(e_{p_1},...,e_{p_n})で表示したときの, p_1,...,p_n
            private final int[] permutationVertical;
            //P^{T}=(e_{q_1},...,e_{q_n})で表示したときの, q_1,...,q_n
            private final int[] permutationHorizontal;
            private final boolean even;

            private PermutationMatrixImpl(Builder builder) {
                this.matrixDimension = builder.matrixDimension;
                this.even = builder.even;
                this.permutationVertical = builder.permutationVertical;
                this.permutationHorizontal = builder.permutationHorizontal;
            }

            /**
             * 内部から呼ばれるコンストラクタ.
             */
            private PermutationMatrixImpl(
                    final MatrixDimension matrixDimension,
                    final int[] permutationVertical, final int[] permutationHorizontal, final boolean even) {
                this.matrixDimension = matrixDimension;
                this.even = even;
                this.permutationVertical = permutationVertical;
                this.permutationHorizontal = permutationHorizontal;
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
                                    "行列外,matrix:%s,row=%d,column=%d",
                                    matrixDimension, row, column));
                }
                return permutationHorizontal[row] == column ? 1.0 : 0.0;
            }

            @Override
            protected PermutationMatrix createTranspose() {
                return new InverseAndDeterminantAttachedPermutationMatrixImpl(
                        new PermutationMatrixImpl(
                                this.matrixDimension,
                                this.permutationHorizontal,
                                this.permutationVertical,
                                this.even),
                        this);
            }

            @Override
            public boolean isEven() {
                return this.even;
            }

            @Override
            public Vector operate(Vector operand) {
                final var vectorDimension = operand.vectorDimension();
                if (!matrixDimension.rightOperable(vectorDimension)) {
                    throw new MatrixFormatMismatchException(
                            String.format(
                                    "右から演算不可:matrix:%s, operand:%s",
                                    matrixDimension, vectorDimension));
                }

                final double[] operandEntry;
                operandEntry = operand.entry();

                final int dimension = vectorDimension.intValue();
                final double[] resultEntry = new double[dimension];
                for (int i = 0; i < dimension; i++) {
                    resultEntry[i] = operandEntry[this.permutationHorizontal[i]];
                }

                var builder = Vector.Builder.zeroBuilder(vectorDimension);
                builder.setEntryValue(resultEntry);
                return builder.build();
            }

            @Override
            public Vector operateTranspose(Vector operand) {
                final var vectorDimension = operand.vectorDimension();
                if (!matrixDimension.leftOperable(vectorDimension)) {
                    throw new MatrixFormatMismatchException(
                            String.format(
                                    "左から演算不可:matrix:%s, operand:%s",
                                    matrixDimension, vectorDimension));
                }
                final double[] operandEntry;
                operandEntry = operand.entry();

                final int dimension = vectorDimension.intValue();
                final double[] resultEntry = new double[dimension];
                for (int i = 0; i < dimension; i++) {
                    resultEntry[i] = operandEntry[this.permutationVertical[i]];
                }

                var builder = Vector.Builder.zeroBuilder(vectorDimension);
                builder.setEntryValue(resultEntry);
                return builder.build();
            }

            @Override
            public double entryNormMax() {
                return 1;
            }

            @Override
            public double determinant() {
                return this.isEven() ? 1.0 : -1.0;
            }

            @Override
            public double logAbsDeterminant() {
                return 0.0;
            }

            @Override
            public int signOfDeterminant() {
                return this.isEven() ? 1 : -1;
            }

            /**
             * このオブジェクトの文字列説明表現を返す.
             * 
             * <p>
             * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
             * おそらくは次のような表現であろう. <br>
             * {@code Matrix[dim:(%dimension), permutation(%sign)]}
             * </p>
             * 
             * @return 説明表現
             */
            @Override
            public String toString() {
                return String.format(
                        "Matrix[dim:%s, permutation(%s)]",
                        this.matrixDimension(), this.isEven() ? "even" : "odd");
            }
        }

        /**
         * 逆行列に相当する置換行列を直接結びつけた置換行列. <br>
         * originalをラップし, 逆行列関連のメソッドを隠ぺいする.
         */
        private static final class InverseAndDeterminantAttachedPermutationMatrixImpl
                implements PermutationMatrixSealed {

            private final PermutationMatrix original;
            private final Optional<PermutationMatrix> opInverse;

            /**
             * 唯一のコンストラクタ.
             * 引数の正当性はチェックしていない.
             */
            InverseAndDeterminantAttachedPermutationMatrixImpl(
                    PermutationMatrix original, PermutationMatrix inverse) {
                super();
                this.original = original;
                this.opInverse = Optional.of(inverse);
            }

            @Override
            public MatrixDimension matrixDimension() {
                return this.original.matrixDimension();
            }

            @Override
            public PermutationMatrix transpose() {
                return this.opInverse.get();
            }

            @Override
            public double valueAt(int row, int column) {
                return this.original.valueAt(row, column);
            }

            @Override
            public double entryNormMax() {
                return this.original.entryNormMax();
            }

            @Override
            public Vector operate(Vector operand) {
                return this.original.operate(operand);
            }

            @Override
            public Vector operateTranspose(Vector operand) {
                return this.original.operateTranspose(operand);
            }

            @Override
            public double determinant() {
                return this.original.determinant();
            }

            @Override
            public double logAbsDeterminant() {
                return this.original.logAbsDeterminant();
            }

            @Override
            public int signOfDeterminant() {
                return this.original.signOfDeterminant();
            }

            @Override
            public boolean isEven() {
                return this.original.isEven();
            }

            @Override
            public Optional<? extends PermutationMatrix> inverse() {
                return this.opInverse;
            }

            @Override
            public String toString() {
                return this.original.toString();
            }
        }
    }
}
