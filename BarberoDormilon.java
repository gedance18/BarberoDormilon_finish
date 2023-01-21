import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BarberoDormilon {
	
	public static void main (String a[]) throws InterruptedException {	
		
		int Barberos=2, customerId=1, numeroDeClientes=100, numSillas;	//initializing the number of barber and customers
		
		Scanner sc = new Scanner(System.in);
		
		System.out.println("Cuantos barberos hay?:");			//input M barbers
    	Barberos=sc.nextInt();
    	
    	System.out.println("Cuantas sillas hay?"			//input N waiting chairs
    			+ " chairs(N):");
    	numSillas=sc.nextInt();
    	
//    	System.out.println("Enter the number of customers:");			//inout the number of customers for the shop
//    	numeroDeClientes=sc.nextInt();
    	
		ExecutorService exec = Executors.newFixedThreadPool(12);		//initializing with 12 threads as multiple of cores in the CPU, here 6
    	Tienda shop = new Tienda(Barberos, numSillas);				//initializing the barber shop with the number of barbers
    	Random r = new Random();  										//a random number to calculate delays for customer arrivals and haircut
       	    	
        System.out.println("\nTienda abierta con "
        		+Barberos+" barbero(s)\n");
        
        long startTime  = System.currentTimeMillis();					//start time of program
        
        for(int i=1; i<=Barberos;i++) {								//generating the specified number of threads for barber
        	
        	Barbero barber = new Barbero(shop, i);
        	Thread thbarber = new Thread(barber);
            exec.execute(thbarber);
        }
        
        for(int i=0;i<numeroDeClientes;i++) {								//customer generator; generating customer threads
        
            Cliente customer = new Cliente(shop);
            customer.setInTime(new Date());
            Thread thcustomer = new Thread(customer);
            customer.setclienteId(customerId++);
            exec.execute(thcustomer);
            
            try {
            	
            	double val = r.nextGaussian() * 2000 + 2000;			//'r':object of Random class, nextGaussian() generates a number with mean 2000 and
            	int millisDelay = Math.abs((int) Math.round(val));		//standard deviation as 2000, thus customers arrive at mean of 2000 milliseconds
            	Thread.sleep(millisDelay);								//and standard deviation of 2000 milliseconds
            }
            catch(InterruptedException iex) {
            
                iex.printStackTrace();
            }
            
        }
        
        exec.shutdown();												//shuts down the executor service and frees all the resources
        exec.awaitTermination(12, SECONDS);								//waits for 12 seconds until all the threads finish their execution
 
        long elapsedTime = System.currentTimeMillis() - startTime;
        
        System.out.println("\nTienda cerrada");
        System.out.println("\nTiempo total transcurrido en segundos"
        		+ " para servir a "+numeroDeClientes+" clientes por "
        		+Barberos+" barberos con "+numSillas+
        		" sillas en la sala de espera es: "
        		+TimeUnit.MILLISECONDS
        	    .toSeconds(elapsedTime));
        System.out.println("\nTotal de clientes: "+numeroDeClientes+
        		"\nTotal de clientes que se cortan el pelo: "+shop.getTotalCortesDePelo()
        		+"\nTotal de clientes perdidos: "+shop.getClientesPerdidos());
               
        sc.close();
    }
}
 
class Barbero implements Runnable {										// initializing the barber

    Tienda shop;
    int barberId;
 
    public Barbero(Tienda shop, int barberId) {
    
        this.shop = shop;
        this.barberId = barberId;
    }
    
    public void run() {
    
        while(true) {
        
            shop.cortarPelo(barberId);
        }
    }
}

class Cliente implements Runnable {

    int clienteId;
    Date inTime;
 
    Tienda shop;
 
    public Cliente(Tienda shop) {
    
        this.shop = shop;
    }
 
    public int getClienteId() {										//getter and setter methods
        return clienteId;
    }
 
    public Date getInTime() {
        return inTime;
    }
 
    public void setclienteId(int customerId) {
        this.clienteId = customerId;
    }
 
    public void setInTime(Date inTime) {
        this.inTime = inTime;
    }
 
    public void run() {													//customer thread goes to the shop for the haircut
    
        quierenCortarseElPelo();
    }
    private synchronized void quierenCortarseElPelo() {							//customer is added to the list
    
        shop.add(this);
    }
}
 
class Tienda {

	private final AtomicInteger totalCortesDePelo = new AtomicInteger(0);
	private final AtomicInteger clientesPerdidos = new AtomicInteger(0);
	int sillas, barberos, barberosDisponibles;
    List<Cliente> listClientes;
    
    Random r = new Random();	 
    
    public Tienda(int numeroDeBarberos, int numeroDeSillas){
    
        this.sillas = numeroDeSillas;														//number of chairs in the waiting room
        listClientes = new LinkedList<Cliente>();						//list to store the arriving customers
        this.barberos = numeroDeBarberos;									//initializing the total number of barbers
        barberosDisponibles = numeroDeBarberos;
    }
 
    public AtomicInteger getTotalCortesDePelo() {
    	
    	totalCortesDePelo.get();
    	return totalCortesDePelo;
    }
    
    public AtomicInteger getClientesPerdidos() {
    	
    	clientesPerdidos.get();
    	return clientesPerdidos;
    }
    
    public void cortarPelo(int barberoId)
    {
        Cliente customer;
        synchronized (listClientes) {									//listCustomer is a shared resource so it has been synchronized to avoid any
        															 	//unexpected errors in the list when multiple threads access it
            while(listClientes.size()==0) {
            
                System.out.println("\nEl Barbero "+barberoId+" esta esperando "
                		+ "a los clientes y esta durmiendo en su silla");
                
                try {
                
                    listClientes.wait();								//barber sleeps if there are no customers in the shop
                }
                catch(InterruptedException iex) {
                
                    iex.printStackTrace();
                }
            }
            
            customer = (Cliente)((LinkedList<?>) listClientes).poll();	//takes the first customer from the head of the list for haircut
            
            System.out.println("El Cliente "+customer.getClienteId()+
            		" busca al barbero dormido y si lo esta lo despierta, el cliente "+customer.getClienteId()+" ha despertado al "
            		+ "barbero "+barberoId);
        }
        
        int millisDelay=0;
                
        try {
        	
        	barberosDisponibles--; 										//decreases the count of the available barbers as one of them starts
        																//cutting hair of the customer and the customer sleeps
            System.out.println("El Barbero "+barberoId+" esta cortando al cliente "+
            		customer.getClienteId()+ " por lo tanto si entra un cliente tendra que esperar");
        	
            double val = r.nextGaussian() * 2000 + 4000;				//time taken to cut the customer's hair has a mean of 4000 milliseconds and
        	millisDelay = Math.abs((int) Math.round(val));				//and standard deviation of 2000 milliseconds
        	Thread.sleep(millisDelay);
        	
        	System.out.println("\nSe ha completado el corte de pelo del cliente "+
        			customer.getClienteId()+" por el barbero " +
        			barberoId +" en "+millisDelay+ " millisegundos.");
        
        	totalCortesDePelo.incrementAndGet();
            															//exits through the door
            if(listClientes.size()>0) {
            	System.out.println("El Barbero "+barberoId+					//barber finds a sleeping customer in the waiting room, wakes him up and
            			" avisa al o los cliente que esta esperando en "					//and then goes to his chair and sleeps until a customer arrives
            			+ "la sala de espera y duerme hasta que llegue un cliente de fuera de la tienda o de la sala de espera");
            }
            
            barberosDisponibles++;											//barber is available for haircut for the next customer
        }
        catch(InterruptedException iex) {
        
            iex.printStackTrace();
        }
        
    }
 
    public void add(Cliente customer) {
    
        System.out.println("\nEl Cliente "+customer.getClienteId()+
        		" entre a la tienda a las "
        		+customer.getInTime());
 
        synchronized (listClientes) {
        
            if(listClientes.size() == sillas) {							//No chairs are available for the customer so the customer leaves the shop
            
                System.out.println("\nNo hay sillas disponibles "
                		+ "para el cliente "+customer.getClienteId()+
                		" asi que el cliente decide irse de la tienda");
                
              clientesPerdidos.incrementAndGet();
                
                return;
            }
            else if (barberosDisponibles > 0) {							//If barber is available then the customer wakes up the barber and sits in
            															//the chair
            	((LinkedList<Cliente>) listClientes).offer(customer);
				listClientes.notify();
			}
            else {														//If barbers are busy and there are chairs in the waiting room then the customer
            															//sits on the chair in the waiting room
            	((LinkedList<Cliente>) listClientes).offer(customer);
                
            	System.out.println("Todos los barberos estan ocupados asi que el cliente "+
            			customer.getClienteId()+
                		" ocupa una silla en la sala de espera");
                 
                if(listClientes.size()==1)
                    listClientes.notify();
            }
        }
    }
}
