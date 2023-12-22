/**
 * 2023.11.30
 */
package matsu.num.matrix.base;

import java.util.Optional;

/**
 * 紐づけられた行列を逆行列に変換するインターフェース.
 * 
 * <p>
 * 紐づけられる{@linkplain Matrix}はインスタンスの生成と同時に決定される. <br>
 * {@linkplain Matrix}が不変であるので, このインターフェースにかかわる属性は実質的に不変であり,
 * 全てのメソッドは関数的かつスレッドセーフである.
 * </p>
 * 
 * <p>
 * 実装仕様: <br>
 * 逆行列が対称行列であることが確証できる場合,
 * {@linkplain #inverse()}の戻り値に{@linkplain Symmetric}マーカインターフェースを付与し,
 * その旨を文書化すべきである.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 17.1
 */
public interface Inversion {

    /**
     * このインターフェースが紐づく行列を返す.
     * 
     * <p>
     * このインターフェースを実装したクラスが{@linkplain Matrix}も実装する場合, {@code this}が返る.
     * </p>
     * 
     * @return このインターフェースが紐づく行列
     */
    public abstract Matrix target();

    /**
     * ターゲット行列の逆行列を取得する. <br>
     * 逆行列が生成できない場合は空を返す.
     *
     * @return ターゲット行列の逆行列
     */
    public Optional<? extends Matrix> inverse();
}
