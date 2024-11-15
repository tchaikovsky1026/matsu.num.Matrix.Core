/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.15
 */
package matsu.num.matrix.base.helper.util;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * {@link Optional} に関するユーティリティ.
 * 
 * @author Matsuura Y.
 * @version 22.4
 */
public final class OptionalUtil {

    private OptionalUtil() {
        //インスタンス化不可
        throw new AssertionError();
    }

    /**
     * {@link Optional#orElse(Object)} の模倣. <br>
     * ジェネリックメソッドと境界ワイルドカードにより柔軟性の向上.
     * 
     * @param <R> 戻り値型
     * @param src 元となるオプショナル
     * @param other オプショナルが空の場合の戻り値, null なら null が返る
     * @return オプショナルの要素, あるいは other
     * @throws NullPointerException オプショナルがnullの場合
     */
    public static <R> R orElse(Optional<? extends R> src, R other) {
        return src.isPresent() ? src.get() : other;
    }

    /**
     * {@link Optional#orElseGet(java.util.function.Supplier)} の模倣. <br>
     * ジェネリックメソッドと境界ワイルドカードにより柔軟性の向上.
     * 
     * @param <R> 戻り値型
     * @param src 元となるオプショナル
     * @param supplier オプショナルが空の場合の戻り値のサプライヤ
     * @return オプショナルの要素, あるいは other
     * @throws NullPointerException
     *             {@code src.orElseGet(supplier)}
     *             が {@link NullPointerException} をスローする条件に適合する場合,
     *             オプショナルがnullの場合
     */
    public static <R> R orElseGet(Optional<? extends R> src, Supplier<? extends R> supplier) {
        return src.isPresent() ? src.get() : supplier.get();
    }
}
