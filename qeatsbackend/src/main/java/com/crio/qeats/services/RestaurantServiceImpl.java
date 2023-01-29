
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
        // if(getRestaurantsRequest.getSearchFor() != null){
        //   //if searchFor exists then call search api
        //   return findRestaurantsBySearchQuery(getRestaurantsRequest, currentTime);
        // }
        // log.debug("DEBUG : findAllRestaurantsCloseBy():(RestaurantServiceImple.java) currentTime = "+currentTime.toString());
        long startTimeInMillis = System.currentTimeMillis();
        List<Restaurant> listOfRestaurants = restaurantRepositoryService.findAllRestaurantsCloseBy(latitude, longitude, currentTime, getServingRadiusInKms(currentTime));
        long endTimeInMillis = System.currentTimeMillis();
        log.debug("SERVICE LAYER: findAllRestaurantsCloseBy took :" + (endTimeInMillis - startTimeInMillis));
        // log.debug("DEBUG -------------------- HArdik " + latitude+" "+longitude+" "+currentTime.toString());
        return new GetRestaurantsResponse(listOfRestaurants);
  }

  private Double getServingRadiusInKms(LocalTime currentTime){
      if(isPeakHours(currentTime)){
        return peakHoursServingRadiusInKms;
      }
      return normalHoursServingRadiusInKms;
  }
  private boolean isPeakHours(LocalTime currentTime){

      return isTimeWithinRange(LocalTime.of(7, 59), LocalTime.of(10, 1), currentTime) || 
             isTimeWithinRange(LocalTime.of(12, 59), LocalTime.of(14, 1), currentTime) ||
             isTimeWithinRange(LocalTime.of(18, 59), LocalTime.of(21, 1), currentTime);
  }
  private boolean isTimeWithinRange(LocalTime start,LocalTime end, LocalTime current){
    return current.isAfter(start) && current.isBefore(end);
  }

  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Implement findRestaurantsBySearchQuery. The request object has the search string.
  // We have to combine results from multiple sources:
  // 1. Restaurants by name (exact and inexact)
  // 2. Restaurants by cuisines (also called attributes)
  // 3. Restaurants by food items it serves
  // 4. Restaurants by food item attributes (spicy, sweet, etc)
  // Remember, a restaurant must be present only once in the resulting list.
  // Check RestaurantService.java file for the interface contract.
  @Override
  public GetRestaurantsResponse findRestaurantsBySearchQuery(
      GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {

      final Double latitude = getRestaurantsRequest.getLatitude();
      final Double longitude = getRestaurantsRequest.getLongitude();
      String searchString = getRestaurantsRequest.getSearchFor();

      // log.debug("DEBUG : findAllRestaurantsCloseBy():(RestaurantServiceImple.java) currentTime = "+currentTime.toString());
      long startTimeInMillis = System.currentTimeMillis();
      List<Restaurant> listOfRestaurants = new ArrayList<>();
      
      if(!(searchString.isBlank())){
        //fetch all search results
        List<Restaurant> listByName = restaurantRepositoryService.findRestaurantsByName(latitude, longitude,searchString, currentTime, getServingRadiusInKms(currentTime));
        List<Restaurant> listByAttributes = restaurantRepositoryService.findRestaurantsByAttributes(latitude, longitude,searchString, currentTime, getServingRadiusInKms(currentTime));
        List<Restaurant> listByItemName = restaurantRepositoryService.findRestaurantsByItemName(latitude, longitude,searchString, currentTime, getServingRadiusInKms(currentTime));
        List<Restaurant> listByItemAttribute = restaurantRepositoryService.findRestaurantsByItemAttributes(latitude, longitude,searchString, currentTime, getServingRadiusInKms(currentTime));
        //merge them all in single list
        listOfRestaurants.addAll(listByName);
        listOfRestaurants.addAll(listByAttributes);
        listOfRestaurants.addAll(listByItemName);
        listOfRestaurants.addAll(listByItemAttribute);
      }


      long endTimeInMillis = System.currentTimeMillis();
      log.debug("SERVICE LAYER: findRestaurantsBySearchQuery took :" + (endTimeInMillis - startTimeInMillis));
      return new GetRestaurantsResponse(listOfRestaurants);

  }

}

