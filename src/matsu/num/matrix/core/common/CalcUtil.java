/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.24
 */
package matsu.num.matrix.core.common;

/**
 * 計算補助.
 * 
 * @author Matsuura Y.
 * @version 23.0
 */
public final class CalcUtil {

    private CalcUtil() {
        //インスタンス化不可
        throw new AssertionError();
    }

    /**
     * 1からnまでの総和を返す.
     * オーバーフローしてはいけない.
     * 
     * @param n n
     * @return n*(n+1)/2
     */
    public static int sumOf1To(int n) {
        return (n & 1) == 1
                ? n * ((n + 1) / 2)
                : (n / 2) * (n + 1);
    }
}
