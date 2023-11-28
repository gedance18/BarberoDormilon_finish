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

        // Paso 1: Entrada de Usuario
		int Barberos=2, customerId=1, numeroDeClientes=100, numSillas;
		
		Scanner sc = new Scanner(System.in);
		
		System.out.println("Cuantos barberos hay?:");
    	Barberos=sc.nextInt();
    	
    	System.out.println("Cuantas sillas hay?"
    			+ " chairs(N):");
    	numSillas=sc.nextInt();


        // Paso 2: Configuración Inicial
		ExecutorService exec = Executors.newFixedThreadPool(12);
    	Tienda shop = new Tienda(Barberos, numSillas);
    	Random r = new Random();
       	    	
        System.out.println("\nTienda abierta con "
        		+Barberos+" barbero(s)\n");
        
        long startTime  = System.currentTimeMillis();

        // Paso 3: Creación de Threads de Barberos
        for(int i=1; i<=Barberos;i++) {
        	
        	Barbero barber = new Barbero(shop, i);
        	Thread thbarber = new Thread(barber);
            exec.execute(thbarber);
        }

        // Paso 4: Creación de Threads de Clientes
        for(int i=0;i<numeroDeClientes;i++) {
        
            Cliente customer = new Cliente(shop);
            customer.setInTime(new Date());
            Thread thcustomer = new Thread(customer);
            customer.setclienteId(customerId++);
            exec.execute(thcustomer);
            
            try {
            	
            	double val = r.nextGaussian() * 2000 + 2000;
            	int millisDelay = Math.abs((int) Math.round(val));
            	Thread.sleep(millisDelay);
            }
            catch(InterruptedException iex) {
            
                iex.printStackTrace();
            }
            
        }

        // Paso 5: Finalización de la Simulación
        exec.shutdown();
        exec.awaitTermination(12, SECONDS);


        // Paso 6: Cálculo de Estadísticas y Tiempo Total
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

        // Paso 7: Cierre del Programa
        sc.close();
    }

    // Paso 8: Ejecución Continua de Threads de Barberos
    // Los Threads de barbero siguen ejecutándose en un bucle infinito.
}
 
class Barbero implements Runnable {

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
 
    public int getClienteId() {
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
 
    public void run() {
    
        quierenCortarseElPelo();
    }
    private synchronized void quierenCortarseElPelo() {
    
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
    
        this.sillas = numeroDeSillas;
        listClientes = new LinkedList<Cliente>();
        this.barberos = numeroDeBarberos;
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
        synchronized (listClientes) {

            while(listClientes.size()==0) {
            
                System.out.println("\nEl Barbero "+barberoId+" esta esperando "
                		+ "a los clientes y esta durmiendo en su silla");
                
                try {
                
                    listClientes.wait();
                }
                catch(InterruptedException iex) {
                
                    iex.printStackTrace();
                }
            }
            
            customer = (Cliente)((LinkedList<?>) listClientes).poll();
            
            System.out.println("El Cliente "+customer.getClienteId()+
            		" busca al barbero dormido y si lo esta lo despierta, el cliente "+customer.getClienteId()+" ha despertado al "
            		+ "barbero "+barberoId);
        }
        
        int millisDelay=0;
                
        try {
        	
        	barberosDisponibles--;

            System.out.println("El Barbero "+barberoId+" esta cortando al cliente "+
            		customer.getClienteId()+ " por lo tanto si entra un cliente tendra que esperar");
        	
            double val = r.nextGaussian() * 2000 + 4000;
        	millisDelay = Math.abs((int) Math.round(val));
        	Thread.sleep(millisDelay);
        	
        	System.out.println("\nSe ha completado el corte de pelo del cliente "+
        			customer.getClienteId()+" por el barbero " +
        			barberoId +" en "+millisDelay+ " millisegundos.");
        
        	totalCortesDePelo.incrementAndGet();

            if(listClientes.size()>0) {
            	System.out.println("El Barbero "+barberoId+
            			" avisa al o los cliente que estan esperando "
            			+ "y duerme hasta que llegue un cliente de fuera de la tienda o de la sala de espera");
            }
            
            barberosDisponibles++;
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
        
            if(listClientes.size() == sillas) {
            
                System.out.println("\nNo hay sillas disponibles "
                		+ "para el cliente "+customer.getClienteId()+
                		" asi que el cliente decide irse de la tienda");
                
              clientesPerdidos.incrementAndGet();
                
                return;
            }
            else if (barberosDisponibles > 0) {

            	((LinkedList<Cliente>) listClientes).offer(customer);
				listClientes.notify();
			}
            else {

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
