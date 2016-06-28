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
	 * peut-être qu'il faut compter un tour sur 2 pour stun. non nécessaire
	 * 
	 * prendre en compet state = 2 pour stun
	 * ===idées d'amélioration===
	 * enregistre position présumée des ghost non capturés
	 * 
	 * améliorer la fonction travel
	 * 
	 * prendre en compte les cas d'égalité
	 */

	static Ghost[] ghosts;
	static Buster busters[];
	static OpponentBuster opponentBusters[];
	static int bustersPerPlayer;
	static int ghostCount;
	static int myTeamId;
	
	static int boardTravelEven=0;//travel the board if no ghost is visible 
	static int boardTravelOdd=0;
	
    public static int ghostIndex;   //nb of each entity visble
    static int bustersIndex;
    static int opponentBustersIndex;
    
    static int nbturn;
	
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
        
        nbturn = 0;
        // game loop
        while (true) {
        	nbturn++;
        	
            for (int i = 0; i < bustersPerPlayer; i++) {//set visible to false for all before correction
    			opponentBusters[i].setVisible(false);
    		}
            for (int i = 0; i < ghostCount; i++) {
    			ghosts[i].setVisible(false);
    		}
            
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
				} else { //=-1 || =1   && !ghost => opponent ghost //TODO il y a un bug ici à corriger
					opponentBusters[entityId-bustersPerPlayer].update(x, y, state,value);
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
	private int stunTurn;//last turn he has used stun()
	private String answer;
	
	public Buster(int id) {
		this.id=id;
		this.stunTurn=-30;
	}	
	
	public void update(int x, int y,int state,int value) {//change to update
		this.x = x;
		this.y = y;
		this.state = state;
		this.value=value;
		this.answer="";
	}



	public String action(){
		if(!this.unload()){						//if he has a ghost
			if(!this.stun()){					//if he can stun an opponent
				if(!this.ghostAround()){		//if he can bust a ghost
					if(!this.closestGhost()){	//if he can go to catch a ghost
					    travel();				//if these is no ghost visible
					    
					    }
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
			System.err.println("unload");
			return true;
		} 
		return false;
	}
	
	

	private boolean stun() {//stun an opponent if close enough
		if(this.canBust()){
			for (int i = 0; i < Player.bustersPerPlayer; i++) {
				if(Player.opponentBusters[i].getVisible() && Player.opponentBusters[i].canBeBusted()){
					int OX=Player.opponentBusters[i].getX();
					int OY=Player.opponentBusters[i].getY();
					int BX=this.x;
					int BY=this.y;
					if((OX-BX)*(OX-BX)+(OY-BY)*(OY-BY)<1760*1760){
						answer="STUN "+i;
						stunTurn=Player.nbturn;
						Player.opponentBusters[i].setStunedTurn(Player.nbturn);
						System.err.println("stun");
						return true;
					}
				}
			}			
		}

		return false;
	}

	
	private boolean ghostAround(){//if a ghost around, bust
		for (int i = 0; i < Player.ghostCount; i++) {
			if(Player.ghosts[i].getVisible()){
			
				int GX=Player.ghosts[i].getX();
				int GY=Player.ghosts[i].getY();
				int BX=this.x;
				int BY=this.y;
				if((GX-BX)*(GX-BX)+(GY-BY)*(GY-BY)<1760*1760){// && (GX-BX)*(GX-BX)+(GY-BY)*(GY-BY)>900
					answer="BUST "+Player.ghosts[i].getID();
					System.err.println("bust");
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
		System.err.println("go");
		return true;
	}
	
	
	private void travel() {//peut être fait de façon beaucoups plus efficace.
		/*
		 * les x a checker vont de 2000 à 14000 soit 7 étapes 1 à 7
		 * les y a checker vont de 2000 à 6000  soit 3 étapes 1 à 3
		 */
			int boardTravel=Player.boardTravelOdd;
			if(id%2==0){
				boardTravel=Player.boardTravelEven;
			}		
		
		int xTravel;//=Player.boardTravel%7;
		int yTravel;//=Player.boardTravel/7;
		do{

			xTravel=boardTravel%8;
			yTravel=(boardTravel/7)%4;
			xTravel+=1;
			yTravel+=1;
			if(id%2==0){yTravel=4-yTravel;}
			xTravel*=2000;
			yTravel*=2000;

			System.err.println((xTravel-x)*(xTravel-x)+(yTravel-y)*(yTravel-y));
		}while((xTravel-x)*(xTravel-x)+(yTravel-y)*(yTravel-y)<2000*2000);
			if(id%2==0){
				Player.boardTravelEven++;
			}
			if(id%2!=0){
				Player.boardTravelOdd++;
			}
		System.err.println("travel");
		answer="MOVE "+xTravel+" "+yTravel;
	}
	
	private boolean canBust(){
		if (Player.nbturn>stunTurn+20){
			return true;
		}
		return false;
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
	


	public void update(int x, int y,int value) {
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
	private int stunedTurn;//last turn it has been stuned
	private boolean visible;
	
	public OpponentBuster(int id) {
		this.id=id;
		this.stunedTurn=-30;
	}
	
	public void update(int x, int y,int state,int value) {
		this.x = x;
		this.y = y;
		this.state = state;
		this.value=value;
		this.visible=true;
	}
	
	public boolean canBeBusted() {
		if(state==2){
			return true;
		}
		return false;
	}

//———————————————————
//——getters setters——
//———————————————————
	public void setStunedTurn(int nbTurn){
		this.stunedTurn=nbTurn;
	}
	
	public void setVisible(boolean set){
		this.visible=set;
	}
	
	public boolean getVisible(){
		return visible;
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