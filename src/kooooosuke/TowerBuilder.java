package kooooosuke;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class TowerBuilder {
	Stage stage;
	Level level;
	int stage_num;
	int level_num;
	int max_route_length;// 限界まで遠回りさせたときの最長の長さで探索を打ち切る
	boolean defense_surround_flag = true;
	Map[][] route_F;
	int[][] reachability_F;
	boolean[] reached;
	int stop_searching_distance = Integer.MAX_VALUE;
	boolean need_fsp = true;// makerouteする前にfinsDhortestPathが必要かどうか
	// 立てる予定のタワー情報は全部ここに入れて最後に出力する
	ArrayList<String> build_tower_list;
	// 各Mapごとに想定される最大ライフ値（少し小さめ）
	int[] LeM_max = { 605, 1352, 2195, 3167, 4125, 5158, 6263, 7434, 8669,
			9752, 11090, 12247, 13680, 14904, 16154, 17709, 19020, 20355,
			21712, 23091, 24493, 25915, 27358, 28821, 30659, 31805, 33325,
			34863, 36418, 37991, 39580, 41185, 42806, 44443, 46094, 47760,
			49012, 50702, 52406, 54124, 55854, 57144, 58896, 60661, 62437,
			63754, 65551, 67360, 69179, 70521, 72360, 74210, 75568, 77436,
			79314, 80688, 82583, 84488, 85878, 87799, 89729, 91134, 93081,
			95035, 96455, 98425, 99852, 101837, 103829, 105270, 107277, 108725,
			110746, 112774, 114236, 116277, 117745, 119800, 121274, 123342 };

	public TowerBuilder(Stage s, Level l) {
		stage = s;
		level = l;
	}

	// タワーを設置する
	public void buildTower(int s_num, int l_num) {
		build_tower_list = new ArrayList<String>();
		stage_num = s_num;
		level_num = l_num;
		max_route_length = (stage.H - 1) * (stage.W - 1);

		int area = stage.W * stage.H - 310;
		if (area < 0) {
			area = 0;
		}
		if (level_num == 0) {
			// 広いマップはあらかじめランダムにいくつか立てておく
			constructRandomly((int) (Math.sqrt(area) * 2.6 + 12 - Math
					.sqrt(stage_num) * 5));

			findShortestPath(-1);
			int tower_num = 0;
			while (true) {
				makeRoute(4);
				tower_num += stage.enemy_spot.size();
				int average_path_length = 0;
				for (EnemySpot es : stage.enemy_spot) {
					average_path_length += es.route.size();
				}
				average_path_length /= stage.enemy_spot.size();
				if (LeM_max[stage_num] / (tower_num * 300/* 3 * 50 *2 */) < average_path_length) {
					break;
				}
			}

			findShortestPath(-1);
		}

		int survived_enemy_id = simulate();

		need_fsp = false;

		int limit = 1;
		if (stage_num < 20) {
			limit = 3;
		} else {
			limit = 4;
		}

		while (survived_enemy_id >= 0) {
			int before_tower_size = level.tower.size();
			for (int i = 0; i < limit; i++) {
				if (i % 2 == 0) {
					makeRoute(0);
				} else {
					makeRoute(4);
				}
				if (area > 70) {
					for (int j = 0; j < stage.enemy_spot.size() / 4 + 1; j++) {
						strengthen();
					}
				}
			}
			stage.strengthen_tower_order = null;

			if (before_tower_size == level.tower.size()) {
				fillSpace();
				stage.strengthen_tower_order = null;
				strengthen();
				strengthen();
				findShortestPath(-1);
				survived_enemy_id = simulate();
				while (survived_enemy_id >= 0
						&& stage.strengthen_tower_order.size() > 2) {
					strengthen();
					strengthen();
					strengthen();
					survived_enemy_id = simulate();
				}
				break;
			}
			findShortestPath(-1);
			survived_enemy_id = simulate();
		}

		// 出力
		System.out.println(build_tower_list.size());
		for (int i = 0; i < build_tower_list.size(); i++) {
			System.out.println(build_tower_list.get(i));
		}
	}

	// エネミーマスの経路を塞いでいく
	private void makeRoute(int strength) {
		// まずエネミーマスから近い防衛マスを完全に囲んでしまう
		if (level.M < stage.enemy_spot.size() * 40) {
			return;
		}

		if (defense_surround_flag) {
			defense_surround_flag = false;
			int size;
			do {
				size = build_tower_list.size();
				for (EnemySpot es : stage.enemy_spot) {
					// 防衛マスの座標
					int x = es.route.get(es.route.size() - 1).x;
					int y = es.route.get(es.route.size() - 1).y;
					// 四方の空マスの場所を記憶して、isConstructable出ない場合あとで元に戻す
					boolean[] empty = new boolean[4];
					empty[0] = false;
					empty[1] = false;
					empty[2] = false;
					empty[3] = false;
					if (stage.F[y][x + 1] == 0) {
						empty[0] = true;
						stage.F[y][x + 1] = 1;
					}
					if (stage.F[y - 1][x] == 0) {
						empty[1] = true;
						stage.F[y - 1][x] = 1;
					}
					if (stage.F[y][x - 1] == 0) {
						empty[2] = true;
						stage.F[y][x - 1] = 1;
					}
					if (stage.F[y + 1][x] == 0) {
						empty[3] = true;
						stage.F[y + 1][x] = 1;
					}
					if ((empty[0] || empty[1] || empty[2] || empty[3])
							&& isConstractable()) {
						if (empty[0] == true) {
							level.tower.add(new Tower(x + 1, y, 0, 0));
							build_tower_list.add((x + 1) + " " + y + " 0 0");
						}
						if (empty[1] == true) {
							level.tower.add(new Tower(x, y - 1, 0, 0));
							build_tower_list.add(x + " " + (y - 1) + " 0 0");
						}
						if (empty[2] == true) {
							level.tower.add(new Tower(x - 1, y, 0, 0));
							build_tower_list.add((x - 1) + " " + y + " 0 0");
						}
						if (empty[3] == true) {
							level.tower.add(new Tower(x, y + 1, 0, 0));
							build_tower_list.add(x + " " + (y + 1) + " 0 0");
						}
						level.M -= 40;
					} else {
						if (empty[0] == true) {
							stage.F[y][x + 1] = 0;
						}
						if (empty[1] == true) {
							stage.F[y - 1][x] = 0;
						}
						if (empty[2] == true) {
							stage.F[y][x - 1] = 0;
						}
						if (empty[3] == true) {
							stage.F[y + 1][x] = 0;
						}
					}
				}
				findShortestPath(-1);
			} while (size != build_tower_list.size());
		}

		// 次に、エネミーマスの近くから最短経路をふさいでゆく
		if (level.M < stage.enemy_spot.size() * 10) {
			return;
		}
		for (int i = 0; i < stage.enemy_spot.size(); i++) {
			if (need_fsp) {
				findShortestPath(i);
			} else {
				need_fsp = true;
			}
			// 前から
			for (int j = 1; j < stage.enemy_spot.get(i).route.size() - 1; j++) {
				// 場所を探す
				int x = stage.enemy_spot.get(i).route.get(j).x;// 新しくタワーを立てる候補地
				int y = stage.enemy_spot.get(i).route.get(j).y;// 新しくタワーを立てる候補地
				int before_x = stage.enemy_spot.get(i).route.get(j - 1).x;
				int before_y = stage.enemy_spot.get(i).route.get(j - 1).y;
				if (stage.enemy_spot.get(i).route.get(j).distance(
						stage.enemy_spot.get(i).route.get(j - 1)) > 1.1) {// ななめ
					int defense_x = stage.enemy_spot.get(i).route
							.get(stage.enemy_spot.get(i).route.size() - 1).x;
					int defense_y = stage.enemy_spot.get(i).route
							.get(stage.enemy_spot.get(i).route.size() - 1).y;

					if (x < before_x && y < before_y) {// 左上
						if (Math.abs(defense_x - x) < Math.abs(defense_y - y)) {
							x++;
						} else {
							y++;
						}
					} else if (x < before_x && y > before_y) {// 左下
						if (Math.abs(defense_x - x) < Math.abs(defense_y - y)) {
							x++;
						} else {
							y--;
						}
					} else if (x > before_x && y > before_y) {// 右下
						if (Math.abs(defense_x - x) < Math.abs(defense_y - y)) {
							x--;
						} else {
							y--;
						}
					} else if (x > before_x && y < before_y) {// 右上
						if (Math.abs(defense_x - x) < Math.abs(defense_y - y)) {
							x--;
						} else {
							y++;
						}
					}
				}
				// 立てる
				if (stage.F[y][x] == 0) {
					stage.F[y][x] = 1;
					if (isConstractable()
							&& level.M >= (strength == 0 ? 10 : 140)) {
						level.tower.add(new Tower(x, y, strength, 0));
						build_tower_list.add(x + " " + y + " " + strength
								+ " 0");
						level.M -= (strength == 0 ? 10 : 140);
						break;
					} else {
						stage.F[y][x] = 0;
					}
				}
			}
			// 後ろから
			if ((stage.W < 18 && stage.H < 18) || stage_num >= 25) {
				return;
			}
			for (int j = stage.enemy_spot.get(i).route.size() - 2; j > 1; j--) {
				// 場所を探す
				int x = stage.enemy_spot.get(i).route.get(j).x;// 新しくタワーを立てる候補地
				int y = stage.enemy_spot.get(i).route.get(j).y;// 新しくタワーを立てる候補地
				int before_x = stage.enemy_spot.get(i).route.get(j - 1).x;
				int before_y = stage.enemy_spot.get(i).route.get(j - 1).y;
				if (stage.enemy_spot.get(i).route.get(j).distance(
						stage.enemy_spot.get(i).route.get(j - 1)) > 1.1) {// ななめ
					int enemy_spot_x = stage.enemy_spot.get(i).route.get(0).x;
					int enemy_spot_y = stage.enemy_spot.get(i).route.get(0).y;

					if (x < before_x && y < before_y) {// 左上
						if (Math.abs(enemy_spot_x - x) < Math.abs(enemy_spot_y
								- y)) {
							x++;
						} else {
							y++;
						}
					} else if (x < before_x && y > before_y) {// 左下
						if (Math.abs(enemy_spot_x - x) < Math.abs(enemy_spot_y
								- y)) {
							x++;
						} else {
							y--;
						}
					} else if (x > before_x && y > before_y) {// 右下
						if (Math.abs(enemy_spot_x - x) < Math.abs(enemy_spot_y
								- y)) {
							x--;
						} else {
							y--;
						}
					} else if (x > before_x && y < before_y) {// 右上
						if (Math.abs(enemy_spot_x - x) < Math.abs(enemy_spot_y
								- y)) {
							x--;
						} else {
							y++;
						}
					}
				}
				// 立てる
				if (stage.F[y][x] == 0) {
					stage.F[y][x] = 1;
					if (isConstractable()
							&& level.M >= (strength == 0 ? 10 : 140)) {
						level.tower.add(new Tower(x, y, strength, 0));
						build_tower_list.add(x + " " + y + " " + strength
								+ " 0");
						level.M -= (strength == 0 ? 10 : 140);
						break;
					} else {
						stage.F[y][x] = 0;
					}
				}
			}
		}
	}

	// タワーを立てても問題ない中央付近のマスにタワーを立てる
	private void fillSpace() {
		int[][] empty_F = new int[stage.H][stage.W];
		for (int i = 0; i < stage.H; i++) {
			for (int j = 0; j < stage.W; j++) {
				if (stage.F[i][j] == 0) {
					empty_F[i][j] = 0;
				} else {
					empty_F[i][j] = 1;
				}
			}
		}
		for (EnemySpot es : stage.enemy_spot) {
			for (Point p : es.route) {
				empty_F[p.y][p.x] = 1;
			}
		}
		for (int i = 0; i < stage.H; i++) {
			for (int j = 0; j < stage.W; j++) {
				if (empty_F[i][j] == 0) {
					stage.F[i][j] = 1;
					if (isConstractable() && level.M >= 10) {
						level.tower.add(new Tower(j, i, 0, 0));
						build_tower_list.add(j + " " + i + " 0 0");
						level.M -= 10;
					} else {
						stage.F[i][j] = 0;
					}
				}
			}
		}
	}

	// 強化MAXでないタワーを1つMAＸにする
	private void strengthen() {
		// 強化する順番を決める
		if (stage.strengthen_tower_order == null) {
			stage.strengthen_tower_order = new ArrayList<Integer>();
			ArrayList<Double> distance = new ArrayList<Double>();
			ArrayList<Double> sorted_distance = new ArrayList<Double>();
			double aspect_ratio = (double) stage.H / stage.W;
			for (Tower t : level.tower) {
				double d = Math.pow((stage.W / 2 - t.X) * aspect_ratio, 2)
						+ Math.pow(stage.H / 2 - t.Y, 2);
				distance.add(d);
				sorted_distance.add(d);
			}
			Collections.sort(sorted_distance);
			for (int i = 0; i < level.tower.size(); i++) {
				stage.strengthen_tower_order.add(distance
						.indexOf(sorted_distance.get(0)));
				distance.set(distance.indexOf(sorted_distance.get(0)), -1.0);
				sorted_distance.remove(0);
			}
		}
		// 強化する
		while (true) {
			if (stage.strengthen_tower_order.size() != 0 && level.M >= 140) {
				Tower t = level.tower.get(stage.strengthen_tower_order.get(0));
				// フリーズは強化しない
				if (t.C == 2 || t.A == 4) {
					stage.strengthen_tower_order.remove(0);
					continue;
				}
				String s = t.X + " " + t.Y + " 4 " + t.C;
				build_tower_list.add(s);
				t.A = 4;
				stage.strengthen_tower_order.remove(0);
			}
			break;
		}
	}

	// シミュレーションに必要なデータを初期化
	private void initSimutator() {
		for (Tower tow : level.tower) {
			tow.isCharged = true;
			tow.charge_time = 0;
			tow.insight_enemy_id = new ArrayList<Integer>();
			tow.enemy = level.enemy;
			if (tow.C == 0) {
				tow.charge_need_time = 10 - 2 * tow.A + 1;
			} else if (tow.C == 1) {
				tow.charge_need_time = 20 - 2 * tow.A + 1;
			} else if (tow.C == 2) {
				tow.charge_time = 20 + 1;
			} else {
				tow.charge_need_time = Integer.MAX_VALUE;
			}
			// 攻撃力算出
			if (tow.C == 0) {
				tow.power = 10 * (tow.A + 1);
			} else if (tow.C == 1) {
				tow.power = 20 * (tow.A + 1);
			} else {
				tow.power = 3 * (tow.A + 1);
			}
			// 射程距離算出
			if (tow.C == 0) {
				tow.range = 4 + tow.A;
			} else if (tow.C == 1) {
				tow.range = 5 + tow.A;
			} else {
				tow.range = 2 + tow.A;
			}
		}
		for (Enemy ene : level.enemy) {
			ene.L = ene.life;
			ene.waiting_time = 0;
			ene.appeared = false;
			ene.alive = true;
			ene.my_route = new ArrayList<Point>();
			ene.enemy_spot = stage.enemy_spot;
		}
	}

	// タワーの建設を確定する前に敵の行動をシミュレーションして、ライフを削られるようなら増設あるいは強化する
	private int simulate() {
		initSimutator();
		boolean all_dead;
		int survived_enemy_id = -1;
		do {
			// フレームを進める
			for (Enemy en : level.enemy) {
				en.waiting();
			}
			// エネミー出現判定
			for (Enemy en : level.enemy) {
				en.appear();
			}
			// エネミーを移動させるかどうかを判定
			for (Enemy en : level.enemy) {
				en.move();
			}
			// 射撃対象の敵を更新
			for (Tower tow : level.tower) {
				tow.rockON();// stage.F, stage.original_F, level.tower);
			}
			// 充電
			for (Tower tow : level.tower) {
				tow.Charge();
			}
			// 発射
			for (Tower tow : level.tower) {
				tow.fire();
			}
			// エネミーの生死判定と、防衛マスへの到達判断
			for (Enemy en : level.enemy) {
				int tmp_id = en.checkLifeAndReached();
				if (tmp_id != -1) {// 防衛マスに到達していたらそのエネミーのidを返す。それ以外の時は-1を返す。
					survived_enemy_id = tmp_id;
					break;
				}
			}
			if (survived_enemy_id != -1) {
				break;
			}
			// 全てのエネミーが死んでたら終了
			all_dead = true;
			for (Enemy en : level.enemy) {
				all_dead &= !en.alive;
			}
		} while (!all_dead);

		return survived_enemy_id;
	}

	// エネミーマスと防衛マスとの最短経路を求める
	private void findShortestPath(int target_enemy_spot_num) {
		stage.initEnemySpot();// 絶対必要！触るな！動かすな！
		int shortest_distance = Integer.MAX_VALUE;
		// 一番短い距離で到達できるディフェンスマスの決定
		try {
			int enemy_index = 0;
			for (EnemySpot es : stage.enemy_spot) {
				// -1のときは全てのenemy spotのpathを調べる
				if (target_enemy_spot_num != -1
						&& target_enemy_spot_num != enemy_index) {
					enemy_index++;
					continue;
				}
				enemy_index++;
				stop_searching_distance = Integer.MAX_VALUE;
				// マップのコピー
				route_F = new Map[stage.H][stage.W];// ここに距離とルート全部入れる
				for (int y = 0; y < stage.H; y++) {
					for (int x = 0; x < stage.W; x++) {
						route_F[y][x] = new Map(stage.F[y][x] == 1 ? -1
								: Integer.MAX_VALUE);// 障害物なら-1、空マスならめちゃくちゃでかい値
					}
				}
				// マップ上を探索してエネミーマスからの距離を算出
				int start_x = es.X;
				int start_y = es.Y;
				route_F[start_y][start_x].distance = 0;
				route_F[start_y][start_x].route
						.add(new Point(start_x, start_y));
				visitAround(start_x, start_y);
				// 一番短い距離で到達できるディフェンスマスの決定とルートの記録
				shortest_distance = Integer.MAX_VALUE;
				int id = 0;// defense_spotに付けるincrementalID
				for (DefenseSpot ds : stage.defense_spot) {
					if (shortest_distance >= route_F[ds.Y][ds.X].distance) {
						// 距離が同じ場合は、進む方向が右隣から反時計周り順に若い方を優先
						if (shortest_distance == route_F[ds.Y][ds.X].distance) {
							int base_x = es.X;
							int base_y = es.Y;
							boolean rewrite_flag = true;// 書き換えが必要かどうか
							for (int i = 0; es.route.size() != 0; i++) {
								int old_x = es.route.get(i).x;
								int old_y = es.route.get(i).y;
								int new_x = route_F[ds.Y][ds.X].route.get(i).x; // 更新候補の防衛マスへの経路
								int new_y = route_F[ds.Y][ds.X].route.get(i).y; // 更新候補の防衛マスへの経路
								if (old_x == new_x && old_y == new_y) {// 次が同じマスなら中心点を更新してもう一回チェック
									base_x = old_x;
									base_y = old_y;
									continue;
								}
								int old_order = getOrder(base_x - old_x, base_y
										- old_y);
								int new_order = getOrder(base_x - new_x, base_y
										- new_y);
								if (old_order < new_order) {
									rewrite_flag = false;
								}
								break;
							}
							if (!rewrite_flag) {
								continue;
							}
							// 新しいルートの方が正しければこの後ルート更新
						}
						// ルートを更新
						shortest_distance = route_F[ds.Y][ds.X].distance;
						es.nearest_defense_spot_index = id;
						es.nearest_cost = shortest_distance;
						es.route.clear();
						for (Point r : route_F[ds.Y][ds.X].route) {
							es.route.add(new Point(r.x, r.y));
						}
					}
					id++;
				}
			}
		} catch (Exception e) {
			System.out.println("$$$");
			System.out.println("shortest distance: " + shortest_distance);
			for (EnemySpot es : stage.enemy_spot) {
				System.out.println("es.route.size() " + es.route.size());
			}
			System.out.println("original_F");
			for (int[] F1 : stage.original_F) {
				for (int F2 : F1) {
					System.out.print(F2 + " ");
				}
				System.out.println();
			}
			System.out.println();
			System.out.println("F");
			for (int[] F1 : stage.F) {
				for (int F2 : F1) {
					System.out.print(F2 + " ");
				}
				System.out.println();
			}
			e.printStackTrace();
			try {
				Thread.sleep(99999);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

	// 周り8箇所を再帰的に訪れてエネミーからディフェンスまでの距離を求める
	private void visitAround(int x, int y) {
		// 時間短縮のため遠すぎる所は探索しない
		if (route_F[y][x].distance >= max_route_length
				|| route_F[y][x].distance > stop_searching_distance) {
			return;
		}
		if (stage.F[y][x] == 3
				&& route_F[y][x].distance < stop_searching_distance) {// 防衛マス
			stop_searching_distance = route_F[y][x].distance + 1;
		}

		// 次に訪れるマスが障害物でもタワーでなく、自分が訪れるよりも早く訪れたルートがなければ距離を更新して再帰
		if (route_F[y][x + 1].distance != -1
				&& route_F[y][x + 1].distance > route_F[y][x].distance + 2) {
			route_F[y][x + 1].distance = route_F[y][x].distance + 2;
			route_F[y][x + 1].route.clear();
			for (int i = 0; i < route_F[y][x].route.size(); i++) {
				route_F[y][x + 1].route.add(route_F[y][x].route.get(i));
			}
			route_F[y][x + 1].route.add(new Point(x + 1, y));
			visitAround(x + 1, y);
		}
		if (route_F[y - 1][x + 1].distance != -1
				&& route_F[y][x + 1].distance != -1
				&& route_F[y - 1][x].distance != -1
				&& route_F[y - 1][x + 1].distance > route_F[y][x].distance + 3) {
			// 距離を更新
			route_F[y - 1][x + 1].distance = route_F[y][x].distance + 3;
			// ルートを更新
			route_F[y - 1][x + 1].route.clear();
			for (int i = 0; i < route_F[y][x].route.size(); i++) {
				route_F[y - 1][x + 1].route.add(route_F[y][x].route.get(i));
			}
			route_F[y - 1][x + 1].route.add(new Point(x + 1, y - 1));
			// 移動して再帰
			visitAround(x + 1, y - 1);
		}
		if (route_F[y - 1][x].distance != -1
				&& route_F[y - 1][x].distance > route_F[y][x].distance + 2) {
			route_F[y - 1][x].distance = route_F[y][x].distance + 2;
			route_F[y - 1][x].route.clear();
			for (int i = 0; i < route_F[y][x].route.size(); i++) {
				route_F[y - 1][x].route.add(route_F[y][x].route.get(i));
			}
			route_F[y - 1][x].route.add(new Point(x, y - 1));
			visitAround(x, y - 1);
		}
		if (route_F[y - 1][x - 1].distance != -1
				&& route_F[y - 1][x].distance != -1
				&& route_F[y][x - 1].distance != -1
				&& route_F[y - 1][x - 1].distance > route_F[y][x].distance + 3) {
			route_F[y - 1][x - 1].distance = route_F[y][x].distance + 3;
			route_F[y - 1][x - 1].route.clear();
			for (int i = 0; i < route_F[y][x].route.size(); i++) {
				route_F[y - 1][x - 1].route.add(route_F[y][x].route.get(i));
			}
			route_F[y - 1][x - 1].route.add(new Point(x - 1, y - 1));
			visitAround(x - 1, y - 1);
		}
		if (route_F[y][x - 1].distance != -1
				&& route_F[y][x - 1].distance > route_F[y][x].distance + 2) {
			route_F[y][x - 1].distance = route_F[y][x].distance + 2;
			route_F[y][x - 1].route.clear();
			for (int i = 0; i < route_F[y][x].route.size(); i++) {
				route_F[y][x - 1].route.add(route_F[y][x].route.get(i));
			}
			route_F[y][x - 1].route.add(new Point(x - 1, y));
			visitAround(x - 1, y);
		}
		if (route_F[y + 1][x - 1].distance != -1
				&& route_F[y][x - 1].distance != -1
				&& route_F[y + 1][x].distance != -1
				&& route_F[y + 1][x - 1].distance > route_F[y][x].distance + 3) {
			route_F[y + 1][x - 1].distance = route_F[y][x].distance + 3;
			route_F[y + 1][x - 1].route.clear();
			for (int i = 0; i < route_F[y][x].route.size(); i++) {
				route_F[y + 1][x - 1].route.add(route_F[y][x].route.get(i));
			}
			route_F[y + 1][x - 1].route.add(new Point(x - 1, y + 1));
			visitAround(x - 1, y + 1);
		}
		if (route_F[y + 1][x].distance != -1
				&& route_F[y + 1][x].distance > route_F[y][x].distance + 2) {
			route_F[y + 1][x].distance = route_F[y][x].distance + 2;
			route_F[y + 1][x].route.clear();
			for (int i = 0; i < route_F[y][x].route.size(); i++) {
				route_F[y + 1][x].route.add(route_F[y][x].route.get(i));
			}
			route_F[y + 1][x].route.add(new Point(x, y + 1));
			visitAround(x, y + 1);
		}
		if (route_F[y + 1][x + 1].distance != -1
				&& route_F[y + 1][x].distance != -1
				&& route_F[y][x + 1].distance != -1
				&& route_F[y + 1][x + 1].distance > route_F[y][x].distance + 3) {
			route_F[y + 1][x + 1].distance = route_F[y][x].distance + 3;
			route_F[y + 1][x + 1].route.clear();
			for (int i = 0; i < route_F[y][x].route.size(); i++) {
				route_F[y + 1][x + 1].route.add(route_F[y][x].route.get(i));
			}
			route_F[y + 1][x + 1].route.add(new Point(x + 1, y + 1));
			visitAround(x + 1, y + 1);
		}
	}

	// 進む方向の優先順位を求める
	private int getOrder(int x, int y) {
		if (x == -1 && y == 0) {
			return 1;
		} else if (x == -1 && y == 1) {
			return 2;
		} else if (x == 0 && y == 1) {
			return 3;
		} else if (x == 1 && y == 1) {
			return 4;
		} else if (x == 1 && y == 0) {
			return 5;
		} else if (x == 1 && y == -1) {
			return 6;
		} else if (x == 0 && y == -1) {
			return 7;
		} else if (x == -1 && y == -1) {
			return 8;
		} else {
			// デバッグ用
			// System.out.println("0 1 2 3 4 5 ogyaaaaaaaa " + x + " " + y);
			return 0;// こうするとバグがあっても現れないので本番用のときだけこうする
		}
	}

	// 防衛マスに到達できないエネミーマスが存在してはいけないルールに反していないか調べる
	private boolean isConstractable() {
		reached = new boolean[stage.enemy_spot.size()];
		for (int i = 0; i < reached.length; i++) {
			reached[i] = false;
		}
		for (int i = 0; i < stage.enemy_spot.size(); i++) {
			if (reached[i]) {
				continue;
			}
			reachability_F = new int[stage.H][stage.W];
			// マップをコピー
			for (int yy = 0; yy < stage.H; yy++) {
				for (int xx = 0; xx < stage.W; xx++) {
					reachability_F[yy][xx] = stage.F[yy][xx];
				}
			}
			int x = stage.enemy_spot.get(i).X;
			int y = stage.enemy_spot.get(i).Y;
			if (!fillAround(y, x)) {
				return false;// どの防衛マスにも到達できない敵出現マスが存在した
			}
		}
		return true;
	}

	// 上下左右に防衛マスがないかを再帰的に探索する
	private boolean fillAround(int y, int x) {
		// 障害物マスか既に到達したことのあるマスは何も調べない
		if (reachability_F[y][x] == 1 || reachability_F[y][x] == 5) {
			return false;
		} else {
			boolean here_you_are = false;// 一度でも到達できたらこのドメインにいるすべてのエネミーマスは到達済みにする
			if (reachability_F[y][x] == 2) {// エネミーマス
				for (int i = 0; i < stage.enemy_spot.size(); i++) {
					if (stage.enemy_spot.get(i).X == x
							&& stage.enemy_spot.get(i).Y == y) {
						reached[i] = true;
					}
				}
			} else if (reachability_F[y][x] == 3) {// 防衛マス
				here_you_are = true;
			}
			// 空マス
			reachability_F[y][x] = 5;// 到達済みマーキング
			boolean a = fillAround(y, x + 1);
			boolean b = fillAround(y - 1, x);
			boolean c = fillAround(y, x - 1);
			boolean d = fillAround(y + 1, x);
			return a || b || c || d || here_you_are;
		}
	}

	// ランダムにたてる
	private void constructRandomly(int limit) {
		Random rand = new Random();
		int w, h;
		while (level.M >= 10) {
			while (true) {
				w = rand.nextInt(stage.W);
				h = rand.nextInt(stage.H);
				if (stage.F[h][w] == 0 && !isNear(h, w)) {// 空マスに当たるまでランダム生成
					break;
				}
			}
			stage.F[h][w] = 1;// タワーは通れないから障害物と見なす
			if (isConstractable()) {
				build_tower_list.add(w + " " + h + " 0 0");
				level.tower.add(new Tower(w, h, 0, 0));
				level.M -= 10;
			} else {
				stage.F[h][w] = 0;
			}
			if (limit-- <= 0) {
				break;
			}
		}
	}

	private boolean isNear(int h, int w) {
		for (int i = 0; i < stage.enemy_spot.size(); i++) {
			if (Math.sqrt(Math.pow(stage.enemy_spot.get(i).X - w, 2)
					+ Math.pow(stage.enemy_spot.get(i).Y - h, 2)) <= 3) {
				return true;
			}
		}
		for (int i = 0; i < stage.defense_spot.size(); i++) {
			if (Math.sqrt(Math.pow(stage.defense_spot.get(i).X - w, 2)
					+ Math.pow(stage.defense_spot.get(i).Y - h, 2)) <= 3) {
				return true;
			}
		}
		return false;
	}

}
