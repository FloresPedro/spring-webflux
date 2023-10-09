package com.springboot.webflux.app;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.springboot.webflux.app.models.dao.IProductoDao;
import com.springboot.webflux.app.models.documents.Producto;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootWebfluxApplication implements CommandLineRunner{
	
	@Autowired
	private IProductoDao dao;
	
	@Autowired
	private ReactiveMongoTemplate mongoTemplate;
	
	private static final Logger log = LoggerFactory.getLogger(SpringBootWebfluxApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluxApplication.class, args);
		
	}

	@Override
	public void run(String... args) throws Exception {
		
		//Borrar los productos insertados cada vez que se inicie la aplicacion
		mongoTemplate.dropCollection("productos").subscribe();
		
		Flux.just(new Producto("TV Panasonic Pantalla LCD", 456.3),
				new Producto("Sony Camara HD Digital", 150.2),
				new Producto("Apple iPod", 49.8),
				new Producto("Sony NoteBook", 846.49),
				new Producto("Hewlett Packard Multifuncional", 200.2),
				new Producto("Bianchi Bicicleta", 70.50),
				new Producto("HP NoteBook Omen 17", 2500.48),
				new Producto("Mica Comoda 5 Cajones ", 70.56),
				new Producto("TV Sony Bravia OLED 4K Ultra HD", 3550.20)
				).flatMap(producto -> {
					producto.setCreateAt(new Date());
					return dao.save(producto);})
		.subscribe(producto -> log.info("Insert: " + producto.getId() +" "+ producto.getNombre()));
		
	}

}
