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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import matsu.num.matrix.core.BandMatrix;
import matsu.num.matrix.core.DiagonalMatrix;
import matsu.num.matrix.core.EntryReadableMatrix;
import matsu.num.matrix.core.HouseholderMatrix;
import matsu.num.matrix.core.LowerUnitriangular;
import matsu.num.matrix.core.LowerUnitriangularMatrix;
import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.OrthogonalMatrix;
import matsu.num.matrix.core.UnitMatrix;
import matsu.num.matrix.core.Vector;
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

    private final double scale;
    private final Vector[] columnVectors;

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

        //ターゲット行列のスケール(成分を正規化して分解を行うための前処理)
        this.scale = this.target.entryNormMax();
        if (scale == 0d) {
            throw new ProcessFailedException("target is zero matrix");
        }

        /* 初期値の用意 */
        this.columnVectors = this.calcColumnVectorsWithScaling();
        MatrixDimension mxQDimension = target.matrixDimension().leftSquareDimension();
        this.baseMatrix = UnitMatrix.matrixOf(mxQDimension);
        this.hhList = new ArrayList<>(mxQDimension.rowAsIntValue());

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

    /**
     * scaleフィールドを参照し, 行列をスケール化した後に列ベクトルにする.
     */
    private Vector[] calcColumnVectorsWithScaling() {
        final EntryReadableMatrix matrix = this.target;
        final MatrixDimension matrixDimension = matrix.matrixDimension();
        final VectorDimension columnVectorDimension = matrixDimension.leftOperableVectorDimension();
        final int columnNumber = matrixDimension.columnAsIntValue();

        final Vector[] columnVectors = new Vector[columnNumber];

        /* 行列の成分をスケールする. */
        for (int j = 0; j < columnNumber; j++) {
            double[] entry = new double[columnVectorDimension.intValue()];
            for (int i = 0; i < entry.length; i++) {
                entry[i] = matrix.valueAt(i, j) / this.scale;
            }
            Vector.Builder builder = Vector.Builder.zeroBuilder(columnVectorDimension);
            builder.setEntryValue(entry);
            columnVectors[j] = builder.build();
        }

        return columnVectors;
    }

    /**
     * QR分解の実行.
     * 
     * 事前条件は, hhListが空, 列ベクトル配列がスケールされたsrc状態であること.
     * 事後条件は, ｈｈListはmatrix(Q), 列ベクトル配列はスケールされた状態のmatrix(RD)になる.
     * 
     * @throws ProcessFailedException 数値的にフルランクでない場合
     */
    private void factorize() throws ProcessFailedException {

        Vector[] columnVectors = this.columnVectors;

        final VectorDimension columnVectorDimension = this.target.matrixDimension().leftOperableVectorDimension();
        final int columnNumber = columnVectors.length;
        for (int i = 0; i < columnNumber; i++) {

            //途中の行列のうち, 第i成分以降を使って鏡映変換を考える
            double[] vecX_i = Arrays.copyOfRange(
                    columnVectors[i].entryAsArray(), i, columnVectorDimension.intValue());

            /* Householder行列の作成 */
            //ここで例外が発生する可能性がある
            HouseholderMatrix mxH = this.createMxH(columnVectorDimension, i, vecX_i);
            if (Objects.isNull(mxH)) {
                //nullが返った場合, 鏡映変換が必要ないことを表す
                continue;
            }

            /* Qの更新 */
            this.hhList.add(mxH);

            /*
             * 鏡映変換により行列を更新.
             * 第(i-1)列以前は変換不要.
             */
            for (int k = i; k < columnNumber; k++) {
                columnVectors[k] = mxH.operate(columnVectors[k]);
            }
        }
    }

    /**
     * [0, ..., 0, a, *, ..., *]
     * を
     * [0, ..., 0, a', 0, ..., 0]
     * に変換する鏡映変換を得る (a' > 0). <br>
     * 変換が必要ない場合はnullを返す.
     * 
     * 
     * @param dimension 列ベクトルの次元(srcの行サイズに相当)
     * @param i 鏡映変換のスタート位置(aの位置)
     * @param vecX_i i以降の成分([a, *, ..., *])
     * @return Householder変換, 変換が必要ない場合はnullを返す
     * @throws ProcessFailedException vecXが0に近い場合
     *             (すなわち元の行列がランク落ちしている場合)
     */
    private HouseholderMatrix createMxH(VectorDimension dimension, int i, double[] vecX_i)
            throws ProcessFailedException {

        double epsilon = this.epsilon;

        int l = vecX_i.length;
        //コピーに対して処理を行う
        vecX_i = vecX_i.clone();

        //途中の行列のうち, 第i成分以降を使って鏡映変換を考える
        double absX = ArraysUtil.norm2(vecX_i);
        if (absX < epsilon) {
            throw new ProcessFailedException("rank deficient");
        }
        //xを規格化してから渡す
        for (int j = 0; j < l; j++) {
            vecX_i[j] /= absX;
        }
        double[] vectorHh_i = this.calcVecU(vecX_i);
        if (Objects.isNull(vectorHh_i)) {
            return null;
        }

        return HouseholderMatrixFactoryForSparse.from(
                LocalSparseVector.of(dimension, i, vectorHh_i));
    }

    /**
     * 入力されたvecX(大きさ1)を[1,0,...,0]に移す鏡映変換を計算する. <br>
     * ただし, 戻り値となる鏡映変換ベクトルは規格化されていないかもしれない.
     * 
     * @param vecX 入力, 2-ノルムが1でなければならない
     * @return 変換が必要ない場合はnullを返す
     */
    private double[] calcVecU(double[] vecX) {
        double[] vectorHh_i = vecX.clone();

        //第0成分が0以下ならば, (x-y)の丸め誤差は無視できる
        if (vectorHh_i[0] <= 0d) {
            vectorHh_i[0] -= 1.0;
            return vectorHh_i;
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
        double x0 = vectorHh_i[0];
        vectorHh_i[0] = 0;
        double x_square_without_x0 = ArraysUtil.norm2Square(vectorHh_i);

        //uの第0成分を計算し, 再代入する
        vectorHh_i[0] = -x_square_without_x0 / (1d + x0);

        if (ArraysUtil.normMax(vectorHh_i) == 0d) {
            //x=yなので変換不要
            return null;
        }
        return vectorHh_i;
    }

    /**
     * スケールを戻してmxDを構成する.
     * 
     * @return mxD
     * @throws ProcessFailedException
     */
    private DiagonalMatrix convertToMxD() throws ProcessFailedException {

        final Vector[] columnVectors = this.columnVectors;

        //スケーリングはDの成分に反映される
        final int columnNumber = this.columnVectors.length;
        final DiagonalMatrix.Builder mxDBuilder = DiagonalMatrix.Builder
                .zeroBuilder(this.target.matrixDimension().rightSquareDimension());
        for (int k = 0; k < columnNumber; k++) {
            mxDBuilder.setValue(k, columnVectors[k].valueAt(k) * this.scale);
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
    private LowerUnitriangularMatrix convertToMxRt() {
        final Vector[] columnVectors = this.columnVectors;

        final int columnNumber = this.columnVectors.length;
        final LowerUnitriangularMatrix.Builder mxRtBuilder =
                LowerUnitriangularMatrix.Builder
                        .unit(this.target.matrixDimension().rightSquareDimension());
        for (int k = 0; k < columnNumber; k++) {
            Vector vector_k = columnVectors[k];
            double diag = vector_k.valueAt(k);
            for (int j = 0; j < k; j++) {
                mxRtBuilder.setValue(k, j, vector_k.valueAt(j) / diag);
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
