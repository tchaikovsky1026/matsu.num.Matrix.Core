/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.4
 */
package matsu.num.matrix.core;

/**
 * 紐づけられた行列の逆行列を取得するインターフェース.
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
 * {@link #inverse()} メソッドは必ずターゲット行列の逆行列を返さなければならない. <br>
 * したがって, 逆行列を持たないようなターゲット行列を紐づけるようなインスタンスは存在してはならない. <br>
 * {@link #inverse()} メソッドの戻り値は,
 * 複数回の呼び出しにおいて同一のインスタンスであるべきである.
 * </p>
 * 
 * <blockquote>
 * 
 * <pre>
 * // 推奨: 次がtrue (複数回の呼び出しで同一のインスタンスを指す)
 * this.inverse() == this.inverse()
 * </pre>
 * 
 * </blockquote>
 * 
 * <p>
 * 逆行列の逆行列は自身であるという数学的事実から,
 * {@link #inverse()} メソッドの戻り値に {@link Invertible} を実装し,
 * その {@link Invertible#inverse()} でターゲット行列を返したくなる. <br>
 * これは過剰であり, {@link #inverse()} メソッドの戻り値に {@link Invertible}
 * を実装するのは推奨されない.
 * </p>
 * 
 * <p>
 * 逆行列が対称行列であることが確証できる場合,
 * {@link #inverse()}の戻り値に{@link Symmetric} インターフェースを付与し,
 * その旨を文書化すべきである.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 22.0
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
