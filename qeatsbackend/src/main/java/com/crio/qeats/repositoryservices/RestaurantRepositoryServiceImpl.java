/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.repositoryservices;

import lombok.extern.log4j.Log4j2;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import javax.inject.Provider;
import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.models.RestaurantEntity;
import com.crio.qeats.repositories.RestaurantRepository;
import com.crio.qeats.utils.GeoUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import ch.hsr.geohash.GeoHash;
import com.crio.qeats.QEatsApplication;
import com.crio.qeats.configs.RedisConfiguration;
import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.globals.GlobalConstants;
import com.crio.qeats.models.RestaurantEntity;
import com.crio.qeats.repositories.RestaurantRepository;
import com.crio.qeats.utils.GeoLocation;
import com.crio.qeats.utils.GeoUtils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Provider;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Primary
@Service
@Log4j2
public class RestaurantRepositoryServiceImpl implements RestaurantRepositoryService {

  @Autowired
  private RestaurantRepository restaurantRepository;

  @Autowired
  private RedisConfiguration redisConfiguration;

  @Autowired
  private MongoTemplate mongoTemplate;

  @Autowired
  private Provider<ModelMapper> modelMapperProvider;


  @Override
  public List<Restaurant> findAllRestaurantsCloseBy(Double latitude, Double longitude,
      LocalTime currentTime, Double servingRadiusInKms) {
     if(redisConfiguration.isCacheAvailable()){
      return findAllRestaurantsCloseByWithCache(latitude, longitude, currentTime, servingRadiusInKms);
     }
     return findAllRestaurantsCloseByNoCache(latitude, longitude, currentTime, servingRadiusInKms);
  }

  // TODO: CRIO_TASK_MODULE_NOSQL
  // Objectives:
  // 1. Implement findAllRestaurantsCloseby.
  // 2. Remember to keep the precision of GeoHash in mind while using it as a key.
  // Check RestaurantRepositoryService.java file for the interface contract.
  public List<Restaurant> findAllRestaurantsCloseByNoCache(Double latitude,
      Double longitude, LocalTime currentTime, Double servingRadiusInKms) {
    
    long startTimeInMillis = System.currentTimeMillis();
    log.debug("Cache is not available , get from DB");
    List<RestaurantEntity> restaurantEntities = restaurantRepository.findAll();
    // redisConfiguration.initCache();
    // log.debug("DEBUG Hardik : findAllRestaurantsCloseBy():(RestaurantRepositoryServiceImpl.java) RestaurantEntities = "+ restaurantEntities.toString());
    List<Restaurant> restaurants = restaurantEntities.stream()
    .filter(restaurantEntity -> isRestaurantCloseByAndOpen(restaurantEntity, currentTime, latitude, longitude, servingRadiusInKms))
    .map(restaurantEntity -> {
      restaurantEntity.setCity("Boisar");
      return modelMapperProvider.get().map(restaurantEntity, Restaurant.class);
    })
    .collect(Collectors.toList());

    //Add in cache
    JedisPool jedisPool = redisConfiguration.getJedisPool();
    Jedis jedis = jedisPool.getResource();
    String coordsKey = getGeoHashString(latitude, longitude, 7);
    String jsonString = "";
    try {
      jsonString = new ObjectMapper().writeValueAsString(restaurants);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    jedis.set(coordsKey, jsonString);
    // for (RestaurantEntity restaurantEntity : restaurantEntities){
    //   if(isRestaurantCloseByAndOpen(restaurantEntity,currentTime,latitude,longitude,servingRadiusInKms)){
    //     restaurants.add(modelMapperProvider.get().map(restaurantEntity, Restaurant.class));
    //   }
    // }
      //CHECKSTYLE:OFF
      //CHECKSTYLE:ON
      // log.debug("DEBUG Hardik : Restaurants "+restaurants.size());
      long endTimeInMillis = System.currentTimeMillis();
      log.debug("REPOSITORY LAYER: restaurantRepository.findAll() FROM DB took :" + (endTimeInMillis - startTimeInMillis));
    return restaurants;
  }

  // TODO: CRIO_TASK_MODULE_NOSQL
  // Objective:
  // 1. Check if a restaurant is nearby and open. If so, it is a candidate to be returned.
  // NOTE: How far exactly is "nearby"?
  //   LocalTime openingTime = LocalTime.parse(res.getOpensAt());
  //   LocalTime closingTime = LocalTime.parse(res.getClosesAt());

  //   return time.isAfter(openingTime) && time.isBefore(closingTime);
  // }

  public List<Restaurant> findAllRestaurantsCloseByWithCache(Double latitude,
      Double longitude, LocalTime currentTime, Double servingRadiusInKms)  {

    // TODO: CRIO_TASK_MODULE_REDIS
    // We want to use cache to speed things up. Write methods that perform the same functionality,
    // but using the cache if it is present and reachable.
    // Remember, you must ensure that if cache is not present, the queries are directed at the
    // database instead.


      //CHECKSTYLE:OFF
      long startTimeInMillis = System.currentTimeMillis();
      JedisPool jedisPool = redisConfiguration.getJedisPool();
      List<Restaurant> restaurants = new ArrayList<>();
      
      Jedis jedis = jedisPool.getResource();
      String coordsKey = getGeoHashString(latitude, longitude, 7);
      String restaurantList = jedis.get(coordsKey);

      if(restaurantList!=null){
        //Fetch list from cache
        try {
          restaurants = new ObjectMapper().readValue(restaurantList,new TypeReference<List<Restaurant>> (){
          });
        } catch (IOException e) {
          e.printStackTrace();
        }

        // try {
        //   jsonString = new ObjectMapper().writeValueAsString(restaurantEntities);
        // } catch (JsonProcessingException e) {
        //   e.printStackTrace();
        // }
        // jedis.set(coordsKey, jsonString);
      }
      else {
        //Fetch from DB and Update Cache
        log.debug("No Data in cache , Fetch from DB and update cache");
        restaurants = findAllRestaurantsCloseByNoCache(latitude, longitude, currentTime, servingRadiusInKms);
        // try {
        //   jsonString = new ObjectMapper().writeValueAsString(restaurants);
        // } catch (JsonProcessingException e) {
        //   // TODO Auto-generated catch block
        //   e.printStackTrace();
        // }
        // jedis.set(coordsKey, jsonString);
      }
      // log.debug("DEBUG Hardik : findAllRestaurantsCloseBy():(RestaurantRepositoryServiceImpl.java) RestaurantEntities = "+ restaurantEntities.toString());
      long endTimeInMillis = System.currentTimeMillis();
      log.debug("REPOSITORY LAYER: restaurantRepository.findAll() FROM CACHE took :" + (endTimeInMillis - startTimeInMillis));
    return restaurants;
  }

  /**
   * Utility method to check if a restaurant is within the serving radius at a given time.
   * @return boolean True if restaurant falls within serving radius and is open, false otherwise
   */
  private boolean isRestaurantCloseByAndOpen(RestaurantEntity restaurantEntity,
      LocalTime currentTime, Double latitude, Double longitude, Double servingRadiusInKms) {
    if (isOpenNow(currentTime, restaurantEntity)) {
      return GeoUtils.findDistanceInKm(latitude, longitude,
          restaurantEntity.getLatitude(), restaurantEntity.getLongitude())
          < servingRadiusInKms;
    }

    return false;
  }

  private String getGeoHashString (Double lat,Double lon ,int precision){
    return  GeoHash.withCharacterPrecision(lat, lon, precision).toBase32();
  }

  private boolean isOpenNow(LocalTime time, RestaurantEntity res) {
    // log.debug("DEBUG Hardik : isOpenNow():(RestaurantRepositoryServiceImpl.java) RestaurantEntity = "+res.toString());

    LocalTime openingTime = LocalTime.parse(res.getOpensAt());
    LocalTime closingTime = LocalTime.parse(res.getClosesAt());
    return time.isAfter(openingTime) && time.isBefore(closingTime);
  }


}

