/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.4.4
 */
package matsu.num.matrix.base;

/**
 * <p>
 * 紐づけられた行列の逆行列を取得するインターフェース.
 * </p>
 * 
 * <p>
 * このインターフェースの実装がインスタンス化された時点で,
 * {@linkplain #inverse()} により逆行列が取得できることが確定する.
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
 * @version 21.0
 */
public interface Inversion {

    /**
     * <p>
     * このインターフェースが紐づく行列を返す.
     * </p>
     * 
     * @return このインターフェースが紐づく行列
     */
    public abstract Matrix target();

    /**
     * ターゲット行列の逆行列を取得する.
     *
     * @return ターゲット行列の逆行列
     */
    public Matrix inverse();
}
