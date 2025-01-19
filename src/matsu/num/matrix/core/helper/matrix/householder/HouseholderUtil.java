/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2025.1.19
 */
package matsu.num.matrix.core.helper.matrix.householder;

import matsu.num.matrix.core.Vector;

/**
 * Householder変換に関連するユーティリティ.
 * 
 * @author Matsuura Y.
 */
final class HouseholderUtil {

    private HouseholderUtil() {
        //インスタンス化不可
        throw new AssertionError();
    }

    /**
     * 与えた大きさ0でないベクトルを0番目の標準基底ベクトルに変換する,
     * Householder変換の鏡映ベクトルを計算する.
     * 
     * <p>
     * 与えるソースの次元は2以上でなければならない. <br>
     * 与えるソースの大きさは0であってはならない.
     * </p>
     * 
     * @param src 変換元のベクトル
     * @return 鏡映ベクトル
     * @throws IllegalArgumentException srcの大きさが0の場合
     * @throws NullPointerException 引数がnullの場合
     */
    static Vector computeReflectionVectorToStandardBasis(Vector src) {
        var dimension = src.vectorDimension();

        assert dimension.intValue() >= 2 : "不正: 次元1";
        Vector normalizedSrc = src.normalizedEuclidean();
        assert normalizedSrc.norm2() > 0d : "不正: 大きさ0";

        // p-eを計算する
        // p = [p_0, p_1, ... , p_{d-1}]
        double[] diffEntry = normalizedSrc.entryAsArray();
        if (diffEntry[0] <= 0d) {
            diffEntry[0] -= 1d;
        } else {
            //p_1^2 + ... + p_{m-1}^2
            double sum_without_first = 0d;
            for (int i = 1; i < diffEntry.length; i++) {
                double v = diffEntry[i];
                sum_without_first += v * v;
            }

            diffEntry[0] = -sum_without_first / (1 + Math.sqrt(1 - sum_without_first));
        }

        // normalized(p-e)の候補
        var builder = Vector.Builder.zeroBuilder(dimension);
        builder.setEntryValue(diffEntry);
        var out = builder.build().normalizedEuclidean();

        // p-e が0ベクトルの場合, p=eなので,
        // 返すべき鏡映ベクトルはe'である.
        return out.norm2() > 0d
                ? out
                : Vector.standardBasis(dimension, 1);
    }
}
