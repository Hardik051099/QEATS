/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.models;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// Java class that maps to Mongo collection.
@Data
@Document(collection = "restaurants")
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantEntity {

  @Id
  private String id;

  @NotNull
  private String restaurantId;

  @NotNull
  private String name;

  @NotNull
  private String imageUrl;

  @NotNull
  private Double latitude;

  @NotNull
  private Double longitude;

  @NotNull
  private List<String> attributes = new ArrayList<>();

  @NotNull
  private String opensAt;

  @NotNull
  private String closesAt;

  @NotNull
  private String city;


  public String getOpensAt() {
    return opensAt;
  }

  public String getClosesAt() {
    return closesAt;
  }

  

}

