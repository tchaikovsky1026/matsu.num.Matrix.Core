/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.17
 */
package matsu.num.matrix.base.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.MatrixDimension;
import matsu.num.matrix.base.Vector;
import matsu.num.matrix.base.VectorDimension;
import matsu.num.matrix.base.validation.MatrixFormatMismatchException;

/**
 * 行列のブロック構造を扱う. <br>
 * イミュータブルでありスレッドセーフに振る舞う.
 * 
 * <p>
 * 行列のブロック構造について, 例を挙げて説明する. <br>
 * たとえば行数2, 列数3のブロック構造は, 以下のようになる. <br>
 * &lceil; A B C &rceil; <br>
 * &lfloor; D E F &rfloor; <br>
 * ここで, A, B, C, D, E, F は行列である. <br>
 * c(A) = c(D), c(B) = c(E), c(C) = c(F),
 * r(A) = r(B) = r(C), r(D) = r(E) = r(F) でなければならない. <br>
 * ただし, c(&middot;) はその行列の列数を, r(&middot;) はその行列の行数を表す.
 * </p>
 * 
 * <p>
 * このクラスでのブロック構造表現では, ブロック要素に"空"が許可されている. <br>
 * 空の場合, そのブロックサイズは周辺から推定される. <br>
 * 空要素のサイズが周辺から推定できず"不定"となるようなブロック構造の存在は禁止されている.
 * </p>
 * 
 * <p>
 * このインスタンスの生成はビルダ ({@link BlockMatrixStructure.Builder}) を介して行う. <br>
 * ビルダの生成は {@link #builderOf(MatrixDimension)} メソッドでのみ可能.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 22.5
 * @param <T> このブロック構造が扱う行列要素の型
 */
public final class BlockMatrixStructure<T extends Matrix> {

    private final MatrixDimension entireMatrixDimension;
    private final MatrixDimension structureDimension;
    private final List<List<Optional<T>>> matrixList;

    private final MatrixDimension[][] elementDimensions;

    /**
     * ビルダから構造を作成する. <br>
     * nullを渡してはいけない.
     * 
     * <p>
     * ビルダのList-Listは読まれるのみであり, 書き込まれない.
     * </p>
     * 
     * @throws MatrixFormatMismatchException ブロック構造が不能あるいは不定
     */
    private BlockMatrixStructure(Builder<T> builder) {
        this.structureDimension = builder.structureDimension;

        int rows = structureDimension.rowAsIntValue();
        int columns = structureDimension.columnAsIntValue();
        this.elementDimensions = new MatrixDimension[rows][columns];

        this.matrixList = builder.matrixList;

        if (!analyzeDimension()) {
            throw new MatrixFormatMismatchException("ブロック構造が不能あるいは不定");
        }

        this.entireMatrixDimension = this.calcEntireMatrixDimension();
    }

    /**
     * 指定したブロック構造を持つビルダを生成する.
     * 
     * @param <T> 生成されるブロック構造ビルダが扱う行列要素の型
     * @param structureDimension ブロック構造
     * @return 新しいビルダ
     */
    public static <T extends Matrix> BlockMatrixStructure.Builder<T>
            builderOf(MatrixDimension structureDimension) {
        return new Builder<>(Objects.requireNonNull(structureDimension));
    }

    /**
     * リストの行列構造が正当であるかを検証し, elementDimensionsを埋める. <br>
     * うまくいけばtrueを, 正当でない場合(不能, 不定)はfalseを返す.
     */
    private boolean analyzeDimension() {

        int rows = structureDimension.rowAsIntValue();
        int columns = structureDimension.columnAsIntValue();

        //行方向の各ブロックの行サイズ
        int[] dimensionRows = new int[rows];
        //列方向の各ブロックの列サイズ
        int[] dimensionColumns = new int[columns];

        for (int j = 0; j < rows; j++) {
            List<Optional<T>> matrixList_j = matrixList.get(j);

            for (int k = 0; k < columns; k++) {
                Optional<T> element = matrixList_j.get(k);

                if (element.isPresent()) {
                    MatrixDimension elementDimension = element.get().matrixDimension();
                    dimensionRows[j] = elementDimension.rowAsIntValue();
                    dimensionColumns[k] = elementDimension.columnAsIntValue();
                }
            }
        }

        //不定の検出
        if (Arrays.stream(dimensionRows).anyMatch(i -> i == 0)
                || Arrays.stream(dimensionColumns).anyMatch(i -> i == 0)) {
            return false;
        }

        //不能の検出
        for (int j = 0; j < rows; j++) {
            List<Optional<T>> matrixList_j = matrixList.get(j);
            int rowsOfElement_j = dimensionRows[j];
            MatrixDimension[] elementDimensions_j = elementDimensions[j];

            for (int k = 0; k < columns; k++) {
                Optional<T> element = matrixList_j.get(k);
                int columnsOfElement_k = dimensionColumns[k];

                if (element.isPresent()) {
                    MatrixDimension elementDimension = element.get().matrixDimension();
                    if (elementDimension.rowAsIntValue() != rowsOfElement_j) {
                        return false;
                    }
                    if (elementDimension.columnAsIntValue() != columnsOfElement_k) {
                        return false;
                    }
                    elementDimensions_j[k] = elementDimension;
                } else {
                    elementDimensions_j[k] = MatrixDimension.rectangle(rowsOfElement_j, columnsOfElement_k);
                }
            }
        }

        return true;
    }

    /**
     * elementDimensionを元に、ブロック構造全体の行列次元を算出する.
     */
    private MatrixDimension calcEntireMatrixDimension() {

        int entireColumns = 0;
        for (MatrixDimension bd : this.elementDimensions[0]) {
            entireColumns += bd.columnAsIntValue();
        }

        int entireRows = 0;
        for (MatrixDimension[] arrBd : this.elementDimensions) {
            entireRows += arrBd[0].rowAsIntValue();
        }

        return MatrixDimension.rectangle(entireRows, entireColumns);
    }

    /**
     * ブロック構造全体の行列としての次元を返す.
     * 
     * @return ブロック構造全体の行列としての次元
     */
    public MatrixDimension entireMatrixDimension() {
        return this.entireMatrixDimension;
    }

    /**
     * ブロック構造の次元 (行方向, 列方向の行列要素の数) を返す.
     * 
     * @return ブロック構造の次元
     */
    public MatrixDimension structureDimension() {
        return this.structureDimension;
    }

    /**
     * <i>j</i> 列のブロック要素が右から演算可能であるようなベクトル次元を返す.
     * 
     * @param column <i>j</i>, 列index
     * @return 右から演算可能なベクトルの次元
     * @throws IndexOutOfBoundsException 引数が構造範囲外の場合
     */
    public VectorDimension rightOperableVectorDimensionAt(int column) {
        if (!(structureDimension.isValidColumnIndex(column))) {
            throw new IndexOutOfBoundsException(
                    String.format(
                            "行列構造外:structure:%s, column=%s",
                            structureDimension, column));
        }
        return this.elementDimensions[0][column].rightOperableVectorDimension();
    }

    /**
     * <i>i</i> 行のブロック要素が左から演算可能であるようなベクトル次元を返す.
     * 
     * @param row <i>i</i>, 行index
     * @return 左から演算可能なベクトルの次元
     * @throws IndexOutOfBoundsException 引数が構造範囲外の場合
     */
    public VectorDimension leftOperableVectorDimensionAt(int row) {
        if (!(structureDimension.isValidRowIndex(row))) {
            throw new IndexOutOfBoundsException(
                    String.format(
                            "行列構造外:structure:%s, row=%s",
                            structureDimension, row));
        }
        return this.elementDimensions[row][0].leftOperableVectorDimension();
    }

    /**
     * (<i>i</i>, <i>j</i>) ブロック要素を返す. <br>
     * 指定されていない場合は空が返る.
     * 
     * @param row <i>i</i>, 行index
     * @param column <i>j</i>, 列index
     * @return (<i>i</i>, <i>j</i>) ブロック要素
     * @throws IndexOutOfBoundsException (<i>i</i>, <i>j</i>) が構造の内部でない場合
     */
    public Optional<T> matrixAt(int row, int column) {
        if (!(structureDimension.isValidIndexes(row, column))) {
            throw new IndexOutOfBoundsException(
                    String.format(
                            "行列構造外:structure:%s, (row, column)=(%s, %s)",
                            structureDimension, row, column));
        }
        return this.matrixList.get(row).get(column);
    }

    /**
     * (<i>i</i>, <i>j</i>) ブロック要素の行列次元を返す.
     * 
     * @param row <i>i</i>, 行index
     * @param column <i>j</i>, 列index
     * @return (<i>i</i>, <i>j</i>) ブロックの行列次元
     * @throws IndexOutOfBoundsException (<i>i</i>, <i>j</i>) が構造の内部でない場合
     */
    public MatrixDimension elementDimensionAt(int row, int column) {
        if (!(structureDimension.isValidIndexes(row, column))) {
            throw new IndexOutOfBoundsException(
                    String.format(
                            "行列構造外:structure:%s, (row, column)=(%s, %s)",
                            structureDimension, row, column));
        }

        return this.elementDimensions[row][column];
    }

    /**
     * 右から演算可能なoperandをブロック分割する.
     * 
     * <p>
     * 内部から呼ばれるため, バリデーションは {@code assert} オプションである. <br>
     * よって公開してはならない.
     * </p>
     */
    Vector[] rightSplit(Vector operand) {

        assert this.entireMatrixDimension().rightOperable(operand.vectorDimension()) : "assert: 右から演算不可";

        Vector[] splitted = new Vector[this.structureDimension().columnAsIntValue()];

        double[] arrOperand = operand.entryAsArray();
        int startIndex = 0;
        for (int k = 0; k < splitted.length; k++) {
            VectorDimension elementRightDimension = this.rightOperableVectorDimensionAt(k);
            int dimensionValue = elementRightDimension.intValue();
            int endIndex = startIndex + dimensionValue;

            double[] entry = Arrays.copyOfRange(arrOperand, startIndex, endIndex);
            Vector.Builder vBuilder = Vector.Builder.zeroBuilder(elementRightDimension);
            vBuilder.setEntryValue(entry);
            splitted[k] = vBuilder.build();
            startIndex = endIndex;
        }

        return splitted;
    }

    /**
     * 左から演算可能なoperandをブロック分割する.
     * 
     * <p>
     * 内部から呼ばれるため, バリデーションは {@code assert} オプションである. <br>
     * よって公開してはならない.
     * </p>
     */
    Vector[] leftSplit(Vector operand) {

        assert this.entireMatrixDimension().leftOperable(operand.vectorDimension()) : "assert: 左から演算不可";

        Vector[] splitted = new Vector[this.structureDimension().rowAsIntValue()];

        double[] arrOperand = operand.entryAsArray();
        int startIndex = 0;
        for (int k = 0; k < splitted.length; k++) {
            VectorDimension elementLeftDimension = this.leftOperableVectorDimensionAt(k);
            int dimensionValue = elementLeftDimension.intValue();
            int endIndex = startIndex + dimensionValue;

            double[] entry = Arrays.copyOfRange(arrOperand, startIndex, endIndex);
            Vector.Builder vBuilder = Vector.Builder.zeroBuilder(elementLeftDimension);
            vBuilder.setEntryValue(entry);
            splitted[k] = vBuilder.build();
            startIndex = endIndex;
        }

        return splitted;
    }

    /**
     * 列方向のブロック構造にマッチするベクトルたちをマージする. <br>
     * マージ後はブロック行列全体の列数とベクトルの次元とが一致する.
     * 
     * <p>
     * 内部から呼ばれるため, バリデーションは {@code assert} オプションである. <br>
     * よって公開してはならない.
     * </p>
     */
    Vector mergeColumnsMatchedVectors(Vector[] splittedColumnMatched) {
        VectorDimension entireVectorDimension = this.entireMatrixDimension().rightOperableVectorDimension();

        double[] mergedArray = new double[entireVectorDimension.intValue()];
        int startIndex = 0;
        for (int i = 0; i < splittedColumnMatched.length; i++) {
            double[] arrElementVec = splittedColumnMatched[i].entryAsArray();
            int size = arrElementVec.length;

            assert this.rightOperableVectorDimensionAt(i).intValue() == size : "assert: 列ブロックの行数にサイズがマッチしない";
            assert startIndex + size <= mergedArray.length : "assert: ?Bug:はみ出している（引数が不正の場合,この前の行で落ちるはず）";

            System.arraycopy(arrElementVec, 0, mergedArray, startIndex, size);

            startIndex += size;
        }

        assert startIndex == mergedArray.length : "合計サイズが一致しない";

        Vector.Builder vBuilder = Vector.Builder.zeroBuilder(entireVectorDimension);
        vBuilder.setEntryValue(mergedArray);
        return vBuilder.build();
    }

    /**
     * 行方向のブロック構造にマッチするベクトルたちをマージする. <br>
     * マージ後はブロック行列全体の行数とベクトルの次元とが一致する.
     * 
     * <p>
     * 内部から呼ばれるため, バリデーションは {@code assert} オプションである. <br>
     * よって公開してはならない.
     * </p>
     */
    Vector mergeRowMatchedVectors(Vector[] splittedRowMatched) {
        VectorDimension entireVectorDimension = this.entireMatrixDimension().leftOperableVectorDimension();

        double[] mergedArray = new double[entireVectorDimension.intValue()];
        int startIndex = 0;
        for (int i = 0; i < splittedRowMatched.length; i++) {
            double[] arrElementVec = splittedRowMatched[i].entryAsArray();
            int size = arrElementVec.length;

            assert this.leftOperableVectorDimensionAt(i).intValue() == size : "assert: 行ブロックの行数にサイズがマッチしない";
            assert startIndex + size <= mergedArray.length : "assert: ?Bug:はみ出している（引数が不正の場合,この前の行で落ちるはず）";

            System.arraycopy(arrElementVec, 0, mergedArray, startIndex, size);

            startIndex += size;
        }

        assert startIndex == mergedArray.length : "合計サイズが一致しない";

        Vector.Builder vBuilder = Vector.Builder.zeroBuilder(entireVectorDimension);
        vBuilder.setEntryValue(mergedArray);
        return vBuilder.build();
    }

    /**
     * このインスタンスの文字列表現を返す.
     * 
     * @return 文字列表現
     */
    @Override
    public String toString() {

        int[] rows = new int[this.structureDimension.rowAsIntValue()];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = this.elementDimensions[i][0].rowAsIntValue();
        }
        int[] columns = new int[this.structureDimension.columnAsIntValue()];
        MatrixDimension[] bd = this.elementDimensions[0];
        for (int i = 0; i < columns.length; i++) {
            columns[i] = bd[i].columnAsIntValue();
        }

        return String.format("(%s:%s)", Arrays.toString(rows), Arrays.toString(columns));
    }

    /**
     * 行列のブロック構造のビルダ. <br>
     * このビルダはミュータブルであり, スレッドセーフでない.
     * 
     * <p>
     * このビルダの生成は,
     * {@link BlockMatrixStructure#builderOf(MatrixDimension)}
     * によって行う.
     * </p>
     * 
     * <p>
     * 行列がセットされたビルダからブロック構造をビルドするには, {@link #build()} メソッドをコールする. <br>
     * このとき, ブロック構造は必ず生成されるわけではなく, 次の場合は失敗する.
     * </p>
     * 
     * <ul>
     * <li>ブロック要素のサイズが整合しない場合 (不能)</li>
     * <li>ブロック要素に空が多く, 空が表す行列サイズが決まらない場合 (不定)</li>
     * </ul>
     * 
     * <p>
     * サイズの整合や"空"については, {@link BlockMatrixStructure} の説明を参照.
     * </p>
     * 
     * <p>
     * このビルダはミュータブルであり, 再利用することはできない. <br>
     * {@link #build()} メソッドがコールされブロック構造が生成された場合,
     * そのビルダは使用不能となる. <br>
     * 必要な場合は {@link #copy()} メソッドによりビルダのコピーを作成する.
     * </p>
     * 
     * @param <T> このブロック構造ビルダが扱う行列要素の型
     */
    public static final class Builder<T extends Matrix> {

        private final MatrixDimension structureDimension;
        private List<List<Optional<T>>> matrixList;

        /**
         * 通常のコンストラクタ. <br>
         * nullを渡してはいけない.
         */
        private Builder(MatrixDimension structureDimension) {
            this.structureDimension = structureDimension;

            this.matrixList = this.createList();
        }

        /**
         * コピーコンストラクタ
         */
        private Builder(Builder<T> src) {
            this.structureDimension = src.structureDimension;

            this.matrixList = copyList(src.matrixList);
        }

        /**
         * 構造次元の形でリスト-リストを作成する. <br>
         * リスト-リストの要素はnull埋めされている. <br>
         * [row][column]の構造.
         */
        private List<List<Optional<T>>> createList() {
            int rows = this.structureDimension.rowAsIntValue();
            int columns = this.structureDimension.columnAsIntValue();

            List<List<Optional<T>>> outerList = new ArrayList<>(rows);
            for (int j = 0; j < rows; j++) {
                List<Optional<T>> columnList = new ArrayList<>(columns);
                for (int k = 0; k < columns; k++) {
                    columnList.add(Optional.empty());
                }
                outerList.add(columnList);
            }

            return outerList;
        }

        /**
         * (<i>i</i>, <i>j</i>) ブロック要素を指定した行列, あるいは"空"に置き換える. <br>
         * {@code element} に {@code null} を渡した場合は空となる.
         * 
         * @param row <i>i</i>, 行index
         * @param column <i>j</i>, 列index
         * @param element 置き換えた後の行列要素, {@code null} を許容する
         * @throws IndexOutOfBoundsException (<i>i</i>, <i>j</i>) が構造の内部でない場合
         * @throws IllegalStateException すでにビルドされている場合
         */
        public void setBlockElement(final int row, final int column, T element) {
            if (!this.canBeUsed()) {
                throw new IllegalStateException("すでにビルドされています");
            }
            if (!(structureDimension.isValidIndexes(row, column))) {
                throw new IndexOutOfBoundsException(
                        String.format(
                                "行列構造外:structure:%s, (row, column)=(%s, %s)",
                                structureDimension, row, column));
            }

            this.matrixList.get(row).set(column, Optional.ofNullable(element));
        }

        /**
         * 自身ビルダをコピーして (新しいインスタンスとして) 返す. <br>
         * ビルド後にはコピー不能になるので, ビルド前にコールされる必要がある.
         * 
         * @return 自身のコピー
         * @throws IllegalStateException すでにビルドされている場合
         */
        public BlockMatrixStructure.Builder<T> copy() {
            if (!this.canBeUsed()) {
                throw new IllegalStateException("すでにビルドされています");
            }

            return new Builder<>(this);
        }

        /**
         * このビルダが使用可能か (ビルド前であるか)
         * を判定する.
         * 
         * @return 使用可能なら true
         */
        public boolean canBeUsed() {
            return Objects.nonNull(this.matrixList);
        }

        /**
         * ブロック構造をビルドする. <br>
         * ビルドに成功した場合, このビルダは使用不能になる
         * (失敗した場合は使える).
         * 
         * @return ブロック構造
         * @throws IllegalStateException すでにビルドされている場合
         * @throws MatrixFormatMismatchException ブロック構造が不能あるいは不定
         */
        public BlockMatrixStructure<T> build() {
            if (!this.canBeUsed()) {
                throw new IllegalStateException("すでにビルドされています");
            }

            BlockMatrixStructure<T> out = new BlockMatrixStructure<>(this);
            this.matrixList = null;
            return out;
        }

        /**
         * リスト-リストをコピーするstaticメソッド.
         */
        private static <E extends Matrix> List<List<Optional<E>>>
                copyList(List<List<Optional<E>>> src) {
            List<List<Optional<E>>> outerList = new ArrayList<>(src.size());
            for (List<Optional<E>> innerList : src) {
                outerList.add(new ArrayList<>(innerList));
            }

            return outerList;
        }
    }
}
