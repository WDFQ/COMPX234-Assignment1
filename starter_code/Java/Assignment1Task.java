import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

import javax.print.attribute.PrintRequestAttribute;

class Assignment1 {

    // Simulation Initialisation
    private static int NUM_MACHINES = 50; // Number of machines in the system that issue print requests
    private static int NUM_PRINTERS = 5; // Number of printers in the system that print requests
    private static int SIMULATION_TIME = 30;
    private static int MAX_PRINTER_SLEEP = 3;
    private static int MAX_MACHINE_SLEEP = 5;
    private static boolean sim_active = true;

    //starts at 5 as the queue can only accept 5 requests at a time
    private static Semaphore machineSemaphore = new Semaphore(5);
    
    //starts at 0 as there is nothing in the queue to print
    private static Semaphore printerSemaphore = new Semaphore(0);

    //lock to protect the print queue so that only one thread can access it at a time
    private static ReentrantLock mutexLock = new ReentrantLock();

    // Create an empty list of print requests
    printList list = new printList();

    public void startSimulation() {

        // ArrayList to keep for machine and printer threads
        ArrayList<Thread> mThreads = new ArrayList<Thread>();
        ArrayList<Thread> pThreads = new ArrayList<Thread>();

        // Create Machine and Printer threads
        // Write code here

        // Create machine threads
        for (int i = 0; i < NUM_MACHINES; i++) {
            machineThread m = new machineThread(i);
            mThreads.add(m);
        }

        // Create printer threads
        for (int i = 0; i < NUM_PRINTERS; i++) {
            printerThread p = new printerThread(i);
            pThreads.add(p);
        }
        
        
        
        // start all the threads
        // Write code here

        for (Thread machine : mThreads) {
            machine.start();
        }


        for (Thread printer : pThreads) {
            printer.start();
        }

        // let the simulation run for some time
        sleep(SIMULATION_TIME);

        // finish simulation
        sim_active = false;

        // Wait until all printer threads finish by using the join function
        // Write code here

        for (Thread printer : pThreads) {
            try {
                printer.join();
            } 
            catch (InterruptedException e) {
                e.printStackTrace(); 
            }
        }
        
        for (Thread machine : mThreads) {
            try {
                machine.join();
            } 
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
    }

    // Printer class
    public class printerThread extends Thread {
        private int printerID;

        public printerThread(int id) {
            printerID = id;
        }

        public void run() {
            while (sim_active) {
               
                // Grab the request at the head of the queue and print it
                // Write code here

                try {
                    
                    //tries to acquire a permit from the printer semaphore if the any machine threads has released one
                    printerSemaphore.acquire();
                    mutexLock.lock();
                    // Simulate printer taking some time to print the document
                    printerSleep();
                    //print the document
                    printDox(printerID);

                    //releases a permit to the machine 
                    machineSemaphore.release();
                

                } catch (Exception e) {
                    //prints exception if there is one
                    e.printStackTrace();
                }
                finally{
                    //releases the lock
                    mutexLock.unlock();
                }
                
            }
        }

        public void printerSleep() {
            int sleepSeconds = 1 + (int) (Math.random() * MAX_PRINTER_SLEEP);
            // sleep(sleepSeconds*1000);
            try {
                sleep(sleepSeconds * 1000);
            } catch (InterruptedException ex) {
                System.out.println("Sleep Interrupted");
            }
        }

        public void printDox(int printerID) {
            System.out.println("Printer ID:" + printerID + " : now available");
            // print from the queue
            list.queuePrint(list, printerID);
        }

    }

    // Machine class
    public class machineThread extends Thread {
        private int machineID;

        public machineThread(int id) {
            machineID = id;
        }

        public void run() {
            while (sim_active) {

                try {
                    //aquires the permit from the machine semaphore if there is one available
                    machineSemaphore.acquire();

                    //tries to acquire the lock to access the print queue
                    machineSleep();
                    mutexLock.lock();
                    printRequest(machineID);
                    
                    //print the document
                    printerSemaphore.release();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally{
                    //releases the lock
                    mutexLock.unlock(); 
                }
            }
                
        }

        // machine sleeps for a random amount of time
        public void machineSleep() {
            int sleepSeconds = 1 + (int) (Math.random() * MAX_MACHINE_SLEEP);

            try {
                sleep(sleepSeconds * 1000);
            } catch (InterruptedException ex) {
                System.out.println("Sleep Interrupted");
            }
        }

        public void printRequest(int id) {
            System.out.println("Machine " + id + " Sent a print request");
            // Build a print document
            printDoc doc = new printDoc("My name is machine " + id, id);
            // Insert it in print queue
            list = list.queueInsert(list, doc);

        }
    }

    private static void sleep(int s) {
        try {
            Thread.sleep(s * 1000);
        } catch (InterruptedException ex) {
            System.out.println("Sleep Interrupted");
        }
    }
}
