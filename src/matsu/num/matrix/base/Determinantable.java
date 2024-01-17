/**
 * 2023.12.25
 */
package matsu.num.matrix.base;

/**
 * <p>
 * 紐づけられた行列についての行列式が計算可能であることを示すインターフェース.
 * </p>
 * 
 * <p>
 * 紐づけられる {@linkplain Matrix} はこのインスタンスの生成と同時に決定される. <br>
 * {@linkplain Matrix} が不変であるので, このインターフェースにかかわる属性は実質的に不変であり,
 * 全てのメソッドは関数的かつスレッドセーフである.
 * </p>
 * 
 * <p>
 * このインターフェースは, 行列式が計算可能な行列に対して付与される場合もある. <br>
 * この場合, determinantable のターゲットは自身であり, <br>
 * {@code this.target() == this} <br>
 * となる.
 * </p>
 * 
 * <p>
 * 実装仕様: <br>
 * このインターフェースを実装するクラスが {@linkplain Inversion} も実装する場合,
 * 符号 {@linkplain #signOfDeterminant()} が0であることと
 * {@linkplain Inversion#inverse()} が例外をスローすることは等価である.
 * </p>
 *
 * @author Matsuura Y.
 * @version 18.0
 * @see Inversion
 */
public interface Determinantable {

    /**
     * <p>
     * このインターフェースが紐づく行列を返す. <br>
     * このインターフェースを実装したクラスが{@linkplain Matrix}も実装する場合, {@code this}が返る.
     * </p>
     * 
     * @return このインターフェースが紐づく行列
     */
    public abstract Matrix target();

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
