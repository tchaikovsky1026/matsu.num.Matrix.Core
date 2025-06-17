/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2025.6.17
 */
package matsu.num.matrix.core.qr;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import matsu.num.matrix.core.BandMatrix;
import matsu.num.matrix.core.BandMatrixDimension;
import matsu.num.matrix.core.DiagonalMatrix;
import matsu.num.matrix.core.HouseholderMatrix;
import matsu.num.matrix.core.LowerUnitriangular;
import matsu.num.matrix.core.LowerUnitriangularBandMatrix;
import matsu.num.matrix.core.OrthogonalMatrix;
import matsu.num.matrix.core.UnitMatrix;
import matsu.num.matrix.core.VectorDimension;
import matsu.num.matrix.core.common.ArraysUtil;
import matsu.num.matrix.core.sparse.HouseholderMatrixFactoryForSparse;
import matsu.num.matrix.core.sparse.LocalSparseVector;

/**
 * 正方帯行列の Householder 変換によるQR分解のヘルパ.
 * 
 * @author Matsuura Y.
 */
final class HouseholderQRBandHelper {

    private final BandMatrix target;
    private final double epsilon;

    private final int dimensionValue;
    private final int lowerBandWidth;
    private final int upperBandWidth;

    //上三角行列の片側帯幅
    //QR分解の性質により, lower+upperの幅になる
    private final int extendedBandWidth;

    private final double scale;

    //スケーリング済みの成分値,ただし上側は拡張して0埋めする
    /*
     * 行列の各要素は,
     * 対角成分, 狭義下三角成分, 狭義上三角成分,
     * に分けて, それぞれ1次元配列として扱う.
     * 
     * 例えば4*4行列で下側帯幅2, 上側帯幅4の場合:
     * d[0] u[0] u[1] u[2] (u[3])}
     * l[0] d[1] u[4] u[5] (u[6] u[7])
     * l[1] l[2] d[2] u[8] (u[9] u[10] u[11])
     * ---- l[3] l[4] d[3] (u[12] u[13] u[14] u[15])
     * (--- ---- l[5] l[6])
     * (--- ---- ---- l[7])
     * と格納される.
     */
    private final double[] diagEntry;
    private final double[] lowerBandEntry;
    private final double[] upperBandEntry;

    private final UnitMatrix baseMatrix;
    private final List<HouseholderMatrix> hhList;

    private final DiagonalMatrix mxD;
    private final LowerUnitriangular mxRt;
    private final OrthogonalMatrix mxQ;

    /**
     * 唯一のコンストラクタ.
     * 
     * <p>
     * 引数はバリデーションされていない. <br>
     * epsilonは正でなければならない.
     * </p>
     * 
     * @throws ProcessFailedException 数値的にフルランクでない場合
     */
    HouseholderQRBandHelper(BandMatrix target, double epsilon)
            throws ProcessFailedException {
        super();

        this.target = target;
        this.epsilon = epsilon;

        BandMatrixDimension bandMatrixDimension = target.bandMatrixDimension();
        this.dimensionValue = bandMatrixDimension.dimension().rowAsIntValue();
        this.lowerBandWidth = Math.min(bandMatrixDimension.lowerBandWidth(), this.dimensionValue - 1);
        this.upperBandWidth = Math.min(bandMatrixDimension.upperBandWidth(), this.dimensionValue - 1);
        this.extendedBandWidth = Math.min(this.lowerBandWidth + this.upperBandWidth, this.dimensionValue - 1);

        //ターゲット行列のスケール(成分を正規化して分解を行うための前処理)
        this.scale = this.target.entryNormMax();
        if (scale == 0d) {
            throw new ProcessFailedException("target is zero matrix");
        }

        /* 初期値の用意 */
        this.diagEntry = this.calcDiagEntryWithScaling();
        this.lowerBandEntry = this.calcLowerBandEntryWithScaling();
        this.upperBandEntry = this.calcUpperBandEntryWithScaling();
        this.baseMatrix = UnitMatrix.matrixOf(target.matrixDimension());
        this.hhList = new ArrayList<>(target.matrixDimension().rowAsIntValue());

        this.factorize();

        this.mxD = this.convertToMxD();
        this.mxRt = this.convertToMxRt();
        this.mxQ = this.convertToMxQ();
    }

    BandMatrix target() {
        return this.target;
    }

    DiagonalMatrix mxD() {
        return this.mxD;
    }

    OrthogonalMatrix mxQ() {
        return this.mxQ;
    }

    LowerUnitriangular mxRt() {
        return this.mxRt;
    }

    private double[] calcDiagEntryWithScaling() {
        double[] out = new double[this.dimensionValue];
        for (int i = 0; i < this.dimensionValue; i++) {
            out[i] = this.target.valueAt(i, i) / this.scale;
        }
        return out;
    }

    private double[] calcLowerBandEntryWithScaling() {
        double[] out = new double[this.dimensionValue * this.lowerBandWidth];
        for (int k = 0; k < this.dimensionValue; k++) {
            int k_j = this.lowerBandWidth * k;
            for (int j = 0, len_j = Math.min(this.lowerBandWidth, this.dimensionValue - k - 1);
                    j < len_j; j++) {
                out[k_j + j] = this.target.valueAt(j + k + 1, k) / this.scale;
            }
        }
        return out;
    }

    private double[] calcUpperBandEntryWithScaling() {
        double[] out = new double[this.dimensionValue * this.extendedBandWidth];
        for (int j = 0; j < this.dimensionValue; j++) {
            int j_k = this.extendedBandWidth * j;
            for (int k = 0, len_j = Math.min(this.upperBandWidth, this.dimensionValue - j - 1);
                    k < len_j; k++) {
                out[j_k + k] = this.target.valueAt(j, j + k + 1) / this.scale;
            }
        }
        return out;
    }

    /**
     * QR分解の実行.
     * 
     * 事前条件は, hhListが空, entry配列がスケールされたsrc状態であること.
     * 事後条件は, hhListはmatrix(Q), entry配列はスケールされた状態のmatrix(RD)になる.
     * 
     * @throws ProcessFailedException 数値的にフルランクでない場合
     */
    private void factorize() throws ProcessFailedException {

        final int dimensionValue = this.dimensionValue;
        final int lowerBandWidth = this.lowerBandWidth;
        final int extendedBandWidth = this.extendedBandWidth;

        final double[] diagEntry = this.diagEntry;
        final double[] lowerBandEntry = this.lowerBandEntry;
        final double[] upperBandEntry = this.upperBandEntry;

        final VectorDimension columnVectorDimension =
                this.target.matrixDimension().rightOperableVectorDimension();

        for (int i = 0; i < dimensionValue; i++) {

            /* 鏡映変換の作成 */
            int hhDimension = Math.min(lowerBandWidth + 1, dimensionValue - i);
            double[] vecX = new double[hhDimension];
            vecX[0] = diagEntry[i];
            System.arraycopy(lowerBandEntry, i * lowerBandWidth, vecX, 1, hhDimension - 1);

            //ここで例外が発生する場合がある
            double[] hh = this.createHhVector(i, vecX);

            //hhがnullの場合, 鏡映変換が必要ないことを表す
            if (Objects.isNull(hh)) {
                continue;
            }

            //鏡映変換をQに反映
            this.hhList.add(
                    HouseholderMatrixFactoryForSparse.from(
                            LocalSparseVector.of(columnVectorDimension, i, hh)));

            /* 鏡映変換により行列を更新する */
            /*
             * u^T Aの計算.
             * u^T Aのフィルインは,
             * i <= j <= i + bl + bu
             */
            int uADimension = Math.min(extendedBandWidth + 1, dimensionValue - i);
            double[] uA = new double[uADimension];
            for (int j = 0; j < hhDimension; j++) {
                uA[j] += hh[j] * diagEntry[i + j];
            }
            for (int k = 0; k < hhDimension; k++) {
                double uA_k = uA[k];
                int i_p_k_t_bl = (i + k) * lowerBandWidth;

                for (int j = 0, len_j = hhDimension - k - 1; j < len_j; j++) {
                    uA_k += hh[j + k + 1] * lowerBandEntry[i_p_k_t_bl + j];
                }

                uA[k] = uA_k;
            }
            for (int j = 0; j < hhDimension; j++) {
                double hh_j = hh[j];
                int i_p_j_t_be = (i + j) * extendedBandWidth;

                for (int k = 0, len_k = uADimension - j - 1; k < len_k; k++) {
                    uA[j + k + 1] += hh_j * upperBandEntry[i_p_j_t_be + k];
                }
            }

            /*
             * Aの書き換え.
             * A = A - 2u(u^T A)
             */
            for (int j = 0; j < hhDimension; j++) {
                diagEntry[i + j] -= 2 * hh[j] * uA[j];
            }
            for (int k = 0; k < hhDimension; k++) {
                double uA_k = uA[k];
                int i_p_k_t_bl = (i + k) * lowerBandWidth;

                for (int j = 0, len_j = hhDimension - k - 1; j < len_j; j++) {
                    lowerBandEntry[i_p_k_t_bl + j] -= 2 * hh[j + k + 1] * uA_k;
                }
            }
            for (int j = 0; j < hhDimension; j++) {
                double hh_j = hh[j];
                int i_p_j_t_be = (i + j) * extendedBandWidth;

                for (int k = 0, len_k = uADimension - j - 1; k < len_k; k++) {
                    upperBandEntry[i_p_j_t_be + k] -= 2 * hh_j * uA[j + k + 1];
                }
            }
        }
    }

    /**
     * [0, ..., 0, a, *, ..., *, 0, ..., 0]
     * を
     * [0, ..., 0, a', 0, ..., 0]
     * に変換する鏡映変換を得る (a' > 0). <br>
     * 変換が必要ない場合はnullを返す.
     * 
     * @param dimension 列ベクトルの次元(srcの行サイズに相当)
     * @param i 鏡映変換のスタート位置(aの位置)
     * @param vecX_i i以降の0でない成分([a, *, ..., *])
     * @return Householder変換の鏡映ベクトル, 変換が必要ない場合はnullを返す
     * @throws ProcessFailedException Hhベクトルが小さすぎる,特異
     */
    private double[] createHhVector(int i, double[] vecX_i) throws ProcessFailedException {

        final int hhDimension = vecX_i.length;
        vecX_i = vecX_i.clone();

        //途中の行列のうち, 第i成分以降を使って鏡映変換を考える
        double absX = ArraysUtil.norm2(vecX_i);
        if (absX < epsilon) {
            throw new ProcessFailedException("rank deficient");
        }
        //vecXを規格化して渡す
        for (int j = 0; j < hhDimension; j++) {
            vecX_i[j] /= absX;
        }
        double[] hh = this.calcVecU(vecX_i);
        if (Objects.isNull(hh)) {
            return null;
        }

        //hhを規格化して返す
        final double absU = ArraysUtil.norm2(hh);
        if (absU == 0d) {
            throw new AssertionError("Bug: norm 0");
        }
        for (int j = 0; j < hhDimension; j++) {
            hh[j] /= absU;
        }
        return hh;
    }

    /**
     * 入力されたvecX(大きさ1)を[1,0,...,0]に移す鏡映変換を計算する. <br>
     * ただし, 戻り値となる鏡映変換ベクトルは規格化されていないかもしれない.
     * 
     * @param vecX 入力, 2-ノルムが1でなければならない
     * @return 変換が必要ない場合はnullを返す
     */
    private double[] calcVecU(double[] vecX) {

        double[] hh = vecX.clone();

        //第0成分が0以下ならば, (x-y)の丸め誤差は無視できる
        if (hh[0] <= 0d) {
            hh[0] -= 1.0;
            return hh;
        }
        /*
         * 第0成分が正かつ, 第1成分以降が適度に小さい場合,
         * (x-y)の第0成分の持つ丸め誤差が|x-y|とコンパラになる危険性がある.
         * このフォローを以下のように行う.
         * 
         * 第1成分以降は加減算を行わないので相対精度は高い.
         * 第0成分は
         * x0 - sqrt(x0^2 + x_1^2 + ...)
         * = -(x_1^2 + ...) / (x0 + sqrt(x0^2 + x_1^2 + ...))
         * とすれば, 高い相対精度の値が得られる.
         */
        //uの第0成分のみを0(xの第0成分を0)にして,xの第0成分以外の二乗和を計算する
        double x0 = hh[0];
        hh[0] = 0;
        double x_square_without_x0 = ArraysUtil.norm2Square(hh);

        //uの第0成分を計算し, 再代入する
        hh[0] = -x_square_without_x0 / (1d + x0);

        //規格化して戻す, ノルム0ならx=yであり変換不要なのでnull
        if (ArraysUtil.normMax(hh) == 0d) {
            return null;
        }
        return hh;
    }

    /**
     * スケールを戻してmxDを構成する.
     * 
     * @return mxD
     * @throws ProcessFailedException mxDが特異
     */
    private DiagonalMatrix convertToMxD() throws ProcessFailedException {
        final int dimensionValue = this.dimensionValue;

        final double[] diagEntry = this.diagEntry;

        final double scale = this.scale;

        //スケーリングはDの成分に反映される
        final DiagonalMatrix.Builder mxDBuilder = DiagonalMatrix.Builder
                .zeroBuilder(this.target.matrixDimension());
        for (int i = 0; i < dimensionValue; i++) {
            mxDBuilder.setValue(i, diagEntry[i] * scale);
        }

        DiagonalMatrix out = mxDBuilder.build();
        //逆行列が存在するかを検証する
        if (out.signOfDeterminant() == 0) {
            throw new ProcessFailedException("rank deficient");
        }

        return out;
    }

    /**
     * mxRtを構成する.
     * 列ベクトル配列はmatrix(RD)なので, 各成分を対角成分で除する.
     * 
     * @return mxRt
     */
    private LowerUnitriangularBandMatrix convertToMxRt() {
        final int dimensionValue = this.dimensionValue;
        final int extendedBandWidth = this.extendedBandWidth;

        final double[] diagEntry = this.diagEntry;
        final double[] upperBandEntry = this.upperBandEntry;

        final LowerUnitriangularBandMatrix.Builder mxRtBuilder =
                LowerUnitriangularBandMatrix.Builder
                        .unit(BandMatrixDimension.of(dimensionValue, extendedBandWidth, 0));
        for (int j = 0; j < dimensionValue; j++) {
            for (int k = 0, len_k = Math.min(extendedBandWidth, dimensionValue - j - 1);
                    k < len_k; k++) {
                mxRtBuilder.setValue(
                        j + k + 1, j,
                        upperBandEntry[j * extendedBandWidth + k] / diagEntry[j + k + 1]);
            }
        }
        return mxRtBuilder.build();
    }

    /**
     * mxQを構成する.
     * 
     * @return mxQ
     */
    private OrthogonalMatrix convertToMxQ() {
        return OrthogonalMatrix.multiply(
                baseMatrix,
                hhList.toArray(OrthogonalMatrix[]::new));
    }
}
