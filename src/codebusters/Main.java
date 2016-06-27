import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Send your busters out into the fog to trap ghosts and bring them home!
 **/
class Player {
	
	/*
	 * on suppose que les id des ghosts et busters 
	 * vont de 0 à n 
	 * peut-être faudra-t-il le vérifier
	 * 
	 */
	
	/*
	 * TODO
	 * instancier toutes les classes au début //fait
	 * 	 et donc ajouter un boolean visible modifié à chaque tour //fait
	 * 
	 *  ajouter une variable no de tour 
	 *  	qui permet notemment de savoir buster stuned(10 tours) et si stun possible (20 tours)
	 * 
	 * ajouter la fonciton stun
	 * 
	 * enregistre position présumées des ghost non capturés
	 */

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
        

        for (int i = 0; i < bustersPerPlayer; i++) {
			busters[i]=new Buster(i);
			opponentBusters[i]=new OpponentBuster(i);
		}
        for (int i = 0; i < ghostCount; i++) {
			ghosts[i]=new Ghost(i);
		}
        

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
               
					busters[entityId].update(x, y, state,value);
					bustersIndex++;
				} else if (type == myTeamId+1) {//ghosts    (myTeamId+1 is not a constant => not suitable for switch/case)
					ghosts[entityId].update(x, y,value);
					ghostIndex++;
				} else { //=-1 || =1   && !ghost => opponent ghost
					opponentBusters[entityId].update(x, y, state,value);
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
	private int id;
	private int x;
	private int y;	
	private int state;
	private int value;
	private String answer;
	
	public Buster(int id) {
		this.id=id;
	}	
	
	public void update(int x, int y,int state,int value) {//change to update
		this.x = x;
		this.y = y;
		this.state = state;
		this.value=value;
		this.answer="";
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
		for (int i = 0; i < Player.ghostCount; i++) {
			if(Player.ghosts[i].getVisible()){
			
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
		}
		return false;
	}

	private boolean closestGhost(){//go to closest ghost
		int closestGhost = 0; 
		int closestGhostID=-1;
		
		for (int i = 0; i < Player.ghostCount; i++) {
			if(Player.ghosts[i].getVisible()){
				int GX=Player.ghosts[i].getX();
				int GY=Player.ghosts[i].getY();
				int BX=this.x;
				int BY=this.y;
				
				if((GX-BX)*(GX-BX)+(GY-BY)*(GY-BY)>closestGhost){
					closestGhost=(GX-BX)*(GX-BX)+(GY-BY)*(GY-BY);
					closestGhostID=i;
				}
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

//————————————————————————————————————————————————————————————————————————————
//————————————————————————————————————————————————————————————————————————————
//————————————————————————————new class———————————————————————————————————————
//————————————————————————————————————————————————————————————————————————————
//————————————————————————————————————————————————————————————————————————————

class Ghost {
	private int id;
	private int x;
	private int y;
	private int value;
	private boolean visible;
	
	public Ghost(int id) {
		this.id=id;
	}
	
	public void update(int x, int y,int value) {// TODO change to update
		this.visible = true;
		this.x = x;
		this.y = y;
		this.value = value;
	}

//———————————————————
//——getters setters——
//———————————————————
	public void setVisible(boolean set){
		this.visible=set;
	}
	
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getID() {
		return id;
	}	
	
	public boolean getVisible() {
		return visible;
	}
	
}

//————————————————————————————————————————————————————————————————————————————
//————————————————————————————————————————————————————————————————————————————
//————————————————————————————new class———————————————————————————————————————
//————————————————————————————————————————————————————————————————————————————
//————————————————————————————————————————————————————————————————————————————

class OpponentBuster {//My busters
	private int id;
	private int x;
	private int y;	
	private int state;
	private int value;
	private boolean visible;
	
	
	public void update(int x, int y,int state,int value) {//TODO Change to update
		this.x = x;
		this.y = y;
		this.state = state;
		this.value=value;
		this.visible=true;
	}

	public OpponentBuster(int id) {
		this.id=id;
	}

//———————————————————
//——getters setters——
//———————————————————
	public void setVisible(boolean set){
		this.visible=set;
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