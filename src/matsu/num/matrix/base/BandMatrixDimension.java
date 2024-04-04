/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.4.4
 */
package matsu.num.matrix.base;

import java.util.Objects;

import matsu.num.matrix.base.validation.MatrixFormatMismatchException;

/**
 * <p>
 * 帯行列の構造 (次元(サイズ) + 帯幅) を扱う不変クラス. <br>
 * サイズ1以上の正方形であり, 下側, 上側帯幅とも0以上をとる. <br>
 * このクラスのインスタンスは, 行列次元, 上側下側帯幅の値に基づくequalityを有する.
 * </p>
 *
 * @author Matsuura Y.
 * @version 21.0
 */
public final class BandMatrixDimension {

    private final MatrixDimension matrixDimension;
    private final int lowerBandWidth;
    private final int upperBandWidth;
    private final Triangular triangular;

    //評価結果を使いまわすためのフィールド
    private final int hashCode;

    //循環参照が生じるため, 遅延初期化される
    //軽量オブジェクトのためロックを行わず,複数回の初期化を許す
    private volatile BandMatrixDimension transposedDimension;

    /**
     * 
     * @param dimension
     * @param lowerBandWidth
     * @param upperBandWidth
     * @throws IllegalArgumentException 次元が0以下の場合, 帯幅が不正値(負)が含まれる場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    private BandMatrixDimension(int dimension, int lowerBandWidth, int upperBandWidth) {
        this(MatrixDimension.square(dimension), lowerBandWidth, upperBandWidth);
    }

    /**
     * 
     * @param dimension
     * @param lowerBandWidth
     * @param upperBandWidth
     * @throws MatrixFormatMismatchException 正方でない場合
     * @throws IllegalArgumentException 帯幅が不正値(負)が含まれる場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    private BandMatrixDimension(MatrixDimension dimension, int lowerBandWidth, int upperBandWidth) {
        if (!dimension.isSquare()) {
            throw new MatrixFormatMismatchException(
                    String.format("正方形ではない行列サイズ:%s", dimension));
        }
        this.matrixDimension = dimension;
        if (lowerBandWidth < 0 || upperBandWidth < 0) {
            throw new IllegalArgumentException(
                    String.format(
                            "不正な帯幅:(lower, upper)=(%d, %d)", lowerBandWidth, upperBandWidth));
        }
        this.lowerBandWidth = lowerBandWidth;
        this.upperBandWidth = upperBandWidth;
        this.triangular = Triangular.triangular(lowerBandWidth, upperBandWidth);

        this.hashCode = this.calcHashCode();
    }

    /**
     * 行列次元を返す.
     *
     * @return 行列次元
     */
    public MatrixDimension dimension() {
        return matrixDimension;
    }

    /**
     * 下側帯幅を {@code int} 値で返す.
     *
     * @return 下側帯幅
     */
    public int lowerBandWidth() {
        return lowerBandWidth;
    }

    /**
     * 上側帯幅を {@code int} 値で返す.
     *
     * @return 上側帯幅
     */
    public int upperBandWidth() {
        return upperBandWidth;
    }

    /**
     * 帯構造が対称かどうかを判定.
     *
     * @return 帯構造が対称であればtrue
     */
    public boolean isSymmetric() {
        return lowerBandWidth == upperBandWidth;
    }

    /**
     * 帯構造が下三角行列であるか (上側帯幅が0かどうか) を判定する.
     *
     * @return 帯構造が下三角行列ならばtrue
     */
    public boolean isLowerTriangular() {
        return triangular == Triangular.LOWER || triangular == Triangular.DIAGONAL;
    }

    /**
     * 帯構造が上三角行列であるか (下側帯幅が0かどうか) を判定する.
     *
     * @return 帯構造が上三角行列ならばtrue
     */
    public boolean isUpperTriangular() {
        return triangular == Triangular.UPPER || triangular == Triangular.DIAGONAL;
    }

    /**
     * 帯構造が対角行列であるか (帯幅が両側とも0かどうか) を判定する.
     *
     * @return 帯構造が対角行列ならばtrue
     */
    public boolean isDiagonal() {
        return triangular == Triangular.DIAGONAL;
    }

    /**
     * <p>
     * 他オブジェクトとの等価性を判定する. <br>
     * 等価性の基準はクラス説明のとおりである.
     * </p>
     * 
     * @param obj 比較対象
     * @return 自身とobjが等価の場合はtrue
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BandMatrixDimension)) {
            return false;
        }

        BandMatrixDimension target = (BandMatrixDimension) obj;

        return this.matrixDimension.equals(target.matrixDimension)
                && this.lowerBandWidth == target.lowerBandWidth
                && this.upperBandWidth == target.upperBandWidth;
    }

    /**
     * ハッシュコードを返す.
     * 
     * @return ハッシュコード
     */
    @Override
    public int hashCode() {
        return this.hashCode;
    }

    /**
     * ハッシュコードを計算する.
     * 
     * @return ハッシュコード
     */
    private int calcHashCode() {
        int result = Objects.hashCode(this.matrixDimension);
        result = 31 * result + Integer.hashCode(this.lowerBandWidth);
        result = 31 * result + Integer.hashCode(this.upperBandWidth);
        return result;
    }

    /**
     * <p>
     * このオブジェクトの文字列説明表現を返す.
     * </p>
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code (dim(%dimension), band(%l, %u))}
     * </p>
     * 
     * @return 説明表現
     */
    @Override
    public String toString() {
        return String.format(
                "[dim%s, band(%s, %s)]",
                this.matrixDimension, this.lowerBandWidth, this.upperBandWidth);
    }

    /**
     * このインスタンスの次元の転置の次元を返す.
     *
     * @return thisの転置次元
     */
    public BandMatrixDimension transpose() {
        BandMatrixDimension out = this.transposedDimension;
        if (Objects.nonNull(out)) {
            return out;
        }

        if (this.isSymmetric()) {
            this.transposedDimension = this;
            return this;
        }

        //複数回の初期化を許すため,オブジェクトのロックを行わない
        //matrixDimensionは正方形なので,そのまま使える.
        out = BandMatrixDimension.of(this.matrixDimension, this.upperBandWidth, this.lowerBandWidth);
        this.transposedDimension = out;
        out.transposedDimension = this;
        return out;

    }

    /**
     * 正方形の帯行列の構造オブジェクトの作成. <br>
     * 帯幅が0のとき, 下三角, 上三角, 対角行列を表す.
     *
     * @param dimension 行サイズ = 列サイズ
     * @param lowerBandWidth 下側帯幅
     * @param upperBandWidth 上側帯幅
     * @return 帯行列構造オブジェクト
     * @throws IllegalArgumentException 行列サイズが1未満, もしくは帯幅が0未満である場合
     */
    public static BandMatrixDimension of(int dimension, int lowerBandWidth, int upperBandWidth) {
        return new BandMatrixDimension(dimension, lowerBandWidth, upperBandWidth);
    }

    /**
     * 正方形の帯行列の構造オブジェクトの作成. <br>
     * 帯幅が0のとき, 下三角, 上三角, 対角行列を表す.
     *
     * @param dimension 行列次元(サイズ)
     * @param lowerBandWidth 下側帯幅
     * @param upperBandWidth 上側帯幅
     * @return 帯行列構造オブジェクト
     * @throws MatrixFormatMismatchException 正方行列でない場合
     * @throws IllegalArgumentException 帯幅が0未満である場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static BandMatrixDimension of(MatrixDimension dimension, int lowerBandWidth, int upperBandWidth) {
        return new BandMatrixDimension(dimension, lowerBandWidth, upperBandWidth);
    }

    /**
     * 対称な帯行列の構造オブジェクトの作成. <br>
     * 帯幅が0のとき, 対角行列を表す.
     *
     * @param dimension 行サイズ = 列サイズ
     * @param bandWidth 下側帯幅 = 上側帯幅
     * @return 対称な帯行列構造オブジェクト
     * @throws IllegalArgumentException 行列サイズが1未満, もしくは帯幅が0未満である場合
     */
    public static BandMatrixDimension symmetric(int dimension, int bandWidth) {
        return new BandMatrixDimension(dimension, bandWidth, bandWidth);
    }

    /**
     * 対称な帯行列の構造オブジェクトの作成. <br>
     * 帯幅が0のとき, 対角行列を表す.
     *
     * @param dimension 行列次元(サイズ)
     * @param bandWidth 下側帯幅 = 上側帯幅
     * @return 対称な帯行列構造オブジェクト
     * @throws MatrixFormatMismatchException 正方行列でない場合
     * @throws IllegalArgumentException もしくは帯幅が0未満である場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static BandMatrixDimension symmetric(MatrixDimension dimension, int bandWidth) {
        return new BandMatrixDimension(dimension, bandWidth, bandWidth);
    }

    /**
     * 帯行列が上三角か下三角かを表す.
     */
    private static enum Triangular {
        BOTH, LOWER, UPPER, DIAGONAL;

        public static Triangular triangular(int lowerBandWidth, int upperBandWidth) {
            if (lowerBandWidth == 0) {
                if (upperBandWidth == 0) {
                    return DIAGONAL;
                }
                return UPPER;
            }
            if (upperBandWidth == 0) {
                return LOWER;
            }
            return BOTH;
        }
    }
}
