package kooooosuke;

import java.awt.Point;
import java.util.ArrayList;

public class Map {
	int distance;
	ArrayList<Point> route = new ArrayList<Point>();
	boolean lock=false;
	
	public Map(int d) {
		distance = d;
	}

}
