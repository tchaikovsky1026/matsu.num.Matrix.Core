/**
 * 2024.1.16
 */
package matsu.num.matrix.base.nlsf.helper.fact;

import matsu.num.matrix.base.DiagonalMatrix;
import matsu.num.matrix.base.EntryReadableMatrix;
import matsu.num.matrix.base.LowerUnitriangularBuilder;
import matsu.num.matrix.base.LowerUnitriangularEntryReadableMatrix;
import matsu.num.matrix.base.MatrixDimension;
import matsu.num.matrix.base.exception.ProcessFailedException;

/**
 * Cholesky分解のヘルパ. <br>
 * A = LD<sup>1/2</sup>D<sup>1/2</sup>L<sup>T</sup>. <br>
 * <br>
 * 数値安定性を得るため, 与えられた行列Aを最初に定数倍してから分解する. <br>
 * その定数倍は対角行列Dに押し付ける.
 *
 * @author Matsuura Y.
 * @version 18.3
 */
public final class CholeskyFactorizationHelper {

    private final double scale;
    private final MatrixDimension matrixDimension;
    private final double[] mxLowerEntry;

    private DiagonalMatrix mxSqrtD;
    private LowerUnitriangularEntryReadableMatrix mxL;

    /**
     * @param matrix 
     * @param relativeEpsilon 
     * @throws IllegalArgumentException 行列の有効要素数が大きすぎる場合(dim * (dim + 1) > IntMax)
         * @throws ProcessFailedException 行列が正定値でない場合, 極端な値を含み分解が完了できない場合
     */
    public CholeskyFactorizationHelper(final EntryReadableMatrix matrix, double relativeEpsilon) {
        this.matrixDimension = matrix.matrixDimension();
        this.scale = matrix.entryNormMax();
        if (this.scale == 0.0) {
            throw new ProcessFailedException("正定値でない(零行列である)");
        }

        this.mxLowerEntry = lowerSideOfMatrixToArray(matrix);
        this.factorize(relativeEpsilon);
        this.convertToEachMatrix();
    }

    public DiagonalMatrix getMxSqrtD() {
        return this.mxSqrtD;
    }

    public LowerUnitriangularEntryReadableMatrix getMxL() {
        return this.mxL;
    }

    /**
     * 成分を配列に落とし込む際にスケールする.
     * 
     * @throws IllegalArgumentException 行列の有効要素数が大きすぎる場合(dim * (dim + 1) > IntMax)
     */
    private double[] lowerSideOfMatrixToArray(final EntryReadableMatrix matrix) {
        final int thisDimension = this.matrixDimension.rowAsIntValue();
        final long long_entrySize = ((long) thisDimension * thisDimension + thisDimension) / 2L;
        if (long_entrySize > Integer.MAX_VALUE / 2) {
            throw new IllegalArgumentException("サイズが大きすぎる");
        }

        double[] outArray = new double[(int) long_entrySize];
        int c = 0;
        for (int j = 0; j < thisDimension; j++) {
            for (int k = 0; k <= j; k++) {
                outArray[c] = matrix.valueAt(j, k) / this.scale;
                c++;
            }
        }
        return outArray;
    }

    /**
     * 行列を分解する.
     *
     * @throws ProcessFailedException 行列が正定値でない場合
     */
    private void factorize(double threshold) {
        final int thisDimension = this.matrixDimension.rowAsIntValue();
        final double[] thisMxEntry = this.mxLowerEntry;
        final double[] thisMxUEntry_bk = new double[thisDimension];

        int in = 0;
        for (int i = 0; i < thisDimension; i++) {
            in += i;
            //正則性チェック
            final double d = thisMxEntry[in + i];
            if (!(d >= threshold)) {
                throw new ProcessFailedException("行列が正定値でない");
            }
            //Dの計算
            final double invD = 1 / d;
            thisMxEntry[in + i] = Math.sqrt(d);
            //Lの計算と引っ張り
            int jn = (i * (i + 1)) / 2;
            for (int j = i + 1; j < thisDimension; j++) {
                jn += j;
                thisMxUEntry_bk[j] = thisMxEntry[jn + i];
                thisMxEntry[jn + i] *= invD;
            }
            //前進消去
            jn = (i * (i + 1)) / 2;
            for (int j = i + 1; j < thisDimension; j++) {
                jn += j;
                final double l_j = thisMxEntry[jn + i];
                for (int k = i + 1; k <= j; k++) {
                    thisMxEntry[jn + k] -= l_j * thisMxUEntry_bk[k];
                }
            }
        }
    }

    /**
     * 分解されたmxEntryを行列オブジェクトに変換.
     *
     * @throws ProcessFailedException mxEntryに不正な値が入っている場合
     */
    private void convertToEachMatrix() {
        final int thisDimension = this.matrixDimension.rowAsIntValue();
        final double[] thisMxEntry = this.mxLowerEntry;

        DiagonalMatrix.Builder mxSqrtDBuilder = DiagonalMatrix.Builder.zeroBuilder(this.matrixDimension);
        LowerUnitriangularBuilder mxLBuilder = LowerUnitriangularBuilder
                .unitBuilder(this.matrixDimension);

        //対角行列(sqrtD)にスケールを反映させる
        double sqrtScale = Math.sqrt(this.scale);
        try {
            int c = 0;
            for (int i = 0; i < thisDimension; i++) {
                for (int k = 0; k < i; k++) {
                    mxLBuilder.setValue(i, k, thisMxEntry[c]);
                    c++;
                }
                mxSqrtDBuilder.setValue(i, thisMxEntry[c] * sqrtScale);
                c++;
            }
        } catch (IllegalArgumentException e) {
            //setValueで不正値が入り込む可能性がある
            throw new ProcessFailedException("行列にの成分に極端な値が含まれる");
        }

        this.mxSqrtD = mxSqrtDBuilder.build();
        this.mxL = mxLBuilder.build();

        //スケールの関係で特異になるかもしれないので, 正則判定
        if (this.mxSqrtD.signOfDeterminant() == 0) {
            throw new ProcessFailedException("行列が正定値でない");
        }
    }

}
