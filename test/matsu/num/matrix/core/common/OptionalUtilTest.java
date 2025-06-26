/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package matsu.num.matrix.core.common;

import static matsu.num.matrix.core.common.OptionalUtil.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Optional;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

/**
 * {@link OptionalUtil} クラスのテスト.
 */
@RunWith(Enclosed.class)
final class OptionalUtilTest {

    public static class castSafeのテスト {

        @Test
        public void test_nullを渡しても良い() {
            assertThat(castSafe(null), is(Optional.empty()));
        }
    }
}
