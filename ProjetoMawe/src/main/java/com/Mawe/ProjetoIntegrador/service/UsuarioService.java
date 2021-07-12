package com.Mawe.ProjetoIntegrador.service;

import java.nio.charset.Charset;
import java.util.Optional;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.Mawe.ProjetoIntegrador.DTO.UsuarioDTO;
import com.Mawe.ProjetoIntegrador.DTO.UsuarioLoginDTO;
import com.Mawe.ProjetoIntegrador.model.Usuario;
import com.Mawe.ProjetoIntegrador.repository.UsuarioRepository;

@Service
public class UsuarioService {
	
	@Autowired
	private UsuarioRepository repository;
	
	public Optional<Object> CadastrarUsuario(Usuario novoUuario) {
		return repository.findByEmail(novoUuario.getEmail()).map(UsuarioExistente -> {
			return Optional.empty();
		}).orElseGet(() -> {
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			String senhaCriptografada = encoder.encode(novoUuario.getSenha());
			novoUuario.setSenha(senhaCriptografada);
			return Optional.ofNullable(repository.save(novoUuario));
		});
	}
	
	public Optional<Usuario>alterar(long id, UsuarioDTO novoUsuario){
		return repository.findById(id).map(atualizarUsuario -> {
			atualizarUsuario.setNome(novoUsuario.getNome());
			atualizarUsuario.setSenha(novoUsuario.getSenha());
			return Optional.ofNullable(repository.save(atualizarUsuario));
		}).orElseGet(()->{
			return Optional.empty();
		});
	}
	
	public Optional<?> Logar(UsuarioLoginDTO dadosLogin) {
		return repository.findByEmail(dadosLogin.getEmail()).map(UsuarioExistente -> {
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

			if (encoder.matches(dadosLogin.getSenha(), UsuarioExistente.getSenha())) {
				String basic = dadosLogin.getEmail() + ":" + dadosLogin.getSenha();
				byte[] autorizacao = Base64.encodeBase64(basic.getBytes(Charset.forName("US-ASCII")));
				String autorizacaoHeader = "Basic " + new String(autorizacao);

				dadosLogin.setToken(autorizacaoHeader);
				dadosLogin.setId(UsuarioExistente.getId());
				dadosLogin.setNome(UsuarioExistente.getNome());
				dadosLogin.setSenha(UsuarioExistente.getSenha());
				dadosLogin.setTipoUsuario(UsuarioExistente.getTipoUsuario());

				return Optional.ofNullable(dadosLogin);
			} else {
				return Optional.empty();
			}

		}).orElseGet(() -> {
			return Optional.empty();
		});
	}

}