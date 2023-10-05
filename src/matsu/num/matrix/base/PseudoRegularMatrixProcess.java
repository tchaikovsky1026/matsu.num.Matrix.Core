/**
 * 2023.8.17
 */
package matsu.num.matrix.base;

/**
 * 正則な(もしくはそれに準ずる)行列の処理に関する基本定数に関する. 
 * 
 * @author Matsuura Y.
 * @version 15.0
 */
public final class PseudoRegularMatrixProcess {

    /**
     * 行列の正則性を判定する相対epsilonのデフォルト値. <br>
     * {@code DEFAULT_EPSILON = 1.0E-15}
     */
    public static final double DEFAULT_EPSILON;

    static {
        //利用先に値がハードコーディングされるのを避けるため, staticイニシャライザで初期化
        DEFAULT_EPSILON = 1E-15;
    }

    private PseudoRegularMatrixProcess() {
        throw new AssertionError();
    }

}
