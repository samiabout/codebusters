import java.util.*;

import sun.net.www.content.text.plain;

import java.io.*;
import java.math.*;

/**
 * Send your busters out into the fog to trap ghosts and bring them home!
 **/
class Player {
	
	/*
	 * TODO
	 * 
	 * peut-être qu'il faut compter un tour sur 2 pour stun. 
	 * 
	 * ===idées d'amélioration===
	 * 
	 * les ghost n'on pas la même endurence!!!!!!!!!!!!!!!!!!!
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
    			ghosts[i].setTracked(false);
    			ghosts[i].setBlustersBlusting(0);
    			ghosts[i].setOpponentBlustersBlusting(0);
    			ghosts[i].setBusted(false);
    			
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
					busters[entityId-bustersPerPlayer*myTeamId].update(entityId,x, y, state,value);
					bustersIndex++;
				} else if (type == myTeamId+1) {//ghosts    (myTeamId+1 is not a constant => not suitable for switch/case)
					ghosts[entityId].update(x, y,value,state);
					ghostIndex++;
				} else { //=-1 || =1   && !ghost => opponent ghost 
					opponentBusters[entityId-bustersPerPlayer*((myTeamId+1)%2)].update(entityId,x, y, state,value);
					opponentBustersIndex++;
				}
				//System.err.println(bustersIndex);System.err.println(opponentBustersIndex);System.err.println(ghostIndex);
                
            }
            
            for (int i = 0; i < bustersPerPlayer; i++) {//permet de détecter un ghost perdu par un buster stuned
            	if(busters[i].getState()==1){
            		for (int u = 0; u < bustersPerPlayer; u++) {//permet d'empécher d'aller chercher un ghost déjà blusted
            			if(busters[u].getAimGhost()==busters[i].getvalue()){
            				busters[u].setAim(false);
            			}
            		}
            		ghosts[busters[i].getvalue()].setRelevant(false);//permet de détecter un ghost perdu par un buster stuned(reste true)
            		ghosts[busters[i].getvalue()].setTracked(true);//permet d'empêcher que deux blusters aillent chercher le même ghost
            	}
            }
            for (int i = 0; i < ghostCount; i++) {//savoir s'il y autant de busters que d'opponent busters sur le même ghost
    			if((float)ghosts[i].getBlustersBlusting()==(float)((float)ghosts[i].getValue())/2){
    				ghosts[i].setBusted(true);
    			}
    			if(ghosts[i].getState()>22 && nbturn*2<300){//ne pas prendre les ghost qui ont trop d'endurance en début de partie
    				ghosts[i].setToEarly(true);
    			}
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
	boolean aim;
	int aimX;
	int aimY;
	int aimGhost;
	
	public Buster(int id) {
		this.id=id;
		this.stunTurn=-30;
		
		this.aim=false;
		this.aimX=30000;
		this.aimY=30000;
	}	
	
	public void update(int id ,int x, int y,int state,int value) {//change to update
		this.id=id;
		this.x = x;
		this.y = y;
		this.state = state;
		this.value=value;
		this.answer="";
		
		if(state==3) {
			Player.ghosts[value].setBlustersBlusting(Player.ghosts[value].getBlustersBlusting());
		}
		
		if((aimX-x)*(aimX-x)+(aimY-y)*(aimY-y)<1000*1000 ){//1760*1760
			this.aim=false;
			Player.ghosts[aimGhost].setRelevant(false);
		}
		
	}



	public String action(){
		answer="MOVE "+aimX+" "+aimY;
		//if(!dodge()){									//esquive if carry a ghost
			if(!this.unload()){						//if he has a ghost
				if(!this.stun()){					//if he can stun an opponent
					if(!this.ghostAround()){		//if he can bust a ghost
						//if(aim  && !helpBuster())//if there or even nb of busters of each team on same ghost
						{			
							//if(!aim)
							{
								if(!this.closestGhost()){	//if he can go to catch a ghost
								    travel();				//if these is no ghost visible && may stun
								    
								    }						
							}
						}
					}	
				}
			}			
		//}

		return answer;
	}


	private boolean dodge(){
		if(state == 1){
			for (int i = 0; i < Player.bustersPerPlayer; i++) {
				if(Player.opponentBusters[i].getVisible() ){//visible and carry a ghost
						int OX=Player.opponentBusters[i].getX();
						int OY=Player.opponentBusters[i].getY();
						int BX=this.x;
						int BY=this.y;
						if(((OX-BX)*(OX-BX)+(OY-BY)*(OY-BY))<1800*1800){
							answer="MOVE "+(2*BX-OX)+" "+(2*BY-OY);
							System.err.println("dodge");
							return true;
						}
					}
				}			
			}
		return false;
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
				if(Player.opponentBusters[i].getVisible() && Player.opponentBusters[i].canBeBusted() ){//visible and carry a ghost
					int OX=Player.opponentBusters[i].getX();
					int OY=Player.opponentBusters[i].getY();
					int BX=this.x;
					int BY=this.y;
					if(((OX-BX)*(OX-BX)+(OY-BY)*(OY-BY))<1760*1760){
						answer="STUN "+Player.opponentBusters[i].getId();
						stunTurn=Player.nbturn;
						Player.opponentBusters[i].setStunedTurn(Player.nbturn);
						System.err.println("stun");
						return true;
					}/*else{
						answer="MOVE "+Player.opponentBusters[i].getX()+" "+Player.opponentBusters[i].getY();
						return true;
					}*/
					
				}
			}			
		}

		return false;
	}

	
	private boolean ghostAround(){//if a ghost around, bust
		for (int i = 0; i < Player.ghostCount; i++) {
			if(Player.ghosts[i].getVisible() && !Player.ghosts[i].getToEarly()){
				
				if(Player.ghosts[i].getBusted()){//autant de busters dans chaque équipe
					for (int u = 0; u < Player.bustersPerPlayer; u++) {
						if(Player.opponentBusters[u].getVisible() ){//visible and carry a ghost
							if(Player.opponentBusters[u].getValue()==value){
								boolean firstStun=true;//check if another bluster hasn't stuned
									for (int a = 0; a < id-(Player.myTeamId*Player.bustersPerPlayer); a++) {
										if(Player.busters[a].getvalue()==value){
											firstStun=false;
										}
									}
								
								if(firstStun){
									int OX=Player.opponentBusters[u].getX();
									int OY=Player.opponentBusters[u].getY();
									int BX=this.x;
									int BY=this.y;
									if(((OX-BX)*(OX-BX)+(OY-BY)*(OY-BY))<1760*1760){
										answer="STUN "+Player.opponentBusters[u].getId();
										stunTurn=Player.nbturn;
										Player.opponentBusters[u].setStunedTurn(Player.nbturn);
										System.err.println("stun");
										return true;
										}									
								}
							}
						}
					}
				}
			
				int GX=Player.ghosts[i].getX();
				int GY=Player.ghosts[i].getY();
				int BX=this.x;
				int BY=this.y;
				if((GX-BX)*(GX-BX)+(GY-BY)*(GY-BY)<1760*1760){// && (GX-BX)*(GX-BX)+(GY-BY)*(GY-BY)>900
					answer="BUST "+Player.ghosts[i].getID();
					Player.ghosts[i].setRelevant(false);
					System.err.println("bust");
					return true;
				}
				
			}	
		}
		return false;
	}
	
	
	public boolean helpBuster(){//en construction
		int closestGhostBlusted = 0;//permet de capturer un ghost qui n'est pas le plus proche mais que les adversaires tantent de prendre
		int closestGhostBlustedID = -1;
		for (int i = 0; i < Player.ghostCount; i++) {
			if(Player.ghosts[i].getRelevantPosition()&&Player.ghosts[i].getX()!=0 && !Player.ghosts[i].getToEarly() && Player.ghosts[i].getBusted()){// && !Player.ghosts[i].getTracked()
				int GX=Player.ghosts[i].getX();
				int GY=Player.ghosts[i].getY();
				int BX=this.x;
				int BY=this.y;
				
				int bonus=0;
				
				
				/*
				 * comparer la décision avec aimX et aimY	
				 */
				
				if(((GX-BX)*(GX-BX)+(GY-BY)*(GY-BY))-(Player.ghosts[i].getState()*500)+bonus>closestGhostBlusted){
					closestGhostBlusted=(GX-BX)*(GX-BX)+(GY-BY)*(GY-BY);
					closestGhostBlustedID=i;
				}
			}	
		}
		return false;
	}
	
	

	private boolean closestGhost(){//go to closest ghost
		int closestGhost = 0; 
		int closestGhostID=-1;
		
		int closestGhostBlusted = 0;//permet de capturer un ghost qui n'est pas le plus proche mais que les adversaires tantent de prendre
		int closestGhostBlustedID = -1;
		
		for (int i = 0; i < Player.ghostCount; i++) {
			if(Player.ghosts[i].getRelevantPosition()&&Player.ghosts[i].getX()!=0 && !Player.ghosts[i].getToEarly() && !Player.ghosts[i].getTracked()){// 
				int GX=Player.ghosts[i].getX();
				int GY=Player.ghosts[i].getY();
				int BX=this.x;
				int BY=this.y;
				
				int bonus=0;
				
				if(Player.ghosts[i].getBusted()){
					bonus = 5000*5000+2000*2000;
				}
				if(((GX-BX)*(GX-BX)+(GY-BY)*(GY-BY))-(Player.ghosts[i].getState()*500)+bonus>closestGhost){
					closestGhost=(GX-BX)*(GX-BX)+(GY-BY)*(GY-BY);
					closestGhostID=i;
				}
			}	
		}
		//System.err.println(closestGhostID);
		//System.err.println(Player.ghosts[closestGhostID].getX());
		if (closestGhostID==-1){return false;}
		answer="MOVE "+Player.ghosts[closestGhostID].getX()+" "+Player.ghosts[closestGhostID].getY();
		
		if(Player.ghosts[closestGhostID].getState()<5 && Player.ghosts[closestGhostID].getValue()<2){	//s'il a peu d'endurance, un seul buster s'en charge
			Player.ghosts[closestGhostID].setTracked(true);
		}
		aim=true;
		aimX=Player.ghosts[closestGhostID].getX();
		aimY=Player.ghosts[closestGhostID].getY();
		aimGhost=closestGhostID;
		System.err.println("go "+nbTurnsBeforeCatsh(x, y, closestGhostID)+" "+closestGhostID);
		return true;
	}
	
	
	private void travel() {
		/*
		 * les x a checker vont de 2000 à 14000 soit 7 étapes 1 à 7
		 * les y a checker vont de 2000 à 6000  soit 3 étapes 1 à 3
		 */
		
		if(this.canBust()){
			for (int i = 0; i < Player.bustersPerPlayer; i++) {
				if(Player.opponentBusters[i].getVisible() && Player.opponentBusters[i].canBeBusted2() ){//visible and carry a ghost
					int OX=Player.opponentBusters[i].getX();
					int OY=Player.opponentBusters[i].getY();
					int BX=this.x;
					int BY=this.y;
					if(((OX-BX)*(OX-BX)+(OY-BY)*(OY-BY))<1760*1760){
						answer="STUN "+Player.opponentBusters[i].getId();
						stunTurn=Player.nbturn;
						Player.opponentBusters[i].setStunedTurn(Player.nbturn);
						System.err.println("stun");
						return;
					}
				}
			}			
		}
		
		
			int boardTravel=Player.boardTravelOdd;
			if(id%2==0){
				boardTravel=Player.boardTravelEven;
			}		
		
		int xTravel;//=Player.boardTravel%7;
		int yTravel;//=Player.boardTravel/7;
		//do{

			xTravel=boardTravel%8;
			yTravel=(boardTravel/8)%4;
			xTravel+=1;
			yTravel+=1;

			xTravel*=2000;
			yTravel*=2000;
			xTravel-=1000;
			yTravel-=1000;
			boardTravel++;
			
			if(id%3==1){
				yTravel=4-yTravel;
				xTravel=
			}			
			//System.err.println((xTravel-x)*(xTravel-x)+(yTravel-y)*(yTravel-y));
		//}while((xTravel-x)*(xTravel-x)+(yTravel-y)*(yTravel-y)<1000*1000);
			if((xTravel-x)*(xTravel-x)+(yTravel-y)*(yTravel-y)<2000*2000){
				if(id%3==0){
					Player.boardTravelEven++;
				}
				if(id%3==1){
					Player.boardTravelOdd++;
				}
			}
			
		System.err.println("travel "+id);
		answer="MOVE "+xTravel+" "+yTravel;
		
		if(id%3==2 && Player.bustersPerPlayer>2){
			answer="MOVE "+(1500+13000*((Player.myTeamId+1)%2))+" "+(1500+6000*((Player.myTeamId+1)%2));
		}
	}
	
	private boolean canBust(){
		if (Player.nbturn>stunTurn+10){
			return true;
		}
		return false;
	}
	
	private int nbTurnsBeforeCatsh(int BX,int BY, int ghostID){
		int GX=Player.ghosts[ghostID].getX();
		int GY=Player.ghosts[ghostID].getY();
		int distance=(int) Math.sqrt((GX-BX)*(GX-BX)+(GY-BY)*(GY-BY));
		int endurance = Player.ghosts[ghostID].getState();
		
		return (int) ( ((distance-1760)/800)+endurance );
		
	}
//———————————————————
//——getters setters——
//———————————————————		
	public void setAim(boolean set) {
		aim=set;
	}
	
	public int getState(){
		return state;
	}
	
	public int getvalue() {
		return value;
	}
	
	public int getAimGhost(){
		return aimGhost;
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
	private int state;
	private boolean visible;
	private boolean tracked;//déjà poursuivi par un buster

	private int blustersBlusting;
	private int opponentBlustersBlusting;
	private boolean busted;//is beeing blusted by an even number of busters and opponentbusters 
	private boolean toEarly;
	
	private boolean relevantPosition;
	
	public Ghost(int id) {
		this.id=id;
	}
	


	public void update(int x, int y,int value,int state) {
		this.visible = true;
		this.x = x;
		this.y = y;
		this.value = value;
		this.state=state;
		this.toEarly=false;
		relevantPosition=true;
	}
	
	

//———————————————————
//——getters setters——
//———————————————————
	public boolean getToEarly(){
		return toEarly;
	}
	
	public void setToEarly(boolean set){
		toEarly=set;
	}
	
	public int getValue(){
		return value;
	}
	
	public int getState(){
		return state;
	}
	
	public void setBusted(boolean set) {
		busted=set;
	}
	
	public boolean getBusted() {
		return busted;
	}
	
	public void setBlustersBlusting(int set){
		blustersBlusting=set;
	}
	
	public void setOpponentBlustersBlusting(int set){
		opponentBlustersBlusting=set;
	}
	
	public int getBlustersBlusting(){
		return blustersBlusting;
	}
	
	public int getOpponentBlustersBlusting(){
		return opponentBlustersBlusting;
	}
	
	public void setTracked(boolean set){
		tracked=set;
	}
	
	public void setRelevant(boolean set) {
		this.relevantPosition=set;
	}
	
	public void setVisible(boolean set){
		this.visible=set;
	}
	
	public boolean getRelevantPosition() {
		return relevantPosition;
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
	
	public boolean getTracked() {
		return tracked;
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
	


	public void update(int id,int x, int y,int state,int value) {
		this.id=id;
		this.x = x;
		this.y = y;
		this.state = state;
		this.value=value;
		this.visible=true;
		
		if(state==3) {
			Player.ghosts[value].setOpponentBlustersBlusting(Player.ghosts[value].getOpponentBlustersBlusting());
		}
	}
	
	public boolean canBeBusted() {//carry a ghost
		if(state==1 ){
			return true;
		}
		return false;
	}
	public boolean canBeBusted2() {
		if(state==2 ){
			return false;
		}
		return true;
	}
//———————————————————
//——getters setters——
//———————————————————
	
	public int getValue(){
		return value;
	}
	
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