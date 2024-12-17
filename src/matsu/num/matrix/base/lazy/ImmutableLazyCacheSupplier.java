/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.12.17
 */
package matsu.num.matrix.base.lazy;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 返す値が変化しないサプライヤの, 遅延生成とキャッシュの仕組みを提供する.
 * 
 * <p>
 * オブジェクトのコンピュータ (作成器) を与えてサプライヤを生成する. <br>
 * 初めて {@link Supplier#get()} をコールしたときに, コンピュータによりオブジェクトの生成する. <br>
 * 同時にそのオブジェクトをキャッシュしておき,
 * 2回目以降の呼び出しではキャッシュしたオブジェクトを返す.
 * </p>
 * 
 * <p>
 * この仕組みでは, キャッシュが {@code null} であることにより「キャッシュされていない」と判断している. <br>
 * したがって, コンピュータが {@code null} を返す仕組みの場合は毎回コンピュータのgetが呼ばれることに注意せよ.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 25.1
 * @param <T> 生成されるインスタンスの型
 */
public final class ImmutableLazyCacheSupplier<T> implements Supplier<T> {

    private final Supplier<? extends T> computer;

    //getされるオブジェクト：遅延初期化される
    private volatile T product;

    //遅延初期化用のロックオブジェクト
    private final Object lock = new Object();

    /**
     * @param computer オブジェクトの生成器
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    private ImmutableLazyCacheSupplier(Supplier<? extends T> computer) {
        this.computer = Objects.requireNonNull(computer);
    }

    @Override
    public T get() {
        T out = this.product;
        if (Objects.nonNull(out)) {
            return out;
        }
        synchronized (this.lock) {
            out = this.product;
            if (Objects.nonNull(out)) {
                return out;
            }
            out = this.computer.get();
            this.product = out;
            return out;
        }
    }

    /**
     * 与えられたコンピュータから, このクラスの仕組みを実現するサプライヤを生成する.
     * 
     * <p>
     * クラス説明の通り, コンピュータによる計算はキャッシュされる. <br>
     * したがって,
     * 引数で与えるサプライヤは可変な状態を持つべきではない.
     * </p>
     * 
     * @param <T> 生成されるインスタンスの型
     * @param computer コンピュータ: {@code T} 型のインスタンスを作成する機構
     * @return サプライヤ
     * @throws NullPointerException 引数がnullの場合
     */
    public static <T> ImmutableLazyCacheSupplier<T> of(Supplier<? extends T> computer) {
        return new ImmutableLazyCacheSupplier<>(computer);
    }
}
