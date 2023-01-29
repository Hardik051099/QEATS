/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.repositories;

import com.crio.qeats.models.RestaurantEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface RestaurantRepository extends MongoRepository<RestaurantEntity, String> {
    //db.restaurants.find({name:{$regex : "^Bask"}})
    
    @Query("{name:{$regex:'^?0',$options:'i'}}")
    Optional<List<RestaurantEntity>> findRestaurantsByNameExact(String name);
    //db.restaurants.find({attributes:{$regex : "^Dess"}})
    @Query("{attributes:{$regex:'^?0',$options:'i'}}")
    Optional<List<RestaurantEntity>> findRestaurantsByAttributes(String attributes);

    Optional<List<RestaurantEntity>> findRestaurantsByRestaurantId (List<String> Ids);



}

