/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2025.1.7
 */
package matsu.num.matrix.core.common;

import java.util.Objects;
import java.util.Optional;

/**
 * {@link Optional} インスタンスの操作を扱うユーティリティクラス.
 * 
 * @author Matsuura Y.
 */
public final class OptionalUtil {

    private OptionalUtil() {
        //インスタンス化不可
        throw new AssertionError();
    }

    /**
     * {@code Optional} のバインドされた型パラメータをスーパータイプに変更 (緩和) する.
     * 
     * @param <T> 緩和された後の型パラメータ
     * @param src src
     * @return 変換後の {@code Optional}
     * @throws NullPointerException 引数がnullである場合
     */
    public static <T> Optional<T> changeBoundType(Optional<? extends T> src) {

        // Optionalクラスの実装の性質上, 
        // Optional<? extends T> を 
        // Optional<Matrix>として扱っても問題にならない.
        @SuppressWarnings("unchecked")
        Optional<T> out = (Optional<T>) Objects.requireNonNull(src);
        return out;
    }
}
