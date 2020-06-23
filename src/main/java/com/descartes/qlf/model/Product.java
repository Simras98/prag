package com.descartes.qlf.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "product")
public class Product implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private String name;
  private String description;
  private String price;
  private String urlPhoto;
  private ProductCategory category;
  private Customer customer;

  public Product() {}

  public Product(
      long id,
      String name,
      String description,
      String price,
      String urlPhoto,
      ProductCategory category,
      Customer customer) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.price = price;
    this.urlPhoto = urlPhoto;
    this.category = category;
    this.customer = customer;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getPrice() {
    return price;
  }

  public void setPrice(String price) {
    this.price = price;
  }

  public String getUrlPhoto() {
    return urlPhoto;
  }

  public void setUrlPhoto(String urlPhoto) {
    this.urlPhoto = urlPhoto;
  }

  @OneToOne
  @JoinColumn(name = "product_category_id", nullable = false)
  public ProductCategory getCategory() {
    return category;
  }

  public void setCategory(ProductCategory category) {
    this.category = category;
  }

  @OneToOne
  @JoinColumn(name = "customer_id", nullable = false)
  public Customer getCustomer() {
    return customer;
  }

  public void setCustomer(Customer customer) {
    this.customer = customer;
  }
}