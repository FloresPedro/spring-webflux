package com.springboot.webflux.app.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.springboot.webflux.app.models.documents.Producto;

public interface IProductoDao extends ReactiveMongoRepository<Producto, String> {

}
