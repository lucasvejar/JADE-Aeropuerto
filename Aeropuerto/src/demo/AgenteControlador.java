package demo;

import java.util.LinkedList;
import java.util.Queue;

import jade.core.*;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;


public class AgenteControlador extends Agent {

	
	//------------ atributos --------------------//
	private static final long serialVersionUID = 1L;
	private Logger myLogger = Logger.getMyLogger(getClass().getName());
	
	
	
	
	//------------------------------------------------//
	private class ControlarAerupuerto extends Behaviour {
		
		//---------- Atributos -----------------//
		private static final long serialVersionUID = 1L;
		private Boolean[] pistas = {false,false,false,false,false};    
		private Queue<String> turnos = new LinkedList<String>();
		private Integer avionesAterrizados = 0;

		
		//----------- Constructor --------------//
		public ControlarAerupuerto(Agent a) {
			super(a);
		}

		
		//------------- busca pista vacia y lo asigna ------------//
		//
		//	El metodo asignaPista busca si hay lugares vacios en pistas[] y si hay lugares vacios le asigna la pista
		//	y le devuelve el "nro de pista que le asigno", en caso contrario le devuelve un "-1"
		//
		public Integer asignaPista() {
			Integer i = 0;
			Integer j = 0;
			Boolean encontrado = true;
			while (i<5 && encontrado) {
				if (pistas[i] == false) {
					encontrado= false;
					pistas[i] = true;
					j = i;
				} else {
				i++;
				}
			}
			if (encontrado) {
				return -1;
			} else {
				return j;
			}
			// devuelve -1 si no hay pistas libres para aterrizar y todas estan ocupadas
		}
		
		
		
		
		
		
				
		//-------- Defino que hace el comportamiento del Controlador cuando le llegan mensajes pidiendo pista, aterrizaje -------------//
		@Override
		public void action() {
			ACLMessage mensaje = receive();
			if (mensaje!= null) {
				
				  	//----------------- el avion pide una pista para aterrizar, si hay una vacia se la asigna y se espera hasta que aterrize, sino informa ----//
				 if ((mensaje.getPerformative()==ACLMessage.REQUEST)&&(mensaje.getContent().equals("solicitudPista"))) {
					ACLMessage res = new ACLMessage(ACLMessage.INFORM);
					res.addReceiver(new AID(mensaje.getSender().getLocalName(), AID.ISLOCALNAME));
					Integer nroPistaLibre = asignaPista();
					if (nroPistaLibre == -1) {  // si viene por este if quiere decir que no hay pistas libres, estan ocupadas
						
						///-----> aca se verifica si ya tiene un turno
						if (turnos.contains(mensaje.getSender().getLocalName())) {
							System.out.println("--->  Controlador Aereo: El avion "+mensaje.getSender().getLocalName()+" ya esta en la cola turnos..");
						} else {
	
							turnos.add(mensaje.getSender().getLocalName()); //-----> Asigno el nombre del avion a la cola de turnos
							System.out.println("--->  Controlador Aereo: El avion "+mensaje.getSender().getLocalName()+" fue asignado a la cola turnos..");
						}		
						res.setContent("Esperar");
						send(res);
					} else {    // si viene por este else quiere decir que hay pista libre y ya le asigno la pista libre
						System.out.println("--->  Controlador Aereo: La pista "+nroPistaLibre+" esta libre, tiene permiso para aterrizar "+mensaje.getSender().getLocalName());   
						ACLMessage men = new ACLMessage(ACLMessage.INFORM);
						men.addReceiver(new AID(mensaje.getSender().getLocalName(), AID.ISLOCALNAME));
						men.setContent(nroPistaLibre.toString());
						send(men);
					}
				}   //--------- el mensaje de tipo Confirm seria el mensaje que envia el avion cuando ya aterrizo, el contenido del mensaje es el numero de pista que uso---//
				else if (mensaje.getPerformative() == ACLMessage.CONFIRM) {
					Integer nroPista = Integer.parseInt(mensaje.getContent());		
					pistas[nroPista] = false;  //----> cambio el estado de esa pista como que esta desocupada 			
					avionesAterrizados++; // incremento el contador de aviones aterrizados, si este contador llega a 10(que son la cantidad de aviones) entonces se termina todo		
				    if (!turnos.isEmpty()) {
				    	ACLMessage men = new ACLMessage(ACLMessage.INFORM);
						   men.addReceiver(new AID(turnos.element(), AID.ISLOCALNAME));
						   men.setContent(nroPista.toString());
						   send(men);
						   turnos.remove();
				    } 	
				}
				
			} else {block();}
		}   // termina el action


		@Override
		public boolean done() {
			if (avionesAterrizados == 10) {  // si los 10 aviones aterrizaron termina el comportamiento
				System.out.println("----> Todos los aviones aterrizaron bien");
				doDelete();
				return true;
			} else {
				return false;
			}
		}


		
	}
	
	
	
	
	//------------ metodo setup ---------------------//
	protected void setup() {
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("AgenteControlador"); 
		sd.setName(getName());
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
		
		addBehaviour(new ControlarAerupuerto(this));
		
	}
	

}
