/**
 * 2023.12.25
 */
package matsu.num.matrix.base;

import java.util.Optional;

/**
 * <p>
 * 逆行列が取得可能であることを表すインターフェース.
 * </p>
 * 
 * <p>
 * このインターフェースにかかわる属性は実質的に不変であり,
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
 * @version 19.0
 */
public interface Invertible {

    /**
     * 逆行列を取得する. <br>
     * 逆行列が存在しない場合は空を返す.
     *
     * @return ターゲット行列の逆行列
     */
    public Optional<? extends Matrix> inverse();
}
