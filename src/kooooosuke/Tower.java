package kooooosuke;

import java.awt.Point;
import java.util.ArrayList;

public class Tower {
	int X, Y, A, C;// 立っているXY座標、強化された回数、種類
	boolean isCharged = true;
	int charge_time = 0;
	int charge_need_time = 0;
	int power;
	int range;
	Point me;
	ArrayList<Integer> insight_enemy_id = new ArrayList<Integer>();
	ArrayList<Enemy> enemy;

	public Tower(int x, int y, int a, int c) {
		X = x;
		Y = y;
		A = a;
		C = c;
		me = new Point(X, Y);
	}

	// 射程圏内にいる敵の更新
	public void rockON() {// int[][] F, int[][] original_F, ArrayList<Tower>
							// tower) {
		ArrayList<Integer> tmp_enemy_id_list = new ArrayList<Integer>();// 今フレームで射程内に入ったエネミーを一時的に入れておく。複数居る場合は出現時間順に追加
		// 敵との距離を算出
		for (Enemy en : enemy) {
			// try {
			if (en.alive && en.appeared
					&& range >= en.my_route.get(0).distance(me)) { // 射程内に入ったか出現した敵を射程内リストに登録
				if (insight_enemy_id.indexOf(en.id) == -1) {
					tmp_enemy_id_list.add(en.id);
				}
			} else if (insight_enemy_id.indexOf(en.id) != -1) { // 射程の外に出たか死んだ敵を射程内リストから取り除く
				insight_enemy_id.remove(insight_enemy_id.indexOf(en.id));
			}
			// デバッグ用
			// } catch (Exception e) {
			// System.out.println("original_F");
			// for (int[] F1 : original_F) {
			// for (int F2 : F1) {
			// System.out.print(F2 + " ");
			// }
			// System.out.println();
			// }
			// System.out.println();
			// System.out.println("F");
			// for (int[] F1 : F) {
			// for (int F2 : F1) {
			// System.out.print(F2 + " ");
			// }
			// System.out.println();
			// }
			// System.out.println("Tower");
			// for (Tower t : tower) {
			// System.out.println(t.X + " " + t.Y);
			// }
			// e.printStackTrace();
			// try {
			// Thread.sleep(99999);
			// } catch (InterruptedException e1) {
			// e1.printStackTrace();
			// }
			// }
		}
		// 一時リストの中から出現時間の早い順に本登録
		while (tmp_enemy_id_list.size() > 0) {
			int earliest_time = enemy.get(tmp_enemy_id_list.get(0)).T;
			int earliest_enemy_id = enemy.get(tmp_enemy_id_list.get(0)).id;
			;
			for (int enemy_id : tmp_enemy_id_list) {
				if (enemy.get(enemy_id).T < earliest_time) {// 同じ出現時間だったら標準入力で先に与えられた順
					earliest_time = enemy.get(enemy_id).T;
					earliest_enemy_id = enemy_id;
				}
			}
			insight_enemy_id.add(earliest_enemy_id);
			tmp_enemy_id_list.remove(tmp_enemy_id_list
					.indexOf(earliest_enemy_id));
		}
	}

	// 充電する。満タンになったらフラグ立てる。
	public void Charge() {
		if (!isCharged) {
			charge_time++;
		}
		if (charge_time == charge_need_time) {
			isCharged = true;
		}
	}

	// 発射する
	public void fire() {
		if (!isCharged || insight_enemy_id.size() == 0) {// まだ充電できていないか射程圏内に敵がいないときは打たない
			return;
		} else {// 一番始めに射程圏内に入った敵を打つ
			// ダメージを与える
			enemy.get(insight_enemy_id.get(0)).L -= power;
			// 特殊効果
			if (C == 2) {
				enemy.get(insight_enemy_id.get(0)).waiting_time -= (charge_time - 1) / 10;
			}
			isCharged = false;
			charge_time = 0;
		}
	}

}
