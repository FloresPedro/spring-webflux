package com.springboot.webflux.app.models.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springboot.webflux.app.models.dao.ICategoriaDao;
import com.springboot.webflux.app.models.dao.IProductoDao;
import com.springboot.webflux.app.models.documents.Categoria;
import com.springboot.webflux.app.models.documents.Producto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductoServiceImpl implements IProductoService{
	
	@Autowired
	private IProductoDao dao;
	
	@Autowired
	private ICategoriaDao categoriaDao;

	@Override
	public Flux<Producto> findAll() {

		return dao.findAll();
	}

	@Override
	public Mono<Producto> findById(String id) {

		return dao.findById(id);
	}

	@Override
	public Mono<Producto> save(Producto p) {

		return dao.save(p);
	}

	@Override
	public Mono<Void> delete(Producto p) {

		return dao.delete(p);
	}

	@Override
	public Flux<Producto> findAllNameUpperCase() {
		
		return dao.findAll().map(producto -> {
			producto.setNombre(producto.getNombre().toUpperCase());
			return producto; });
	}

	@Override
	public Flux<Producto> findAllNameUpperCaseRepeat() {
		return findAllNameUpperCase().repeat(5000);
	}

	@Override
	public Flux<Categoria> findAllCategoria() {
		return categoriaDao.findAll();
	}

	@Override
	public Mono<Categoria> findCategoriaById(String id) {
		return categoriaDao.findById(id);
	}

	@Override
	public Mono<Categoria> saveCategoria(Categoria categoria) {
		return categoriaDao.save(categoria);
	}

}
