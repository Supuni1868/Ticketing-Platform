package com.example.ticketing;

import org.springframework.web.bind.annotation.*;
import javax.annotation.PostConstruct;
import org.springframework.http.ResponseEntity;


@RestController
@RequestMapping("/api/ticketing")
public class TicketingController {
    private Configuration config;
    private TicketPool ticketPool;

    @PostConstruct
    public void init() {
        config = new Configuration();
    }


    @PostMapping("/start")
    public ResponseEntity<?> startTicketing(@RequestBody TicketingRequest request) {
        int totalTickets = request.getTotalTickets();
        int ticketReleaseRate = request.getTicketReleaseRate();
        int customerRetrievalRate = request.getCustomerRetrievalRate();
        int maxTicketCapacity = request.getMaxTicketCapacity();

        // Validate input
        if (totalTickets < 0) {
            return ResponseEntity.badRequest().body("Invalid Input, Total tickets should be zero or a positive number.");
        }
        if (ticketReleaseRate <= 0 || customerRetrievalRate <= 0 || maxTicketCapacity <= 0) {
            return ResponseEntity.badRequest().body("Invalid Input, Rates and capacity should be greater than zero.");
        }
        if (totalTickets > maxTicketCapacity) {
            return ResponseEntity.badRequest().body("Invalid Input, Total tickets should be less than the max capacity.");
        }

        // Update Configuration
        config.setTotalTickets(totalTickets);
        config.setTicketReleaseRate(ticketReleaseRate);
        config.setCustomerRetrievalRate(customerRetrievalRate);
        config.setMaxTicketCapacity(maxTicketCapacity);
        config.saveConfiguration();

        // Initialize TicketPool and start threads
        ticketPool = new TicketPool(maxTicketCapacity);
        ticketPool.initializeTickets(totalTickets > 0 ? totalTickets - 1 : 0);
        Vendor vendor = new Vendor(ticketPool, ticketReleaseRate);
        Customer customer = new Customer(ticketPool, customerRetrievalRate);
        new Thread(vendor).start();
        new Thread(customer).start();

        return ResponseEntity.ok().body(new SuccessResponse("Ticketing system started with " + totalTickets + " total tickets."));
    }

    public static class SuccessResponse {
        private String message;

        public SuccessResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }


    @GetMapping("/config")
    public ResponseEntity<Configuration> getConfig() {
        config.loadConfiguration(); // Ensure the latest configuration is loaded from file
        return ResponseEntity.ok(config);
    }



}
