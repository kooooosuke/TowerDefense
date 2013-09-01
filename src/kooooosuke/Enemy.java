package kooooosuke;

import java.awt.Point;
import java.util.ArrayList;

public class Enemy {
	int life;
	int X, Y, T, L, S;// 現れるXY座標、時間、ライフ、スピード
	int id;
	int waiting_time = 0;
	boolean appeared = false;
	boolean alive = true;
	ArrayList<Point> my_route = new ArrayList<Point>();// 通る経路。indexの0が現在地。通り過ぎたPointはremoveしていく
	ArrayList<EnemySpot> enemy_spot;

	public Enemy(int i, int x, int y, int t, int l, int s) {
		id = i;
		X = x;
		Y = y;
		T = t;
		L = l;
		life = l;// 2回目以降のシミュレーションのために、初期ライフを記憶しておく
		S = s;
		my_route.add(new Point(X, Y));
		// enemy_spot = es;
	}

	// 時間を進める
	public void waiting() {
		waiting_time++;
	}

	// 出現判定
	public void appear() {
		if (!alive || appeared) {
			return;
		}
		if (waiting_time == T) {
			appeared = true;
			waiting_time = 0;
			// 通る最短ルートのコピー
			for (EnemySpot es : enemy_spot) {
				if (es.X == X && es.Y == Y) {
					my_route.clear();
					for (Point r : es.route) {
						my_route.add(new Point(r.x, r.y));
					}
				}
			}
		}
	}

	// 移動判定
	public void move() {
		if (!alive) {
			return;
		}
		try {
			if (appeared && waiting_time >= S) {
				if (my_route.get(0).distance(my_route.get(1)) > 1.2
						&& waiting_time < S * 14 / 10) {// ななめ判定。本当は1.0でいいはずだけど誤差があったら怖いので念のため大きめに
					return;
				}
				// 移動する時間になった
				waiting_time = 0;
				my_route.remove(0);
			}
		} catch (Exception e) {
			System.out.println("$$$");
			for (EnemySpot es : enemy_spot) {
				System.out.println("es.route.size() " + es.route.size());
			}
			System.out.println(e);
		}
	}

	// 生死判定
	public int checkLifeAndReached() {
		if (alive && L <= 0) {
			alive = false;
		}
		if (alive && my_route.size() == 1) {// 経路を全てたどり終えて防衛マスに到達した
			return id;// 生き残ったエネミーidを報告
		} else {
			return -1;
		}
	}

}
