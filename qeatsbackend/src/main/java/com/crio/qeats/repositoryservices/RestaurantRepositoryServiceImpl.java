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
import java.util.Collections;
import java.util.Comparator;
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
import ch.hsr.geohash.GeoHash;
import com.crio.qeats.configs.RedisConfiguration;
import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.globals.GlobalConstants;
import com.crio.qeats.models.ItemEntity;
import com.crio.qeats.models.MenuEntity;
import com.crio.qeats.models.RestaurantEntity;
import com.crio.qeats.repositories.ItemRepository;
import com.crio.qeats.repositories.MenuRepository;
import com.crio.qeats.repositories.RestaurantRepository;
import com.crio.qeats.utils.GeoLocation;
import com.crio.qeats.utils.GeoUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
  private ItemRepository itemRepository;

  @Autowired
  private MenuRepository menuRepository;

  @Autowired
  private RedisConfiguration redisConfiguration;

  @Autowired
  private MongoTemplate mongoTemplate;

  @Autowired
  private Provider<ModelMapper> modelMapperProvider;




// Interface Methods
  // TODO: CRIO_TASK_MODULE_NOSQL
  // Objectives:
  // 1. Implement findAllRestaurantsCloseby.
  // 2. Remember to keep the precision of GeoHash in mind while using it as a key.
  // Check RestaurantRepositoryService.java file for the interface contract.

  // TODO: CRIO_TASK_MODULE_NOSQL
  // Objective:
  // 1. Check if a restaurant is nearby and open. If so, it is a candidate to be returned.
  // NOTE: How far exactly is "nearby"?
  @Override
  public List<Restaurant> findAllRestaurantsCloseBy(Double latitude, Double longitude,
      LocalTime currentTime, Double servingRadiusInKms) {
     if(redisConfiguration.isCacheAvailable()){
      return findAllRestaurantsCloseByWithCache(latitude, longitude, currentTime, servingRadiusInKms);
     }
     return findAllRestaurantsCloseByNoCache(latitude, longitude, currentTime, servingRadiusInKms);
  }

  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Objective:
  // Find restaurants whose names have an exact or partial match with the search query.
  @Override
  public List<Restaurant> findRestaurantsByName(Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {
        if(redisConfiguration.isCacheAvailable()){
          return findRestaurantByNameWithCache(latitude, longitude,searchString,currentTime, servingRadiusInKms);
         }
         return findRestaurantByNameNoCache(latitude, longitude,searchString, currentTime, servingRadiusInKms);
  }

  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Objective:
  // Find restaurants whose attributes (cuisines) intersect with the search query.
  @Override
  public List<Restaurant> findRestaurantsByAttributes(
      Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {
        if(redisConfiguration.isCacheAvailable()){
          return findRestaurantByAttributesWithCache(latitude, longitude,searchString,currentTime, servingRadiusInKms);
         }
         return findRestaurantByAttributesNoCache(latitude, longitude,searchString, currentTime, servingRadiusInKms);
  }


  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Objective:
  // Find restaurants which serve food items whose names form a complete or partial match
  // with the search query.
  @Override
  public List<Restaurant> findRestaurantsByItemName(
      Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {
        if(redisConfiguration.isCacheAvailable()){
          return findRestaurantByItemNameWithCache(latitude, longitude,searchString,currentTime, servingRadiusInKms);
         }
         return findRestaurantByItemNameNoCache(latitude, longitude,searchString, currentTime, servingRadiusInKms);
  }

  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Objective:
  // Find restaurants which serve food items whose attributes intersect with the search query.
  @Override
  public List<Restaurant> findRestaurantsByItemAttributes(Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {
        if(redisConfiguration.isCacheAvailable()){
          return findRestaurantByItemAttributeNoCache(latitude, longitude,searchString,currentTime, servingRadiusInKms);
         }
         return findRestaurantByItemAttributeWithCache(latitude, longitude,searchString, currentTime, servingRadiusInKms);
  }



//Actual Implementations
private List<Restaurant> findAllRestaurantsCloseByNoCache(Double latitude,Double longitude, LocalTime currentTime, Double servingRadiusInKms) {

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

private List<Restaurant> findAllRestaurantsCloseByWithCache(Double latitude,Double longitude, LocalTime currentTime, Double servingRadiusInKms)  {

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
  }
  else {
    //Fetch from DB and Update Cache
    log.debug("No Data in cache , Fetch from DB and update cache");
    restaurants = findAllRestaurantsCloseByNoCache(latitude, longitude, currentTime, servingRadiusInKms);
  }
  // log.debug("DEBUG Hardik : findAllRestaurantsCloseBy():(RestaurantRepositoryServiceImpl.java) RestaurantEntities = "+ restaurantEntities.toString());
  long endTimeInMillis = System.currentTimeMillis();
  log.debug("REPOSITORY LAYER: restaurantRepository.findAll() FROM CACHE took :" + (endTimeInMillis - startTimeInMillis));
  return restaurants;
}

private List<Restaurant> findRestaurantByNameNoCache(Double latitude,Double longitude, String searchString,LocalTime currentTime, Double servingRadiusInKms) {

  long startTimeInMillis = System.currentTimeMillis();
  log.debug("Cache is not available , get from DB");
  List<RestaurantEntity> restaurantEntities = restaurantRepository.findRestaurantsByNameExact("^"+searchString).get();

  List<Restaurant> restaurants = restaurantEntities.stream()
    .filter(restaurantEntity -> isRestaurantCloseByAndOpen(restaurantEntity, currentTime, latitude, longitude, servingRadiusInKms))
    .map(restaurantEntity -> {
    restaurantEntity.setCity("Boisar");
    return modelMapperProvider.get().map(restaurantEntity, Restaurant.class);
  })
    .collect(Collectors.toList());
    
    //Sort by exact matches at start
    Collections.sort(restaurants,(r1,r2)-> {
      if(r1.getName().startsWith(searchString)){
          return r2.getName().startsWith(searchString)?r1.getName().compareTo(r2.getName()):-1;
      }else {
        return r2.getName().startsWith(searchString)? 1: r1.getName().compareTo(r2.getName()); 
    }
    });

  //Add in cache
  JedisPool jedisPool = redisConfiguration.getJedisPool();
  Jedis jedis = jedisPool.getResource();
  String coordsKey = getGeoHashString(latitude, longitude, 7)+searchString;
  String jsonString = "";
  try {
    jsonString = new ObjectMapper().writeValueAsString(restaurants);
  } catch (JsonProcessingException e) {
    e.printStackTrace();
  }
  jedis.set(coordsKey, jsonString);
  //CHECKSTYLE:OFF
  //CHECKSTYLE:ON
  long endTimeInMillis = System.currentTimeMillis();
  log.debug("REPOSITORY LAYER: restaurantRepository.findByName() FROM DB took :" + (endTimeInMillis - startTimeInMillis));
  return restaurants;
}

private List<Restaurant> findRestaurantByNameWithCache(Double latitude,Double longitude,String searchString, LocalTime currentTime, Double servingRadiusInKms)  {
  //CHECKSTYLE:OFF
  long startTimeInMillis = System.currentTimeMillis();
  JedisPool jedisPool = redisConfiguration.getJedisPool();
  List<Restaurant> restaurants = new ArrayList<>();
  Jedis jedis = jedisPool.getResource();
  String coordsKey = getGeoHashString(latitude, longitude, 7)+searchString;
  String restaurantList = jedis.get(coordsKey);

  if(restaurantList!=null){
    //Fetch list from cache
    try {
      restaurants = new ObjectMapper().readValue(restaurantList,new TypeReference<List<Restaurant>> (){
      });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  else {
    //Fetch from DB and Update Cache
    log.debug("No Data in cache , Fetch from DB and update cache");
    restaurants = findRestaurantByNameNoCache(latitude, longitude,searchString, currentTime, servingRadiusInKms);
  }
  long endTimeInMillis = System.currentTimeMillis();
  log.debug("REPOSITORY LAYER: restaurantRepository.findByName() FROM CACHE took :" + (endTimeInMillis - startTimeInMillis));
  return restaurants;
}

private List<Restaurant> findRestaurantByAttributesNoCache(Double latitude,Double longitude, String searchString,LocalTime currentTime, Double servingRadiusInKms) {

  long startTimeInMillis = System.currentTimeMillis();
  log.debug("Cache is not available , get from DB");
  List<RestaurantEntity> restaurantEntities = restaurantRepository.findRestaurantsByAttributes("^"+searchString).get();

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
  String coordsKey = getGeoHashString(latitude, longitude, 7)+searchString;
  String jsonString = "";
  try {
    jsonString = new ObjectMapper().writeValueAsString(restaurants);
  } catch (JsonProcessingException e) {
    e.printStackTrace();
  }
  jedis.set(coordsKey, jsonString);
  //CHECKSTYLE:OFF
  //CHECKSTYLE:ON
  long endTimeInMillis = System.currentTimeMillis();
  log.debug("REPOSITORY LAYER: restaurantRepository.findByAttribute() FROM DB took :" + (endTimeInMillis - startTimeInMillis));
  return restaurants;
}

private List<Restaurant> findRestaurantByAttributesWithCache(Double latitude,Double longitude,String searchString, LocalTime currentTime, Double servingRadiusInKms)  {
  //CHECKSTYLE:OFF
  long startTimeInMillis = System.currentTimeMillis();
  JedisPool jedisPool = redisConfiguration.getJedisPool();
  List<Restaurant> restaurants = new ArrayList<>();
  Jedis jedis = jedisPool.getResource();
  String coordsKey = getGeoHashString(latitude, longitude, 7)+searchString;
  String restaurantList = jedis.get(coordsKey);

  if(restaurantList!=null){
    //Fetch list from cache
    try {
      restaurants = new ObjectMapper().readValue(restaurantList,new TypeReference<List<Restaurant>> (){
      });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  else {
    //Fetch from DB and Update Cache
    log.debug("No Data in cache , Fetch from DB and update cache");
    restaurants = findRestaurantByAttributesNoCache(latitude, longitude,searchString, currentTime, servingRadiusInKms);
  }
  long endTimeInMillis = System.currentTimeMillis();
  log.debug("REPOSITORY LAYER: restaurantRepository.findRestaurantByAttributes() FROM CACHE took :" + (endTimeInMillis - startTimeInMillis));
  return restaurants;
}

private List<Restaurant> findRestaurantByItemNameNoCache(Double latitude,Double longitude, String searchString,LocalTime currentTime, Double servingRadiusInKms) {

  long startTimeInMillis = System.currentTimeMillis();
  log.debug("Cache is not available , get from DB");
  //Fetch all items by its name
  List<ItemEntity> itemEntities = itemRepository.findItemsByName("^"+searchString).get();
  List<String> itemIds = new ArrayList<>();
  List<String> restaurantIds = new ArrayList<>();

  //Sort by exact matches at start
  Collections.sort(itemEntities,(i1,i2)-> {
    if(i1.getName().startsWith(searchString)){
        return i2.getName().startsWith(searchString)?i1.getName().compareTo(i2.getName()):-1;
    }else {
      return i2.getName().startsWith(searchString)? 1: i1.getName().compareTo(i2.getName()); 
  }
  });

  //fetch their Ids
  itemEntities.stream()
  .forEach(e -> itemIds.add(e.getItemId()));

  //fetch all menus with those ids
  List<MenuEntity> menuEntities = menuRepository.findMenusByItemsItemIdIn(itemIds).get();

  //fetch restaurant ids of all menus
  menuEntities.stream()
  .forEach(e -> restaurantIds.add(e.getRestaurantId()));

  //fetch restaurant by restaurant Ids
  List<RestaurantEntity> restaurantEntities = restaurantRepository.findRestaurantsByRestaurantIds(restaurantIds).get();

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
  String coordsKey = getGeoHashString(latitude, longitude, 7)+searchString;
  String jsonString = "";
  try {
    jsonString = new ObjectMapper().writeValueAsString(restaurants);
  } catch (JsonProcessingException e) {
    e.printStackTrace();
  }
  jedis.set(coordsKey, jsonString);
  //CHECKSTYLE:OFF
  //CHECKSTYLE:ON
  long endTimeInMillis = System.currentTimeMillis();
  log.debug("REPOSITORY LAYER: restaurantRepository.findByItemName() FROM DB took :" + (endTimeInMillis - startTimeInMillis));
  return restaurants;
}

private List<Restaurant> findRestaurantByItemNameWithCache(Double latitude,Double longitude,String searchString, LocalTime currentTime, Double servingRadiusInKms)  {
  //CHECKSTYLE:OFF
  long startTimeInMillis = System.currentTimeMillis();
  JedisPool jedisPool = redisConfiguration.getJedisPool();
  List<Restaurant> restaurants = new ArrayList<>();
  Jedis jedis = jedisPool.getResource();
  String coordsKey = getGeoHashString(latitude, longitude, 7)+searchString;
  String restaurantList = jedis.get(coordsKey);

  if(restaurantList!=null){
    //Fetch list from cache
    try {
      restaurants = new ObjectMapper().readValue(restaurantList,new TypeReference<List<Restaurant>> (){
      });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  else {
    //Fetch from DB and Update Cache
    log.debug("No Data in cache , Fetch from DB and update cache");
    restaurants = findRestaurantByItemNameNoCache(latitude, longitude,searchString, currentTime, servingRadiusInKms);
  }
  long endTimeInMillis = System.currentTimeMillis();
  log.debug("REPOSITORY LAYER: restaurantRepository.findRestaurantByItemNameNoCache() FROM CACHE took :" + (endTimeInMillis - startTimeInMillis));
  return restaurants;
}
  
private List<Restaurant> findRestaurantByItemAttributeNoCache(Double latitude,Double longitude, String searchString,LocalTime currentTime, Double servingRadiusInKms) {

  long startTimeInMillis = System.currentTimeMillis();
  log.debug("Cache is not available , get from DB");

  //Fetch all items by its name
  List<ItemEntity> itemEntities = itemRepository.findItemsByAttributes("^"+searchString).get();
  List<String> itemIds = new ArrayList<>();
  List<String> restaurantIds = new ArrayList<>();

  //fetch their Ids
  itemEntities.stream()
  .forEach(e -> itemIds.add(e.getItemId()));

  //fetch all menus with those ids
  List<MenuEntity> menuEntities = menuRepository.findMenusByItemsItemIdIn(itemIds).get();

  //fetch restaurant ids of all menus
  menuEntities.stream()
  .forEach(e -> restaurantIds.add(e.getRestaurantId()));

  //fetch restaurant by restaurant Ids
  List<RestaurantEntity> restaurantEntities = restaurantRepository.findRestaurantsByRestaurantIds(restaurantIds).get();

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
  String coordsKey = getGeoHashString(latitude, longitude, 7)+searchString;
  String jsonString = "";
  try {
    jsonString = new ObjectMapper().writeValueAsString(restaurants);
  } catch (JsonProcessingException e) {
    e.printStackTrace();
  }
  jedis.set(coordsKey, jsonString);
  //CHECKSTYLE:OFF
  //CHECKSTYLE:ON
  long endTimeInMillis = System.currentTimeMillis();
  log.debug("REPOSITORY LAYER: restaurantRepository.findByItemAttr() FROM DB took :" + (endTimeInMillis - startTimeInMillis));
  return restaurants;
}

private List<Restaurant> findRestaurantByItemAttributeWithCache(Double latitude,Double longitude,String searchString, LocalTime currentTime, Double servingRadiusInKms)  {
  //CHECKSTYLE:OFF
  long startTimeInMillis = System.currentTimeMillis();
  JedisPool jedisPool = redisConfiguration.getJedisPool();
  List<Restaurant> restaurants = new ArrayList<>();
  Jedis jedis = jedisPool.getResource();
  String coordsKey = getGeoHashString(latitude, longitude, 7)+searchString;
  String restaurantList = jedis.get(coordsKey);

  if(restaurantList!=null){
    //Fetch list from cache
    try {
      restaurants = new ObjectMapper().readValue(restaurantList,new TypeReference<List<Restaurant>> (){
      });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  else {
    //Fetch from DB and Update Cache
    log.debug("No Data in cache , Fetch from DB and update cache");
    restaurants = findRestaurantByItemAttributeWithCache(latitude, longitude,searchString, currentTime, servingRadiusInKms);
  }
  long endTimeInMillis = System.currentTimeMillis();
  log.debug("REPOSITORY LAYER: restaurantRepository.findByName() FROM CACHE took :" + (endTimeInMillis - startTimeInMillis));
  return restaurants;
}
  




//Utility Methods
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

