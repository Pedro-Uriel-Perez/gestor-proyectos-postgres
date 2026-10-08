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

import com.mx.cbtis.gestor.modelo.Tareas;
import com.mx.cbtis.gestor.modelo.Usuario;
import com.mx.cbtis.gestor.repository.TareasRepository;

@RestController
public class TareasController {
	
	@Autowired
	TareasRepository tareastable;
	
	@Autowired
	VerificadorExistencia verificador;
	
	@PostMapping("/getTarea")
	public List<Tareas> getTarea(@RequestBody Tareas id_proyecto) {
		List<Tareas> listaTareas = tareastable.findByIdProyecto(id_proyecto.getId_proyecto());
		return listaTareas;
	}
	
	@GetMapping("/buscarTarea")
	public Tareas getTarea(@RequestParam int id_tarea) {
		
		Tareas buscar = new Tareas();
		Optional<Tareas> tarea = tareastable.findById(id_tarea);
		if(tarea.isPresent()) {
			buscar = tarea.get();
		}
		
		return buscar;
	}
	
	@PostMapping("/addTareas")
	public int addTareas(@RequestBody Tareas tareas) {
		
		int flag = 0;
		if(!tareas.equals(null)) {
			tareastable.save(tareas);
			flag = 1;
		}
		
		return flag;
	}
	
	@PostMapping("/updateTareas")
	public int updateTareas(@RequestBody Tareas tareas) {
		verificador.exigir(tareastable, tareas.getId_tarea(), "tarea");
		
		int flag = 0;
		if(!tareas.equals(null)) {
			tareastable.save(tareas);
			flag = 1;
		}
		
		return flag;
	}
	
	@DeleteMapping("/deleteTareas")
	public int deleteTareas(@RequestBody Tareas tareas) {
		verificador.exigir(tareastable, tareas.getId_tarea(), "tarea");
		
		int flag = 0;
		if(!tareas.equals(null)) {
			tareastable.delete(tareas);
			flag = 1;
		}
		
		return flag;
	}
	
	@PostMapping("/buscarTareas")
	public List<Tareas> getTareas(){
		
		List<Tareas> listaTareas = new ArrayList<>();
		tareastable.findAll().forEach(listaTareas :: add);
		return listaTareas;
	}

}
