# matsu.num.Matrix.Core

![Version](https://img.shields.io/badge/version-28.5.0-blue)
![Java](https://img.shields.io/badge/Java-17%2B-brightgreen)
![License](https://img.shields.io/badge/license-MIT-green)
[![Javadoc](https://img.shields.io/badge/docs-Javadoc-blue)](https://tchaikovsky1026.github.io/matsu.num.Matrix.Core/)
[![SemVer](https://img.shields.io/badge/SemVer-2.0.0-blue)](https://semver.org/spec/v2.0.0.html)

`matsu.num.Matrix.Core` は Java 言語向けの数値計算における, 線形代数の基本コンポーネントを扱うライブラリである.  
現在のリリースバージョンは `28.5.0` であり, Java 17 に準拠する.  
このバージョンにおいて, 次がサポートされている.

- ベクトル  &middot; 行列コンポーネント
    - ベクトルクラスとその演算
    - 行列コンポーネントと行列ベクトル積
    - 様々な行列
        - 成分にアクセス可能な行列
        - 対称行列
        - 帯行列
        - 単位下三角行列
        - 対角行列
        - 直交行列
        - 置換行列
        - 符号行列
        - 単位行列
        - 零行列
        - ブロック行列
        - Householder 行列
    - スパースベクトル型と関連するコンポーネント
        - スパースベクトルを使った Householder 行列
- 行列分解による線形連立方程式の解法
    - 帯行列向け LU 分解
    - 部分ピボッティング付き LU 分解
    - Cholesky 分解
    - 帯行列向け Cholesky 分解
    - 帯行列向け修正 Cholesky 分解
    - 対称部分ピボッティング付き修正 Cholesky 分解
- 行列分解による線形連立方程式の最小二乗最小ノルム解の求解法
    - 列フルランク行列の QR 分解
    - フルランクの帯行列向け QR 分解

## Documentation
- API Documentation (Javadoc):  
https://tchaikovsky1026.github.io/matsu.num.Matrix.Core/  
This site contains the public API reference generated from source code.  
The documentation is generated from the latest release.

## History
更新履歴は history.txt を参照のこと.

## License

This project is licensed under the MIT License, see the LICENSE.txt file for details.
