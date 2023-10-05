/**
 * 2023.8.15
 */
package matsu.num.matrix.base.helper.value;

import matsu.num.commons.Exponentiation;
import matsu.num.matrix.base.Determinantable;

/**
 * 行列式({@linkplain Determinantable})に関する値のデータクラス.
 * 
 * @author Matsuura Y.
 * @version 15.0
 */
public final class DeterminantValues {

    private final double determinant;
    private final double logAbsDeterminant;
    private final int sign;

    public DeterminantValues(double logAbsDeterminant, int sign) {
        super();
        double absDet = Exponentiation.exp(logAbsDeterminant);
        this.determinant = sign >= 0 ? absDet : -absDet;
        this.logAbsDeterminant = logAbsDeterminant;
        this.sign = sign;
    }

    public double determinant() {
        return this.determinant;
    }

    public double logAbsDeterminant() {
        return this.logAbsDeterminant;
    }

    public int sign() {
        return this.sign;
    }

    public DeterminantValues createInverse() {
        if (this.sign == 0) {
            throw new IllegalStateException("符号が0");
        }
        return new DeterminantValues(
                -this.logAbsDeterminant, this.sign);
    }
}
