package com.springboot.webflux.app.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.webflux.app.models.dao.IProductoDao;
import com.springboot.webflux.app.models.documents.Producto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/productos")
public class ProductoRestController {

	@Autowired
	private IProductoDao dao;

	private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

	@GetMapping // al no tener ruta asignada automaticamente se va a la raiz del requestMapping
	public Flux<Producto> index() {
		Flux<Producto> productos = dao.findAll().map(producto -> {
			producto.setNombre(producto.getNombre().toUpperCase());
			return producto;
		}).doOnNext(prod -> log.info(prod.getNombre()));

		return productos;
	}

	@GetMapping("/{id}")
	public Mono<Producto> show(@PathVariable String id) {
		// Mono<Producto> producto = dao.findById(id); // la forma mas facil incluso
		// haciendo: return dao.findById
		Flux<Producto> productos = dao.findAll();// recuperamos todos y despues filtramos
		Mono<Producto> producto = productos.filter(p -> p.getId().equals(id)).next()
				.doOnNext(prod -> log.info(prod.getNombre()));
		return producto;
	}

}
