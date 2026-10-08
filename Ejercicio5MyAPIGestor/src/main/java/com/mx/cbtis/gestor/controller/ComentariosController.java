package com.mx.cbtis.gestor.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mx.cbtis.gestor.modelo.Comentarios;
import com.mx.cbtis.gestor.repository.ComentariosRepository;

@RestController
public class ComentariosController {
	
	@Autowired
	ComentariosRepository comentariostable;
	
	@Autowired
	VerificadorExistencia verificador;
	
	@PostMapping("/getComentario")

	public List<Comentarios> getComentario(@RequestBody Comentarios idComentario){
		List<Comentarios> listaComentarios = comentariostable.findByTarea(idComentario.getId_tarea());
		return listaComentarios;
	}
	
	@GetMapping("/buscarComentario")
	public Comentarios getComentario(@RequestParam int id) {
		
		Comentarios buscar = new Comentarios();
		Optional<Comentarios> comentario = comentariostable.findById(id);
		if(comentario.isPresent()) {
			buscar = comentario.get();
		}
		
		return buscar;
	}
	
	@PostMapping("/addComentario")
	public int addComentario(@RequestBody Comentarios comentario) {
		
		int flag = 0;
		if(!comentario.equals(null)) {
			comentariostable.save(comentario);
			flag = 1;
		}
		
		return flag;
	}
	
	@PostMapping("/updateComentario")
	public int updateComentario(@RequestBody Comentarios comentario) {
		verificador.exigir(comentariostable, comentario.getId_comentario(), "comentario");
		
		int flag = 0;
		if(!comentario.equals(null)) {
			comentariostable.save(comentario);
			flag = 1;
		}
		
		return flag;
	}
	
	@DeleteMapping("/deleteComentario")
	public int deleteComentario(@RequestBody Comentarios comentario) {
		verificador.exigir(comentariostable, comentario.getId_comentario(), "comentario");
		
		int flag = 0;
		if(!comentario.equals(null)) {
			comentariostable.delete(comentario);
			flag = 1;
		}
		
		return flag;
	}
	
	@PostMapping("/buscarComentarios")
	public List<Comentarios> getComentarios(){
		
		List<Comentarios> listaComentarios = new ArrayList<>();
		comentariostable.findAll().forEach(listaComentarios :: add);
		return listaComentarios;
	}

}
