package kooooosuke;
import java.util.ArrayList;
import java.util.Scanner;

public class TowerDefense {
	public static void main(String[] args){
		Scanner scanner  = new Scanner(System.in);
		int S = Integer.parseInt(scanner.nextLine());
		ArrayList<Tower> tower;
		
		//ステージの数だけ実行
		for(int stage_num=0; stage_num<S; stage_num++){
			Stage stage = new Stage(scanner);
			tower =new ArrayList<Tower>();
			stage.readStage();
		
			//レベルの数だけ実行
			for(int level_num=0; level_num<stage.L; level_num++){
				Level level = new Level(scanner, tower);
				level.readLevel();
				TowerBuilder tower_builder = new TowerBuilder(stage, level);
				tower_builder.buildTower(stage_num, level_num);
			}
		}
	}
}
