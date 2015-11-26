package br.gov.servicos.editor.usuarios;

import br.gov.servicos.editor.usuarios.cadastro.TokenRecuperacaoSenhaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

import static java.lang.Long.valueOf;

@Component
public class RecuperacaoSenhaService {

    @Autowired
    private GeradorToken geradorToken;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenRecuperacaoSenhaRepository repository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RecuperacaoSenhaValidator validator;

    @Value("${eds.max-tentativas-token}")
    private int maxTentativasToken;

    private Clock clock = Clock.systemUTC();

    public String gerarTokenParaUsuario(String usuarioId) {
        String token = geradorToken.gerar();
        TokenRecuperacaoSenha tokenRecuperacaoSenha = new TokenRecuperacaoSenha()
                                                            .withUsuario(new Usuario().withId(valueOf(usuarioId)))
                                                            .withDataCriacao(LocalDateTime.now(clock))
                                                            .withTentativas(0)
                                                            .withToken(passwordEncoder.encode(token));
        repository.save(tokenRecuperacaoSenha);
        return token;
    }

    public void trocarSenha(FormularioRecuperarSenha formulario) throws TokenInvalido {
        Long usuarioId = formulario.getUsuarioId();
        TokenRecuperacaoSenha token = repository.findByUsuarioId(usuarioId);
        Usuario usuario = token.getUsuario();

        if(validator.isValid(formulario, token)) {
            usuarioRepository.save(usuario.withSenha(passwordEncoder.encode(formulario.getCamposSenha().getSenha())));
            repository.delete(token.getId());
        } else {
            TokenRecuperacaoSenha novoToken = token.withTentativas(token.getTentativas() + 1);
            repository.save(novoToken);
            throw new TokenInvalido(maxTentativasToken - novoToken.getTentativas());
        }
    }
}
