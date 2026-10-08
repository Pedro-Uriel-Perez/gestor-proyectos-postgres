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

import com.mx.cbtis.gestor.modelo.Usuario;
import com.mx.cbtis.gestor.repository.UsuarioRepository;

@RestController
public class UsuarioController {
	
	@Autowired
	UsuarioRepository usuariotable;
	
	@Autowired
	VerificadorExistencia verificador;
	
	@PostMapping("/getUsuarioLogin")
	public Usuario getUsuarioLogin(@RequestBody Usuario userParam) {
		
		Optional<Usuario> optionaluser = usuariotable.findByCorreoPass(
				userParam.getCorreo_electronico(),
				userParam.getContrasena());
		
		Usuario user = new Usuario();
		
		if(optionaluser.isPresent()) {
			user = optionaluser.get();
		}else {
			user.setNombre("000");
		}
		
		return user;
	}
	
	@GetMapping("/buscarUsuario")
	public Usuario getUsuario(@RequestParam int id) {
		
		Usuario buscar = new Usuario();
		Optional<Usuario> usuario = usuariotable.findById(id);
		if(usuario.isPresent())
		{
			buscar = usuario.get();
		}
		
		return buscar;
	}
	
	@PostMapping("/addUsuario")
	public int addUsuario(@RequestBody Usuario usuario) {
		int flag = 0;
		if(!usuario.equals(null)) {
			usuariotable.save(usuario);
			flag = 1;
		}
		
		return flag;
	}
	
	@PostMapping("/updateUsuario")
	public int updateUsuario(@RequestBody Usuario usuario) {
		verificador.exigir(usuariotable, usuario.getId_usuario(), "usuario");
		int flag = 0;
		if(!usuario.equals(null)) {
			usuariotable.save(usuario);
			flag = 1;
		}
		
		return flag;
	}
	
	@DeleteMapping("/deleteUsuario")
	public int deleteUsuario(@RequestBody Usuario usuario) {
		verificador.exigir(usuariotable, usuario.getId_usuario(), "usuario");
		int flag = 0;
		if(!usuario.equals(null)) {
			usuariotable.delete(usuario);
			flag = 1;
		}
		
		return flag;
	}
	
	@PostMapping("/buscarUsuarios")
	public List<Usuario> getUsuarios(){
		List<Usuario> listaUsuarios = new ArrayList<>(); 
		usuariotable.findAll().forEach(listaUsuarios::add);
		return listaUsuarios;
	}		
}
