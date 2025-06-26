/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2025.6.25
 */
package matsu.num.matrix.core.common;

/**
 * <p>
 * 配列に対するベクトル基本演算を扱う.
 * </p>
 *
 * @author Matsuura Y.
 */
public final class ArraysUtil {

    private ArraysUtil() {
        //インスタンス化不可
        throw new AssertionError();
    }

    /**
     * <p>
     * あるベクトルを他のベクトルに加算する:
     * <b>v</b> &larr; <b>v</b> + <b>u</b>. <br>
     * <b>v</b>: 作用ベクトル <br>
     * <b>u</b>: 参照ベクトル
     * </p>
     * 
     * <p>
     * <b>u</b> と <b>v</b> のサイズが0の場合, 何もしない.
     * </p>
     *
     * @param operand 作用ベクトル <b>v</b>
     * @param reference 参照ベクトル <b>u</b>
     * @throws IllegalArgumentException <b>u</b> と <b>v</b> のサイズが一致しない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static final void add(double[] operand, double[] reference) {
        final int dimension = operand.length;
        if (dimension != reference.length) {
            throw new IllegalArgumentException("size mismatch");
        }
        for (int i = 0; i < dimension; i++) {
            operand[i] += reference[i];
        }
    }

    /**
     * <p>
     * あるベクトルを他のベクトルから減算する:
     * <b>v</b> &larr; <b>v</b> - <b>u</b>. <br>
     * <b>v</b>: 作用ベクトル <br>
     * <b>u</b>: 参照ベクトル
     * </p>
     * 
     * <p>
     * <b>u</b> と <b>v</b> のサイズが0の場合, 何もしない.
     * </p>
     *
     * @param operand 作用ベクトル <b>v</b>
     * @param reference 参照ベクトル <b>u</b>
     * @throws IllegalArgumentException <b>u</b> と <b>v</b> のサイズが一致しない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static final void subtract(double[] operand, double[] reference) {
        final int dimension = operand.length;
        if (dimension != reference.length) {
            throw new IllegalArgumentException("size mismatch");
        }
        for (int i = 0; i < dimension; i++) {
            operand[i] -= reference[i];
        }
    }

    /**
     * <p>
     * あるベクトルのスカラー倍を他のベクトルに加算する:
     * <b>v</b> &larr; <b>v</b> + <i>c</i> <b>u</b>. <br>
     * <b>v</b>: 作用ベクトル <br>
     * <b>u</b>: 参照ベクトル <br>
     * <i>c</i>: スカラー
     * </p>
     * 
     * <p>
     * <b>u</b> と <b>v</b> のサイズが0の場合, 何もしない.
     * </p>
     *
     * @param operand 作用ベクトル <b>v</b>
     * @param reference 参照ベクトル <b>u</b>
     * @param scalar スカラー <i>c</i>
     * @throws IllegalArgumentException <b>u</b> と <b>v</b> のサイズが一致しない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static final void addCTimes(double[] operand, double[] reference, double scalar) {
        final int dimension = operand.length;
        if (dimension != reference.length) {
            throw new IllegalArgumentException("size mismatch");
        }
        for (int i = 0; i < dimension; i++) {
            operand[i] += scalar * reference[i];
        }
    }

    /**
     * <p>
     * あるベクトルをスカラー倍する:
     * <b>v</b> &larr; <i>c</i> <b>v</b>. <br>
     * <b>v</b>: 作用ベクトル <br>
     * <i>c</i>: スカラー
     * </p>
     * 
     * <p>
     * <b>v</b> のサイズが0の場合, 何もしない.
     * </p>
     *
     * @param operand 作用ベクトル <b>v</b>
     * @param scalar スカラー <i>c</i>
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static final void multiply(double[] operand, double scalar) {
        for (int i = 0, dimension = operand.length; i < dimension; i++) {
            operand[i] *= scalar;
        }
    }

    /**
     * <p>
     * 2個のベクトルの内積を返す:
     * <b>u</b> &middot; <b>v</b>.
     * </p>
     * 
     * <p>
     * <b>u</b> と <b>v</b> のサイズが0の場合, 0が返る.
     * </p>
     *
     * @param vector1 ベクトル1 <b>u</b>
     * @param vector2 ベクトル2 <b>v</b>
     * @return 内積 <b>u</b> &middot; <b>v</b>
     * @throws IllegalArgumentException <b>u</b> と <b>v</b> のサイズが一致しない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static double dot(double[] vector1, double[] vector2) {
        final int dimension = vector1.length;
        if (dimension != vector2.length) {
            throw new IllegalArgumentException("size mismatch");
        }

        /*
         * 主要ループで4成分の計算を同時に行う.
         * 影響する変数を分けることで, 並列実行できる可能性がある.
         */
        double v0 = 0.0;
        double v1 = 0.0;
        double v2 = 0.0;
        double v3 = 0.0;
        int index;
        for (index = dimension - 1; index >= 3; index -= 4) {
            v0 += vector1[index] * vector2[index];
            v1 += vector1[index - 1] * vector2[index - 1];
            v2 += vector1[index - 2] * vector2[index - 2];
            v3 += vector1[index - 3] * vector2[index - 3];
        }
        for (; index >= 0; index--) {
            v0 += vector1[index] * vector2[index];
        }
        return (v0 + v1) + (v2 + v3);
    }

    /**
     * <p>
     * ベクトルの1-ノルムを返す:
     * ||<b>v</b>||<sub>1</sub>.
     * </p>
     * 
     * <p>
     * <b>v</b> のサイズが0の場合, 0が返る.
     * </p>
     *
     * @param vector ベクトル <b>v</b>
     * @return 1-ノルム ||<b>v</b>||<sub>1</sub>
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static final double norm1(double[] vector) {

        /*
         * 主要ループで4成分の計算を同時に行う.
         * 影響する変数を分けることで, 並列実行できる可能性がある.
         */
        double v0 = 0.0;
        double v1 = 0.0;
        double v2 = 0.0;
        double v3 = 0.0;
        int index;
        for (index = vector.length - 1; index >= 3; index -= 4) {
            v0 += Math.abs(vector[index]);
            v1 += Math.abs(vector[index - 1]);
            v2 += Math.abs(vector[index - 2]);
            v3 += Math.abs(vector[index - 3]);
        }
        for (; index >= 0; index--) {
            v0 += Math.abs(vector[index]);
        }
        return (v0 + v1) + (v2 + v3);
    }

    /**
     * <p>
     * ベクトルの2-ノルムを返す:
     * ||<b>v</b>||<sub>2</sub>.
     * </p>
     * 
     * <p>
     * <b>v</b> のサイズが0の場合, 0が返る.
     * </p>
     *
     * @param vector ベクトル <b>v</b>
     * @return 2-ノルム ||<b>v</b>||<sub>2</sub>
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static final double norm2(double[] vector) {
        double normMax = normMax(vector);
        return norm2(vector, normMax);
    }

    /**
     * <p>
     * ベクトルの最大値ノルムを与えて,
     * ベクトルの2-ノルムを返す:
     * ||<b>v</b>||<sub>2</sub>.
     * </p>
     * 
     * <p>
     * <b>v</b> のサイズが0の場合, 0が返る.
     * </p>
     *
     * @param vector ベクトル <b>v</b>
     * @param normMax vectorの最大値ノルム, {@code normMax(vector)} に一致しなければならない.
     * @return 2-ノルム ||<b>v</b>||<sub>2</sub>
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static final double norm2(double[] vector, double normMax) {

        //PInf, NaN, 0d をはじく
        if (!(Double.isFinite(normMax) && normMax > 0d)) {
            return normMax;
        }

        double invNormMax = 1d / normMax;
        //逆数が特殊値の場合は別処理
        if (!(Double.isFinite(invNormMax) && invNormMax > 0d)) {
            return norm2Abnormal(vector, normMax);
        }

        /*
         * 主要ループで4成分の計算を同時に行う.
         * 影響する変数を分けることで, 並列実行できる可能性がある.
         */
        int index;
        double v0 = 0.0;
        double v1 = 0.0;
        double v2 = 0.0;
        double v3 = 0.0;
        for (index = vector.length - 1; index >= 3; index -= 4) {
            double e0 = vector[index] * invNormMax;
            double e1 = vector[index - 1] * invNormMax;
            double e2 = vector[index - 2] * invNormMax;
            double e3 = vector[index - 3] * invNormMax;
            v0 += e0 * e0;
            v1 += e1 * e1;
            v2 += e2 * e2;
            v3 += e3 * e3;
        }
        for (; index >= 0; index--) {
            double e0 = vector[index] * invNormMax;
            v0 += e0 * e0;
        }
        return normMax * Math.sqrt((v0 + v1) + (v2 + v3));
    }

    /**
     * 最大ノルムが非常に小さいベクトルに関する, 2-ノルムを計算する.
     */
    private static final double norm2Abnormal(double[] vector, double normMax) {

        /*
         * 主要ループで4成分の計算を同時に行う.
         * 影響する変数を分けることで, 並列実行できる可能性がある.
         */
        int index;
        double v0 = 0.0;
        double v1 = 0.0;
        double v2 = 0.0;
        double v3 = 0.0;
        for (index = vector.length - 1; index >= 3; index -= 4) {
            double e0 = vector[index] / normMax;
            double e1 = vector[index - 1] / normMax;
            double e2 = vector[index - 2] / normMax;
            double e3 = vector[index - 3] / normMax;
            v0 += e0 * e0;
            v1 += e1 * e1;
            v2 += e2 * e2;
            v3 += e3 * e3;
        }
        for (; index >= 0; index--) {
            double e0 = vector[index] / normMax;
            v0 += e0 * e0;
        }
        return normMax * Math.sqrt((v0 + v1) + (v2 + v3));
    }

    /**
     * <p>
     * ベクトルの2-ノルムの2乗を返す:
     * ||<b>v</b>||<sub>2</sub><sup>2</sup>.
     * </p>
     * 
     * <p>
     * <b>v</b> のサイズが0の場合, 0が返る.
     * </p>
     *
     * @param vector ベクトル <b>v</b>
     * @return 2-ノルムの2乗 ||<b>v</b>||<sub>2</sub><sup>2</sup>
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static final double norm2Square(double[] vector) {

        /*
         * 主要ループで4成分の計算を同時に行う.
         * 影響する変数を分けることで, 並列実行できる可能性がある.
         */
        int index;
        double v0 = 0.0;
        double v1 = 0.0;
        double v2 = 0.0;
        double v3 = 0.0;
        for (index = vector.length - 1; index >= 3; index -= 4) {
            double e0 = vector[index];
            double e1 = vector[index - 1];
            double e2 = vector[index - 2];
            double e3 = vector[index - 3];
            v0 += e0 * e0;
            v1 += e1 * e1;
            v2 += e2 * e2;
            v3 += e3 * e3;
        }
        for (; index >= 0; index--) {
            double e0 = vector[index];
            v0 += e0 * e0;
        }
        return (v0 + v1) + (v2 + v3);
    }

    /**
     * <p>
     * 最大値ノルムを計算する:
     * ||<b>v</b>||<sub>&infin;</sub>.
     * </p>
     *
     * <p>
     * <b>v</b> のサイズが0の場合, 0が返る.
     * </p>
     *
     * @param vector ベクトル <b>v</b>
     * @return 最大ノルム ||<b>v</b>||<sub>&infin;</sub>
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static final double normMax(double[] vector) {

        /*
         * 主要ループで4成分の計算を同時に行う.
         * 影響する変数を分けることで, 並列実行できる可能性がある.
         */
        int index;
        double v0 = 0.0;
        double v1 = 0.0;
        double v2 = 0.0;
        double v3 = 0.0;
        for (index = vector.length - 1; index >= 3; index -= 4) {
            v0 = Math.max(v0, Math.abs(vector[index]));
            v1 = Math.max(v1, Math.abs(vector[index - 1]));
            v2 = Math.max(v2, Math.abs(vector[index - 2]));
            v3 = Math.max(v3, Math.abs(vector[index - 3]));
        }
        for (; index >= 0; index--) {
            v0 = Math.max(v0, Math.abs(vector[index]));
        }
        return Math.max(Math.max(v0, v1), Math.max(v2, v3));
    }

    /**
     * <p>
     * 引数のベクトルをEuclidノルムにより規格化する:
     * <b>v</b> / ||<b>v</b>||<sub>2</sub>. <br>
     * ノルムが0の場合は何もしない. <br>
     * 不正値を含む場合, 結果は不定である.
     * </p>
     * 
     * @param operand 作用ベクトル
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static final void normalizeEuclidean(double[] operand) {
        double normMax = normMax(operand);
        normalizeEuclidean(operand, normMax);
    }

    /**
     * <p>
     * ベクトルの最大値ノルムを与えて,
     * 引数のベクトルをEuclidノルムにより規格化する:
     * <b>v</b> / ||<b>v</b>||<sub>2</sub>. <br>
     * ノルムが0の場合は何もしない. <br>
     * 不正値を含む場合, 結果は不定である.
     * </p>
     * 
     * @param operand 作用ベクトル
     * @param normMax operandの最大値ノルム, {@code normMax(operand)} に一致しなければならない.
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static final void normalizeEuclidean(double[] operand, double normMax) {

        if (!(Double.isFinite(normMax) && normMax > 0d)) {
            return;
        }

        if (normMax < 1E-280) {
            ArraysUtil.multiply(operand, 1E200);
        }
        if (normMax > 1E280) {
            ArraysUtil.multiply(operand, 1E-200);
        }

        double canoNorm2 = ArraysUtil.norm2(operand);
        ArraysUtil.multiply(operand, 1 / canoNorm2);
    }

    /**
     * <p>
     * 引数のベクトルの加法逆元をとる (-1倍する). <br>
     * 不正値を含む場合, 結果は不定である.
     * </p>
     * 
     * @param operand 作用ベクトル
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static final void negate(double[] operand) {
        for (int i = 0, len = operand.length; i < len; i++) {
            operand[i] = -operand[i];
        }
    }
}
