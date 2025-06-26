/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2025.6.26
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
     * {@code Optional} のバインドされた型パラメータをスーパータイプに安全にキャストする.
     * 
     * <p>
     * 引数が {@code null} の場合は空が返る.
     * </p>
     * 
     * @param <T> 緩和された後の型パラメータ
     * @param src src
     * @return 変換後の {@code Optional}
     */
    public static <T> Optional<T> castSafe(Optional<? extends T> src) {

        /*
         * Optionalクラスの実装の性質上,
         * Optional<? extends T> を
         * Optional<Matrix>として扱っても問題にならない.
         */
        @SuppressWarnings("unchecked")
        Optional<T> out = (Optional<T>) src;
        return Objects.isNull(out)
                ? Optional.empty()
                : out;
    }
}
