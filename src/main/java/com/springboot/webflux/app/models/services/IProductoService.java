package com.springboot.webflux.app.models.services;

import com.springboot.webflux.app.models.documents.Categoria;
import com.springboot.webflux.app.models.documents.Producto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IProductoService {
	
	Flux<Producto> findAll();
	
	Flux<Producto> findAllNameUpperCase();
	
	Flux<Producto> findAllNameUpperCaseRepeat();
	
	Mono<Producto> findById(String id);
	
	Mono<Producto> save(Producto p);
	
	Mono<Void> delete(Producto p);
	
	Flux<Categoria> findAllCategoria();
	
	Mono<Categoria> findCategoriaById(String id);
	
	Mono<Categoria> saveCategoria(Categoria categoria);
	
}
