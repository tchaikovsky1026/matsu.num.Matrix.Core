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

/**
 * 行列の次元(サイズ)を扱う不変クラス. <br>
 * 行サイズ, 列サイズともに1以上の整数値をとる. <br>
 * このクラスのインスタンスは, 行サイズ, 列サイズの値に基づくequalityを有する.
 *
 * @author Matsuura Y.
 * @version 23.3
 */
public final class MatrixDimension {

    private static final int MIN_DIMENSION = 1;

    private static final int CACHE_SIZE = 255;
    private static final MatrixDimension[] squareCache;

    static {
        squareCache = new MatrixDimension[CACHE_SIZE];
        for (int i = 0; i < CACHE_SIZE; i++) {
            final VectorDimension dim = VectorDimension.valueOf(MIN_DIMENSION + i);
            squareCache[i] = new MatrixDimension(dim, dim);
        }
    }

    private final VectorDimension rowVectorDimension;
    private final VectorDimension columnVectorDimension;
    private final MatrixShape shape;

    //評価結果を使いまわすためのフィールド
    private final int hashCode;
    private final boolean accepedForDenseMatrix;

    //循環参照が生じるため, 遅延初期化を行う
    //軽量オブジェクトのためロックを行わず,複数回の初期化を許す
    private volatile MatrixDimension transposedDimension;
    private volatile MatrixDimension leftSquareDimension;
    private volatile MatrixDimension rightSquareDimension;

    private MatrixDimension(VectorDimension rowDimension, VectorDimension columnDimension) {
        super();
        this.rowVectorDimension = Objects.requireNonNull(rowDimension);
        this.columnVectorDimension = Objects.requireNonNull(columnDimension);
        this.shape = MatrixShape.shape(rowDimension, columnDimension);
        this.hashCode = this.calcHashCode();
        this.accepedForDenseMatrix = this.calcAccepedForDenseMatrix();

        if (this.isSquare()) {
            this.transposedDimension = this;
            this.leftSquareDimension = this;
            this.rightSquareDimension = this;
        }
    }

    /**
     * 行の {@code int} 値を返す.
     *
     * @return 行
     */
    public int rowAsIntValue() {
        return this.rowVectorDimension.intValue();
    }

    /**
     * 列の {@code int} 値を返す.
     *
     * @return 列
     */
    public int columnAsIntValue() {
        return this.columnVectorDimension.intValue();
    }

    /**
     * 左から演算可能なベクトルの次元を返す.
     *
     * @return 左から演算可能なベクトルの次元
     */
    public VectorDimension leftOperableVectorDimension() {
        return this.rowVectorDimension;
    }

    /**
     * 右から演算可能なベクトルの次元を返す.
     *
     * @return 右から演算可能なベクトルの次元
     */
    public VectorDimension rightOperableVectorDimension() {
        return this.columnVectorDimension;
    }

    /**
     * 行列サイズが正方形かどうかを判定.
     *
     * @return 行列サイズが正方形であれば {@code true}
     */
    public boolean isSquare() {
        return this.shape == MatrixShape.SQUARE;
    }

    /**
     * 行列サイズが狭義横長かどうかを判定.
     *
     * @return 行列サイズが狭義横長であれば{@code true}
     */
    public boolean isHorizontal() {
        return this.shape == MatrixShape.HORIZONTAL;
    }

    /**
     * 行列サイズが狭義縦長かどうかを判定する.
     *
     * @return 行列サイズが狭義縦長であれば {@code true}
     */
    public boolean isVertical() {
        return this.shape == MatrixShape.VERTICAL;
    }

    /**
     * 行列サイズが密行列の要素数として受け入れられるかを判定する.
     * 
     * <p>
     * 密行列の要素数は, <br>
     * {@code rows * columns <= Integer.MAX_VALUE} <br>
     * を有効とする.
     * </p>
     * 
     * @return 受け入れられるなら {@code true}
     */
    public boolean isAccepedForDenseMatrix() {
        return this.accepedForDenseMatrix;
    }

    /**
     * 行列サイズが密行列の要素数として受け入れられるかを計算する.
     * 
     * @see #isAccepedForDenseMatrix()
     */
    private boolean calcAccepedForDenseMatrix() {
        final long entrySize = (long) this.rowAsIntValue() * this.columnAsIntValue();
        return entrySize <= Integer.MAX_VALUE;
    }

    /**
     * 与えられた次元のベクトルを左から演算できるかを判定.
     *
     * @param referenceDimension 演算可否を考えるベクトルの次元
     * @return 左から演算できるなら{@code true}
     */
    public boolean leftOperable(VectorDimension referenceDimension) {
        return this.rowVectorDimension.equals(referenceDimension);
    }

    /**
     * 与えられた次元のベクトルを右から演算できるかを判定.
     *
     * @param referenceDimension 演算可否を考えるベクトルの次元
     * @return 右から演算できるなら{@code true}
     */
    public boolean rightOperable(VectorDimension referenceDimension) {
        return this.columnVectorDimension.equals(referenceDimension);
    }

    /**
     * 与えられた行indexが行列の内部かを判定.
     *
     * @param rowIndex 行index
     * @return 行indexが行列の内部なら{@code true}
     */
    public boolean isValidRowIndex(int rowIndex) {
        return this.rowVectorDimension.isValidIndex(rowIndex);
    }

    /**
     * 与えられた列indexが行列の内部かを判定.
     *
     * @param columnIndex 列index
     * @return 列indexが行列の内部なら{@code true}
     */
    public boolean isValidColumnIndex(int columnIndex) {
        return this.columnVectorDimension.isValidIndex(columnIndex);
    }

    /**
     * 与えられた(行index, 列index)が行列の内部かを判定.
     *
     * @param rowIndex 行index
     * @param columnIndex 列index
     * @return (行index, 列index)が行列の内部なら{@code true}
     */
    public boolean isValidIndexes(int rowIndex, int columnIndex) {
        return this.isValidRowIndex(rowIndex) && this.isValidColumnIndex(columnIndex);
    }

    /**
     * 他オブジェクトとの等価性を判定する. <br>
     * 等価性の基準はクラス説明のとおりである.
     * 
     * @param obj 比較対象
     * @return 自身とobjが等価の場合はtrue
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MatrixDimension target)) {
            return false;
        }

        return this.rowVectorDimension.equals(target.rowVectorDimension)
                && this.columnVectorDimension.equals(target.columnVectorDimension);
    }

    /**
     * ハッシュコードを返す.
     * 
     * @return ハッシュコード
     */
    @Override
    public int hashCode() {
        return this.hashCode;
    }

    /**
     * ハッシュコードを計算する.
     * 
     * @return ハッシュコード
     */
    private int calcHashCode() {
        int result = Objects.hashCode(this.rowVectorDimension);
        result = 31 * result + Objects.hashCode(this.columnVectorDimension);
        return result;
    }

    /**
     * このオブジェクトの文字列説明表現を返す.
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code (%r, %c)}
     * </p>
     * 
     * @return 説明表現
     */
    @Override
    public String toString() {
        return String.format(
                "(%s, %s)",
                this.rowAsIntValue(), this.columnAsIntValue());
    }

    /**
     * このインスタンスの次元の転置の次元を返す.
     *
     * @return thisの転置次元
     */
    public MatrixDimension transpose() {
        MatrixDimension out = this.transposedDimension;
        if (Objects.nonNull(out)) {
            return out;
        }

        if (this.isSquare()) {
            this.transposedDimension = this;
            return this;
        }

        //複数回の初期化を許すため,オブジェクトのロックを行わない
        out = MatrixDimension.rectangle(this.columnAsIntValue(), this.rowAsIntValue());
        this.transposedDimension = out;
        out.transposedDimension = this;

        out.leftSquareDimension = this.rightSquareDimension;
        out.rightSquareDimension = this.leftSquareDimension;

        return out;
    }

    /**
     * このインスタンスの左に適合する正方形ディメンジョンを返す.
     * 
     * @return 左に適合する正方形ディメンジョン
     */
    public MatrixDimension leftSquareDimension() {
        var out = this.leftSquareDimension;
        if (Objects.nonNull(out)) {
            return out;
        }

        if (this.isSquare()) {
            this.leftSquareDimension = this;
            this.rightSquareDimension = this;
            return this;
        }

        //複数回の初期化を許すため,オブジェクトのロックを行わない
        out = MatrixDimension.square(this.rowAsIntValue());
        this.leftSquareDimension = out;
        return out;
    }

    /**
     * このインスタンスの右に適合する正方形ディメンジョンを返す.
     * 
     * @return 右に適合する正方形ディメンジョン
     */
    public MatrixDimension rightSquareDimension() {
        var out = this.rightSquareDimension;
        if (Objects.nonNull(out)) {
            return out;
        }

        if (this.isSquare()) {
            this.leftSquareDimension = this;
            this.rightSquareDimension = this;
            return this;
        }

        //複数回の初期化を許すため,オブジェクトのロックを行わない
        out = MatrixDimension.square(this.columnAsIntValue());
        this.rightSquareDimension = out;
        return out;
    }

    /**
     * 与えた行数, 列数を持つ長方形の行列サイズオブジェクトを返す.
     *
     * @param rowDimension 行サイズ
     * @param columnDimension 列サイズ
     * @return 長方形の行列サイズオブジェクト
     * @throws IllegalArgumentException 引数のどちらかが1未満である場合
     */
    public static MatrixDimension rectangle(int rowDimension, int columnDimension) {
        if (rowDimension == columnDimension) {
            return MatrixDimension.square(rowDimension);
        }
        if (rowDimension < MIN_DIMENSION || columnDimension < MIN_DIMENSION) {
            throw new IllegalArgumentException(
                    String.format(
                            "不正なサイズ:dimension:(row, column)=(%d, %d)",
                            rowDimension, columnDimension));
        }
        return MatrixDimension.rectangle(
                VectorDimension.valueOf(rowDimension),
                VectorDimension.valueOf(columnDimension));
    }

    /**
     * このメソッドは公開しない.
     *
     * @param rowDimension 行サイズに相当するベクトルディメンジョン
     * @param columnDimension 列サイズに相当するベクトルディメンジョン
     * @return 長方形の行列サイズオブジェクト
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    private static MatrixDimension rectangle(VectorDimension rowDimension, VectorDimension columnDimension) {
        if (rowDimension.equals(columnDimension)) {
            return MatrixDimension.square(rowDimension);
        }
        return new MatrixDimension(rowDimension, columnDimension);
    }

    /**
     * 与えた値と同等の行数, 列数を持つ正方形の行列サイズオブジェクトを返す.
     *
     * @param dimension 行サイズ = 列サイズ
     * @return 正方形の行列サイズオブジェクト
     * @throws IllegalArgumentException 引数が1未満である場合
     */
    public static MatrixDimension square(int dimension) {
        var out = getFromCache(dimension);
        if (Objects.nonNull(out)) {
            return out;
        }

        var vectorDimension = VectorDimension.valueOf(dimension);
        return new MatrixDimension(vectorDimension, vectorDimension);
    }

    /**
     * 与えたベクトルディメンジョンと同等の行ディメンジョン, 列ディメンジョンを持つ正方形の行列サイズオブジェクトを返す.
     *
     * @param dimension 行サイズ = 列サイズに相当するベクトルディメンジョン
     * @return 正方形の行列サイズオブジェクト
     * @throws NullPointerException 引数にullが含まれる場合
     */
    public static MatrixDimension square(VectorDimension dimension) {
        var out = getFromCache(dimension.intValue());
        if (Objects.nonNull(out)) {
            return out;
        }

        return new MatrixDimension(dimension, dimension);
    }

    /**
     * 与えたdimensionに対してキャッシュが存在するならそれを返す. <br>
     * 存在しないならnull.
     */
    private static MatrixDimension getFromCache(int dimension) {
        int cacheIndex = dimension - MIN_DIMENSION;
        if (0 <= cacheIndex && cacheIndex < CACHE_SIZE) {
            return squareCache[cacheIndex];
        }

        return null;
    }

    private enum MatrixShape {
        SQUARE, VERTICAL, HORIZONTAL;

        public static MatrixShape shape(VectorDimension rowDimension, VectorDimension columnDimension) {
            if (rowDimension.equals(columnDimension)) {
                return SQUARE;
            }
            if (rowDimension.compareTo(columnDimension) > 0) {
                return VERTICAL;
            }
            return HORIZONTAL;
        }
    }
}
