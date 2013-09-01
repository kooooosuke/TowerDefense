package kooooosuke;

import java.util.ArrayList;
import java.util.Scanner;

public class Level {
	Scanner scanner;
	int Lp, M, T, E;// 残りライフ、資金、タワー数、敵数
	ArrayList<Tower> tower;
	ArrayList<Enemy> enemy;
	boolean high_level_mode = false;

	public Level(Scanner s, ArrayList<Tower> t) {
		scanner = s;
		tower = t;
	}

	// 標準入力から送られてくるレベルデータを読み込んで整形する
	public void readLevel() {
		enemy = new ArrayList<Enemy>();
		// レベル開始時の情報
		String[] tmpStrAry = scanner.nextLine().split(" ");
		Lp = Integer.parseInt(tmpStrAry[0]);
		M = Integer.parseInt(tmpStrAry[1]);
		T = Integer.parseInt(tmpStrAry[2]);
		E = Integer.parseInt(tmpStrAry[3]);

		// タワー
		// tower = new ArrayList<Tower>();
		for (int i = 0; i < T; i++) {
			scanner.nextLine();
			// String[] strAry = scanner.nextLine().split(" ");
			// Tower t = new Tower(
			// Integer.parseInt(strAry[0]),
			// Integer.parseInt(strAry[1]),
			// Integer.parseInt(strAry[2]),
			// Integer.parseInt(strAry[3]) );
			// tower.add(t);
		}

		// 敵
		int average_enemy_life = 0;
		int average_enemy_speed = 0;
		for (int i = 0; i < E; i++) {
			String[] strAry = scanner.nextLine().split(" ");
			Enemy e = new Enemy(i, Integer.parseInt(strAry[0]),
					Integer.parseInt(strAry[1]), Integer.parseInt(strAry[2]),
					Integer.parseInt(strAry[3]), Integer.parseInt(strAry[4]));
			average_enemy_life += e.L;
			average_enemy_speed += e.S;
			enemy.add(e);
		}
		average_enemy_life /= E;
		average_enemy_speed /= E;
		if (average_enemy_life > 1000 || average_enemy_speed < 50) {
			high_level_mode = true;
		}

		if (!scanner.nextLine().equals("END")) {
			System.out.println("invalid input 2. \"END\" didn't come");
		}

	}

}
