package kooooosuke;
import java.util.ArrayList;
import java.util.Scanner;

public class Stage {
	Scanner scanner;
	int L,W,H;//レベル数、マップの幅、マップの高さ
	int[][] F;//originalをコピーしてシミュレーションとかに使う用のマップ
	/*private*/ int[][] original_F;//マップ
	ArrayList<EnemySpot> enemy_spot;
	ArrayList<DefenseSpot> defense_spot;
	ArrayList<Integer> strengthen_tower_order;
	
	public Stage(Scanner s) {
		scanner = s;
	}

	
	//標準入力から送られてくるステージ（マップ）データを読み込んで整形する
	public void readStage() {
		//ステージ情報
		String[] tmpStrAry = scanner.nextLine().split(" ");
		W = Integer.parseInt(tmpStrAry[0]);
		H = Integer.parseInt(tmpStrAry[1]);
		original_F = new int[H][W];

		//マップ
		for(int y=0; y<H; y++){
			char[] chrAry = scanner.nextLine().toCharArray();
			for(int x=0; x<chrAry.length; x++){
				if(chrAry[x]=='s'){
					original_F[y][x] = 2;//敵出現マス
				}else if(chrAry[x]=='g'){
					original_F[y][x] = 3;//防衛マス
				}else{
					original_F[y][x] = Byte.parseByte(""+chrAry[x]);
				}
			}
		}
		L = Integer.parseInt(scanner.nextLine());
		if(!scanner.nextLine().equals("END")){
			System.out.println("invalid input 1. \"END\" didn't come");
		}
		initMap(new ArrayList<Tower>());
		initEnemySpot();
		initDefenseSpot();
	}
	
	
	//操作用のマップを初期化
	public void initMap(ArrayList<Tower> tower) {
		F = new int[H][W];
		for(int y=0; y<H; y++){
			for(int x=0; x<W; x++){
				F[y][x] = original_F[y][x]; 
			}
		}
		for (Tower t : tower) {
			F[t.Y][t.X] = 1;
		}
	}


	//敵出現マスを探して登録
	public void initEnemySpot() {
		enemy_spot = new ArrayList<EnemySpot>();
		for(int y=1; y<H-1; y++){
			for(int x=1; x<W-1; x++){
				if(F[y][x]==2){
					EnemySpot es = new EnemySpot(x,y);
					enemy_spot.add(es);
				}
			}			
		}
	}
	
	
	//防衛マスを探して登録
	public void initDefenseSpot() {
		defense_spot = new ArrayList<DefenseSpot>();
		for(int y=1; y<H-1; y++){
			for(int x=1; x<W-1; x++){
				if(F[y][x]==3){
					DefenseSpot ds = new DefenseSpot(x,y);
					defense_spot.add(ds);
				}
			}			
		}
	}
	
	

	
}
