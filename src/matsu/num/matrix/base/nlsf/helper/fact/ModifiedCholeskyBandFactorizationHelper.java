/**
 * 2023.8.15
 */
package matsu.num.matrix.base.nlsf.helper.fact;

import matsu.num.matrix.base.BandMatrix;
import matsu.num.matrix.base.BandMatrixDimension;
import matsu.num.matrix.base.DiagonalMatrix;
import matsu.num.matrix.base.LowerUnitriangularBandBuilder;
import matsu.num.matrix.base.LowerUnitriangularEntryReadableMatrix;
import matsu.num.matrix.base.MatrixDimension;
import matsu.num.matrix.base.exception.ProcessFailedException;

/**
 * 帯行列の修正Cholesky分解のヘルパ. <br>
 * A = LDL<sup>T</sup>. <br>
 * <br>
 * 数値安定性を得るため, 与えられた行列Aを最初に定数倍してから分解する. <br>
 * その定数倍は対角行列Dに押し付ける.
 *
 * @author Matsuura Y.
 * @version 15.0
 */
public final class ModifiedCholeskyBandFactorizationHelper {

    private final BandMatrixDimension bandMatrixDimension;
    private final double[] mxDiagonalEntry;
    private final double[] mxLowerEntry;
    private final double scale;

    private DiagonalMatrix mxD;
    private LowerUnitriangularEntryReadableMatrix mxL;

    /**
     * @param matrix 
     * @param relativeEpsilon 
     * @throws IllegalArgumentException 行列の有効要素数が大きすぎる場合(dim * lb > IntMax)
     * @throws ProcessFailedException 行列が特異の場合, ピボッティングが必要な場合, 極端な値を含み分解が完了できない場合
     */
    public ModifiedCholeskyBandFactorizationHelper(final BandMatrix matrix, double relativeEpsilon) {
        this.scale = matrix.entryNormMax();
        if (this.scale == 0.0) {
            throw new ProcessFailedException("行列が特異(零行列である)");
        }

        this.bandMatrixDimension = matrix.bandMatrixDimension();
        this.mxDiagonalEntry = diagonalOfMatrixToArray(matrix);
        this.mxLowerEntry = lowerOfMatrixToArray(matrix);
        this.factorize(relativeEpsilon);
        this.convertToEachMatrix();
    }

    public DiagonalMatrix getMxD() {
        return this.mxD;
    }

    public LowerUnitriangularEntryReadableMatrix getMxL() {
        return this.mxL;
    }

    /**
     * 対角成分を配列へ. 
     * 成分を配列に落とし込む際にスケールする.
     */
    private double[] diagonalOfMatrixToArray(final BandMatrix matrix) {

        final int thisDimension = this.bandMatrixDimension.dimension().rowAsIntValue();

        double[] outArray = new double[thisDimension];
        for (int j = 0; j < thisDimension; j++) {
            outArray[j] = matrix.valueAt(j, j) / this.scale;
        }
        return outArray;
    }

    /**
     * 狭義下三角成分を配列へ. 
     * 成分を配列に落とし込む際にスケールする.
     * 
     * @throws IllegalArgumentException 行列の有効要素数が大きすぎる場合(dim * lb > IntMax)
     */
    private double[] lowerOfMatrixToArray(final BandMatrix matrix) {

        final int thisDimension = this.bandMatrixDimension.dimension().rowAsIntValue();
        final int thisLowerBandWidth = this.bandMatrixDimension.lowerBandWidth();
        final long long_entrySize = (long) thisDimension * thisLowerBandWidth;
        if (long_entrySize > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("サイズが大きすぎる");
        }

        double[] outArray = new double[(int) long_entrySize];
        for (int i = 0; i < thisDimension; i++) {
            final int shift = i * thisLowerBandWidth;
            for (int j = 0, l = Math.min(thisLowerBandWidth, thisDimension - i - 1); j < l; j++) {
                int r = j + i + 1;
                int c = i;
                outArray[shift + j] = matrix.valueAt(r, c) / this.scale;
            }
        }
        return outArray;
    }

    /**
     * 行列を分解する.
     *
     * @throws ProcessFailedException 行列が特異の場合, あるいはピボッティングが必要な場合
     */
    private void factorize(double threshold) {
        final int thisDimension = this.bandMatrixDimension.dimension().rowAsIntValue();
        final int thisBandWidth = this.bandMatrixDimension.lowerBandWidth();
        final double[] thisMxDEntry = this.mxDiagonalEntry;
        final double[] thisMxLEntry = this.mxLowerEntry;

        final double[] thisMxUEntry_bk = new double[thisBandWidth];

        int in = -thisBandWidth;
        for (int i = 0; i < thisDimension; i++) {
            in += thisBandWidth;
            //正則性チェック
            final double d = thisMxDEntry[i];
            if (Math.abs(d) <= threshold) {
                throw new ProcessFailedException("行列が特異あるいはピボッティングが必要");
            }
            //Dの計算
            final double invD = 1 / d;
            //Lの計算
            for (int j = 0, l = Math.min(thisBandWidth, thisDimension - i - 1); j < l; j++) {
                thisMxUEntry_bk[j] = thisMxLEntry[in + j];
                thisMxLEntry[in + j] *= invD;
            }
            //前進消去
            for (int j = 0, l = Math.min(thisBandWidth, thisDimension - i - 1); j < l; j++) {
                thisMxDEntry[i + j + 1] -= thisMxLEntry[in + j] * thisMxUEntry_bk[j];
            }
            int kp1n = 0;
            for (int k = 0; k < thisBandWidth; k++) {
                kp1n += thisBandWidth;
                final double u_k = thisMxUEntry_bk[k];
                for (int j = 0, l = Math.min(thisBandWidth - k - 1, thisDimension - i - k - 1); j < l; j++) {
                    thisMxLEntry[in + kp1n + j] -= thisMxLEntry[in + k + j + 1] * u_k;
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
        final MatrixDimension thisMatrixDimension = this.bandMatrixDimension.dimension();
        final int thisDimension = this.bandMatrixDimension.dimension().rowAsIntValue();
        final int thisLowerBandWidth = this.bandMatrixDimension.lowerBandWidth();
        final BandMatrixDimension lowerBandMatrixDimension = BandMatrixDimension.of(
                thisMatrixDimension, thisLowerBandWidth, 0);

        final double[] thisDiagonalEntry = this.mxDiagonalEntry;
        final double[] thisLowerEntry = this.mxLowerEntry;

        DiagonalMatrix.Builder mxDBuilder = DiagonalMatrix.Builder.zeroBuilder(thisMatrixDimension);
        LowerUnitriangularBandBuilder mxLBuilder = LowerUnitriangularBandBuilder
                .unitBuilder(lowerBandMatrixDimension);

        try {
            //対角成分
            //スケールを対角成分へ反映 
            for (int i = 0; i < thisDimension; i++) {
                mxDBuilder.setValue(i, thisDiagonalEntry[i] * this.scale);
            }

            //狭義下三角成分
            for (int i = 0; i < thisDimension; i++) {
                final int shift = i * thisLowerBandWidth;
                for (int j = 0, l = Math.min(thisLowerBandWidth, thisDimension - i - 1); j < l; j++) {
                    int r = j + i + 1;
                    int c = i;
                    mxLBuilder.setValue(r, c, thisLowerEntry[shift + j]);
                }
            }
        } catch (IllegalArgumentException e) {
            //setValueで不正値が入り込む可能性がある
            throw new ProcessFailedException("行列の成分に極端な値が含まれる");
        }

        this.mxD = mxDBuilder.build();
        this.mxL = mxLBuilder.build();

        //スケールの関係で特異になるかもしれないので, 正則判定
        if (this.mxD.signOfDeterminant() == 0) {
            throw new ProcessFailedException("行列が特異あるいはピボッティングが必要");
        }
    }

}
