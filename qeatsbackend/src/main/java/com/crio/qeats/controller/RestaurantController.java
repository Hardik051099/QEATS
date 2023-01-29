/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.controller;

import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.exchanges.GetRestaurantsRequest;
import com.crio.qeats.exchanges.GetRestaurantsResponse;
import com.crio.qeats.services.RestaurantService;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// TODO: CRIO_TASK_MODULE_RESTAURANTSAPI
// Implement Controller using Spring annotations.
// Remember, annotations have various "targets". They can be class level, method level or others.

@Log4j2
@RestController
@RequestMapping(RestaurantController.RESTAURANT_API_ENDPOINT)
public class RestaurantController {

  public static final String RESTAURANT_API_ENDPOINT = "/qeats/v1";
  public static final String RESTAURANTS_API = "/restaurants";
  public static final String MENU_API = "/menu";
  public static final String CART_API = "/cart";
  public static final String CART_ITEM_API = "/cart/item";
  public static final String CART_CLEAR_API = "/cart/clear";
  public static final String POST_ORDER_API = "/order";
  public static final String GET_ORDERS_API = "/orders";
  public static final Double MIN_LAT = -90.0;
  public static final Double MAX_LAT = 90.0;
  public static final Double MIN_LON = -180.0;


  @Autowired
  private RestaurantService restaurantService;


  @GetMapping(RESTAURANTS_API)
public ResponseEntity<GetRestaurantsResponse> getRestaurants(@Valid
       GetRestaurantsRequest getRestaurantsRequest) {
    long startTimeInMillis = System.currentTimeMillis();
    String searchFor = getRestaurantsRequest.getSearchFor();
    // if(getRestaurantsRequest.getLatitude()==null || getRestaurantsRequest.getLongitude()==null){
    //   return ResponseEntity.badRequest().body(null);
    // }
    // log.info("getRestaurants called with {}", getRestaurantsRequest);
    GetRestaurantsResponse getRestaurantsResponse;

      //CHECKSTYLE:OFF
      

      // Call the function

      // getRestaurantsResponse = restaurantService.findAllRestaurantsCloseBy(getRestaurantsRequest, LocalTime.now());
      // getRestaurantsResponse = restaurantService.findAllRestaurantsCloseBy(getRestaurantsRequest, LocalTime.parse("09:00"));
      // long endTimeInMillis = System.currentTimeMillis();
      // log.debug("CONTROLELER LAYER: findAllRestaurantsCloseBy took :" + (endTimeInMillis - startTimeInMillis));

      // log.info("getRestaurants returned {}", getRestaurantsResponse);
      //CHECKSTYLE:ON

    // List<Restaurant> modifiedRestaurants = getRestaurantsResponse.getRestaurants().stream().map(restaurant -> {
    //   String s = restaurant.getName().replaceAll("[^\\u0000-\\uFFFF]", "?");
    //   restaurant.setName(s);
    //   return restaurant;
    // }).collect(Collectors.toList());

    if(searchFor != null && !searchFor.equals("")) {
      getRestaurantsResponse = restaurantService.findRestaurantsBySearchQuery(getRestaurantsRequest, LocalTime.parse("09:00"));
      // getRestaurantsResponse = restaurantService.findRestaurantsBySearchQuery(getRestaurantsRequest, LocalTime.now());
    }else{
      getRestaurantsResponse = restaurantService.findAllRestaurantsCloseBy(getRestaurantsRequest, LocalTime.parse("09:00"));
      // getRestaurantsResponse = restaurantService.findAllRestaurantsCloseBy(getRestaurantsRequest, LocalTime.now());
    }

    List<Restaurant> modifiedRestaurants = new ArrayList<>();
    
    if(getRestaurantsResponse!=null && !getRestaurantsResponse.getRestaurants().isEmpty()){
    modifiedRestaurants= getRestaurantsResponse.getRestaurants().stream().map(restaurant -> {
      String s = restaurant.getName().replace("é", "e");
      restaurant.setName(s);
      return restaurant;
    }).collect(Collectors.toList());

  }
    return ResponseEntity.ok().body(new GetRestaurantsResponse(modifiedRestaurants));
  }

  // TIP(MODULE_MENUAPI): Model Implementation for getting menu given a restaurantId.
  // Get the Menu for the given restaurantId
  // API URI: /qeats/v1/menu?restaurantId=11
  // Method: GET
  // Query Params: restaurantId
  // Success Output:
  // 1). If restaurantId is present return Menu
  // 2). Otherwise respond with BadHttpRequest.
  //
  // HTTP Code: 200
  // {
  //  "menu": {
  //    "items": [
  //      {
  //        "attributes": [
  //          "South Indian"
  //        ],
  //        "id": "1",
  //        "imageUrl": "www.google.com",
  //        "itemId": "10",
  //        "name": "Idly",
  //        "price": 45
  //      }
  //    ],
  //    "restaurantId": "11"
  //  }
  // }./set
  // Error Response:
  // HTTP Code: 4xx, if client side error.
  //          : 5xx, if server side error.
  // Eg:
  // curl -X GET "http://localhost:8081/qeats/v1/menu?restaurantId=11"













}

