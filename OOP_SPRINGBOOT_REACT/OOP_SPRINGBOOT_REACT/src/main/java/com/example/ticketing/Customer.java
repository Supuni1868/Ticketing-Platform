package com.example.ticketing;

public class Customer implements Runnable {
    private final TicketPool ticketPool;
    private final int ticketPurchaseRate;

    public Customer(TicketPool ticketPool, int ticketPurchaseRate) {
        this.ticketPool = ticketPool;
        this.ticketPurchaseRate = ticketPurchaseRate;
    }

    @Override
    public void run() {
        while (true) {
            Ticket ticket = ticketPool.purchaseTicket();
            if (ticket != null) {
                System.out.println("Ticket purchased: " + ticket.getId());
            }

            try {
                Thread.sleep(ticketPurchaseRate);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
