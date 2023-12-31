package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        Train train = trainRepository.findById(bookTicketEntryDto.getTrainId()).get();
        List<Ticket> bookedTickets = train.getBookedTickets();
        int seatsBooked = 0;
        for(Ticket ticket : bookedTickets) {
            seatsBooked += ticket.getPassengersList().size();
        }

        if(seatsBooked + bookTicketEntryDto.getNoOfSeats() > train.getNoOfSeats()) {
            throw new Exception("Less tickets are available");
        }

        List<Passenger> passengerList = new ArrayList<>();
        List<Integer> passengerIds = bookTicketEntryDto.getPassengerIds();
        for(int id : passengerIds) {
            passengerList.add(passengerRepository.findById(id).get());
        }
        String[] stations = train.getRoute().split(",");
        int fromStationIndex = -1;
        int toStationIndex = -1;
        for(int i=0; i<stations.length; i++) {
            if(bookTicketEntryDto.getFromStation().toString().equals(stations[i])) {
                fromStationIndex = i;
            }
            if(bookTicketEntryDto.getToStation().toString().equals(stations[i])) {
                toStationIndex = i;
            }
        }
        if(fromStationIndex == -1 || toStationIndex == -1 || toStationIndex - fromStationIndex < 0) {
            throw new Exception("Invalid stations");
        }

        int fare = bookTicketEntryDto.getNoOfSeats() * (toStationIndex - fromStationIndex) * 300;

        Ticket ticket = new Ticket();
        ticket.setPassengersList(passengerList);
        ticket.setTrain(train);
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        ticket.setTotalFare(fare);

        train.getBookedTickets().add(ticket);
        train.setNoOfSeats(train.getNoOfSeats() - bookTicketEntryDto.getNoOfSeats());

        Passenger passenger = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();
        passenger.getBookedTickets().add(ticket);

        trainRepository.save(train);

        return ticketRepository.save(ticket).getTicketId();

    }
}
