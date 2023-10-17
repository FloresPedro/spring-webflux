package com.springboot.webflux.app.controllers;

import java.io.File;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;

import com.springboot.webflux.app.models.services.IProductoService;
import com.springboot.webflux.app.models.documents.Categoria;
import com.springboot.webflux.app.models.documents.Producto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SessionAttributes("producto")
@Controller
public class ProductoController {

	@Autowired
	private IProductoService service;
	
	@Value("${config.uploads.path}")
	private String path;

	private static final Logger log = LoggerFactory.getLogger(ProductoController.class);
	
	@ModelAttribute("categorias")
	public Flux<Categoria> categorias(){
		return service.findAllCategoria();
	}
	
	@GetMapping("/ver/{id}")
	public Mono<String> ver(Model model, @PathVariable String id){
		return service.findById(id)
				.doOnNext(p -> {
			model.addAttribute("producto", p);
			model.addAttribute("titulo", "Detalle Producto");
		}).switchIfEmpty(Mono.just(new Producto()))
				.flatMap(p ->{
					if (p.getId() == null) {
						return Mono.error(new InterruptedException("No existe el producto"));
					}
					return Mono.just(p);
				}).then(Mono.just("ver"))
				.onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto)"));
	}

	@GetMapping("/listar")
	public Mono<String> listar(Model model) {
		Flux<Producto> productos = service.findAllNameUpperCase();

		productos.subscribe(prod -> log.info(prod.getNombre()));
		model.addAttribute("productos", productos);
		model.addAttribute("titulo", "Listado de Productos");
		return Mono.just("listar");
	}

	// Metodo para listar el formulario
	@GetMapping("/form")
	public Mono<String> crear(Model m) {
		m.addAttribute("producto", new Producto());
		m.addAttribute("titulo", "Formulario de producto");
		m.addAttribute("boton", "Crear");
		return Mono.just("form");
	}

	// Guardar producto
	@PostMapping("/form")
	public Mono<String> guardar(@Valid Producto producto, BindingResult result, Model m,@RequestPart FilePart file ,SessionStatus status) {

		if (result.hasErrors()) {
			m.addAttribute("titulo", "Errores en el formulario producto");
			m.addAttribute("boton", "Guardar");
			return Mono.just("form");
		} else {

			status.setComplete(); // finaliza el proceso y borra el id para que no salga un error
			
			
		Mono<Categoria> categoria = service.findCategoriaById(producto.getCategoria().getId());
		
		return categoria.flatMap(c -> {
			
			if (producto.getCreateAt() == null) {
				producto.setCreateAt(new Date());
			}
			
			if (!file.filename().isEmpty()) {
				producto.setFoto(UUID.randomUUID().toString() + "-" + file.filename()
				.replace(" ", "")
				.replace(":", "")
				.replace("\\", ""));
			}
			producto.setCategoria(c);
			return service.save(producto);
		}).doOnNext(p -> {
				log.info("Producto guardado: " + p.getNombre() + " Id: " + p.getId());
			})
				.flatMap(p -> {
					if (!file.filename().isEmpty()) {
						return file.transferTo(new File( path + p.getFoto()));
					}
					return Mono.empty();
				})
				.thenReturn("redirect:/listar?success=producto+guardado+con+exito");
		}
	}

	// editar el producto
	@GetMapping("/form/{id}")
	public Mono<String> editar(@PathVariable String id, Model model) {
		Mono<Producto> productoMono = service.findById(id).doOnNext(p -> {
			log.info("Producto: " + p.getNombre());
		}).defaultIfEmpty(new Producto());
		model.addAttribute("boton", "Editar");
		model.addAttribute("titulo", "Editar producto");
		model.addAttribute("producto", productoMono);
		return Mono.just("form");
	}

	@GetMapping("/form-v2/{id}")
	public Mono<String> editarV2(@PathVariable String id, Model model) {
		return service.findById(id).doOnNext(p -> {
			log.info("Producto: " + p.getNombre());
			model.addAttribute("boton", "Editar");
			model.addAttribute("titulo", "Editar producto");
			model.addAttribute("producto", p);
		}).defaultIfEmpty(new Producto()).flatMap(p -> {
			if (p.getId() == null) {
				return Mono.error(new InterruptedException("No existe el producto"));
			}
			return Mono.just(p);
		}).then(Mono.just("form")).onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto)"));

	}

	@GetMapping("/listar-datadriver")
	public String listarDataDriver(Model model) {
		Flux<Producto> productos = service.findAllNameUpperCase().delayElements(Duration.ofSeconds(1)); // Tiempo de
																										// espera que se
																										// publican
																										// elementos

		productos.subscribe(prod -> log.info(prod.getNombre()));
		model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos, 2));
		model.addAttribute("titulo", "Listado de Productos");
		return "listar";
	}

	@GetMapping("/listar-full")
	public String listarFull(Model model) {
		Flux<Producto> productos = service.findAllNameUpperCaseRepeat();

		model.addAttribute("productos", productos);
		model.addAttribute("titulo", "Listado de Productos");
		return "listar";
	}

	@GetMapping("/listar-chunked")
	public String listarChunked(Model model) {
		Flux<Producto> productos = service.findAllNameUpperCaseRepeat();

		model.addAttribute("productos", productos);
		model.addAttribute("titulo", "Listado de Productos");
		return "listar-chunked";
	}

	@GetMapping("/eliminar/{id}")
	public Mono<String> eliminar(@PathVariable String id) {

		return service.findById(id).defaultIfEmpty(new Producto()).flatMap(p -> {
			if (p.getId() == null) {
				return Mono.error(new InterruptedException("No existe el producto a eliminar"));
			}
			return Mono.just(p);
		}).flatMap(p -> {
			log.info("Eliminando el producto: " + p.getNombre());
			log.info("Eliminando el producto id: " + p.getId());
			return service.delete(p);
		}).then(Mono.just("redirect:/listar?success=producto+eliminado+con+exito"))
				.onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto+a+eliminar"));
	}

}
