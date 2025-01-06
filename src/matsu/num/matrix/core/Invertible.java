/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.12.11
 */
package matsu.num.matrix.core;

import java.util.Optional;

/**
 * 逆行列が取得可能であることを表すインターフェース.
 * 
 * <p>
 * このインターフェースを実装した全てのクラスは実質的にイミュータブルであり,
 * (このインターフェース以外を含む) 全てのメソッドは関数的かつスレッドセーフである.
 * </p>
 * 
 * <hr>
 * 
 * <h2>実装規約</h2>
 * 
 * <p>
 * 実質的にイミュータブルかつ全てのメソッドは関数的かつスレッドセーフになるようにクラスが設計されなければならず,
 * 違反した場合は振る舞いが保証されない.
 * </p>
 * 
 * <p>
 * 逆行列を返す {@link #inverse()} メソッドの戻り値の要素は,
 * このメソッドの複数回の呼び出しにおいて同一のインスタンスであるべきである
 * (より強く, {@link Optional} 自体が同一であることが望ましい). <br>
 * 自身が {@link Matrix} を実装し, 逆行列が {@link Invertible} を実装している場合,
 * その逆行列の {@link #inverse()} メソッドの戻り値は自身となることが望ましい.
 * </p>
 * 
 * <blockquote>
 * 
 * <pre>
 * // 推奨: 次がtrue (複数回の呼び出しで同一のインスタンスを指す)
 * this.inverse().get() == this.inverse().get()
 * this.inverse() == this.inverse() // より推奨される
 * 
 * // (this instanceof Matrix) {@literal &&} (this.inverse().get() instanceof Invertible) がtrueのときに
 * // 推奨: 次がtrue (逆行列の逆行列は自身)
 * ((Invertible) this.inverse().get()).inverse().get() == this
 * </pre>
 * 
 * </blockquote>
 * 
 * <p>
 * 逆行列が対称行列であることが確証できる場合,
 * {@link #inverse()}の戻り値に{@link Symmetric} インターフェースを付与し,
 * その旨を文書化すべきである.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 25.0
 */
public interface Invertible {

    /**
     * 逆行列を取得する. <br>
     * 逆行列が存在しない場合は空を返す.
     *
     * @implSpec インターフェース説明の通り, 次が {@code true} となることが推奨される.
     *               <blockquote>
     *               {@code this.inverse() == this.inverse() }
     *               </blockquote>
     * 
     *               自身が {@link Matrix} のサブタイプであり,
     *               かつ戻り値が {@link Invertible} のサブタイプである場合,
     *               次が {@code true} となることが推奨される.
     *               <blockquote>
     *               {@code ((Invertible) this.inverse().get()).inverse().get() == this }
     *               </blockquote>
     * 
     * @return ターゲット行列の逆行列
     */
    public Optional<? extends Matrix> inverse();
}
