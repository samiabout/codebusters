import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Send your busters out into the fog to trap ghosts and bring them home!
 **/
class Player {

	static Ghost[] ghosts;
	static Buster busters[];
	static OpponentBuster opponentBusters[];
	static int bustersPerPlayer;
	static int ghostCount;
	static int myTeamId;
	
	static int boardTravel=0;//travel the board if no ghost is visible 
	
    public static int ghostIndex;   //nb of each entity visble
    static int bustersIndex;
    static int opponentBustersIndex;
	
    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        bustersPerPlayer = in.nextInt(); // the amount of busters you control
        busters =new Buster[bustersPerPlayer];
        opponentBusters = new OpponentBuster[bustersPerPlayer];
        ghostCount = in.nextInt(); // the amount of ghosts on the map
        ghosts = new Ghost[ghostCount];
        myTeamId = in.nextInt(); // if this is 0, your base is on the top left of the map, if it is one, on the bottom right
        

        
        

        // game loop
        while (true) {
            int entities = in.nextInt(); // the number of busters and ghosts visible to you
            
            ghostIndex=0;   //nb of each entity visble
            bustersIndex=0;
            opponentBustersIndex=0;

            
            for (int i = 0; i < entities; i++) {
                int entityId = in.nextInt(); // buster id or ghost id
                int x = in.nextInt();
                int y = in.nextInt(); // position of this buster / ghost
                int entityType = in.nextInt(); // the team id if it is a buster, -1 if it is a ghost.
                int state = in.nextInt(); // For busters: 0=idle, 1=carrying a ghost.
                int value = in.nextInt(); // For busters: Ghost id being carried. For ghosts: number of busters attempting to trap this ghost.
                
                int type=myTeamId-entityType;
                if (type == 0) {//my busters
               
					busters[bustersIndex]=new Buster(entityId, x, y, state);
					bustersIndex++;
				} else if (type == myTeamId+1) {//ghosts    (myTeamId+1 is not a constant => not suitable for switch/case)
					ghosts[ghostIndex]=new Ghost(entityId, x, y);
					ghostIndex++;
				} else { //=-1 || =1   && !ghost => opponent ghost
					opponentBusters[opponentBustersIndex]=new OpponentBuster(entityId, x, y, state);
					opponentBustersIndex++;
				}
				//System.err.println(bustersIndex);System.err.println(opponentBustersIndex);System.err.println(ghostIndex);
                
            }
            for (int i = 0; i < bustersPerPlayer; i++) {
                  
				//	System.err.println(this.ghostIndex);      
                // Write an action using System.out.println()
                // To debug: System.err.println("Debug messages...");

                 System.out.println(busters[i].action()); // MOVE x y | BUST id | RELEASE
            }
        }
    }
}


class Buster {//My busters
	int id;
	int x;
	int y;	
	int state;
	String answer;
	
	public Buster(int id, int x, int y,int state) {
		super();
		this.id = id;
		this.x = x;
		this.y = y;
		this.state = state;
		this.answer="";
	}


	public Buster() {

		
	}
	
	public String action(){
		if(!this.unload()){
			if(!this.ghostAround()){
				if(!this.closestGhost()){
				    travel();
				    
				    }
			}
		}
		return answer;
	}




	private boolean unload() {//if a ghost is loaded go to base or unload
		if (state==1){
			if(Player.myTeamId==0){
				if(x*x+y*y>1600*1600){
					answer="MOVE 0 0";
				}else answer="RELEASE";
			}
			if(Player.myTeamId==1){
				if((x-16000)*(x-16000)+(y-9000)*(y-9000)>1600*1600){
					answer="MOVE 16000 9000";
				}else answer="RELEASE";
			}			
			return true;
		} 
		return false;
	}
	
	private boolean ghostAround(){//if a ghost around, bust
	System.err.println(Player.ghostIndex);
		for (int i = 0; i < Player.ghostIndex; i++) {
			int GX=Player.ghosts[i].getX();
			int GY=Player.ghosts[i].getY();
			int BX=this.x;
			int BY=this.y;
			int a=(GX-BX)*(GX-BX)+(GY-BY)*(GY-BY);
			System.err.println(a);
			if((GX-BX)*(GX-BX)+(GY-BY)*(GY-BY)<1760*1760){// && (GX-BX)*(GX-BX)+(GY-BY)*(GY-BY)>900
				answer="BUST "+Player.ghosts[i].getID();
				return true;
			}
				
		}
		return false;
	}

	private boolean closestGhost(){//go to closest ghost
		int closestGhost = 0; 
		int closestGhostID=-1;
		
		for (int i = 0; i < Player.ghostIndex; i++) {
			int GX=Player.ghosts[i].getX();
			int GY=Player.ghosts[i].getY();
			int BX=this.x;
			int BY=this.y;
			
			if((GX-BX)*(GX-BX)+(GY-BY)*(GY-BY)>closestGhost){
				closestGhost=(GX-BX)*(GX-BX)+(GY-BY)*(GY-BY);
				closestGhostID=i;
			}
				
		}
		//System.err.println(closestGhostID);
		//System.err.println(Player.ghosts[closestGhostID].getX());
		if (closestGhostID==-1){return false;}
		answer="MOVE "+Player.ghosts[closestGhostID].getX()+" "+Player.ghosts[closestGhostID].getY();
		return true;
	}
	
	
	private void travel() {//peut être fait de façon beaucoups plus efficace.
		/*
		 * les x a checker vont de 2000 à 16000 soit 8 étapes 0 à 7
		 * les y a checker vont de 2000 à 8000  soit 4 étapes 0 à 3
		 */
		int xTravel;//=Player.boardTravel%7;
		int yTravel;//=Player.boardTravel/7;
		/*xTravel+=1;
		yTravel+=1;
		if(yTravel%2==0){xTravel=9-xTravel;}
		xTravel*=2000;
		yTravel*=2000;*/
		do{
			Player.boardTravel++;
			xTravel=Player.boardTravel%7;
			yTravel=Player.boardTravel/7;
			xTravel+=1;
			yTravel+=1;
			if(yTravel%2==0){xTravel=9-xTravel;}
			xTravel*=2000;
			yTravel*=2000;
		}while((xTravel-x)*(xTravel-x)+(xTravel-x)*(xTravel-x)<1000*1000);
		answer="MOVE "+xTravel+" "+yTravel;
	}

	
}

class Ghost {
	private int id;
	private int x;
	private int y;

	public Ghost(int id, int x, int y) {
		super();
		this.id = id;
		this.x = x;
		this.y = y;
	}


	public Ghost() {

	}
	
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}


	public int getID() {
		// TODO Auto-generated method stub
		return id;
	}	
}



class OpponentBuster {//My busters
	int id;
	int x;
	int y;	
	int state;
	
	public OpponentBuster(int id, int x, int y,int state) {
		super();
		this.id = id;
		this.x = x;
		this.y = y;
		this.state = state;
	}
	
	
	
	
	

	public int getId() {
		return id;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getState() {
		return state;
	}
	
}