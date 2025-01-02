/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.12.26
 */
package matsu.num.matrix.core;

/**
 * ベクトルの次元を扱うイミュータブルなクラス. <br>
 * 扱うのは1次元以上である. <br>
 * このクラスのインスタンスは, 次元の値に基づくequality, comparabilityを有する.
 *
 * @author Matsuura Y.
 * @version 25.2
 */
public final class VectorDimension implements Comparable<VectorDimension> {

    private static final int MIN_DIMENSION = 1;

    private static final int CACHE_SIZE = 255;
    private static final VectorDimension[] cache;

    static {
        cache = new VectorDimension[CACHE_SIZE];
        for (int i = 0; i < CACHE_SIZE; i++) {
            cache[i] = new VectorDimension(MIN_DIMENSION + i);
        }
    }

    private final int dimension;

    //評価結果を使いまわすためのフィールド
    private final int hashCode;

    /**
     * @throws IllegalArgumentException dimensionが不正の場合
     */
    private VectorDimension(int dimension) {
        if (dimension < MIN_DIMENSION) {
            throw new IllegalArgumentException(String.format("不正な次元:dimension=%d", dimension));
        }
        this.dimension = dimension;
        this.hashCode = this.calcHashCode();
    }

    /**
     * このベクトル次元の {@code int} 値を返す.
     *
     * @return ベクトルの次元
     */
    public int intValue() {
        return this.dimension;
    }

    /**
     * このベクトル次元と与えられた値との等価性を判定する.
     *
     * @param otherValue 比較対象
     * @return このベクトル次元が比較対象と一致すれば, true
     */
    public boolean equalsValueOf(int otherValue) {
        return this.dimension == otherValue;
    }

    /**
     * 他オブジェクトとの等価性を判定する. <br>
     * equality はクラス説明の通り.
     * 
     * @param obj 比較対象
     * @return 自身とobjが等価の場合はtrue
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VectorDimension target)) {
            return false;
        }

        return this.equalsValueOf(target.dimension);
    }

    /**
     * ベクトルディメンジョンを比較する. <br>
     * comparability はクラス説明の通り.
     * 
     * @param target 比較相手
     * @return 比較結果
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    @Override
    public int compareTo(VectorDimension target) {
        return Integer.compare(this.dimension, target.dimension);
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
     * <p>
     * 一度だけ呼ばれる.
     * </p>
     * 
     * @return このインスタンスのハッシュコード
     */
    private int calcHashCode() {
        int result = 1;
        result = 31 * result + Integer.hashCode(this.dimension);
        return result;
    }

    /**
     * 与えられたindexがベクトルの内部かどうかを判定する.
     *
     * @param index index
     * @return indexがベクトルの内部ならtrue
     */
    public boolean isValidIndex(int index) {
        return 0 <= index && index < this.dimension;
    }

    /**
     * このオブジェクトの文字列説明表現を返す.
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code %value}
     * </p>
     * 
     * @return 説明表現
     */
    @Override
    public String toString() {
        return String.format("%s", this.dimension);
    }

    /**
     * 与えられた値のベクトル次元を返す.
     * 
     * <p>
     * 次元は1以上でなければならない.
     * </p>
     * 
     * @param dimension <i>n</i> (次元)
     * @return 値が <i>n</i> のベクトル次元
     * @throws IllegalArgumentException 引数が正でない場合
     */
    public static VectorDimension valueOf(int dimension) {

        int cacheIndex = dimension - MIN_DIMENSION;
        if (0 <= cacheIndex && cacheIndex < CACHE_SIZE) {
            return cache[cacheIndex];
        }
        //この内部で例外をスローする
        return new VectorDimension(dimension);
    }
}
