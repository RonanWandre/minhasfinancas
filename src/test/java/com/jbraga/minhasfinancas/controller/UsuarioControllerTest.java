package com.jbraga.minhasfinancas.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jbraga.minhasfinancas.api.dto.UsuarioDTO;
import com.jbraga.minhasfinancas.exception.ErroAutenticacao;
import com.jbraga.minhasfinancas.exception.RegraNegocioException;
import com.jbraga.minhasfinancas.model.entity.Usuario;
import com.jbraga.minhasfinancas.service.JwtService;
import com.jbraga.minhasfinancas.service.LancamentoService;
import com.jbraga.minhasfinancas.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = UsuarioController.class)
@AutoConfigureMockMvc
public class UsuarioControllerTest {

    static  final  String API = "/api/usuarios";
    static  final  MediaType JSON = MediaType.APPLICATION_JSON;

    @Autowired
    MockMvc mvc;

    @MockBean
    UsuarioService service;

    @MockBean
    LancamentoService lancamentoService;

    @MockBean
    JwtService jwtService;

    @Test
    public void deveAcessarEndpointSemAutenticacao() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(API.concat("/autenticar"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"usuario@email.com\",\"senha\":\"123\"}");

        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk()); // Verifica se é acessível
    }

    @Test
    public void deveAutenticarUmUsuario() throws Exception {
        String email = "usuario@email.com";
        String senha = "123";
        UsuarioDTO dto = UsuarioDTO.builder()
                .email(email)
                .senha(senha)
                .build();

        Usuario usuario = Usuario.builder()
                .id(1L)
                .email(email)
                .senha(senha)
                .build();

        Mockito.when(service.autenticar(email, senha)).thenReturn(usuario);

        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(API.concat("/autenticar"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                        .with(csrf -> csrf);



        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(usuario.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("email").value(usuario.getEmail()));
    }

    @Test
    public void deveRetornarBadRequestAoObterErroDeAutenticacao() throws Exception {
        // Cenário
        String email = "usuario@email.com";
        String senha = "123";
        UsuarioDTO dto = UsuarioDTO.builder()
                .email(email)
                .senha(senha)
                .build();


        Mockito.when(service.autenticar(email, senha)).thenThrow(ErroAutenticacao.class);

        String json = new ObjectMapper().writeValueAsString(dto);

        // Execução e verificação
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(API.concat("/autenticar"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void deveCriarUmNovoUsuario() throws Exception {
        // Cenário
        String email = "usuario@email.com";
        String senha = "123";
        UsuarioDTO dto = UsuarioDTO.builder()
                .email(email)
                .senha(senha)
                .build();

        Usuario usuario = Usuario.builder()
                .id(1L)
                .email(email)
                .senha(senha)
                .build();

        Mockito.when(service.salvarUsuario(Mockito.any(Usuario.class))).thenReturn(usuario);

        String json = new ObjectMapper().writeValueAsString(dto);

        // Execução e verificação
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(API)
                .accept(JSON)
                .contentType(JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(usuario.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("nome").value(usuario.getNome()))
                .andExpect(MockMvcResultMatchers.jsonPath("email").value(usuario.getEmail()));

    }

    @Test
    public void deveRetornarBadRequestAoTentarCriarUmUsuarioInvalido() throws Exception {
        // Cenário
        String email = "usuario@email.com";
        String senha = "123";
        UsuarioDTO dto = UsuarioDTO.builder()
                .email(email)
                .senha(senha)
                .build();


        Mockito.when(service.salvarUsuario(Mockito.any(Usuario.class))).thenThrow(RegraNegocioException.class);

        String json = new ObjectMapper().writeValueAsString(dto);

        // Execução e verificação
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(API)
                .accept(JSON)
                .contentType(JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

    }

}
