package kooooosuke;

import java.awt.Point;
import java.util.ArrayList;

public class EnemySpot {
	int X;
	int Y;
	int nearest_defense_spot_index;
	int nearest_cost;
	//敵出現マスから一番近い防衛マスへ向かうときに通る経路
	ArrayList<Point> route = new ArrayList<Point>();

	public EnemySpot(int x, int y) {
		X = x;
		Y = y;
	}	
	
}
