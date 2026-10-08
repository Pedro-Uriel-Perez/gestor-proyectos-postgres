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

import com.mx.cbtis.gestor.modelo.Asignaciones;
import com.mx.cbtis.gestor.modelo.Tareas;
import com.mx.cbtis.gestor.repository.AsignacionesRepository;

@RestController
public class AsignacionesController {
	
	@Autowired
	AsignacionesRepository asignacionestable;
	
	@Autowired
	VerificadorExistencia verificador;
	
	@GetMapping("/buscarAsignacion")
	public Asignaciones getAsignacion(@RequestParam int id) {
		
		Asignaciones buscar = new Asignaciones();
		Optional<Asignaciones> asignacion = asignacionestable.findById(id);
		if(asignacion.isPresent()) {
			buscar = asignacion.get();
		}
		
		return buscar;
	}
	
	@PostMapping("/addAsignacion")
	public int addAsignacion(@RequestBody Asignaciones asignacion) {
		
		int flag = 0;
		if(!asignacion.equals(null)) {
			
			asignacionestable.save(asignacion);
			flag = 1;
		}
		
		return flag;
	}
	
	@PostMapping("/updateAsignacion")
	public int updateAsignacion(@RequestBody Asignaciones asignacion) {
		verificador.exigir(asignacionestable, asignacion.getId_asignacion(), "asignacion");
		
		int flag = 0;
		if(!asignacion.equals(null)) {
			asignacionestable.save(asignacion);
			flag = 1;
		}
		
		return flag;
	}
	
	@DeleteMapping("/deleteAsignacion")
	public int deleteAsignacion(@RequestBody Asignaciones asignacion) {
		verificador.exigir(asignacionestable, asignacion.getId_asignacion(), "asignacion");
		
		int flag = 0;
		if(!asignacion.equals(null)) {
			asignacionestable.delete(asignacion);
			flag = 1;
		}
		
		return flag;
	}   
	
	@PostMapping("/buscarAsignaciones")
	public List<Asignaciones> getAsignaciones(){
		
		List<Asignaciones> listaAsignaciones = new ArrayList<>();
		asignacionestable.findAll().forEach(listaAsignaciones :: add);
		return listaAsignaciones;
	}
}
