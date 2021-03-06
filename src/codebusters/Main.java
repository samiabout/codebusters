import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Send your busters out into the fog to trap ghosts and bring them home!
 **/
class Player {
	
	/*
	 * TODO
	 * 
	 * a partir de tour 200 jouer 2 par 2
	 * 
	 * ralentir avant de dιlivrer//non! (dιsactivι)
	 * 
	 * ===idιes d'amιlioration===
	 * 
	 * /!\/!\/!\/!\/!\/!\pb quand y a autant de buster de chaqe ιquipe sur le mκme ghost**mal dιtectι
	 * 
	 * amιliorer dodge avec la posssibilitι de stun et vιrifier si on peut s'enfuir et ιventuellement ne pas rentrer en fin de partie=>prioritι now
	 * 
	 * vιrifier si dιjΰ buster avant de le faire
	 * 
	 * 
	 * stun un opponentbuster qui bust un ghost s'ils a moins de 10 de vie  en cas de duel =>prioritι 3//corrigι
	 * 
	 * assomer un buster qui est sur un ghost pour le prendre ΰ sa place //fait non vιrifiι
	 * 
	 * checker si ca vaut le coup de se dιplacer pour aider un buster
	 * 
	 * dans ghotaround vιrifier s'il n'y a pas dιjΰ un alliι qui bust pour l'aider.=>prioritι 2 // fait //non vιrifiι
	 * 
	 * vιrifier la zone de guarde =>prioritι 1 //±fait
	 * 
	 * a partir du tour 200 ajouter une variable nb garde dans la zone de dιpos 
	 * 		-se balader par 2
	 * 		-laisser un buster dans  la zone
	 * 		-ne pas revenir dans la zone de dιpot et esquiver les adversaires
	 * 
	 * 
	 * 
	 */

	static Ghost[] ghosts;
	static Buster busters[];
	static OpponentBuster opponentBusters[];
	static int bustersPerPlayer;
	static int ghostCount;
	static int myTeamId;
	
	static InfluenceArea influenceAreas[];
	
	static int boardTravelEven=0;//travel the board if no ghost is visible 
	static int boardTravelOdd=0;
	
    public static int ghostIndex;   //nb of each entity visble
    static int bustersIndex;
    static int opponentBustersIndex;
    
    static TravelTheWorld theWorld=new TravelTheWorld();
    static boolean worldChecked=false;
    
    
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
        //theWorld.describe();
        
        while (true) {
        	nbturn++;
        	
        	theWorld.setWorld();
        	
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
            
            for (int i = 0; i < bustersPerPlayer; i++) {//permet de dιtecter un ghost perdu par un buster stuned
            	if(busters[i].getState()==1){
            		for (int u = 0; u < bustersPerPlayer; u++) {//permet d'empιcher d'aller chercher un ghost dιjΰ blusted
            			if(busters[u].getAimGhost()==busters[i].getvalue()){
            				busters[u].setAim(false);
            			}
            		}
            		ghosts[busters[i].getvalue()].setRelevant(false);//permet de dιtecter un ghost perdu par un buster stuned(reste true)
            		ghosts[busters[i].getvalue()].setTracked(true);//permet d'empκcher que deux blusters aillent chercher le mκme ghost
            	}
            }
            for (int i = 0; i < ghostCount; i++) {//savoir s'il y autant de busters que d'opponent busters sur le mκme ghost
    			if((float)ghosts[i].getBlustersBlusting()==(float)((float)ghosts[i].getValue())/2 && ghosts[i].getBlustersBlusting()>1){
    				ghosts[i].setBusted(true);
    			}
    			if(ghosts[i].getState()>22 && nbturn*2<300 && !worldChecked){//ne pas prendre les ghost qui ont trop d'endurance en dιbut de partie
    				ghosts[i].setToEarly(true);
    			}
    		}
            
   //zone d'influence TODO
            influenceAreas=new InfluenceArea[bustersPerPlayer];
            //mιcannisme pour crιer plusieurs zones d'influence
            for (int i = 0; i < bustersPerPlayer; i++) {
				influenceAreas[i]=new InfluenceArea();
			}
            

            
            
            
            
            
            
            //long start = System.nanoTime();
            for (int i = 0; i < bustersPerPlayer; i++) {
                  
				//	System.err.println(this.ghostIndex);      
                // Write an action using System.out.println()
                // To debug: System.err.println("Debug messages...");

                 System.out.println(busters[i].action()); // MOVE x y | BUST id | RELEASE
            }
           // System.err.println("time "+(long)(System.nanoTime()-start));
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
	
	private boolean nextStep;
	private int nextX;
	private int nextY;
	
	public Buster(int id) {
		this.id=id;
		this.stunTurn=-30;
		
		this.aim=false;
		this.aimX=30000;
		this.aimY=30000;
		
		this.nextStep=false;
		this.nextX=30000;
		this.nextY=30000;
	}	
	
	public void update(int id ,int x, int y,int state,int value) {//change to update
		this.id=id;
		this.x = x;
		this.y = y;
		this.state = state;
		this.value=value;
		this.answer="";
		
		if(state==3) {
			Player.ghosts[value].setBlustersBlusting(Player.ghosts[value].getBlustersBlusting()+1);
		}
		
		if((aimX-x)*(aimX-x)+(aimY-y)*(aimY-y)<1000*1000 ){//1760*1760
			this.aim=false;
			Player.ghosts[aimGhost].setRelevant(false);
		}
		
	}



	public String action(){
		answer="MOVE "+aimX+" "+aimY;
		if(!dodge()){									//esquive if carry a ghost
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
		}
			
		return answer;
	}


	private boolean dodge(){
		if(state == 1){
			if((x-Player.myTeamId*16000)*(x-Player.myTeamId*16000)+(y-Player.myTeamId*9000)*(y-Player.myTeamId*9000)<4960*4960 || Player.nbturn>140){
				int OX=0;
				int OY=0;
				int BX=this.x;
				int BY=this.y;
				boolean dodge=false;
				for (int i = 0; i < Player.bustersPerPlayer; i++) {
					if(Player.opponentBusters[i].getVisible() && (Player.opponentBusters[i].getState()!=2 || (Player.opponentBusters[i].getState()!=2 && Player.opponentBusters[i].getValue()>2))){//visible and carry a ghost		
							if(((OX-BX)*(OX-BX)+(OY-BY)*(OY-BY))<2200*2200){
								OX+=Player.opponentBusters[i].getX();
								OY+=Player.opponentBusters[i].getY();
								dodge=true;
							}
						}
					}
					if (dodge) {
						answer="MOVE "+(2*BX-OX-800)+" "+(2*BY-OY-800);
						System.err.println("dodge");
						return true;
					}
				}
		
			}
		return false;
	}

	private boolean unload() {//if a ghost is loaded go to base or unload //stun 
		if (state==1){
			if(this.canBust()){
				for (int i = 0; i < Player.bustersPerPlayer; i++) {
					if(Player.opponentBusters[i].getVisible() && Player.opponentBusters[i].canBeBusted2() ){//visible and carry a ghost || protιger le buste que je fait
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
			float distance=1;
			if( (x-Player.myTeamId*16000)*(x-Player.myTeamId*16000)+(y-Player.myTeamId*9000)*(y-Player.myTeamId*9000)<4400*4400 && false){
				distance = (float) ((float)400/Math.sqrt(((float)(x-Player.myTeamId*16000)*(float)(x-Player.myTeamId*16000)+(float)(y-Player.myTeamId*9000)*(float)(y-Player.myTeamId*9000)) ));
			}
			System.err.println(distance);
			if((x-Player.myTeamId*16000)*(x-Player.myTeamId*16000)+(y-Player.myTeamId*9000)*(y-Player.myTeamId*9000)>1600*1600 ){
				answer="MOVE "+((int)(x+distance*(Player.myTeamId*16000-x))+" "+((int)(y+distance*(Player.myTeamId*9000-y))));
			}else answer="RELEASE";
			
			/*if(Player.myTeamId==0){
				if(x*x+y*y>1600*1600){
					answer="MOVE 0 0";
				}else answer="RELEASE";
			}
			if(Player.myTeamId==1){
				if((x-16000)*(x-16000)+(y-9000)*(y-9000)>1600*1600){
					answer="MOVE 16000 9000";
				}else answer="RELEASE";
			}*/
			System.err.println("unload");
			return true;
		} 
		return false;
	}
	
	

	private boolean stun() {//stun an opponent if close enough
		if(this.canBust()){
			for (int i = 0; i < Player.bustersPerPlayer; i++) {
				if(Player.opponentBusters[i].getVisible() && Player.opponentBusters[i].canBeBusted(i) ){//visible and carry a ghost || protιger le buste que je fait
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
				
				if(Player.ghosts[i].getBusted() && (float)((float)Player.ghosts[i].getState()/(float)((float)Player.ghosts[i].getValue()/2))<10){//autant de busters dans chaque ιquipe && ghost a assez peu de vie
					for (int u = 0; u < Player.bustersPerPlayer; u++) {
						if(Player.opponentBusters[u].getVisible() ){//visible and carry a ghost
							if(Player.opponentBusters[u].getValue()==value){
								boolean firstStun=true;//check if another bluster hasn't stuned
									for (int a = 0; a < id-(Player.myTeamId*Player.bustersPerPlayer); a++) {
										if(Player.busters[a].getvalue()==value){//TODO ca ne marche pas
											if(Player.busters[a].getStunTurn()==Player.nbturn){
												firstStun=false;
											}
										}
									}
								
								if(firstStun){
									int OX=Player.opponentBusters[u].getX();
									int OY=Player.opponentBusters[u].getY();
									int BX=this.x;
									int BY=this.y;
									if(canBust() && Player.opponentBusters[u].canBeBusted2()){
										if(((OX-BX)*(OX-BX)+(OY-BY)*(OY-BY))<1760*1760){
											answer="STUN "+Player.opponentBusters[u].getId();
											stunTurn=Player.nbturn;
											Player.opponentBusters[u].setStunedTurn(Player.nbturn);
											System.err.println("stun");
											return true;
											}else{
												stunTurn=Player.nbturn;
												answer="MOVE "+Player.ghosts[i].getX()+" "+Player.ghosts[i].getY();
												System.err.println("move to stun");
												return true;													
											}										
									}

								}
							}
						}
					}
				}
				
				for (int j = 0; j < Player.bustersPerPlayer; j++) {//prioriser le cas ou un allier bust un ghost proche qui a plus de 2 d'endurance
					if(Player.busters[j].getState()==3 && Player.ghosts[Player.busters[j].getvalue()].getState()<3){
						int GX=Player.ghosts[i].getX();
						int GY=Player.ghosts[i].getY();
						int BX=this.x;
						int BY=this.y;
						if((GX-BX)*(GX-BX)+(GY-BY)*(GY-BY)<1760*1760){
							if((GX-BX)*(GX-BX)+(GY-BY)*(GY-BY)<900*900 ){// si trop proche&& state!=3
								if (Player.nbturn==Player.ghosts[i].getTurnTooClose()+1){
									if(GX==BX && GY==BY){
										BX+=800;
										BY+=800;
									}
								answer="Move "+(2*BX-GX)+" "+(2*BY-GY);
								System.err.println("move to bust");
								nextStep=false;
								return true;									
								}else{
									Player.ghosts[i].setTurnTooClose(Player.nbturn);
								}
							}
							answer="BUST "+Player.ghosts[i].getID();
							Player.ghosts[i].setRelevant(false);
							System.err.println("bust");
							nextStep=false;
							return true;
						}
					}
				}
				
				int GX=Player.ghosts[i].getX();
				int GY=Player.ghosts[i].getY();
				int BX=this.x;
				int BY=this.y;
				if((GX-BX)*(GX-BX)+(GY-BY)*(GY-BY)<1760*1760){
					if((GX-BX)*(GX-BX)+(GY-BY)*(GY-BY)<900*900 ){// si trop proche&& state!=3
						if (Player.nbturn==Player.ghosts[i].getTurnTooClose()+1){
							if(GX==BX && GY==BY){
								BX++;
								BY++;
							}
						answer="Move "+(2*BX-GX)+" "+(2*BY-GY);
						System.err.println("move to bust");
						nextStep=false;
						return true;									
						}else{
							Player.ghosts[i].setTurnTooClose(Player.nbturn);
						}
					}
					answer="BUST "+Player.ghosts[i].getID();
					Player.ghosts[i].setRelevant(false);
					System.err.println("bust");
					nextStep=false;
					return true;
				}
				
			}	
		}
		if(nextStep){
			answer="MOVE "+nextX+" "+nextY;
			nextStep=false;
			return true;
			
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
				 * comparer la dιcision avec aimX et aimY	
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
		
		boolean closestGhostBlusted = false;//permet de capturer un ghost qui n'est pas le plus proche mais que les adversaires tantent de prendre mκme si on a dιjΰ un aim
		
		for (int i = 0; i < Player.ghostCount; i++) {
			if(Player.ghosts[i].getRelevantPosition()&&Player.ghosts[i].getX()!=0 && !Player.ghosts[i].getToEarly() && !Player.ghosts[i].getTracked()){// 
				int GX=Player.ghosts[i].getX();
				int GY=Player.ghosts[i].getY();
				int BX=this.x;
				int BY=this.y;
				
				int bonus=0;
				
				if(Player.ghosts[i].getBusted()){
					closestGhostBlusted=true;
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
		if(aim && !closestGhostBlusted){
			return true;
		}
		answer="MOVE "+Player.ghosts[closestGhostID].getX()+" "+Player.ghosts[closestGhostID].getY();
		
		if(Player.ghosts[closestGhostID].getState()<5 && Player.ghosts[closestGhostID].getValue()<2 && Player.nbturn<75){	//s'il a peu d'endurance, un seul buster s'en charge que en dιbut de partie (150 premiers tours)
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
		 * les x a checker vont de 2000 ΰ 14000 soit 7 ιtapes 1 ΰ 7
		 * les y a checker vont de 2000 ΰ 6000  soit 3 ιtapes 1 ΰ 3
		 */
		
		if(this.canBust() && id%3<2){
			for (int i = 0; i < Player.bustersPerPlayer; i++) {
				if(Player.opponentBusters[i].getVisible() && Player.opponentBusters[i].canBeBusted2() ){//visible 
					int OX=Player.opponentBusters[i].getX();
					int OY=Player.opponentBusters[i].getY();
					int BX=this.x;
					int BY=this.y;
					if(((OX-BX)*(OX-BX)+(OY-BY)*(OY-BY))<1760*1760){
						nextStep=true;
						if(Player.opponentBusters[i].getState()==3 &&Player.ghosts[Player.opponentBusters[i].getValue()].getVisible()){
							nextX=Player.ghosts[Player.opponentBusters[i].getValue()].getX();
							nextY=Player.ghosts[Player.opponentBusters[i].getValue()].getY();
						}else {
							nextX=Player.opponentBusters[i].getX();
							nextY=Player.opponentBusters[i].getY();
						}
						answer="STUN "+Player.opponentBusters[i].getId();
						stunTurn=Player.nbturn;
						Player.opponentBusters[i].setStunedTurn(Player.nbturn);
						System.err.println("stun");
						return;
					}
				}
			}			
		}
		
		/*
			int boardTravel=Player.boardTravelOdd;
			if(id%2==0){
				boardTravel=Player.boardTravelEven;
			}		
		
		int xTravel;//=Player.boardTravel%7;
		int yTravel;//=Player.boardTravel/7;
		//do{

			xTravel=boardTravel%8;
			yTravel=(boardTravel/8)%3;
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
		*/
		if(Player.myTeamId==0){
			if(id%3==0){
				if(x==Player.theWorld.nextStepEven().getX() && y==Player.theWorld.nextStepEven().getY()){
					Player.theWorld.nextStepEven().setTraveledEven(true);
				}
				System.err.println("travel "+id+" "+Player.theWorld.nextStepEven().getX()+" "+Player.theWorld.nextStepEven().getY());
				answer="MOVE "+Player.theWorld.nextStepEven().getX()+" "+Player.theWorld.nextStepEven().getY();
			}
			
			if(id%3==1){
				if(x==Player.theWorld.nextStepOdd().getX() && y==9000-Player.theWorld.nextStepOdd().getY()){
					Player.theWorld.nextStepOdd().setTraveledOdd(true);
				}	
				System.err.println("travel "+id+" "+Player.theWorld.nextStepOdd().getX()+" "+(9000-Player.theWorld.nextStepOdd().getY()));
				answer="MOVE "+Player.theWorld.nextStepOdd().getX()+" "+(9000-Player.theWorld.nextStepOdd().getY());
			}	
		}
		
		if(Player.myTeamId==1){//on inverse les valeurs
			if(id%3==0){
				if(16000-x==Player.theWorld.nextStepEven().getX() && y==9000-Player.theWorld.nextStepEven().getY()){
					Player.theWorld.nextStepEven().setTraveledEven(true);
				}
				System.err.println("travel "+id+" "+(16000-Player.theWorld.nextStepEven().getX())+" "+(9000-Player.theWorld.nextStepEven().getY()));
				answer="MOVE "+(16000-Player.theWorld.nextStepEven().getX())+" "+(9000-Player.theWorld.nextStepEven().getY());
			}
			
			if(id%3==1 || (Player.bustersPerPlayer==2 && id%3==2)){
				if(16000-x==Player.theWorld.nextStepOdd().getX() && y==Player.theWorld.nextStepOdd().getY()){
					Player.theWorld.nextStepOdd().setTraveledOdd(true);
				}	
				System.err.println("travel "+id+" "+(16000-Player.theWorld.nextStepOdd().getX())+" "+Player.theWorld.nextStepOdd().getY());
				answer="MOVE "+(16000-Player.theWorld.nextStepOdd().getX())+" "+(Player.theWorld.nextStepOdd().getY());
			}	
		}
		//Player.theWorld.describe();
		
	
		
		
		if((id%3==2||id%3==3) && Player.bustersPerPlayer>2){
			System.err.println(id+" go guard");
			answer="MOVE "+(1550+12900*((Player.myTeamId+1)%2))+" "+(1550+5900*((Player.myTeamId+1)%2));
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
//
//getters setters
//
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getStunTurn(){
		return stunTurn;
	}
	
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
	
	public boolean getAim(){
		return aim;
	}
	
}

//
//
//new class
//
//

class Ghost {
	private int id;
	private int x;
	private int y;
	private int value;
	private int state;
	private boolean visible;
	private boolean tracked;//dιjΰ poursuivi par un buster
	
	private int turnTooClose=-10;//no du tour ou il est trop proche=>dans certains cas,s'il est encerclι il en bouge pas donc si ηa arrive 2 tours d'affilι, on bouge.

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
	
	

//
//getters setters
//
	public int getTurnTooClose(){
		return turnTooClose;
	}
	
	public void setTurnTooClose(int set){
		turnTooClose=set;
	}
	
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

//
//
//new class
//
//

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
	
	public boolean canBeBusted(int i) {//carry a ghost//visible and carry a ghost 
		if(state==1 ){
			return true;
		}
		if (state==3 && (  Player.ghosts[value].getState()/((Player.ghosts[value].getValue()/2) +1) ) <10){// protιger le buste que je fait
			return true;
		}
		if(Player.opponentBusters[i].getState()==3){
			if(Player.ghosts[Player.opponentBusters[i].getValue()].getVisible()){//si trop d'endurance on ne stun pas
				if (Player.ghosts[i].getState()>10) {
					return false;
				}
				return true;
			}
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
//
//getters setters
//
	
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


//
//
//new class
//
//

class TravelTheWorld {//My busters
	
	private WorldArea[] world=new WorldArea[47];
	
	public TravelTheWorld() {
		for (int i = 0; i < world.length; i++) {
			world[i]=new WorldArea(i);
		}
	}
	
	public void describe() {
		for (int i = 0; i < world.length; i++) {
			System.err.println(i+" "+world[i].getX()+" "+world[i].getY()+" "+world[i].getTraveledEven()+" "+world[i].getTraveledOdd());
		}
	}

	public void setWorld(){//vιrifie si tout le monde a ιtι parcouru.
		boolean evenTraveled=true;
		boolean oddTraveled=true;
		for (int i = 0; i < world.length; i++) {
			evenTraveled &= world[i].getTraveledEven();
			oddTraveled  &= world[i].getTraveledOdd();
		}
		if(evenTraveled){
			Player.worldChecked=true;
			for (int i = 0; i < world.length; i++) {
				world[i].setTraveledEven(false);
			}
		}
		if(oddTraveled){
			Player.worldChecked=true;
			for (int i = 0; i < world.length; i++) {
				world[i].setTraveledOdd(false);
			}
		}
	}
	
	public  WorldArea nextStepEven(){
		for (int i = 0; i < world.length; i++) {
			if(!world[i].getTraveledEven()){
				return world[i];
			}
		}
		return world[46];
	}
	public WorldArea nextStepOdd() {
		for (int i = 0; i < world.length; i++) {
			if(!world[i].getTraveledOdd()){
				return world[i];
			}
		}
		return world[46];
	}
	
	public void AreaEvenReached(WorldArea worldArea){
		worldArea.setTraveledEven(true);
	}
	public void AreaOddReached(WorldArea worldArea){
		worldArea.setTraveledOdd(true);
	}
}

//
//
//new class
//
//

class WorldArea {//My busters
	int step;
	int x=0;
	int y=0;
	boolean traveledEven;
	boolean traveledOdd;
	
	public WorldArea(int step) {
		this.step=step;
		if(step<=20){
			this.y=2100;
			this.x=step*800;
		}if (step>20 && step<=25) {
			this.x=16000;
			this.y=2100+(step-20)*800;
		}if(step>=26 && step<=46){
			this.x=16000-(step-26)*800;
			this.y=6900;
		}
		
		this.traveledEven=false;
		this.traveledOdd=false;	
		
		if(this.step==0 ||this.step==1 || this.step==26 || this.step==27){
			this.traveledEven=true;
			this.traveledOdd=true;			
		}

	}



//
//getters setters
//
	public boolean getTraveledEven() {
		return traveledEven;
	}

	public boolean getTraveledOdd() {
		return traveledOdd;
	}
	
	public void setTraveledOdd(boolean set) {
		traveledOdd=set;
	}

	public void setTraveledEven(boolean set) {
		traveledEven=set;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
}

//
//
//new class
//
//

class InfluenceArea{
	private boolean ghostsIn[];
	private boolean bustersIn[];
	private boolean opponentBustersIn[];
	
	public InfluenceArea(int opBusterIndex) {
		ghostsIn=new boolean[Player.ghostCount];
		bustersIn=new boolean[Player.bustersPerPlayer];
		opponentBustersIn=new boolean[Player.bustersPerPlayer];
		
		for (int i = 0; i < Player.ghostCount; i++) {
			ghostsIn[i]=false;
		}
		for (int i = 0; i < Player.bustersPerPlayer; i++) {
			bustersIn[i]=false;
			opponentBustersIn[i]=false;
		}
		
		opponentBustersIn[opBusterIndex]=true;

		setArea();
	}

	private void setArea() {
		boolean everybodyIn=false;
		while (!everybodyIn) {//on rajoute les buster (alliιs et adverses)
			everybodyIn=true;
			for (int i = 0; i < opponentBustersIn.length; i++) {//pour chaque opponent de la zone d'influence
				if(opponentBustersIn[i]==true){
					for (int u = 0; u < bustersIn.length; u++) {//est ce qu'un alliι 
						if (bustersIn[i]==false) {					//qui n'est pas dans la zone le vois
							if(iSeeYou(Player.busters[i], Player.opponentBusters[i])){
								bustersIn[i]=true;						//alors on le rajoute a la zone d'influence
								everybodyIn=false;
							}
						}
						if (opponentBustersIn[i]==true){			//qui est dans la zone voit un opponent  
							for (int a = 0; a < opponentBustersIn.length; a++) {
								if(iSeeYou(Player.busters[a], Player.opponentBusters[a])){
									opponentBustersIn[i]=true;			//alors on rajoute l'opponent ΰ la zone d'influence
									everybodyIn=false;
								}
							}
						}
					}
				}
			}
		}
		
		for (int i = 0; i < bustersIn.length; i++) {//on rajoute les ghosts
			if (bustersIn[i]) {
				for (int u = 0; u < ghostsIn.length; u++) {
					if (iSeeYou(Player.busters[i], Player.ghosts[i])) {
						ghostsIn[i]=true;
					}	
				}
				
			}
		}
		
	}
		
	private boolean iSeeYou(Buster me,Ghost him){
		if(  (me.getX()-him.getX())*(me.getX()-him.getX())+(me.getY()-him.getY())*(me.getY()-him.getY())<2200*2200 && him.getVisible()){
			return true;
		}return false;
	}
	
	private boolean iSeeYou(Buster me,OpponentBuster him){
		if(  (me.getX()-him.getX())*(me.getX()-him.getX())+(me.getY()-him.getY())*(me.getY()-him.getY())<2200*2200 && him.getVisible()){
			return true;
		}return false;
	}
	
}

//
//
//new class
//
//

class mummerBuster{
	private int x;
	private int y;
	private int turnBeforeCanMove;
	private int turnBeforeCanStun;
	
	


	
}


//
//
//new class
//
//
class mummerGhost{
	private int x;
	private int y;	
	private int stamina;
	
	
}




