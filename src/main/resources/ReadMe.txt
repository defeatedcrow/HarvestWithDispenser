﻿#### Harvest With Dispenser for Minecraft 1.16.4 ####


# 開発Ver&Mod

・Minecraft1.16.4
・MincraftForge 1.16.4-14.35.1.4+


# 導入方法

MinecraftForgeを導入後、modsフォルダにjarファイルを入れてください。
サーバーサイドのみの導入で多分動作します。


# 概要

ディスペンサーにハサミを入れて起動すると、ディスペンサーの向いている方向の作物を刈り取ってくれるModです。
使用するアイテムや刈り取り範囲はコンフィグファイルで編集できます。

ついでに骨粉のバニラ挙動も弱すぎるので、範囲を5x5に拡大しました。


# 使い方

ハサミをディスペンサーに入れ、RS信号を送れば動きます。
一回動かすたびにハサミの耐久値を消費します。

収穫した作物は、ディスペンサーに隣接しているインベントリに送り込まれるか、
送れない場合はディスペンサーの手前にまとめてドロップします。


・使用可能アイテム
　デフォルトではバニラハサミ（収穫範囲5x5、高さ2）のみ。

　※設定用Jsonファイル（shears_item.json）に追記すれば、ハサミとして使えるアイテムを追加できます。
　　凡例としてハサミが登録されているので、同様の書式で記述してください。
　　アイテム名の指定は"modid:アイテム内部名"のように記述します。
　　範囲は、半径を表します。ハサミは2です。（1では3x3、2では5x5、3では7x7…と広がります）

・収穫できる作物
　バニラの作物（畑作系、かぼちゃ、スイカ、サトウキビ、サボテン、竹）
　IGrowable実装のブロックであれば、他Mod植物も多分使えます

・連携要素は未実装です


# 使用上の注意

・ディスペンサーの使用効果は、アイテムに対して1つしか登録できません。
　ですので、他にハサミを使った効果を登録するModがあると競合します。仕様上、回避はできません。

・IGrowableに成長完了判定がない関係で、骨粉が使えない状態になったら成長済みと判定しています。
　成長途中の状態でも、骨粉使用判定でfalseが返されると収穫してしまいますので、ご注意ください。
　また、常時trueしか返ってこないようなもの（カカオなど）も刈り取り不能ですのでご注意ください。


# 更新履歴

○3.0.1 ベリーの茂みに対応できない問題を修正

○B3.0.0(beta) MC1.16.4に対応

○B2.0.0(beta) MC1.15.2に対応

○1.0.5 AgriCraft(2.12.0_a6)に対応

○1.0.4 HaCv3に対応

○1.0.3 jsonファイルの読み込みタイミングを修正(ModIDによるMod読み込み順が遅いmodのアイテムを適用できていなかった）

○1.0.2 適用アイテムをjsonファイルで削除できない問題を修正

○v1.0.1 HaC v2.4.6に対応

○v1.0.0 初回公開

