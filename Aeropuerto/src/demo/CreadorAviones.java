package demo;

import jade.core.*;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.PlatformController;



	///
	///  Crea 10 agentes
	///


/////////////////////////////////////////////////////////////
//                    ANDA BIEN				               //
/////////////////////////////////////////////////////////////


public class CreadorAviones extends Agent{

	
	//-------------- Atributos ---------------------//
	private static final long serialVersionUID = 1L;
	private Logger myLogger = Logger.getMyLogger(getClass().getName());
	
	
	
	private class Creador extends CyclicBehaviour {
		
		//---------- Atributos -----------------//
		private static final long serialVersionUID = 1L;

		
		//----------- Constructor --------------//
		public Creador(Agent a) {
			super(a);
		}

		//-------- Defino que hace el comportamiento -------------//
		@Override
		public void action() {
			ACLMessage mensaje = receive();
			if (mensaje != null) {	
				PlatformController contenedor = (PlatformController)getContainerController();
				for (int i = 1; i < 11; i++) {
					AgentController c;
					try {
						c = contenedor.createNewAgent("Avion"+i, "demo.AgenteAvionOK", null);
						c.start();				
					} catch (ControllerException e) {
						e.printStackTrace();
					} 
				}
				doDelete();  //----------> El agente se borra
			} else {
				block();
			}
			
		}   // termina el action
		
	}
	
	
	
	
	//---------- metodo setup del agente ----------------//
	protected void setup(){
		
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("CreadorAviones"); 
		sd.setName(getName());
		dfd.setName(getAID());
		dfd.addServices(sd);
		
		try {
			DFService.register(this,dfd);
			addBehaviour(new Creador(this));
		} catch (FIPAException e) {
			myLogger.log(Logger.SEVERE, "Agent "+getLocalName()+" - Cannot register with DF", e);
			doDelete();
		}	
	} // termina el metodo setup
	
	
} // termina la clase principal
