/**
 * 2023.8.18
 */
package matsu.num.matrix.base;

/**
 * ベクトルの次元を扱う不変クラス. <br>
 * ベクトルの次元は1以上の整数値をとる.
 * 
 * <p>
 * このクラスは次の属性を基にした等価性を提供する. 
 * </p>
 * 
 * <ul>
 * <li> 次元 </li>
 * </ul>
 *
 * @author Matsuura Y.
 * @version 15.0
 */
public final class VectorDimension {

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
    private final int immutableHashCode;

    private VectorDimension(int dimension) {
        if (dimension < MIN_DIMENSION) {
            throw new IllegalArgumentException(String.format("不正な次元:dimension=%d", dimension));
        }
        this.dimension = dimension;
        this.immutableHashCode = this.immutableHashCode();
    }

    /**
     * このベクトル次元の{@code int}値を返す.
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
     * 他オブジェクトとの等価性を判定する. 
     * 
     * <p>
     * 等価性の基準はクラス説明のとおりである.
     * </p>
     * 
     * @param obj 比較対象
     * @return 自身とobjが等価の場合はtrue
     */
    @Override
    public boolean equals(Object obj) {
        //同一性判定
        if (this == obj) {
            return true;
        }
        //非整合の排除, 同時にnull排除
        if (!(obj instanceof VectorDimension)) {
            return false;
        }

        VectorDimension target = (VectorDimension) obj;
        return this.dimension == target.dimension;
    }

    /**
     * ハッシュコードを返す.
     * 
     * @return ハッシュコード
     */
    @Override
    public int hashCode() {
        return this.immutableHashCode;
    }

    private int immutableHashCode() {
        int result = -455912;
        result = 31 * result + Integer.hashCode(this.dimension);
        return result;
    }

    /**
     * 与えられたindexがベクトルの内部かを判定.
     *
     * @param index index
     * @return indexがベクトルの内部なら{@code true}
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
     * {@code [%value]}
     * </p>
     * 
     * @return 説明表現
     */
    @Override
    public String toString() {
        return String.format("[%d]", this.dimension);
    }

    /**
     * 与えられた次元のベクトル次元オブジェクトの作成.
     *
     * @param dimension n(次元)
     * @return 次元 n のベクトル次元オブジェクト
     * @throws IllegalArgumentException 引数が1未満である場合
     */
    public static VectorDimension valueOf(int dimension) {

        int cacheIndex = dimension - MIN_DIMENSION;
        if (0 <= cacheIndex && cacheIndex < CACHE_SIZE) {
            return cache[cacheIndex];
        }
        return new VectorDimension(dimension);
    }

}
