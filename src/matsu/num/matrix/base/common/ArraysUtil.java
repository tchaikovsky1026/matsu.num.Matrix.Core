/**
 * 2024.1.16
 */
package matsu.num.matrix.base.common;

/**
 * <p>
 * 配列に対するベクトル基本演算を扱う.
 * </p>
 *
 * @author Matsuura Y.
 * @version 18.3
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
            throw new IllegalArgumentException("ベクトルサイズが一致しない");
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
            throw new IllegalArgumentException("ベクトルサイズが一致しない");
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
            throw new IllegalArgumentException("ベクトルサイズが一致しない");
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
            throw new IllegalArgumentException("ベクトルサイズが一致しない");
        }

        double outputValue = 0.0;
        int index;
        for (index = dimension - 1; index >= 3; index -= 4) {
            double v0, v1, v2, v3;
            v0 = vector1[index] * vector2[index];
            v1 = vector1[index - 1] * vector2[index - 1];
            v2 = vector1[index - 2] * vector2[index - 2];
            v3 = vector1[index - 3] * vector2[index - 3];
            outputValue += (v0 + v1) + (v2 + v3);
        }
        for (; index >= 0; index--) {
            double v0;
            v0 = vector1[index] * vector2[index];
            outputValue += v0;
        }
        return outputValue;
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
        double outputValue = 0.0;
        int index;
        for (index = vector.length - 1; index >= 3; index -= 4) {
            double v0 = Math.abs(vector[index]);
            double v1 = Math.abs(vector[index - 1]);
            double v2 = Math.abs(vector[index - 2]);
            double v3 = Math.abs(vector[index - 3]);
            outputValue += (v0 + v1) + (v2 + v3);
        }
        for (; index >= 0; index--) {
            double v0 = Math.abs(vector[index]);
            outputValue += v0;
        }
        return outputValue;
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
        //オーバー,アンダーフロー対策でスケールする
        double normMax = normMax(vector);
        if (!(Double.isFinite(normMax) && normMax > 0d)) {
            //特殊値の場合は別処理
            return normMax;
        }
        double invNormMax = 1d / normMax;
        if (!(Double.isFinite(invNormMax) && invNormMax > 0d)) {
            //逆数が特殊値の場合は別処理
            return norm2Abnormal(vector);
        }

        double outputValue = 0.0;
        int index;
        for (index = vector.length - 1; index >= 3; index -= 4) {
            double v0, v1, v2, v3;
            v0 = vector[index] * invNormMax;
            v1 = vector[index - 1] * invNormMax;
            v2 = vector[index - 2] * invNormMax;
            v3 = vector[index - 3] * invNormMax;
            v0 = v0 * v0;
            v1 = v1 * v1;
            v2 = v2 * v2;
            v3 = v3 * v3;
            outputValue += (v0 + v1) + (v2 + v3);
        }
        for (; index >= 0; index--) {
            double v0;
            v0 = vector[index] * invNormMax;
            v0 = v0 * v0;
            outputValue += v0;
        }
        return normMax * Math.sqrt(outputValue);
    }

    /**
     * 最大ノルムが非常に小さいベクトルに関する, 2-ノルムを計算する.
     */
    private static final double norm2Abnormal(double[] vector) {
        double normMax = normMax(vector);

        double outputValue = 0.0;
        int index;
        for (index = vector.length - 1; index >= 3; index -= 4) {
            double v0, v1, v2, v3;
            v0 = vector[index] / normMax;
            v1 = vector[index - 1] / normMax;
            v2 = vector[index - 2] / normMax;
            v3 = vector[index - 3] / normMax;
            v0 = v0 * v0;
            v1 = v1 * v1;
            v2 = v2 * v2;
            v3 = v3 * v3;
            outputValue += (v0 + v1) + (v2 + v3);
        }
        for (; index >= 0; index--) {
            double v0;
            v0 = vector[index] / normMax;
            v0 = v0 * v0;
            outputValue += v0;
        }
        return normMax * Math.sqrt(outputValue);
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
        double outputValue = 0.0;
        int index;
        for (index = vector.length - 1; index >= 3; index -= 4) {
            double v0, v1, v2, v3;
            v0 = vector[index];
            v1 = vector[index - 1];
            v2 = vector[index - 2];
            v3 = vector[index - 3];
            v0 = v0 * v0;
            v1 = v1 * v1;
            v2 = v2 * v2;
            v3 = v3 * v3;
            outputValue += (v0 + v1) + (v2 + v3);
        }
        for (; index >= 0; index--) {
            double v0;
            v0 = vector[index];
            v0 = v0 * v0;
            outputValue += v0;
        }
        return outputValue;
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
        double outputValue = 0.0;
        int index;
        for (index = vector.length - 1; index >= 3; index -= 4) {
            double v0 = Math.abs(vector[index]);
            double v1 = Math.abs(vector[index - 1]);
            double v2 = Math.abs(vector[index - 2]);
            double v3 = Math.abs(vector[index - 3]);
            double v01 = Math.max(v0, v1);
            double v23 = Math.max(v2, v3);
            outputValue = Math.max(outputValue, Math.max(v01, v23));
        }
        for (; index >= 0; index--) {
            double v0 = Math.abs(vector[index]);
            outputValue = Math.max(outputValue, v0);
        }
        return outputValue;
    }
}
