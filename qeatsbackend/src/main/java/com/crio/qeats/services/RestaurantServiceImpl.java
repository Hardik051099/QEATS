
/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.services;

import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.exchanges.GetRestaurantsRequest;
import com.crio.qeats.exchanges.GetRestaurantsResponse;
import com.crio.qeats.repositoryservices.RestaurantRepositoryService;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class RestaurantServiceImpl implements RestaurantService {

  private final Double peakHoursServingRadiusInKms = 3.0;
  private final Double normalHoursServingRadiusInKms = 5.0;
  @Autowired
  private RestaurantRepositoryService restaurantRepositoryService;


  // TODO: CRIO_TASK_MODULE_RESTAURANTSAPI - Implement findAllRestaurantsCloseby.
  // Check RestaurantService.java file for the interface contract.
  @Override
  public GetRestaurantsResponse findAllRestaurantsCloseBy(
      GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {
        final Double latitude = getRestaurantsRequest.getLatitude();
        final Double longitude = getRestaurantsRequest.getLongitude();
        List<Restaurant> listOfRestaurants = restaurantRepositoryService.findAllRestaurantsCloseBy(latitude, longitude, currentTime, getServingRadiusInKms(currentTime));
     return new GetRestaurantsResponse(listOfRestaurants);
  }

  private Double getServingRadiusInKms(LocalTime currentTime){
      if(isPeakHours(currentTime)){
        return peakHoursServingRadiusInKms;
      }
      return normalHoursServingRadiusInKms;
  }
  private boolean isPeakHours(LocalTime currentTime){
      if((!currentTime.isBefore(LocalTime.parse("08:00"))&&!currentTime.isAfter(LocalTime.parse("10:00")))||
      (!currentTime.isBefore(LocalTime.parse("13:00"))&&!currentTime.isAfter(LocalTime.parse("14:00"))) ||
      (!currentTime.isBefore(LocalTime.parse("19:00"))&&!currentTime.isAfter(LocalTime.parse("21:00")))){
        return true;
      }
      return false;
  }

}

