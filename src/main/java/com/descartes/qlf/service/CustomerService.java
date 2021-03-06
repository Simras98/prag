package com.descartes.qlf.service;

import com.descartes.qlf.model.Customer;
import com.descartes.qlf.model.Geolocation;
import com.descartes.qlf.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.*;

@Service
public class CustomerService {

  @Autowired private CustomerRepository customerRepository;

  @Autowired private BCryptPasswordEncoder bCryptPasswordEncoder;

  @Qualifier("getJavaMailSender")
  @Autowired
  private JavaMailSender emailSender;

  public void save(Customer customer) {
    customerRepository.save(customer);
  }

  public void delete(Customer customer) {
    customerRepository.delete(customer);
  }

  public Customer getById(Long id) {
    Optional<Customer> customer = customerRepository.findById(id);
    return customer.orElse(null);
  }

  public Customer getByEmail(String email) {
    Optional<Customer> customer = Optional.ofNullable(customerRepository.findByEmail(email));
    return customer.orElse(null);
  }

  public List<Customer> getByType(String type) {
    return customerRepository.findByType(type);
  }

  public List<Customer> getAllCustomers() {
    return customerRepository.findAll();
  }

  public List<Customer> getAllProducers() {
    return customerRepository.findByType("producer");
  }

  public Customer connect(String email, String password) {
    Customer customer = customerRepository.findByEmail(email);
    if (customer != null && bCryptPasswordEncoder.matches(password, customer.getPassword())) {
      return customer;
    } else {
      return null;
    }
  }

  public List<Customer> getBySearch(String keyword) {
    if (keyword != null) {
      return customerRepository.search(keyword);
    } else {
      return customerRepository.findAll();
    }
  }

  public List<Customer> getByDistance(int distance, Customer customer, Geolocation geolocation) {
    List<Customer> filteredCustomerList = new ArrayList<>();
    List<Customer> customerList = getByType("producer");
    if (customer != null) {
      for (Customer value : customerList) {
        if (distance(
                customer.getLatitude(),
                customer.getLongitude(),
                value.getLatitude(),
                value.getLongitude())
            < distance) {
          filteredCustomerList.add(value);
        }
      }
    } else {
      for (Customer value : customerList) {
        if (distance(
                geolocation.getLatitude(),
                geolocation.getLongitude(),
                value.getLatitude(),
                value.getLongitude())
            < distance) {
          filteredCustomerList.add(value);
        }
      }
    }
    return filteredCustomerList;
  }

  public boolean testEmail(String email) {
    Customer customer = customerRepository.findByEmail(email);
    return customer == null;
  }

  public List<Double> addressToCoordinates(String address, String postalCode, String city) {
    address = address.replace(" ", "+").replace("'", "");
    city = city.replace(" ", "+").replace("'", "");
    List<Double> coordinates = new ArrayList<>();
    RestTemplate restTemplate = new RestTemplate();
    String url = "https://geocode.localfocus.nl/geocode.php?q=";
    String urlCompleted = url + address + "+" + postalCode + "+" + city + "&boundary=FRA";
    String body = restTemplate.getForObject(urlCompleted, String.class);
    if (body != null) {
      coordinates.add(
          Double.parseDouble(body.substring(body.indexOf("lat") + 5, body.indexOf("lng") - 2)));
      coordinates.add(
          Double.parseDouble(body.substring(body.indexOf("lng") + 5, body.indexOf("zoom") - 2)));
    } else {
      return Collections.emptyList();
    }
    return coordinates;
  }

  private int distance(double lat1, double lon1, double lat2, double lon2) {
    if ((lat1 == lat2) && (lon1 == lon2)) {
      return 0;
    } else {
      double theta = lon1 - lon2;
      double distance =
          Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
              + Math.cos(Math.toRadians(lat1))
                  * Math.cos(Math.toRadians(lat2))
                  * Math.cos(Math.toRadians(theta));
      distance = Math.acos(distance);
      distance = Math.toDegrees(distance);
      distance = distance * 60 * 1.1515 * 1.609344;
      return (int) Math.round(distance);
    }
  }

  public void resetPassword(String email) throws NoSuchProviderException, NoSuchAlgorithmException {
    SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
    byte[] bytes = new byte[8];
    random.nextBytes(bytes);
    Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    String newPassword = encoder.encodeToString(bytes);
    Customer customer = customerRepository.findByEmail(email);
    customer.setPassword(bCryptPasswordEncoder.encode(newPassword));
    customerRepository.save(customer);
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom("quelaferme@gmail.com");
    message.setTo(email);
    message.setSubject("Votre nouveau mot de passe");
    message.setText(
        "Votre nouveau mot de passe : "
            + newPassword
            + "\n"
            + "https://prag-qlf.herokuapp.com/login");
    emailSender.send(message);
  }
}
