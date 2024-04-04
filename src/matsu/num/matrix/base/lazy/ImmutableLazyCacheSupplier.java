/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.4.4
 */
package matsu.num.matrix.base.lazy;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 返す値が変化しないサプライヤの, 遅延生成とキャッシュの仕組みを提供する.
 * 
 * <p>
 * オブジェクトのコンピュータ(生成器)を与えてサプライヤを生成する. <br>
 * 初めてgetをしたときにコンピュータによりオブジェクトの生成する. <br>
 * 同時にそのオブジェクトをキャッシュしておき,
 * 2回目以降の呼び出しではキャッシュしたオブジェクトを返す.
 * </p>
 * 
 * <p>
 * この仕組みでは, キャッシュが{@code null}であることにより「キャッシュされていない」と判断している. <br>
 * したがって, コンピュータが{@code null}を返す仕組みの場合は毎回コンピュータのgetが呼ばれることに注意せよ.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 21.0
 */
public final class ImmutableLazyCacheSupplier {

    private ImmutableLazyCacheSupplier() {
        throw new AssertionError();
    }

    /**
     * 与えられたコンピュータから, このクラスの仕組みを実現するサプライヤを生成する.
     * 
     * @param <T> サプライヤの型
     * @param computer コンピュータ
     * @return サプライヤ
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static <T> Supplier<T> of(Supplier<? extends T> computer) {
        return new LazyInitializer<>(computer);
    }

    private static final class LazyInitializer<T> implements Supplier<T> {

        private final Supplier<? extends T> computer;

        //getされるオブジェクト：遅延初期化される
        private volatile T product;

        //遅延初期化用のロックオブジェクト
        private final Object lock = new Object();

        /**
         * @param computer オブジェクトの生成器
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        LazyInitializer(Supplier<? extends T> computer) {
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

    }

}
