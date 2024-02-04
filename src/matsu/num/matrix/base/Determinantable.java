/**
 * 2024.1.19
 */
package matsu.num.matrix.base;

/**
 * <p>
 * 紐づけられた行列についての行列式が計算可能であることを示すインターフェース.
 * </p>
 * 
 * <p>
 * このインターフェースにかかわる属性は実質的に不変であり,
 * 全てのメソッドは関数的かつスレッドセーフである.
 * </p>
 * 
 * <p>
 * 実装仕様: <br>
 * このインターフェースを実装するクラスが {@linkplain Invertible} や {@linkplain Inversion}
 * のような逆行列を扱うインターフェースも実装する場合,
 * 符号 {@linkplain #signOfDeterminant()} が0でないことと逆行列が存在することとは等価である.
 * </p>
 *
 * @author Matsuura Y.
 * @version 19.0
 * @see Invertible
 * @see Inversion
 */
public interface Determinantable {

    /**
     * 行列式の値を返す.
     *
     * @return 行列式
     */
    public abstract double determinant();

    /**
     * 行列式の絶対値の自然対数を返す.
     *
     * @return 行列式の絶対値の自然対数
     */
    public abstract double logAbsDeterminant();

    /**
     * 行列式の符号を返す.
     *
     * @return 行列式の符号, 行列式の値が正, 0, 負のときそれぞれ1, 0, -1
     */
    public abstract int signOfDeterminant();
}
