//Salwa Haider


import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class Customer implements Runnable
{
    //Semaphore for maximum number of customers
    public static Semaphore maxCustomers = new Semaphore(5, true);
    
    public static int[] deposit = new int[10]; //Amount of deposit for a particular Customer
    public static int[] withdraw = new int[10]; //Withdrawal amount for a particular Customer
    public static int[] balance = new int[10]; //Balance of each Customer
    public static int[] loan = new int[10]; //Amount of Loan for a particular customer for a transaction
    public static int[] totalLoanAmount = new int[10]; //Total Amount of Loan Taken by a Customer

    //Semaphores for transactions
    public static Semaphore[] RequestBankTeller = new Semaphore[] {new Semaphore(0), new Semaphore(0)};
    public static Semaphore[] depositReceipt = new Semaphore[] {new Semaphore(0), new Semaphore(0)};
    public static Semaphore[] depositComplete = new Semaphore[] {new Semaphore(0), new Semaphore(0)};
    public static Semaphore[] withdrawReceipt = new Semaphore[] {new Semaphore(0), new Semaphore(0)};
    public static Semaphore[] withdrawalComplete = new Semaphore[] {new Semaphore(0), new Semaphore(0)};
    public static Semaphore loanTransactionComplete = new Semaphore(0, true);
    public static Semaphore loanOfficerRequest = new Semaphore(0, true);
    public static Semaphore loanOfficerReceipt = new Semaphore(0, true);
    
    //Semaphores to signal customer is ready
    public static Semaphore[] tellerReady = new Semaphore[]{ new Semaphore(0), new Semaphore(0), new Semaphore(0), new Semaphore(0),new Semaphore(0)};
    public static Semaphore[] loanOfficerReady = new Semaphore[]{ new Semaphore(0), new Semaphore(0), new Semaphore(0), new Semaphore(0),new Semaphore(0)};

    //Semaphores to protect queue data structure
    public static Semaphore waitTellerLine = new Semaphore(1, true);
    public static Semaphore waitOfficerLine = new Semaphore(1, true);

    //Queue represents line for teller and loan officer 
    public static Queue<Integer> TellerQueue = new LinkedList<Integer>(); //Queue for Bank Teller
    public static Queue<Integer> OfficerQueue = new LinkedList<Integer>(); //Queue for loan Officer
    public static Semaphore TellerQueueNotEmpty = new Semaphore(0, true);
    public static Semaphore OfficerQueueNotEmpty = new Semaphore(0, true);

    //Array to assign task to customer
    public static int[] taskCustomer = new int[10]; //Task Allotted to a Customer
    public static int[] tellerServingCustomer = new int[10]; //Teller number Serving the Customer
   
    //private variables to customer
    private int task, i, amount;

    //Method that assigns tasks randomly
    private int assigntask()
    {
        int randomNum;
        randomNum = 1 + (int)(Math.random() * 3);
        return randomNum;
    }

    Customer(int amount)
    {
        //starting value for customer instance
        this.amount = amount;
        balance[amount] = 1000;
        totalLoanAmount[amount] = 0;
    }

    //run method for customer thread
    public void run()  
    {
        for(i = 0; i < 3; i++) //Makes each customer thread run for three times
        {
            try
            {
                maxCustomers.acquire(); //Limits the max number of customer threads running at a time
                task = assigntask(); //Assigns task deposit, withdraw or loan
                taskCustomer[amount] = task;
                //If the task is the deposit task
                if(task == 1)
                {
                    waitTellerLine.acquire(); //critical section for adding to teller queue
                    TellerQueue.add(amount);
                    TellerQueueNotEmpty.release(); //signals teller that queue is not empty
                    waitTellerLine.release();
                    tellerReady[amount].acquire(); //waits till teller is ready for this customer thread
                    deposit[amount] = 100 * (1 + (int)(Math.random()*5));
                    System.out.println("Customer " + amount + " requests of teller " + tellerServingCustomer[amount] + " to make a deposit of $" + deposit[amount]);
                    Thread.sleep(100);
                    RequestBankTeller[tellerServingCustomer[amount]].release(); //signals teller to start processing deposit 
                    depositReceipt[tellerServingCustomer[amount]].acquire(); //waits till teller is done with processing 
                    Thread.sleep(100);
                    System.out.println("Customer " + amount + " gets receipt from teller " + tellerServingCustomer[amount]);
                    depositComplete[tellerServingCustomer[amount]].release(); //signals teller to move to next customer 
                }

                //If task is the withdrawal task
                if(task == 2) //works similar to deposit task
                {
                    waitTellerLine.acquire();
                    TellerQueue.add(amount);
                    TellerQueueNotEmpty.release();
                    waitTellerLine.release();
                    tellerReady[amount].acquire();
                    withdraw[amount] = 100 * (1 + (int)(Math.random() * 5));
                    System.out.println("Customer " + amount + " requests of teller " + tellerServingCustomer[amount] + " to make a withdrawal of $" + withdraw[amount]);
                    Thread.sleep(100);
                    RequestBankTeller[tellerServingCustomer[amount]].release();
                    withdrawReceipt[tellerServingCustomer[amount]].acquire();
                    Thread.sleep(100);
                    System.out.println("Customer " + amount + " gets cash and receipt from teller " + tellerServingCustomer[amount]);
                    withdrawalComplete[tellerServingCustomer[amount]].release();
                }

                //If task is the loan task
                if(task == 3)
                {
                    waitOfficerLine.acquire(); //critical section for adding to loan officer queue
                    OfficerQueue.add(amount);
                    OfficerQueueNotEmpty.release(); //signals loan officer that queue is not empty
                    waitOfficerLine.release();
                    loanOfficerReady[amount].acquire(); //waits till officer is ready for this thread
                    loan[amount] = 100 * (1 + (int)(Math.random()*5));
                    System.out.println("Customer " + amount + " requests of Loan Officer to apply for a loan of $" + loan[amount]);
                    Thread.sleep(100);
                    loanOfficerRequest.release(); //signals teller to start processing loan 
                    loanOfficerReceipt.acquire(); //waits till loan officer is done with processing
                    Thread.sleep(100);
                    System.out.println("Customer " + amount + " gets loan from Loan Officer");
                    loanTransactionComplete.release(); //signals officer to move to next customer
                }

                maxCustomers.release();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        System.out.println("Customer " + amount + " departs the bank");
    }

    public static void main(String args[])
    {
        int i = 0;   
        final int CUSTOMER_NUMBER = 5;
        int sumBalance = 0, sumLoanTotal = 0;

        //Thread created for Loan Officer 
        LoanOfficer officer = new LoanOfficer();   
        Thread OfficerThread = new Thread();
        OfficerThread = new Thread(officer);
        OfficerThread.setDaemon(true);
        OfficerThread.start();
        System.out.println("Loan Officer created ");

        //Threads created for Bank Tellers
        Bankteller teller[] = new Bankteller[2];
        Thread TellerThread[] = new Thread[2];
        for( i = 0; i < 2; ++i )
        {
            teller[i] = new Bankteller(i);
            TellerThread[i] = new Thread( teller[i] );
            TellerThread[i].setDaemon(true);
            TellerThread[i].start();
            System.out.println("Teller " +i+ " created ");
        }

        //Threads created for Customers
        Customer cust[] = new Customer[CUSTOMER_NUMBER];
        Thread customerThread[] = new Thread[CUSTOMER_NUMBER];
        for( i = 0; i < CUSTOMER_NUMBER; ++i )
        {
            cust[i] = new Customer(i);
            customerThread[i] = new Thread( cust[i] );
            customerThread[i].start();
            System.out.println("Customer " +i+ " created ");
        }

        //join customer threads
        for(i = 0; i < CUSTOMER_NUMBER; ++i )
        {
            try
            {
                customerThread[i].join();
                System.out.println("Customer " +i+ " joined by main");
            }

            catch (InterruptedException error)
            {
            }
        }

        //Printing the Bank Simulation Summary
        System.out.println("\n\t\t   Bank Simulation Summary\n");
        System.out.println("\t\tEnding Balance \tLoan Amount\n");
        for(i = 0; i < 5; i++)
        {
            System.out.println("Customer "+i+"\t"+balance[i]+" \t\t "+totalLoanAmount[i]);
            sumBalance = sumBalance + balance[i];
            sumLoanTotal = sumLoanTotal + totalLoanAmount[i];
        }   
        System.out.println("\nTotals\t\t"+sumBalance+"\t\t "+ sumLoanTotal);
        System.out.println("\n\nEnd of simulation!\n");
    }
}

//Class Bankteller responsible for the bank teller thread implementation
class Bankteller implements Runnable
{
    //private variables
    private int nextcustomer;
    private int nextcustomertask;
    private int amount;

    Bankteller(int amount)
    {
        //Setting up of Starting values for each Bankteller instance
        this.amount = amount;
    }

    //run function for bank teller thread
    public void run()
    {
        while(true)
        {
            try
            {
                Customer.TellerQueueNotEmpty.acquire(); //wait till bank teller queue is not empty
                Customer.waitTellerLine.acquire(); //critical section for removing from teller queue
                nextcustomer = Customer.TellerQueue.remove();
                Customer.waitTellerLine.release();
                nextcustomertask = Customer.taskCustomer[nextcustomer];
                Customer.tellerServingCustomer[nextcustomer] = amount;
                System.out.println("Teller " + amount + " begins serving Customer "+ nextcustomer);
                Customer.tellerReady[nextcustomer].release(); //teller signals customer that it is ready to serve 

                //If customer task is deposit
                if(nextcustomertask == 1)
                {
                    Customer.RequestBankTeller[amount].acquire(); //waits for customers request for processing
                    System.out.println("Teller " + amount + " processes deposit for Customer "+ nextcustomer);
                    Thread.sleep(400);
                    Customer.balance[nextcustomer] = Customer.balance[nextcustomer] + Customer.deposit[nextcustomer];
                    Customer.depositReceipt[amount].release(); //signals that processing is done 
                    Customer.depositComplete[amount].acquire(); //wait till customer frees the teller 
                }
                //If customer task is withdrawal
                if(nextcustomertask == 2) //Works Similar to Deposit
                {
                    Customer.RequestBankTeller[amount].acquire();
                    System.out.println("Teller " + amount + " processes withdrawal for Customer "+ nextcustomer);
                    Thread.sleep(400);
                    Customer.balance[nextcustomer] = Customer.balance[nextcustomer] - Customer.withdraw[nextcustomer];
                    Customer.withdrawReceipt[amount].release();
                    Customer.withdrawalComplete[amount].acquire();
                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}

//This is the class for Loan Officer Thread
class LoanOfficer implements Runnable
{
    private int nextcustomer;
    private int nextcustomertask;
    @Override

    public void run()
    {
        while(true)
        {
            try
            {
                Customer.OfficerQueueNotEmpty.acquire(); //wait till loan officer queue is not empty
                Customer.waitOfficerLine.acquire(); //critical section for removing from loan officer queue
                nextcustomer = Customer.OfficerQueue.remove();
                Customer.waitOfficerLine.release();
                nextcustomertask = Customer.taskCustomer[nextcustomer];
                System.out.println("Loan Officer begins serving Customer "+ nextcustomer);
                Customer.loanOfficerReady[nextcustomer].release(); //officer signals customer that it is ready to serve

                if(nextcustomertask == 3)
                {
                    Customer.loanOfficerRequest.acquire(); //waits for customers request for processing 
                    System.out.println("Loan Officer approves loan for Customer "+ nextcustomer);
                    Thread.sleep(400);
                    Customer.totalLoanAmount[nextcustomer] = Customer.totalLoanAmount[nextcustomer] + Customer.loan[nextcustomer];
                    Customer.loanOfficerReceipt.release(); //signals that processing is done
                    Customer.loanTransactionComplete.acquire(); //wait till customer frees the loan officer
                }
            }
            catch (InterruptedException error)
            {
                error.printStackTrace();
            }
        }
    }
}