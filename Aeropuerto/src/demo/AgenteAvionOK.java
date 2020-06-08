package demo;

import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;




public class AgenteAvionOK extends Agent{
	
	
	
	
	//------------- atributos ------------------//
	
	private static final long serialVersionUID = 1L;
	private Logger myLogger = Logger.getMyLogger(getClass().getName());
	Integer pistaAterrizajeAsignada ;   //---> el nro de pista que uso para aterrizar
	
   
	
	
	
	//-------------------------------- comportamiento Aterrizar ----------------------------//
	//
	// 	Recibe un mensaje del AgenteControlador que tiene de contenido un Integer, que es el nro de pista en el que tiene que aterrizar
	// 	una vez recibido este nro entonces llama al comportamiento miWake()
	// 	
	
	private class Aterrizar extends CyclicBehaviour {
       
		//----------- atributos ------------------------//
		private static final long serialVersionUID = 1L;

		//----------- constructor ---------------//
		public Aterrizar(Agent a) {
			super(a);
		}
		
		//----- action ---------------------//
		public void action() {
			ACLMessage  msg = myAgent.receive();
			if(msg != null){			
				String content = msg.getContent();	
				//----------> si el contenido del mensaje!= null y es de tipo INFORM y tambien el contenido de mensaje es un digito (seria el nro de pista que le envio el controlador)
				if((content != null) && (msg.getPerformative() == ACLMessage.INFORM) && (Character.isDigit(msg.getContent().charAt(0))) ){				
					pistaAterrizajeAsignada = Integer.parseInt(content);
					System.out.println("--->  "+getLocalName()+": Recibido el permiso para aterrizar. Ejecutando maniobra de aterrizaje.");
					System.out.println(" . . . . . El avion "+getLocalName()+" esta aterrizando en la pista "+pistaAterrizajeAsignada+". . .  . ");
					addBehaviour(new miWake(myAgent,30000));  // agrego el comportamiento para simular aterrizaje 
				}
			} else {block();}
	    } // END of aterrizar
		
		//--------- realizar las operaciones de limpieza necesarias para la finalizacion correcta del agente
		 @SuppressWarnings("unused")
		protected void takeDown() {
			 System.out.println("El"+ myAgent.getLocalName()+" se retiro");
	  		}	 
	}
	
	
	
	
	
		//------------------------ comportamiento wakeBehaviour ---------------------------//
		// 	Este comportamiento luego de esperar el tiempo que se le paso como parametro, envia un mensaje a el 
		// 	agenteControlador con el "nro de pista" que el utilizo para aterrizar y luego se BORRA EL AGENTE
		// 	
		//
		private class miWake extends WakerBehaviour {
	        
			//--------- atributos ------------------------//
			private static final long serialVersionUID = 1L;
			
			//--------- constructor ---------------//
			public miWake(Agent a, long timeout) {
				super(a, timeout);
			}

	 
			protected void handleElapsedTimeout(){
				System.out.println("--------------------------------------------------------------------");
			    System.out.println("--------------------------------------------------------------------");
			    System.out.println("----> El "+ myAgent.getLocalName()+" aterrizo en la pista "+ pistaAterrizajeAsignada);
			    System.out.println("--------------------------------------------------------------------");
			    System.out.println("--------------------------------------------------------------------");	
				ACLMessage respuesta= new ACLMessage(ACLMessage.CONFIRM);
				 AID agente = new AID("agenteControlador",AID.ISLOCALNAME);
				 respuesta.addReceiver(agente);    
				 respuesta.setContent(Integer.toString(pistaAterrizajeAsignada ));  // envio el numero de pista que uso para aterrizar y que ya esta desocupada
				 myAgent.send(respuesta);
				myAgent.doDelete(); // ---> finaliza el agente avion
			}
			
			// realizar las operaciones de limpieza necesarias para la finalizacion correcta
			 @SuppressWarnings("unused")
			protected void takeDown() {
				 System.out.println("El "+ myAgent.getLocalName()+" se retiro");
			}
	    }
		
	
		
	
	//----------------------------- comportamiento esperar --------------------------------//
	//
	//  Cuando le llega un mensaje .INFORM que tiene de contenido "Esperar" entonces tiene que esperar
	//  un tiempo aleatorio entre 5 y 10 segundos para mandar un mensaje de vuelta, para esto utiliza el comportamiento miTicker()
	//
	//
	private class Esperar extends OneShotBehaviour {
	
	//--------- atributos ------------------------//
	private static final long serialVersionUID = 1L;

	//---------- constructor -----------------//
	public Esperar(Agent a) {
		super(a);
	}
	
	public void action() {
		ACLMessage  msg = myAgent.receive();
		if(msg != null){
		      if(msg.getPerformative()== ACLMessage.INFORM){
				String content = msg.getContent();
				if ((content != null) && (content.equals("Esperar"))){
					System.out.println("--->  El "+ myAgent.getLocalName()+" esta esperando pista.");
					
				}
		      }
		} else {block();}
	}
   }

	
	//--------------------------- comportamiento TickerBehaviour ---------------------------//
	//
	//--------- Este comportamiento miTicker lo usa Esperar, para esperar 30 segundos y luego pide la pista de vuelta a ver si se desocupo la pista ----//
	//
		private class miWakeSol extends WakerBehaviour {    ///----------------------> CAMBIAR EL COMPORTAMIENTO A ONE SHOT
	        
			//--------- atributos ------------------------//
			private static final long serialVersionUID = 1L;
			
			//--------- constructor ---------------//
			public miWakeSol(Agent a, long timeout) {
				super(a, timeout);
			}

	 
			protected void handleElapsedTimeout(){
	    				
				System.out.println("--->  "+myAgent.getLocalName()+ ": Torre de control, solicito pista para aterrizar.");
				addBehaviour(new  solicitarPista(myAgent));  //----->  agrego el comportamiento solicitarPista para que pida pista
	    	}
	    }
	


		// --------------------------- comportamiento Solicitar Pista --------------------------//
		//
		//  Este comportamiento envia un mensaje a el agenteControlador con el contenido "solicitudPista" de tipo REQUEST
		//  solicitarPista va dentro de MiTicker.
		//
		
		private class solicitarPista extends OneShotBehaviour {
			
			private static final long serialVersionUID = -1230696710666210562L;
			
			//---------> constructor
			 public solicitarPista(Agent a){
				 super(a);
			 }
			 
			//---------->  define el comportamineto de la clase
			 public void action(){		 
				 ACLMessage mensaje = new ACLMessage(ACLMessage.REQUEST);
			     AID agente = new AID("agenteControlador",AID.ISLOCALNAME);  // --> envia un mensaje a el Controlador para pedirle una pista
				 mensaje.addReceiver(agente);
				 mensaje.setContent("solicitudPista");
				 myAgent.send(mensaje);
			 }
		 
		}
		
		
		
		
		
	//------------ SETUP --------------------------//
	
	protected void setup() {
		// Registration with the DF 
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();   
		sd.setType("AgenteAvion"); 
		sd.setName(getName());
		sd.setOwnership("TILAB");
		dfd.setName(getAID());
		dfd.addServices(sd);
		try {
			DFService.register(this,dfd);
		} catch (FIPAException e) {
			myLogger.log(Logger.SEVERE, "Agent "+getLocalName()+" - Cannot register with DF", e);
			doDelete();
		}
		
		/////////////////////////////////////////////////////////////
		//                agrego los comportamientos			   //
		/////////////////////////////////////////////////////////////
		
		
		//------> Genero un nro aleatorio para pasarle el paramtero del tiempo que van a tardar en enviar mensaje a el controlador
		//------> en el comportamiento miTicker
		Random nroRandom = new Random();
		Integer segundos = nroRandom.nextInt((100000+1)-50000); 
		addBehaviour(new miWakeSol(this, segundos));
		
		// agrego el comportamiento para esperar si la pista esta ocupada
		addBehaviour(new Esperar(this));
		
		// Agrego el comportamiento Aterrizar
		addBehaviour(new Aterrizar(this));
		
	}
	
}
